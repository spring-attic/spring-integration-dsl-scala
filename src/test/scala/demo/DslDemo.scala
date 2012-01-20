package demo
import org.springframework.eip.dsl._
import org.junit._
import org.springframework.integration.store.SimpleMessageStore
import org.springframework.integration.message.GenericMessage
import org.springframework.integration.Message
import org.springframework.core.task.SimpleAsyncTaskExecutor

/**
 * @author Oleg Zhurakousky
 *
 */
class DslDemo {
  @Test
  def runDemos() = {

    channelConfigsDemo
    println("### End demo\n")
    routerUsageDemo
    println("### End routerUsageDemo \n")
//    directChannelAndServiceWithSpel
//    println("### End demo\n")
//    asyncChannelWithService
//    println("### End demo\n")
//    directChannelWithServiceAndOutputMessageToQueueChannel
//    println("### End demo\n")
//    withPollingConsumerAndSpel
//    println("### End demo\n")
//    withPollingConsumerAndSpelDefaultPoller
//    println("### End demo\n")
//    withPubSubChannel
//    println("### End demo\n")
//    withImplicitChannels
//    println("### End demo\n")
//    withRouter
//    println("### End demo\n")
//    withRouterAndDefaultOutputChannel
//    println("### End demo\n")
  }

  def routerUsageDemo(){

    val compositionWithRouter =
      Channel("inputChannel") -->
        handle.using {m:Message[String] => m.getPayload.toUpperCase} -->
        route.onValueOfHeader ("myRoutingHeader") (
          when("FOO") {
            Channel("queueChannel").withQueue() --> poll.usingFixedRate(8) -->
              handle.using("someSpEL")
          },
          when("Bar") {
            Channel("executorChannel").withDispatcher (taskExecutor = new SimpleAsyncTaskExecutor) -->
              handle.using("someSpEL")
          }
        )

  }
  /**
   *
   */
  def channelConfigsDemo: Unit = {

    val directChannel = Channel("myChannel")
//
    val queueChannel = Channel("myChannel") withQueue(capacity = 2, messageStore = new SimpleMessageStore)
//
    val messageBridge =
      directChannel -->
      queueChannel -->
      poll.usingFixedRate(3) -->  handle.using("")

    println(messageBridge)


    directChannel.send(new GenericMessage[String]("Hello"))

    queueChannel.send(new GenericMessage[String]("Hello"))

    queueChannel.receive()

  }
//
//  /**
//   *
//   */
//  def directChannelAndServiceWithSpel(): Unit = {
//
//    val inputChannel = channel.withName("inChannel")
//
//    val integrationContext = IntegrationContext(
//        inputChannel >=>
//        service.withName("myService").using("T(java.lang.System).out.println(payload)")
//    )
//
//    inputChannel.send(new GenericMessage("==> Hello from Scala"))
//  }
//  /**
//   *
//   */
//  def asyncChannelWithService(): Unit = {
//
//    val inputChannel = channel.withExecutor(Executors.newFixedThreadPool(10))
//
//    val integrationContext = IntegrationContext(
//    	inputChannel >=>
//        service.withName("myService").using { m: Message[String] => println(m.getPayload) }
//    )
//
//    inputChannel.send(new GenericMessage("==> Hello from Scala"))
//  }
//  /**
//   *
//   */
//  def directChannelWithServiceAndOutputMessageToQueueChannel(): Unit = {
//
//    val inputChannel = channel.withName("inputChannel")
//    //    val outputChannel = channel.withQueue(5).andName("outputChannel")
//    val outputChannel = channel.withName("outputChannel").andQueue(5)
//
//    val integrationContext = IntegrationContext(
//        inputChannel >=>
//        service.withName("myService").using { m: Message[String] =>  m.getPayload.toUpperCase() } >=>
//        outputChannel
//    )
//
//    inputChannel.send(new GenericMessage("==> Hello from Scala"))
//    val outputMessage = outputChannel.receive
//    println("Output Message: " + outputMessage)
//  }
//  /**
//   *
//   */
//  def withPollingConsumerAndSpel(): Unit = {
//
//    val inputChannel = channel.withExecutor().andName("inputChannel")
//    val middleChannel = channel.withQueue(5).andName("middleChannel")
//    val resultChannel = channel.withQueue.andName("resultChannel")
//
//    val integrationContext = IntegrationContext(
//      inputChannel >=>
//        service.withName("myService").using { m: Message[String] => m.getPayload.toUpperCase() } >=>
//        middleChannel >=>
//        //transform.withPoller(5, 1000).andName("myTransformer").using{"'### ' + payload.toLowerCase() + ' ###'"} >=>
//        transform.withName("myTransformer").andPoller(1000, 5).using { "'### ' + payload.toLowerCase() + ' ###'" } >=>
//        resultChannel
//    )
//
//    inputChannel.send(new GenericMessage("==> Hello from Scala"))
//    val outputMessage = resultChannel.receive
//    println("Output Message: " + outputMessage)
//  }
//  /**
//   *
//   */
//  def withPollingConsumerAndSpelDefaultPoller(): Unit = {
//
//    val inputChannel = channel.withName("inputChannel").andExecutor
//    val middleChannel = channel.withName("middleChannel").andQueue(5)
//    val resultChannel = channel.withName("resultChannel").andQueue
//
//    val integrationContext = IntegrationContext(
//        inputChannel >=>
//        service.using { m: Message[String] => m.getPayload.toUpperCase() } >=>
//        middleChannel >=>
//        transform.using { "'### ' + payload.toLowerCase() + ' ###'" } >=>
//        resultChannel
//    )
//
//    inputChannel.send(new GenericMessage("==> Hello from Scala"))
//    val outputMessage = resultChannel.receive
//    println("Output Message: " + outputMessage)
//  }
//  /**
//   *
//   */
//  def withPubSubChannel(): Unit = {
//    val inputChannel = pub_sub_channel.withName("inputChannel")
//    val middleChannel = channel.withName("middleChannel").andQueue(5)
//    val resultChannel = channel.withName("resultChannel").andQueue
//
//    val integrationContext = IntegrationContext(
//      inputChannel >=> (
//        // subscriber 1
//    	{
//    		transform.withName("xfmrA").using { "'From Transformer: ' + payload.toUpperCase()" } >=>
//    		middleChannel >=>
//    		transform.withName("xfmrB").using { m: Message[String] => m.getPayload().asInstanceOf[String].toUpperCase() } >=>
//    		resultChannel
//    	},
//        // subscriber 2
//        {
//          service.using { m: Message[String] => println("From Service Activator: " + m) }
//        })
//    )
//
//    inputChannel.send(new GenericMessage("==> Hello from Scala"))
//    val outputMessage = resultChannel.receive
//    println("Output Message: " + outputMessage)
//  }
//  /**
//   *
//   */
//  def withImplicitChannels(): Unit = {
//
//    val inputChannel = channel.withName("inputChannel")
//
//    val integrationContext = IntegrationContext(
//        inputChannel >=>
//        service.using{m:Message[_] => m.getPayload + "_activator1"} >=>
//        transform.using{m:Message[_] => m.getPayload + "_transformer1"} >=>
//        service.using{m:Message[_] => m.getPayload + "_activator2"} >=>
//        transform.using{m:Message[_] => m.getPayload + "_transformer2"} >=>
//        service.using{m:Message[_] => println(m)}
//    )
//
//    inputChannel.send(new GenericMessage("==> Hello from Scala"))
//  }
//  /**
//   *
//   */
//  def withRouter(): Unit = {
//
//    val inputChannel = channel.withName("inputChannel")
//
//    val integrationContext = IntegrationContext(
//        {
//          channel("foo") >=>
//          service.using{ m: Message[String] => println("FROM FOO channel: " + m.getPayload) }
//        },
//    	{
//          channel("bar") >=>
//          service.using{ m: Message[String] => println("FROM BAR channel: " + m.getPayload) }
//        },
//        {
//          inputChannel >=>
//          route.using{m: Message[String] => m.getPayload}
//        }
//    )
//
//    inputChannel.send(new GenericMessage("foo"))
//    inputChannel.send(new GenericMessage("bar"))
//  }
//
//  /**
//   *
//   */
//  def withRouterAndDefaultOutputChannel(): Unit = {
//
//    val inputChannel = channel.withName("inputChannel")
//    val defaultOutputChannel = channel.withName("defaultOutputChannel").andQueue
//
//    val integrationContext = IntegrationContext(
//        {
//          channel("foo") >=>
//          service.using{ m: Message[String] => println("FROM FOO channel: " + m.getPayload) }
//        },
//    	{
//          channel("bar") >=>
//          service.using{ m: Message[String] => println("FROM BAR channel: " + m.getPayload) }
//        },
//        {
//          inputChannel >=>
//          route.using{m: Message[String] =>  m.getPayload} >=>
//          defaultOutputChannel
//        }
//    )
//
//    inputChannel.send(new GenericMessage("foo"))
//    inputChannel.send(new GenericMessage("bar"))
//    inputChannel.send(new GenericMessage("baz"))
//    println("Message from 'defaultOutputChannel' " + defaultOutputChannel.receive)
//  }

}
