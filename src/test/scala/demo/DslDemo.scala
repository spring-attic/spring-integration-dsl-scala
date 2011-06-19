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
  }
  /**
   *
   */
  def directChannelWithGeneratedNameAndServiceWithScalaFunction(): Unit = {
    var integrationContext = SpringIntegrationContext()

    val inputChannel = channel()
    integrationContext <= {
        	inputChannel >>
        	activate.using{ m: Message[String] => { println(m.getPayload) }  }
    }

    integrationContext.init

    inputChannel.send(new GenericMessage("==> Hello from Scala"))
  }

  /**
   *
   */
  def directChannelAndServiceWithSpel(): Unit = {
    var integrationContext = SpringIntegrationContext()

    val inputChannel = channel.withName("inChannel")
    integrationContext <= {
        inputChannel >>
        activate.withName("myService").using("T(java.lang.System).out.println(payload)")
    }

    integrationContext.init

    inputChannel.send(new GenericMessage("==> Hello from Scala"))
  }
  /**
   *
   */
  def asyncChannelWithService(): Unit = {
    var integrationContext = SpringIntegrationContext()
    val inputChannel = channel.withExecutor(Executors.newFixedThreadPool(10))
    integrationContext <= {
        inputChannel >>
        activate.withName("myService").using { { m: Message[String] => { println(m.getPayload) } } }
    }

    integrationContext.init

    inputChannel.send(new GenericMessage("==> Hello from Scala"))
  }
  /**
   *
   */
  def directChannelWithServiceAndOutputMessageToQueueChannel(): Unit = {
    var integrationContext = SpringIntegrationContext()

    val inputChannel = channel.withName("inputChannel")
    //    val outputChannel = channel.withQueue(5).andName("outputChannel")
    val outputChannel = channel.withName("outputChannel").andQueue(5)

    integrationContext <= {
        inputChannel >>
        activate.withName("myService").using { m: Message[String] => { m.getPayload.toUpperCase() } } >>
        outputChannel
    }

    integrationContext.init

    inputChannel.send(new GenericMessage("==> Hello from Scala"))
    var outputMessage = outputChannel.receive
    println("Output Message: " + outputMessage)
  }
  /**
   *
   */
  def withPollingConsumerAndSpel(): Unit = {
    var integrationContext = SpringIntegrationContext()

    val inputChannel = channel.withExecutor().andName("inputChannel")
    val middleChannel = channel.withQueue(5).andName("middleChannel")
    val resultChannel = channel.withQueue.andName("resultChannel")

    integrationContext <= {
        inputChannel >>
        activate.withName("myService").using { m: Message[String] => { m.getPayload.toUpperCase() } } >>
        middleChannel >>
        //transform.withPoller(5, 1000).andName("myTransformer").using{"'### ' + payload.toLowerCase() + ' ###'"} >>
        transform.withName("myTransformer").andPoller(1000, 5).using { "'### ' + payload.toLowerCase() + ' ###'" } >>
        resultChannel
    }

    integrationContext.init

    inputChannel.send(new GenericMessage("==> Hello from Scala"))
    var outputMessage = resultChannel.receive
    println("Output Message: " + outputMessage)
  }
  /**
   *
   */
  def withPollingConsumerAndSpelDefaultPoller(): Unit = {
    var integrationContext = SpringIntegrationContext()

    val inputChannel = channel.withName("inputChannel").andExecutor
    val middleChannel = channel.withName("middleChannel").andQueue(5)
    val resultChannel = channel.withName("resultChannel").andQueue

    integrationContext <= {
        inputChannel >>
        activate.using { m: Message[String] => { m.getPayload.toUpperCase() } } >>
        middleChannel >>
        transform.using { "'### ' + payload.toLowerCase() + ' ###'" } >>
        resultChannel
    }

    integrationContext.init

    inputChannel.send(new GenericMessage("==> Hello from Scala"))
    var outputMessage = resultChannel.receive
    println("Output Message: " + outputMessage)
  }
  /**
   *
   */
  def withPubSubChannel(): Unit = {
    var integrationContext = SpringIntegrationContext()

    val inputChannel = pub_sub_channel.withName("inputChannel")
    val middleChannel = channel.withName("middleChannel").andQueue(5)
    val resultChannel = channel.withName("resultChannel").andQueue

    integrationContext <= {
      inputChannel >> (
          transform.withName("xfmrA").using { "'From Transformer: ' + payload.toUpperCase()" } >>
          middleChannel >>
          transform.withName("xfmrB").using { m: Message[String] => { m.getPayload().asInstanceOf[String].toUpperCase() } } >>
          resultChannel,

          activate.using { m: Message[String] => { println("From Service Activator: " + m) } }
      )
    }

    integrationContext.init

    inputChannel.send(new GenericMessage("==> Hello from Scala"))
    var outputMessage = resultChannel.receive
    println("Output Message: " + outputMessage)
  }
}
