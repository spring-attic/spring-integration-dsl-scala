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

import org.springframework.integration.store.{ SimpleMessageStore, MessageStore }
import java.util.UUID
import org.springframework.beans.factory.support.BeanDefinitionBuilder
import org.springframework.integration.aggregator.DefaultAggregatingMessageGroupProcessor
import org.springframework.integration.aggregator.AggregatingMessageHandler
import org.springframework.util.StringUtils
import org.w3c.dom.Document
import org.w3c.dom.Element

/**
 * This class provides DSL and related components to support "Message Aggregator" pattern
 *
 * @author Oleg Zhurakousky
 */
object aggregate {

  trait RestrictiveFunction[A, B]

  type NotUnitType[T] = RestrictiveFunction[T, Unit]

  implicit def nsub[A, B]: RestrictiveFunction[A, B] = null
  implicit def nsubAmbig1[A, B >: A]: RestrictiveFunction[A, B] = null
  implicit def nsubAmbig2[A, B >: A]: RestrictiveFunction[A, B] = null
  /**
   *
   */
  def apply() = new SendingEndpointComposition(null, new Aggregator()) with On with Until with ExpireGroupsOnCompletion with AggregatorAttributes with SendPartialResultOnExpiry with KeepReleasedMessages{

  }

  def apply[T, R: NotUnitType](aggregationFunction: Function1[Iterable[_], R]) = new SendingEndpointComposition(null, new Aggregator()) with On with Until with ExpireGroupsOnCompletion with AggregatorAttributes with SendPartialResultOnExpiry with KeepReleasedMessages{

  }

  /**
   *
   */
  def on[T, R: NotUnitType](correlationFunction: Function1[_, R]) = new SendingEndpointComposition(null, new Aggregator()) with ExpireGroupsOnCompletion with AggregatorAttributes with SendPartialResultOnExpiry with KeepReleasedMessages{


    def until(releaseFunction: Function1[_, Boolean]) = new SendingEndpointComposition(null, new Aggregator()) with ExpireGroupsOnCompletion with AggregatorAttributes with SendPartialResultOnExpiry with KeepReleasedMessages{

    }

  }
  /**
   *
   */
  def until(releaseFunction: Function1[_, Boolean]) = new SendingEndpointComposition(null, new Aggregator()) with ExpireGroupsOnCompletion with AggregatorAttributes with SendPartialResultOnExpiry with KeepReleasedMessages{

  }

  def additionalAttributes(name: String = "$aggr_" + UUID.randomUUID().toString.substring(0, 8),
    keepReleasedMessages: Boolean = true,
    messageStore: MessageStore = new SimpleMessageStore,
    sendPartialResultsOnExpiry: Boolean = false,
    expireGroupsUponCompletion: Boolean = false) =
    new SendingEndpointComposition(null, new Aggregator(name = name,
      keepReleasedMessages = keepReleasedMessages,
      messageStore = messageStore,
      sendPartialResultsOnExpiry = sendPartialResultsOnExpiry,
      expireGroupsUponCompletion = expireGroupsUponCompletion))
}

private[dsl] class Aggregator(name: String = "$ag_" + UUID.randomUUID().toString.substring(0, 8),
  val keepReleasedMessages: Boolean = true,
  val messageStore: MessageStore = new SimpleMessageStore,
  val sendPartialResultsOnExpiry: Boolean = false,
  val expireGroupsUponCompletion: Boolean = false) extends SimpleEndpoint(name, null) {

  override def toMapOfProperties:Map[String, _] =
    super.toMapOfProperties + ("eipName" -> "AGGREGATOR", "keepReleasedMessages" -> keepReleasedMessages,
        "messageStore" -> messageStore, "sendPartialResultsOnExpiry" -> sendPartialResultsOnExpiry,
        "expireGroupsUponCompletion" -> expireGroupsUponCompletion)

  override def build(document: Document,
    targetDefinitionFunction: Function1[Any, Tuple2[String, String]],
    compositionInitFunction: Function2[BaseIntegrationComposition, AbstractChannel, Unit],
    inputChannel:AbstractChannel,
    outputChannel:AbstractChannel): Element = {

    require(inputChannel != null, "'inputChannel' must be provided")

    val element = document.createElement("int:aggregator")
    element.setAttribute("id", this.name)
    element.setAttribute("input-channel", inputChannel.name);
    if (outputChannel != null){
      element.setAttribute("output-channel", outputChannel.name);
    }
    element
  }
}

private[dsl] trait On {

  import aggregate._

  def on[T, R: NotUnitType](correlationFunction: Function1[_, R]) = new SendingEndpointComposition(null, new Aggregator()) with ExpireGroupsOnCompletion with AggregatorAttributes with SendPartialResultOnExpiry with KeepReleasedMessages{


    def until(releaseFunction: Function1[_, Boolean]) = new SendingEndpointComposition(null, new Aggregator()) with ExpireGroupsOnCompletion with AggregatorAttributes with SendPartialResultOnExpiry with KeepReleasedMessages{

    }

  }
}

private[dsl] trait Until {
  /**
   *
   */
  def until(releaseFunction: Function1[_, Boolean]) = new SendingEndpointComposition(null, new Aggregator()) with ExpireGroupsOnCompletion with SendPartialResultOnExpiry  with AggregatorAttributes {
    //throw new UnsupportedOperationException("Currently this DSL element is not supported. Support will be added in version 1.0.0.M2")
  }
}

private[dsl] trait ExpireGroupsOnCompletion {
  def expireGroupsOnCompletion = new SendingEndpointComposition(null, new Aggregator()) with AggregatorAttributes {
    def sendPartialResultOnExpiry = new SendingEndpointComposition(null, new Aggregator()) with AggregatorAttributes {
      def keepReleasedMessages = new SendingEndpointComposition(null, new Aggregator()) with AggregatorAttributes
    }

    def keepReleasedMessages = new SendingEndpointComposition(null, new Aggregator()) with AggregatorAttributes {
      def sendPartialResultOnExpiry = new SendingEndpointComposition(null, new Aggregator()) with AggregatorAttributes
    }
  }
}

private[dsl] trait SendPartialResultOnExpiry {
  def sendPartialResultOnExpiry = new SendingEndpointComposition(null, new Aggregator()) with AggregatorAttributes {
    def expireGroupsOnCompletion = new SendingEndpointComposition(null, new Aggregator()) with AggregatorAttributes {
      def keepReleasedMessages = new SendingEndpointComposition(null, new Aggregator()) with AggregatorAttributes
    }

    def keepReleasedMessages = new SendingEndpointComposition(null, new Aggregator()) with AggregatorAttributes {
      def expireGroupsOnCompletion = new SendingEndpointComposition(null, new Aggregator()) with AggregatorAttributes
    }
  }
}

private[dsl] trait KeepReleasedMessages {
  def keepReleasedMessages = new SendingEndpointComposition(null, new Aggregator()) with AggregatorAttributes {
    def expireGroupsOnCompletion = new SendingEndpointComposition(null, new Aggregator()) with AggregatorAttributes {
      def sendPartialResultOnExpiry = new SendingEndpointComposition(null, new Aggregator()) with AggregatorAttributes
    }

    def sendPartialResultOnExpiry = new SendingEndpointComposition(null, new Aggregator()) with AggregatorAttributes {
      def expireGroupsOnCompletion = new SendingEndpointComposition(null, new Aggregator()) with AggregatorAttributes
    }
  }
}

private[dsl] trait AggregatorAttributes {
  def additionalAttributes(name: String = "$aggr_" + UUID.randomUUID().toString.substring(0, 8),
      keepReleasedMessages: Boolean = false,
      messageStore: MessageStore = new SimpleMessageStore,
      sendPartialResultsOnExpiry: Boolean = true,
      expireGroupsUponCompletion: Boolean = false) =
      new SendingEndpointComposition(null, new Aggregator(name = name,
        keepReleasedMessages = keepReleasedMessages,
        messageStore = messageStore,
        sendPartialResultsOnExpiry = sendPartialResultsOnExpiry,
        expireGroupsUponCompletion = expireGroupsUponCompletion))
}


