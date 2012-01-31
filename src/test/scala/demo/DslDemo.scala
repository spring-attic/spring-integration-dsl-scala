package demo
import org.springframework.eip.dsl._
import org.junit._
import org.springframework.integration.message.GenericMessage
import org.springframework.integration.Message
import org.springframework.integration.store.SimpleMessageStore
import org.springframework.core.task.SimpleAsyncTaskExecutor
import org.springframework.integration.support.MessageBuilder
import collection.JavaConversions

/**
 * @author Oleg Zhurakousky
 *
 */
class DslDemo {
  @Test
  def runDemos() = {

    channelConfigDemo
    println("### End channelConfigDemo demo\n")
    messagingBridgeDemo
    println("### End messagingBridgeDemo demo\n")
    serviceActivatorDemo
    println("### End serviceActivatorDemo \n")
    transformerDemo
    println("### End transformerDemo \n")
    headerValueRouterDemo
    println("### End headerValueRouterDemo \n")
    payloadTypeRouterDemo
    println("### End payloadTypeRouterDemo \n")
    methodInvokingRouterDemo
    println("### End genericRouterDemo \n")
    messageSplitterDemo
    println("### End messageSplitter \n")
    messageSplitterAndAggregatorDemo
    println("### End messageSplitterAndAggregatorDemo \n")
  }

  /**
   *
   */
  def channelConfigDemo: Unit = {

    val directChannel = Channel("myChannel")

    val queueChannelA = Channel("myChannel").withQueue

    val queueChannelB = Channel("myChannel") withQueue

    val queueChannelC = Channel("myChannel").withQueue(capacity = 10, messageStore = new SimpleMessageStore)

    val executorChannel = Channel("myChannel").withDispatcher(taskExecutor = new SimpleAsyncTaskExecutor)

  }

  def messagingBridgeDemo: Unit = {

    val directChannel = Channel("direct")
    val executorChannel = Channel("executor").withDispatcher(taskExecutor = new SimpleAsyncTaskExecutor)

    val queueChannel = Channel("myChannel") withQueue

    val messageBridgeViaPollableChannel =
      directChannel -->
      queueChannel -->
      poll.usingFixedRate(3) -->  handle.using{m:Message[_] => println(m)}

    val messageBridgeViaSimpleChannels =
      directChannel -->
      executorChannel -->
      handle.using{m:Message[_] => println(m)}
  }
  
  def serviceActivatorDemo = {
    val serviceSpel = handle.using("any valid spel")
    val serviceFunctionOnMessage = handle.using{m:Message[_] => println(m)}
    val serviceFunctionOnPayload = handle.using{s:String => s}
    val serviceWithAdditionalAttributes = handle.using{s:String => s} where(name = "myService")
  }

  def transformerDemo = {
    val transformerSpel = transform.using("any valid spel")
    // the below is illegal since transformer can only accept functions that return AnyRef to exclude Unit
    //val transformerFunctionOnMessage = transform.using{m:Message[_] => println(m)}
    val transformerFunctionOnMessage = transform.using{m:Message[_] => m.getPayload.toString}
    val transformerFunctionOnPayload = transform.using{s:String => s}
    val transformerAdditionalAttributes = transform.using{s:String => s} where(name = "myService")
  }
  
  def headerValueRouterDemo = {

    val headerValueRouter = 
      route.onValueOfHeader("mySpecialHeader")(
        when("foo") {
          transform.using{m:Message[_] => m.getPayload.toString} -->
          handle.using{m:Message[_] => println(m)}
        },
        when("bar") {
          Channel("barChannel")
          transform.using{m:Message[String] => m.getPayload.toUpperCase} -->
          handle.using{m:Message[_] => println(m)}
        }
      )

    headerValueRouter.send("hello", headers = Map("mySpecialHeader" -> "foo"))
    // or
    headerValueRouter.send(MessageBuilder.withPayload("hello").setHeader("mySpecialHeader", "bar").build())
  }

  def payloadTypeRouterDemo = {

    val payloadTypeRouter =
      route.onPayloadType(
        when(classOf[String]) {
          transform.using{m:Message[_] => m.getPayload.toString} -->
          handle.using{m:Message[_] => println(m)}
        },
        when(classOf[Number]) {
          Channel("barChannel") -->
          transform.using{m:Message[Int] => (m.getPayload + 6).toString} -->
          handle.using{m:Message[_] => println(m)}
        }
      )

    payloadTypeRouter.send("hello")
    // or
    payloadTypeRouter.send(MessageBuilder.withPayload(23).build())
  }

  def methodInvokingRouterDemo = {

    val methodInvokingRouter =
      route.using{i:Int => i}(
        when(1) {
          handle.using{m:Message[_] => println("From 1: " + m)}
        },
        when(2) {
          handle.using{m:Message[_] => println("From 2: " + m)}
        }
      )

    methodInvokingRouter.send(1)
    // or
    methodInvokingRouter.send(2)
  }

  def messageSplitterDemo = {

    val splitterReturningScalaIterable =
      split.using{m:Message[List[_]] => m.getPayload} -->
      handle.using{m:Message[_] => println(m)}

    splitterReturningScalaIterable.send(List(1, 2, 3))
  }

  def messageSplitterAndAggregatorDemo = {

    val splitterAndAggregator =
      split.using{m:Message[List[_]] => m.getPayload} -->
      transform.using{m:Message[Int] => (m.getPayload + 2).toString}  -->
      aggregate() -->
      handle.using{m:Message[_] => println(m)}


    splitterAndAggregator.send(List(1, 2, 3))
  }
}
