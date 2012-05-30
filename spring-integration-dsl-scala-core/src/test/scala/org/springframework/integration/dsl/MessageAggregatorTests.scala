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

import org.junit.{ Assert, Test }
import org.junit.Assert._
import org.springframework.integration.Message
import org.springframework.core.task.SimpleAsyncTaskExecutor
import scala.collection.immutable.List
import org.springframework.integration.MessageHeaders

/**
 * @author Oleg Zhurakousky
 */

class MessageAggregatorTests {

  @Test
  def validateDefaultAggregatorConfiguration() {

    val aggregator = aggregate()
    Assert.assertNotNull(aggregator.target.name)

    val namedAggregator = aggregate().additionalAttributes(name = "myAggregator")
    Assert.assertEquals("myAggregator", namedAggregator.target.name)
    val aggr = namedAggregator.target.asInstanceOf[Aggregator]
    Assert.assertNull(aggr.keepReleasedMessages)
  }

  @Test
  def validateAggregatorWithCustomCorrelationAndReleaseStrategy() {

    val aggregatorFlow =
      aggregate.on{payload:String => payload}.until[String] { messages => messages.size == 2 } -->
      handle { s: Iterable[String] => assertEquals(2, s.size); s.foreach(println _) }

    aggregatorFlow.send("1")
    Thread.sleep(1000)
    aggregatorFlow.send("1")
    Thread.sleep(1000)
  }

  @Test
  def validateAggregatorWithCustomAggregationCorrelationAndReleaseStrategy() {

    val aggregatorFlow =
      aggregate{s: Iterable[String] => s}.on{payload:String => payload}.until[String] { messages => messages.size == 2 } -->
      handle { s: Iterable[String] => assertEquals(2, s.size); s.foreach(println _) }

    aggregatorFlow.send("1")
    Thread.sleep(1000)
    aggregatorFlow.send("1")
    Thread.sleep(1000)
  }

  @Test
  def validateAggregatorWithCustomReleaseStrategyAndScalaTypeOutput() {

    val aggregatorFlow =
      aggregate.until[String] { messages => messages.size == 2 } -->
        handle { s: Iterable[String] => assertEquals(2, s.size); s.foreach(println _) }

    aggregatorFlow.send("1", headers = Map(MessageHeaders.CORRELATION_ID -> 1))
    Thread.sleep(1000)
    aggregatorFlow.send("2", headers = Map(MessageHeaders.CORRELATION_ID -> 1))
    Thread.sleep(1000)
  }

  @Test
  def validateAggregatorWithCustomReleaseStrategyAndJavaTypeOutput() {

    val aggregatorFlow =
      aggregate.until[String] { messages => messages.size == 2 } -->
        handle { s: java.util.Collection[String] => assertEquals(2, s.size); println(s) }

    aggregatorFlow.send("1", headers = Map(MessageHeaders.CORRELATION_ID -> 1))
    Thread.sleep(1000)
    aggregatorFlow.send("2", headers = Map(MessageHeaders.CORRELATION_ID -> 1))
    Thread.sleep(1000)
  }

  @Test
  def validateDefaultAggregatorWithParsedOutputMessageAndJavaType() {

    val aggregatorFlow =
      aggregate.until[String] { messages => messages.size == 2 } -->
        handle { (s: java.util.Collection[String], headers:Map[String, _])  => assertEquals(2, s.size); println(s) }

    aggregatorFlow.send("1", headers = Map(MessageHeaders.CORRELATION_ID -> 1))
    Thread.sleep(1000)
    aggregatorFlow.send("2", headers = Map(MessageHeaders.CORRELATION_ID -> 1))
    Thread.sleep(1000)
  }

  @Test
  def validateDefaultAggregatorWithParsedOutputMessageAndScalaType() {

    val aggregatorFlow =
      aggregate.until[String] { messages => messages.size == 2 } -->
      handle { (s: Iterable[String], headers:Map[String, _])  => assertEquals(2, s.size); println(s) }

    aggregatorFlow.send("1", headers = Map(MessageHeaders.CORRELATION_ID -> 1))
    Thread.sleep(1000)
    aggregatorFlow.send("2", headers = Map(MessageHeaders.CORRELATION_ID -> 1))
  }

  @Test
  def validateAggregatorWithTransformerAndScalaTypeOutput() {

    val aggregatorFlow =
      aggregate.until[String] { messages => messages.size == 2 } -->
      transform{s: Iterable[String] => s} -->
      handle { s: Iterable[String] => assertEquals(2, s.size); s.foreach(println _) }

    aggregatorFlow.send("1", headers = Map(MessageHeaders.CORRELATION_ID -> 1))
    Thread.sleep(1000)
    aggregatorFlow.send("2", headers = Map(MessageHeaders.CORRELATION_ID -> 1))
    Thread.sleep(1000)
  }

  @Test
  def validateAggregatorWithTransformerParsedMessageAndScalaTypeOutput() {

    val aggregatorFlow =
      aggregate.until[String] { messages => messages.size == 2 } -->
      transform{s: Iterable[String] => s} -->
      handle { (s: Iterable[String], headers:Map[String, _]) => assertEquals(2, s.size); s.foreach(println _) }

    aggregatorFlow.send("1", headers = Map(MessageHeaders.CORRELATION_ID -> 1))
    Thread.sleep(1000)
    aggregatorFlow.send("2", headers = Map(MessageHeaders.CORRELATION_ID -> 1))
    Thread.sleep(1000)
  }

  @Test
  def validateAggregatorWithFilterAndScalaTypeOutput() {

    val aggregatorFlow =
      aggregate.until[String] { messages => messages.size == 2 } -->
      filter{s: Iterable[String] => s.size == 2} -->
      handle { s: Iterable[String] => assertEquals(2, s.size); s.foreach(println _) }

    aggregatorFlow.send("1", headers = Map(MessageHeaders.CORRELATION_ID -> 1))
    Thread.sleep(1000)
    aggregatorFlow.send("2", headers = Map(MessageHeaders.CORRELATION_ID -> 1))
    Thread.sleep(1000)
  }

  @Test
  def validateAggregatorWithFilterParsedMessageAndScalaTypeOutput() {

    val aggregatorFlow =
      aggregate.until[String] { messages => messages.size == 2 } -->
      filter{(s: Iterable[String], headers:Map[String, _])  => s.size == 2} -->
      handle { (s: Iterable[String], headers:Map[String, _]) => assertEquals(2, s.size); s.foreach(println _) }

    aggregatorFlow.send("1", headers = Map(MessageHeaders.CORRELATION_ID -> 1))
    Thread.sleep(1000)
    aggregatorFlow.send("2", headers = Map(MessageHeaders.CORRELATION_ID -> 1))
    Thread.sleep(1000)
  }

  @Test
  def validateAggregatorWithRouterAndScalaTypeOutput() {

    val aggregatorFlow =
      aggregate.until[String] { messages => messages.size == 2 } -->
      route{s: Iterable[String] => (if (s.size == 2) "foo" else "bar")}(
        when("foo") then
            handle{m:Message[_] => println("In two: " + m)},
        when("bar") then
            handle{m:Message[_] => println("In else: " + m)}
      )

    aggregatorFlow.send("1", headers = Map(MessageHeaders.CORRELATION_ID -> 1))
    Thread.sleep(1000)
    aggregatorFlow.send("2", headers = Map(MessageHeaders.CORRELATION_ID -> 1))
    Thread.sleep(1000)
  }

  @Test
  def validateAggregatorWithSplitterSingleMessageAndScalaTypeOutput() {

    val aggregatorFlow =
      aggregate.until[String] { messages => messages.size == 2 } -->
      split{s: Iterable[String]  => s}.additionalAttributes(name = "foo") -->
      handle { m:Message[_] => println(m) }

    aggregatorFlow.send("1", headers = Map(MessageHeaders.CORRELATION_ID -> 1))
    Thread.sleep(1000)
    aggregatorFlow.send("2", headers = Map(MessageHeaders.CORRELATION_ID -> 1))
    Thread.sleep(1000)
  }

  @Test
  def validateAggregatorWithSplitterParsedMessageAndScalaTypeOutput() {

    val aggregatorFlow =
      aggregate.until[String] { messages => messages.size == 2 } -->
      split{(s: Iterable[String], headers:Map[String, _])  => s} -->
      handle { m:Message[_] => println(m) }

    aggregatorFlow.send("1", headers = Map(MessageHeaders.CORRELATION_ID -> 1))
    Thread.sleep(1000)
    aggregatorFlow.send("2", headers = Map(MessageHeaders.CORRELATION_ID -> 1))
    Thread.sleep(1000)
  }

  @Test
  def validateAggregatorConfiguration() {

    // additionalAttributes(name="foo") is always the last one

    aggregate()
    aggregate.additionalAttributes(name = "foo")

    aggregate.sendPartialResultOnExpiry
    aggregate.sendPartialResultOnExpiry.additionalAttributes(name = "foo")
    aggregate.sendPartialResultOnExpiry.keepReleasedMessages.additionalAttributes(name = "foo")
    aggregate.sendPartialResultOnExpiry.keepReleasedMessages.expireGroupsOnCompletion
    aggregate.sendPartialResultOnExpiry.keepReleasedMessages.expireGroupsOnCompletion.additionalAttributes(name = "foo")
    aggregate.sendPartialResultOnExpiry.expireGroupsOnCompletion
    aggregate.sendPartialResultOnExpiry.expireGroupsOnCompletion.additionalAttributes(name = "foo")
    aggregate.sendPartialResultOnExpiry.expireGroupsOnCompletion.keepReleasedMessages
    aggregate.sendPartialResultOnExpiry.expireGroupsOnCompletion.keepReleasedMessages.additionalAttributes(name = "foo")

    aggregate.keepReleasedMessages
    aggregate.keepReleasedMessages.additionalAttributes(name = "foo")
    aggregate.keepReleasedMessages.sendPartialResultOnExpiry
    aggregate.keepReleasedMessages.sendPartialResultOnExpiry.additionalAttributes(name = "foo")
    aggregate.keepReleasedMessages.sendPartialResultOnExpiry.expireGroupsOnCompletion
    aggregate.keepReleasedMessages.sendPartialResultOnExpiry.expireGroupsOnCompletion.additionalAttributes(name = "foo")
    aggregate.keepReleasedMessages.expireGroupsOnCompletion
    aggregate.keepReleasedMessages.expireGroupsOnCompletion.additionalAttributes(name = "foo")
    aggregate.keepReleasedMessages.expireGroupsOnCompletion.sendPartialResultOnExpiry
    aggregate.keepReleasedMessages.expireGroupsOnCompletion.sendPartialResultOnExpiry.additionalAttributes(name = "foo")

    aggregate.expireGroupsOnCompletion
    aggregate.expireGroupsOnCompletion.additionalAttributes(name = "foo")
    aggregate.expireGroupsOnCompletion.sendPartialResultOnExpiry
    aggregate.expireGroupsOnCompletion.sendPartialResultOnExpiry.additionalAttributes(name = "foo")
    aggregate.expireGroupsOnCompletion.sendPartialResultOnExpiry.keepReleasedMessages
    aggregate.expireGroupsOnCompletion.sendPartialResultOnExpiry.keepReleasedMessages.additionalAttributes(name = "foo")
    aggregate.expireGroupsOnCompletion.keepReleasedMessages
    aggregate.expireGroupsOnCompletion.keepReleasedMessages.additionalAttributes(name = "foo")
    aggregate.expireGroupsOnCompletion.keepReleasedMessages.sendPartialResultOnExpiry
    aggregate.expireGroupsOnCompletion.keepReleasedMessages.sendPartialResultOnExpiry.additionalAttributes(name = "foo")

    aggregate().additionalAttributes(name = "foo")
    aggregate().sendPartialResultOnExpiry

    //ON

    aggregate.on { s: Any => s }
    aggregate.on { s: Any => s }.keepReleasedMessages
//    aggregate.on { s: Any => s }.keepReleasedMessages.additionalAttributes(name = "foo")
    aggregate.on { s: Any => s }.keepReleasedMessages.expireGroupsOnCompletion
//    aggregate.on { s: Any => s }.keepReleasedMessages.expireGroupsOnCompletion.additionalAttributes(name = "foo")
    aggregate.on { s: Any => s }.keepReleasedMessages.sendPartialResultOnExpiry
//    aggregate.on { s: Any => s }.keepReleasedMessages.sendPartialResultOnExpiry.additionalAttributes(name = "foo")

    aggregate.on { s: Any => s }.sendPartialResultOnExpiry
    aggregate.on { s: Any => s }.sendPartialResultOnExpiry.keepReleasedMessages
    aggregate.on { s: Any => s }.sendPartialResultOnExpiry.keepReleasedMessages.expireGroupsOnCompletion
//    aggregate.on { s: Any => s }.sendPartialResultOnExpiry.additionalAttributes(name = "foo")
    aggregate.on { s: Any => s }.sendPartialResultOnExpiry.expireGroupsOnCompletion
//    aggregate.on { s: Any => s }.sendPartialResultOnExpiry.expireGroupsOnCompletion.additionalAttributes(name = "foo")
//    aggregate.on { s: Any => s }.additionalAttributes(name = "foo")
    aggregate.on { s: Any => s }.expireGroupsOnCompletion
    aggregate.on { s: Any => s }.expireGroupsOnCompletion.keepReleasedMessages
    aggregate.on { s: Any => s }.expireGroupsOnCompletion.keepReleasedMessages.sendPartialResultOnExpiry
//    aggregate.on { s: Any => s }.expireGroupsOnCompletion.additionalAttributes(name = "foo")
    aggregate.on { s: Any => s }.expireGroupsOnCompletion.sendPartialResultOnExpiry
//    aggregate.on { s: Any => s }.expireGroupsOnCompletion.sendPartialResultOnExpiry.additionalAttributes(name = "foo")

    //UNTIL

    aggregate.until[String] { messages => messages.size == 3 }
    aggregate.until[String] { messages => messages.size == 3 }.sendPartialResultOnExpiry
//    aggregate.until[String] { messages => messages.size == 3 }.sendPartialResultOnExpiry.additionalAttributes(name = "foo")
    aggregate.until[String] { messages => messages.size == 3 }.sendPartialResultOnExpiry.expireGroupsOnCompletion
//    aggregate.until[String] { messages => messages.size == 3 }.sendPartialResultOnExpiry.expireGroupsOnCompletion.additionalAttributes(name = "foo")
//    aggregate.until[String] { messages => messages.size == 3 }.additionalAttributes(name = "foo")
    aggregate.until[String] { messages => messages.size == 3 }.expireGroupsOnCompletion
//    aggregate.until[String] { messages => messages.size == 3 }.expireGroupsOnCompletion.additionalAttributes(name = "foo")
    aggregate.until[String] { messages => messages.size == 3 }.expireGroupsOnCompletion.sendPartialResultOnExpiry
//    aggregate.until[String] { messages => messages.size == 3 }.expireGroupsOnCompletion.sendPartialResultOnExpiry.additionalAttributes(name = "foo")

    // AGGREGATE UNTIL

    aggregate { s:Iterable[String] => s }.until[String] { messages => messages.size == 3 }
    aggregate { s:Iterable[String] => s }.until[String] { messages => messages.size == 3 }.keepReleasedMessages
    aggregate [String]{ s => s }.until[String] { messages => messages.size == 3 }.sendPartialResultOnExpiry
//    aggregate [String]{ s => s }.until[String] { messages => messages.size == 3 }.sendPartialResultOnExpiry.additionalAttributes(name = "foo")
    aggregate [String]{ s => s }.until[String] { messages => messages.size == 3 }.sendPartialResultOnExpiry.expireGroupsOnCompletion
//    aggregate [String]{ s => s }.until[String] { messages => messages.size == 3 }.sendPartialResultOnExpiry.expireGroupsOnCompletion.additionalAttributes(name = "foo")
//    aggregate [String]{ s => s }.until[String] { messages => messages.size == 3 }.additionalAttributes(name = "foo")
    aggregate [String]{ s => s }.until[String] { messages => messages.size == 3 }.expireGroupsOnCompletion
//    aggregate [String]{ s => s }.until[String] { messages => messages.size == 3 }.expireGroupsOnCompletion.additionalAttributes(name = "foo")
    aggregate [String]{ s => s }.until[String] { messages => messages.size == 3 }.expireGroupsOnCompletion.sendPartialResultOnExpiry
//    aggregate [String]{ s => s }.until[String] { messages => messages.size == 3 }.expireGroupsOnCompletion.sendPartialResultOnExpiry.additionalAttributes(name = "foo")

    // AGGREGATE ON

    aggregate [String]{ s => s }.on { s: Any => s }
    aggregate [String]{ s => s }.on { s: Any => s }.keepReleasedMessages
    aggregate [String]{ s => s }.on { s: Any => s }.sendPartialResultOnExpiry
//    aggregate [String]{ s => s }.on { s: Any => s }.sendPartialResultOnExpiry.additionalAttributes(name = "foo")
    aggregate [String]{ s => s }.on { s: Any => s }.sendPartialResultOnExpiry.expireGroupsOnCompletion
//    aggregate [String]{ s => s }.on { s: Any => s }.sendPartialResultOnExpiry.expireGroupsOnCompletion.additionalAttributes(name = "foo")
//    aggregate [String]{ s => s }.on { s: Any => s }.additionalAttributes(name = "foo")
    aggregate [String]{ s => s }.on { s: Any => s }.expireGroupsOnCompletion
//    aggregate [String]{ s => s }.on { s: Any => s }.expireGroupsOnCompletion.additionalAttributes(name = "foo")
    aggregate [String]{ s => s }.on { s: Any => s }.expireGroupsOnCompletion.sendPartialResultOnExpiry
//    aggregate [String]{ s => s }.on { s: Any => s }.expireGroupsOnCompletion.sendPartialResultOnExpiry.additionalAttributes(name = "foo")

    // AGGREGATE ON/UNTIL

    aggregate [String]{ s => s }.on { s: Any => s }.until[String] { messages => messages.size == 3 }
    aggregate [String]{ s => s }.on { s: Any => s }.until[String] { messages => messages.size == 3 }.expireGroupsOnCompletion
    aggregate [String]{ s => s }.on { s: Any => s }.until[String] { messages => messages.size == 3 }.expireGroupsOnCompletion.sendPartialResultOnExpiry
//    aggregate [String]{ s => s }.on { s: Any => s }.until[String] { messages => messages.size == 3 }.expireGroupsOnCompletion.sendPartialResultOnExpiry.additionalAttributes(name = "foo")
//    aggregate [String]{ s => s }.on { s: Any => s }.until[String] { messages => messages.size == 3 }.additionalAttributes(name = "foo")
    aggregate [String]{ s => s }.on { s: Any => s }.until[String] { messages => messages.size == 3 }.sendPartialResultOnExpiry
//    aggregate [String]{ s => s }.on { s: Any => s }.until[String] { messages => messages.size == 3 }.sendPartialResultOnExpiry.additionalAttributes(name = "foo")
    aggregate [String]{ s => s }.on { s: Any => s }.until[String] { messages => messages.size == 3 }.sendPartialResultOnExpiry.expireGroupsOnCompletion
//    aggregate [String]{ s => s }.on { s: Any => s }.until[String] { messages => messages.size == 3 }.sendPartialResultOnExpiry.expireGroupsOnCompletion.additionalAttributes(name = "foo")
//    aggregate [String]{ s => s }.on { s: Any => s }.until[String] { messages => messages.size == 3 }.expireGroupsOnCompletion.additionalAttributes(name = "foo")

    // AGGRGATE with ON/UNTIL
    aggregate { s:Iterable[String] => s }.on { s: Any => s }.until[String] { messages => messages.size == 3 }
//    aggregate { s:Iterable[String] => s }.on { s: Any => s }.until[String] { messages => messages.size == 3 }.additionalAttributes(name = "foo")
    aggregate { s:Iterable[String] => s }.on { s: Any => s }.until[String] { messages => messages.size == 3 }.expireGroupsOnCompletion
//    aggregate { s:Iterable[String] => s }.on { s: Any => s }.until[String] { messages => messages.size == 3 }.
//      expireGroupsOnCompletion.
//      keepReleasedMessages.
//      additionalAttributes(name = "foo")

  }

}