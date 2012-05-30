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
package org.springframework.integration.dsl

import org.junit.{Assert, Test}
import org.springframework.integration.Message
import org.springframework.core.task.SimpleAsyncTaskExecutor
import scala.collection.immutable.List
import org.springframework.integration.MessageHeaders


/**
 * @author Oleg Zhurakousky
 */

class MessageAggregatorTests {

  @Test
  def validateDefaultAggregatorConfiguration(){

    val aggregator = aggregate()
    Assert.assertNotNull(aggregator.target.name)

    val namedAggregator = aggregate().additionalAttributes(name = "myAggregator")
    Assert.assertEquals("myAggregator", namedAggregator.target.name)
    val aggr = namedAggregator.target.asInstanceOf[Aggregator]
    Assert.assertNull	(aggr.keepReleasedMessages)
  }

  @Test
  def validateAggregatorWithCustomReleaseStartegy(){

    val aggregatorFlow =
      aggregate.until[Message[String]]{messages => messages.size == 2} -->
      handle{s:java.util.Collection[String] => println(s)}

    aggregatorFlow.send("1", headers=Map(MessageHeaders.CORRELATION_ID -> 1))
    Thread.sleep(1000)
    aggregatorFlow.send("2", headers=Map(MessageHeaders.CORRELATION_ID -> 1))
    Thread.sleep(1000)
    aggregatorFlow.send("3", headers=Map(MessageHeaders.CORRELATION_ID -> 1))
    Thread.sleep(1000)
    aggregatorFlow.send("4", headers=Map(MessageHeaders.CORRELATION_ID -> 1))
    Thread.sleep(1000)
    aggregatorFlow.send("5", headers=Map(MessageHeaders.CORRELATION_ID -> 1))
    Thread.sleep(1000)
    aggregatorFlow.send("6", headers=Map(MessageHeaders.CORRELATION_ID -> 1))
  }

  // commented syntax is dues to be implemented in 1.0.0.M2
  @Test
  def validateAggregatorConfiguration(){

    // additionalAttributes(name="foo") is always the last one

    aggregate()
    aggregate.additionalAttributes(name="foo")
    aggregate().additionalAttributes(name="foo")

    //ON

    aggregate.on{s:Any => s}
    aggregate.on{s:Any => s}.keepReleasedMessages
    aggregate.on{s:Any => s}.keepReleasedMessages.additionalAttributes(name="foo")
    aggregate.on{s:Any => s}.keepReleasedMessages.expireGroupsOnCompletion
    aggregate.on{s:Any => s}.keepReleasedMessages.expireGroupsOnCompletion.additionalAttributes(name="foo")
    aggregate.on{s:Any => s}.keepReleasedMessages.sendPartialResultOnExpiry
    aggregate.on{s:Any => s}.keepReleasedMessages.sendPartialResultOnExpiry.additionalAttributes(name="foo")

    aggregate.on{s:Any => s}.sendPartialResultOnExpiry
    aggregate.on{s:Any => s}.sendPartialResultOnExpiry.keepReleasedMessages
    aggregate.on{s:Any => s}.sendPartialResultOnExpiry.keepReleasedMessages.expireGroupsOnCompletion
    aggregate.on{s:Any => s}.sendPartialResultOnExpiry.additionalAttributes(name="foo")
    aggregate.on{s:Any => s}.sendPartialResultOnExpiry.expireGroupsOnCompletion
    aggregate.on{s:Any => s}.sendPartialResultOnExpiry.expireGroupsOnCompletion.additionalAttributes(name="foo")
    aggregate.on{s:Any => s}.additionalAttributes(name="foo")
    aggregate.on{s:Any => s}.expireGroupsOnCompletion
    aggregate.on{s:Any => s}.expireGroupsOnCompletion.keepReleasedMessages
    aggregate.on{s:Any => s}.expireGroupsOnCompletion.keepReleasedMessages.sendPartialResultOnExpiry
    aggregate.on{s:Any => s}.expireGroupsOnCompletion.additionalAttributes(name="foo")
    aggregate.on{s:Any => s}.expireGroupsOnCompletion.sendPartialResultOnExpiry
    aggregate.on{s:Any => s}.expireGroupsOnCompletion.sendPartialResultOnExpiry.additionalAttributes(name="foo")

    //UNTIL

//    aggregate.until{payload:String => payload == "foo"}
//    aggregate.until{payload:String => payload == "foo"}.sendPartialResultOnExpiry
//    aggregate.until{payload:String => payload == "foo"}.sendPartialResultOnExpiry.additionalAttributes(name="foo")
//    aggregate.until{payload:String => payload == "foo"}.sendPartialResultOnExpiry.expireGroupsOnCompletion
//    aggregate.until{payload:String => payload == "foo"}.sendPartialResultOnExpiry.expireGroupsOnCompletion.additionalAttributes(name="foo")
//    aggregate.until{payload:String => payload == "foo"}.additionalAttributes(name="foo")
//    aggregate.until{payload:String => payload == "foo"}.expireGroupsOnCompletion
//    aggregate.until{payload:String => payload == "foo"}.expireGroupsOnCompletion.additionalAttributes(name="foo")
//    aggregate.until{payload:String => payload == "foo"}.expireGroupsOnCompletion.sendPartialResultOnExpiry
//    aggregate.until{payload:String => payload == "foo"}.expireGroupsOnCompletion.sendPartialResultOnExpiry.additionalAttributes(name="foo")

    // AGGREGATE UNTIL

//    aggregate{s => s}.until{payload:String => payload == "foo"}
//    aggregate{s => s}.until{payload:String => payload == "foo"}.sendPartialResultOnExpiry
//    aggregate{s => s}.until{payload:String => payload == "foo"}.sendPartialResultOnExpiry.additionalAttributes(name="foo")
//    aggregate{s => s}.until{payload:String => payload == "foo"}.sendPartialResultOnExpiry.expireGroupsOnCompletion
//    aggregate{s => s}.until{payload:String => payload == "foo"}.sendPartialResultOnExpiry.expireGroupsOnCompletion.additionalAttributes(name="foo")
//    aggregate{s => s}.until{payload:String => payload == "foo"}.additionalAttributes(name="foo")
//    aggregate{s => s}.until{payload:String => payload == "foo"}.expireGroupsOnCompletion
//    aggregate{s => s}.until{payload:String => payload == "foo"}.expireGroupsOnCompletion.additionalAttributes(name="foo")
//    aggregate{s => s}.until{payload:String => payload == "foo"}.expireGroupsOnCompletion.sendPartialResultOnExpiry
//    aggregate{s => s}.until{payload:String => payload == "foo"}.expireGroupsOnCompletion.sendPartialResultOnExpiry.additionalAttributes(name="foo")

    // AGGREGATE ON

    aggregate{s => s}.on{s:Any => s}
    aggregate{s => s}.on{s:Any => s}.sendPartialResultOnExpiry
    aggregate{s => s}.on{s:Any => s}.sendPartialResultOnExpiry.additionalAttributes(name="foo")
    aggregate{s => s}.on{s:Any => s}.sendPartialResultOnExpiry.expireGroupsOnCompletion
    aggregate{s => s}.on{s:Any => s}.sendPartialResultOnExpiry.expireGroupsOnCompletion.additionalAttributes(name="foo")
    aggregate{s => s}.on{s:Any => s}.additionalAttributes(name="foo")
    aggregate{s => s}.on{s:Any => s}.expireGroupsOnCompletion
    aggregate{s => s}.on{s:Any => s}.expireGroupsOnCompletion.additionalAttributes(name="foo")
    aggregate{s => s}.on{s:Any => s}.expireGroupsOnCompletion.sendPartialResultOnExpiry
    aggregate{s => s}.on{s:Any => s}.expireGroupsOnCompletion.sendPartialResultOnExpiry.additionalAttributes(name="foo")

    // AGGREGATE ON/UNTIL

//    aggregate{s => s}.on{s:Any => s}.until{payload:String => payload == "foo"}
//    aggregate{s => s}.on{s:Any => s}.until{payload:String => payload == "foo"}.expireGroupsOnCompletion
//    aggregate{s => s}.on{s:Any => s}.until{payload:String => payload == "foo"}.expireGroupsOnCompletion.sendPartialResultOnExpiry
//    aggregate{s => s}.on{s:Any => s}.until{payload:String => payload == "foo"}.expireGroupsOnCompletion.sendPartialResultOnExpiry.additionalAttributes(name="foo")
//    aggregate{s => s}.on{s:Any => s}.until{payload:String => payload == "foo"}.additionalAttributes(name="foo")
//    aggregate{s => s}.on{s:Any => s}.until{payload:String => payload == "foo"}.sendPartialResultOnExpiry
//    aggregate{s => s}.on{s:Any => s}.until{payload:String => payload == "foo"}.sendPartialResultOnExpiry.additionalAttributes(name="foo")
//    aggregate{s => s}.on{s:Any => s}.until{payload:String => payload == "foo"}.sendPartialResultOnExpiry.expireGroupsOnCompletion
//    aggregate{s => s}.on{s:Any => s}.until{payload:String => payload == "foo"}.sendPartialResultOnExpiry.expireGroupsOnCompletion.additionalAttributes(name="foo")
//    aggregate{s => s}.on{s:Any => s}.until{payload:String => payload == "foo"}.expireGroupsOnCompletion.additionalAttributes(name="foo")

    // AGGRGATE with ON/UNTIL
//    aggregate{s => s}.on{s:Any => s}.until{payload:String => payload == "foo"}
//    aggregate{s => s}.on{s:Any => s}.until{payload:String => payload == "foo"}.additionalAttributes(name="foo")
//    aggregate{s => s}.on{s:Any => s}.until{payload:String => payload == "foo"}.expireGroupsOnCompletion
//    aggregate{s => s}.on{s:Any => s}.until{payload:String => payload == "foo"}.
//    			expireGroupsOnCompletion.
//    			keepReleasedMessages.
//    			additionalAttributes(name="foo")

  }

}