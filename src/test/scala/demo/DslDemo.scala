package demo
import org.springframework.integration.scala.dsl._
import org.springframework.integration.Message
import org.springframework.integration.message.GenericMessage
import java.util.concurrent._
import org.apache.log4j._
import java.util._
import java.util.concurrent.ThreadPoolExecutor._
import org.springframework.context.support._
import org.springframework.beans.factory.support._
import org.springframework.integration.channel._
import org.springframework.integration.scheduling._
import org.springframework.beans._
import org.springframework.beans.factory.config._
import org.springframework.util._
import org.springframework.integration.config._
/**
 * @author Oleg Zhurakousky
 *
 */
object DslDemo {
  def main(args: Array[String]): Unit = {

    directChannelWithGeneratedNameAndServiceWithScalaFunction
    println("### End demo\n")
    directChannelAndServiceWithSpel
    println("### End demo\n")
    asyncChannelWithService
    println("### End demo\n")
    directChannelWithServiceAndOutputMessageToQueueChannel
    println("### End demo\n")
    withPollingConsumerAndSpel
    println("### End demo\n")
    withPollingConsumerAndSpelDefaultPoller
    println("### End demo\n")
    withPubSubChannel
    println("### End demo\n")
    withImplicitChannels
    println("### End demo\n")
    withRouter
    println("### End demo\n")
    withRouterAndDefaultOutputChannel
    println("### End demo\n")
  }
  /**
   *
   */
  def directChannelWithGeneratedNameAndServiceWithScalaFunction(): Unit = {

    val inputChannel = channel()

    val integrationContext = IntegrationContext(
        inputChannel >=>
        service.using { m: Message[String] => { println(m.getPayload) } }
    )

    inputChannel.send(new GenericMessage("==> Hello from Scala"))
  }

  /**
   *
   */
  def directChannelAndServiceWithSpel(): Unit = {
    
    val inputChannel = channel.withName("inChannel")
    
    val integrationContext = IntegrationContext(
        inputChannel >=>
        service.withName("myService").using("T(java.lang.System).out.println(payload)")
    )

    inputChannel.send(new GenericMessage("==> Hello from Scala"))
  }
  /**
   *
   */
  def asyncChannelWithService(): Unit = {
    
    val inputChannel = channel.withExecutor(Executors.newFixedThreadPool(10))
    
    val integrationContext = IntegrationContext(
    	inputChannel >=>
        service.withName("myService").using { { m: Message[String] => { println(m.getPayload) } } }
    )

    inputChannel.send(new GenericMessage("==> Hello from Scala"))
  }
  /**
   *
   */
  def directChannelWithServiceAndOutputMessageToQueueChannel(): Unit = {
    
    val inputChannel = channel.withName("inputChannel")
    //    val outputChannel = channel.withQueue(5).andName("outputChannel")
    val outputChannel = channel.withName("outputChannel").andQueue(5)

    val integrationContext = IntegrationContext(
        inputChannel >=>
        service.withName("myService").using { m: Message[String] => { m.getPayload.toUpperCase() } } >=>
        outputChannel
    )

    inputChannel.send(new GenericMessage("==> Hello from Scala"))
    val outputMessage = outputChannel.receive
    println("Output Message: " + outputMessage)
  }
  /**
   *
   */
  def withPollingConsumerAndSpel(): Unit = {

    val inputChannel = channel.withExecutor().andName("inputChannel")
    val middleChannel = channel.withQueue(5).andName("middleChannel")
    val resultChannel = channel.withQueue.andName("resultChannel")

    val integrationContext = IntegrationContext(
      inputChannel >=>
        service.withName("myService").using { m: Message[String] => { m.getPayload.toUpperCase() } } >=>
        middleChannel >=>
        //transform.withPoller(5, 1000).andName("myTransformer").using{"'### ' + payload.toLowerCase() + ' ###'"} >=>
        transform.withName("myTransformer").andPoller(1000, 5).using { "'### ' + payload.toLowerCase() + ' ###'" } >=>
        resultChannel
    )

    inputChannel.send(new GenericMessage("==> Hello from Scala"))
    val outputMessage = resultChannel.receive
    println("Output Message: " + outputMessage)
  }
  /**
   *
   */
  def withPollingConsumerAndSpelDefaultPoller(): Unit = {

    val inputChannel = channel.withName("inputChannel").andExecutor
    val middleChannel = channel.withName("middleChannel").andQueue(5)
    val resultChannel = channel.withName("resultChannel").andQueue

    val integrationContext = IntegrationContext(
        inputChannel >=>
        service.using { m: Message[String] => { m.getPayload.toUpperCase() } } >=>
        middleChannel >=>
        transform.using { "'### ' + payload.toLowerCase() + ' ###'" } >=>
        resultChannel
    )

    inputChannel.send(new GenericMessage("==> Hello from Scala"))
    val outputMessage = resultChannel.receive
    println("Output Message: " + outputMessage)
  }
  /**
   *
   */
  def withPubSubChannel(): Unit = {
    
    val inputChannel = pub_sub_channel.withName("inputChannel")
    val middleChannel = channel.withName("middleChannel").andQueue(5)
    val resultChannel = channel.withName("resultChannel").andQueue

    val integrationContext = IntegrationContext(
      inputChannel >=> ( 
        // subscriber 1
    	{
    		transform.withName("xfmrA").using { "'From Transformer: ' + payload.toUpperCase()" } >=>
    		middleChannel >=>
    		transform.withName("xfmrB").using { m: Message[String] => { m.getPayload().asInstanceOf[String].toUpperCase() } } >=>
    		resultChannel
    	},
        // subscriber 2
        {
          service.using { m: Message[String] => { println("From Service Activator: " + m) } }
        })
    )

    inputChannel.send(new GenericMessage("==> Hello from Scala"))
    val outputMessage = resultChannel.receive
    println("Output Message: " + outputMessage)
  }
  /**
   * 
   */
  def withImplicitChannels(): Unit = {
    
    val inputChannel = channel.withName("inputChannel")
   
    val integrationContext = IntegrationContext(
        inputChannel >=> 
        service.using{m:Message[_] => m.getPayload + "_activator1"} >=>
        transform.using{m:Message[_] => m.getPayload + "_transformer1"} >=>
        service.using{m:Message[_] => m.getPayload + "_activator2"} >=>
        transform.using{m:Message[_] => m.getPayload + "_transformer2"} >=>
        service.using{m:Message[_] => println(m)}
    )

    inputChannel.send(new GenericMessage("==> Hello from Scala"))
  }
  /**
   * 
   */
  def withRouter(): Unit = {
    
    val inputChannel = channel.withName("inputChannel")
   
    val integrationContext = IntegrationContext(
        {
          channel("foo") >=>
          service.using{ m: Message[String] => { println("FROM FOO channel: " + m.getPayload) }}
        },
    	{
          channel("bar") >=>
          service.using{ m: Message[String] => { println("FROM BAR channel: " + m.getPayload) }}
        },
        {
          inputChannel >=>
          route.using{m: Message[String] => { m.getPayload}}
        }
    )

    inputChannel.send(new GenericMessage("foo"))
    inputChannel.send(new GenericMessage("bar"))
  }
  
  /**
   * 
   */
  def withRouterAndDefaultOutputChannel(): Unit = {
    
    val inputChannel = channel.withName("inputChannel")
    val defaultOutputChannel = channel.withName("defaultOutputChannel").andQueue
   
    val integrationContext = IntegrationContext(
        {
          channel("foo") >=>
          service.using{ m: Message[String] => { println("FROM FOO channel: " + m.getPayload) }}
        },
    	{
          channel("bar") >=>
          service.using{ m: Message[String] => { println("FROM BAR channel: " + m.getPayload) }}
        },
        {
          inputChannel >=>
          route.using{m: Message[String] => { m.getPayload}} >=>
          defaultOutputChannel
        }
    )

    inputChannel.send(new GenericMessage("foo"))
    inputChannel.send(new GenericMessage("bar"))
    inputChannel.send(new GenericMessage("baz"))
    println("Message from 'defaultOutputChannel' " + defaultOutputChannel.receive)
  }

}
