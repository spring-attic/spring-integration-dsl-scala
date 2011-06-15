package demo
import org.springframework.integration.scala.dsl._
import org.springframework.integration.Message
import org.springframework.integration.message.GenericMessage
import java.util.concurrent._

object HelloWorld {
  def main(args : Array[String]) : Unit = {
    
    directChannelWithService
    println("\nEnd test\n")
    asyncChannelWithService
    println("\nEnd test\n")
    directChannelWithServiceAndOutputMessageToQueueChannel
    println("\nEnd test\n")
    withPollingConsumerAndSpel
    println("\nEnd test\n")
    withPollingConsumerAndSpelDefaultPoller
    println("\nEnd test\n")
    withPubSubChannel
  }
  /**
   *
   */
  def directChannelWithService(): Unit = {
    var integrationContext = SpringIntegrationContext()
    
    val inputChannel = channel("inputChannel")
    
    integrationContext <= {
    	inputChannel >>
    	activate("myService"){m:Message[String] => {println(m.getPayload)}}
    }
       
    integrationContext.init
    
    inputChannel.send(new GenericMessage("==> Hello from Scala"))
  }
  /**
   * 
   */
  def asyncChannelWithService(): Unit = {
    var integrationContext = SpringIntegrationContext()
    
    val inputChannel = channel("inputChannel", executor(Executors.newFixedThreadPool(10)))
    
    integrationContext <= {
    	inputChannel >>
    	activate("myService"){m:Message[String] => {println(m.getPayload)}}
    }
       
    integrationContext.init
    
    inputChannel.send(new GenericMessage("==> Hello from Scala"))
  }
  /**
   * 
   */
  def directChannelWithServiceAndOutputMessageToQueueChannel(): Unit = {
    var integrationContext = SpringIntegrationContext()
    
    val inputChannel = channel("inputChannel")
    val outputChannel = queue_channel("outputChannel", queue(5))
    
    integrationContext <= {
    	inputChannel >>
    	activate("myService"){m:Message[String] => {m.getPayload.toUpperCase()}} >>
    	outputChannel
    }
       
    integrationContext.init
    
    inputChannel.send(new GenericMessage("==> Hello from Scala"))
    var outputMessage = outputChannel.recieve
    println("Output Message: " + outputMessage)
  }
  /**
   * 
   */
  def withPollingConsumerAndSpel(): Unit = {
    var integrationContext = SpringIntegrationContext()
    
    val inputChannel = channel("inputChannel", executor())
    val outputChannel = queue_channel("outputChannel", queue(5))
    val resultChannel = queue_channel("resultChannel", queue())
    
    integrationContext <= {
    	inputChannel >>
    	activate("myService"){m:Message[String] => {m.getPayload.toUpperCase()}} >>
    	outputChannel >>
    	activate("myTransformer", poller(1000, 5)){"'### ' + payload.toLowerCase() + ' ###'"} >>
    	resultChannel
    }
       
    integrationContext.init
    
    inputChannel.send(new GenericMessage("==> Hello from Scala"))
    var outputMessage = resultChannel.recieve
    println("Output Message: " + outputMessage)
  }
  
  def withPollingConsumerAndSpelDefaultPoller(): Unit = {
    var integrationContext = SpringIntegrationContext()
    
    val inputChannel = channel("inputChannel", executor())
    val outputChannel = queue_channel("outputChannel", queue(5))
    val resultChannel = queue_channel("resultChannel", queue())
    
    integrationContext <= {
    	inputChannel >>
    	activate(){m:Message[String] => {m.getPayload.toUpperCase()}} >>
    	outputChannel >>
    	transform(){"'### ' + payload.toLowerCase() + ' ###'"} >>
    	resultChannel
    }
       
    integrationContext.init
    
    inputChannel.send(new GenericMessage("==> Hello from Scala"))
    var outputMessage = resultChannel.recieve
    println("Output Message: " + outputMessage)
  }
  /**
   *
   */
  def withPubSubChannel(): Unit = {
    var integrationContext = SpringIntegrationContext()
    
    val inputChannel = pub_sub_channel("inputChannel")
    val middleChannel = queue_channel("middleChannel", queue(5))
    val outputChannel = queue_channel("outputChannel", queue(5))
    
    integrationContext <= {
    	inputChannel >> ( 
    	    
	        transform("firstXfmr"){"'From Transformer: ' + payload.toUpperCase()"} >> 
	        middleChannel >>
	        transform("secondXfmr"){m:Message[String] => {m.getPayload().asInstanceOf[String].toUpperCase()}} >> 
	        outputChannel,
	        
	        activate(){m:Message[String] => {println("From Service Activator: " + m)}}
	    )
    }
       
    integrationContext.init
    
    inputChannel.send(new GenericMessage("==> Hello from Scala"))
    var outputMessage = outputChannel.recieve
    println("Output Message: " + outputMessage)
  }
}
