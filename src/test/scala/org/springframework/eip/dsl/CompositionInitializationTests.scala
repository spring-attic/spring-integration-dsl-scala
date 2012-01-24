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
import org.springframework.integration.message.GenericMessage
import org.springframework.integration.channel.DirectChannel
import org.springframework.integration.{MessageChannel, Message}
import org.springframework.integration.core.PollableChannel

/**
 * @author Oleg Zhurakousky
 */
class CompositionInitializationTests {

  @Test
  def validateEnterableComposition(){
    val compositionA = Channel("foo")
    Assert.assertTrue(compositionA.isInstanceOf[CompletableComposition])

    val compositionB = Channel("foo").withDispatcher(failover = true)
    Assert.assertTrue(compositionB.isInstanceOf[CompletableComposition])

    val compositionC = Channel("foo").withQueue()
    Assert.assertTrue(compositionC.isInstanceOf[CompletableComposition])

    val compositionD = compositionA --> handle.using("spel")
    Assert.assertTrue(compositionD.isInstanceOf[CompletableComposition])

    val compositionE = compositionB --> handle.using("spel")
    Assert.assertTrue(compositionE.isInstanceOf[CompletableComposition])

    val compositionF = compositionC --> poll.usingFixedRate(4) --> transform.using("spel")
    Assert.assertTrue(compositionF.isInstanceOf[CompletableComposition])

    val compositionG = Channel("a") --> Channel("b")
    Assert.assertTrue(compositionG.isInstanceOf[CompletableComposition])

    val compositionH = Channel("a") --> handle.using("") --> transform.using("")  --> handle.using("")
    Assert.assertTrue(compositionH.isInstanceOf[CompletableComposition])

    val compositionI = Channel("a") --> (handle.using("") --> transform.using(""))  --> handle.using("")
    Assert.assertTrue(compositionI.isInstanceOf[CompletableComposition])

    // non-CompletableComposition
    val compositionAn = Channel("a").withQueue() --> poll.usingFixedDelay(5)
    Assert.assertFalse(compositionAn.isInstanceOf[CompletableComposition])

    val compositionBn = handle.using("") --> transform.using("")
    Assert.assertFalse(compositionBn.isInstanceOf[CompletableComposition])

    val compositionCn = handle.using("") --> Channel("")
    Assert.assertFalse(compositionCn.isInstanceOf[CompletableComposition])

    val compositionDn = handle.using("") --> transform.using("")--> Channel("")
    Assert.assertFalse(compositionDn.isInstanceOf[CompletableComposition])

    val compositionEn = handle.using("") --> Channel("") --> transform.using("")--> Channel("")
    Assert.assertFalse(compositionEn.isInstanceOf[CompletableComposition])

    val compositionFn = handle.using("") --> Channel("").withQueue() --> poll.usingFixedDelay(3) --> transform.using("")--> Channel("")
    Assert.assertFalse(compositionFn.isInstanceOf[CompletableComposition])
  }
  @Test
  def validateEIPConfig(){

    // the below should be illegal since it is not a completable composition
    // EIPConfig(handle.using("spel"))
    // EIPConfig(handle.using("spel") --> transform.using("spel"))

    EIPConfig(Channel("foo"))

    EIPConfig(Channel("foo"), Channel("bar").withQueue())

    EIPConfig(
      Channel("a") --> 
        handle.using("") --> 
        transform.using("spel")  --> 
        handle.using("spel")
    )

    EIPConfig(
      Channel("a") --> 
        handle.using("") --> 
        transform.using("spel")  --> 
        handle.using("spel"),
      
      Channel("bar").withQueue(4) --> poll.usingFixedDelay(5) -->
        handle.using("spel")
    )
  }

}