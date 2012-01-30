/*
 * Copyright 2002-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.eip.dsl
import org.junit.{Assert, Test}
import org.springframework.context.support.GenericApplicationContext
import org.springframework.integration.Message
import org.springframework.integration.support.MessageBuilder

/**
 * @author Oleg Zhurakousky
 */
class CompositionInitializationTests {

  @Test
  def validateEnterableComposition(){
    val compositionA = Channel("foo")
    Assert.assertTrue(compositionA.isInstanceOf[CompletableEIPConfigurationComposition])

    val compositionB = Channel("foo").withDispatcher(failover = true)
    Assert.assertTrue(compositionB.isInstanceOf[CompletableEIPConfigurationComposition])

    val compositionC = Channel("foo").withQueue()
    Assert.assertTrue(compositionC.isInstanceOf[CompletableEIPConfigurationComposition])

    val compositionD = compositionA --> handle.using("spel")
    Assert.assertTrue(compositionD.isInstanceOf[CompletableEIPConfigurationComposition])

    val compositionE = compositionB --> handle.using("spel")
    Assert.assertTrue(compositionE.isInstanceOf[CompletableEIPConfigurationComposition])

    val compositionF = compositionC --> poll.usingFixedRate(4) --> transform.using("spel")
    Assert.assertTrue(compositionF.isInstanceOf[CompletableEIPConfigurationComposition])

    val compositionG = Channel("a") --> Channel("b")
    Assert.assertTrue(compositionG.isInstanceOf[CompletableEIPConfigurationComposition])

    val compositionH = Channel("a") --> handle.using("") --> transform.using("")  --> handle.using("")
    Assert.assertTrue(compositionH.isInstanceOf[CompletableEIPConfigurationComposition])

    val compositionI = Channel("a") --> (handle.using("") --> transform.using(""))  --> handle.using("")
    Assert.assertTrue(compositionI.isInstanceOf[CompletableEIPConfigurationComposition])

    val compositionJ = Channel("a") --> handle.using("spel") --> Channel("b") --> handle.using("spel")
    Assert.assertTrue(compositionJ.isInstanceOf[CompletableEIPConfigurationComposition])

    val compositionK = Channel("a")  --> Channel("b") --> handle.using("spel")
    Assert.assertTrue(compositionK.isInstanceOf[CompletableEIPConfigurationComposition])

    val compositionL = Channel("a").withQueue() --> poll.usingFixedRate(4)  --> Channel("b")
    Assert.assertTrue(compositionL.isInstanceOf[CompletableEIPConfigurationComposition])

    val compositionM = Channel("a").withQueue() --> poll.usingFixedRate(4)  --> Channel("b") --> handle.using("spel")
    Assert.assertTrue(compositionM.isInstanceOf[CompletableEIPConfigurationComposition])

    // non-CompletableComposition
    val compositionAn = Channel("a").withQueue() --> poll.usingFixedDelay(5)
    Assert.assertFalse(compositionAn.isInstanceOf[CompletableEIPConfigurationComposition])

    val compositionBn = handle.using("") --> transform.using("")
    Assert.assertFalse(compositionBn.isInstanceOf[CompletableEIPConfigurationComposition])

    val compositionCn = handle.using("") --> Channel("")
    Assert.assertFalse(compositionCn.isInstanceOf[CompletableEIPConfigurationComposition])

    val compositionDn = handle.using("") --> transform.using("")--> Channel("")
    Assert.assertFalse(compositionDn.isInstanceOf[CompletableEIPConfigurationComposition])

    val compositionEn = handle.using("") --> Channel("") --> transform.using("")--> Channel("")
    Assert.assertFalse(compositionEn.isInstanceOf[CompletableEIPConfigurationComposition])

    val compositionFn = handle.using("") --> Channel("").withQueue(8) --> poll.usingFixedDelay(3)
    Assert.assertFalse(compositionFn.isInstanceOf[CompletableEIPConfigurationComposition])
  }
  @Test
  def validateEIPContext(){

    // the below should be illegal since it is not a completable composition
    // EIPContext(handle.using("spel"))
    // EIPContext(handle.using("spel") --> transform.using("spel"))

    EIPContext(Channel("foo"))

    EIPContext(Channel("foo"), Channel("bar").withQueue())

    EIPContext(
      Channel("a") -->
        handle.using("") -->
        transform.using("spel")  -->
        handle.using("spel")
    )

    EIPContext(
      Channel("a") -->
        handle.using("") -->
        transform.using("spel")  -->
        handle.using("spel"),

      Channel("bar").withQueue(4) --> poll.usingFixedDelay(5) -->
        handle.using("spel")
    )

    val parentAc = new GenericApplicationContext
    parentAc.refresh()
    EIPContext(parentAc)(
      Channel("a") -->
        handle.using("") -->
        transform.using("spel")  -->
        handle.using("spel"),

      Channel("bar").withQueue(4) --> poll.usingFixedDelay(5) -->
        handle.using("spel")
    )
    
    val configA = handle.using("spel") --> transform.using("spel")

    val configB = Channel("s") --> transform.using("spel")

    EIPContext(configB --> configA)
  }

  @Test
  def eipChannelInitializationTest() {

    val channelConfigC = Channel("cChannel").withQueue(5)

    val channelConfigD = Channel("dChannel").withDispatcher(failover = true)

    val context = EIPContext(
      Channel("aChannel") -->
        handle.using("spel") -->
        transform.using("spel")  -->
        Channel("myChannel") -->
        handle.using("spel"),

      channelConfigC --> poll.usingFixedDelay(5) -->
        handle.using("spel"),

      channelConfigD -->
        Channel("hello") -->        // bridge
        transform.using("spel"),

      Channel("queueChannel").withQueue(5) --> poll.usingFixedDelay(5) -->
        Channel("fromQueueChannel") -->        // pollable bridge
        transform.using("spel") -->
        Channel("foo") -->
        handle.using("spel") -->
        transform.using("spel")
    )
  }

  @Test
  def validateContextAutoCreation(){

    // although below would not work for explicit creation of EIPContext
    // this flow clearly has a starting point so the inlut channel will be autocreated
    val messageFlowA =
      handle.using("payload.toUpperCase()") -->
      handle.using("T(java.lang.System).out.println('Received message: ' + #this)")
    messageFlowA.send("with SpEL")

    val messageFlowB =
      handle.using{m:Message[String] => m.getPayload.toUpperCase} -->
      handle.using{m:Message[_] => println(m)}
    messageFlowB.send("with Scala Functions")

    val messageFlowC = handle.using{s:String => s.toUpperCase} --> handle.using{m:Message[_] => println(m)}
    messageFlowC.send("with Scala Functions and payload extraction")

    val messageFlowD = Channel("inputChannel") --> handle.using("payload")
    val messageFlowCD = messageFlowD --> messageFlowC
    messageFlowCD.send("with Scala Functions, SpEL and flow reuse")

    val messageFlowE = Channel("inputChannel").withQueue() --> poll.usingFixedRate(5).withMaxMessagesPerPoll(10)   -->
            handle.using{s:String => println("Message payload:" + s)}

    messageFlowE.send("hello")
    
    val fooRoute = Channel("fooChannel") --> handle.using{s:String => s.toUpperCase} -->
      handle.using{s:String => println("FOO route:" + s)}

    val barRoute = Channel("barChannel") --> transform.using{s:String => s.toUpperCase} -->
      handle.using{s:String => println("BAR route:" + s)}

    val messageFlowF =
              Channel("inputChannel") -->
              filter.using {s:String => s.equals("hello")}.where(exceptionOnRejection = true) -->
              route.onValueOfHeader("someHeader") (
                when("foo") {
                  fooRoute
//                  handle.using{s:String => s.toUpperCase} -->
//                  handle.using{s:String => println("FOO route:" + s)}
                },
                when("bar") {
                  barRoute
//                  handle.using{s:String => s.toUpperCase} -->
//                  handle.using{s:String => println("BAR route:" + s)}
                }
              )



    messageFlowF.send(MessageBuilder.withPayload("hello").setHeader("someHeader", "foo").build())
    messageFlowF.send("hello", headers = Map("someHeader" -> "bar"))
    messageFlowF.send("hi", headers = Map("someHeader" -> "bar"))
  }

  @Test(expected = classOf[IllegalStateException])
  def validateFailureWhenFlowEndsWithPoller(){
    val messageFlowA = handle.using("A")  --> handle.using("B")
    val messageFlowC = Channel("inputChannel").withQueue() --> poll.usingFixedRate(3)
    val mergedCompositionC = messageFlowA --> messageFlowC
    mergedCompositionC.send("hello")
  }

  @Test(expected = classOf[IllegalStateException])
  def validateFailureWhenFlowEndsWithDirectChannel(){
    val messageFlowA = handle.using("A")  --> handle.using("B")
    val messageFlowC = Channel("inputChannel")
    val mergedCompositionC = messageFlowA --> messageFlowC
    mergedCompositionC.send("hello")
  }

}

