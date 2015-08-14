/*
 * Copyright 2002-2013 the original author or authors.
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
import org.springframework.integration.dsl.utils.DslUtils
import org.springframework.integration.dsl.utils.Conventions
import java.util.Collection
import scala.collection.JavaConversions
import org.springframework.messaging

/**
 * This class provides DSL and related components to support "Message Aggregator" pattern
 *
 * @author Oleg Zhurakousky
 * @author Soby Chacko
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
  def apply() = doApply(new Aggregator)
  /**
   *
   */
  def apply[I: Manifest](aggregationFunction: Function1[Iterable[I], Iterable[_]]) = doApply(new Aggregator(aggregationFunction = new SingleMessageScalaFunctionWrapper(aggregationFunction)))

  /**
   *
   */
  def on[T, R: NotUnitType](correlationFunction: Function1[T, R]) = doOnTrait(new Aggregator, correlationFunction)
  /**
   *
   */
  def until[T: Manifest](releaseFunction: Function1[Iterable[T], Boolean]) = doUntilTrait(new Aggregator, releaseFunction)

  def sendPartialResultOnExpiry = doSendPartialResultsOnExpiry(new Aggregator)

  def expireGroupsUponCompletion = doExpireGroupsUponCompletion(new Aggregator)

  def additionalAttributes[T <: BaseIntegrationComposition](name: String = "$aggr_" + UUID.randomUUID().toString.substring(0, 8),
      					   messageStore: MessageStore = null,
      					   discardFlow: T = null) = doAdditionalAttributes(new Aggregator, name, messageStore, discardFlow)

  //===================

  private def doAdditionalAttributes[T <: BaseIntegrationComposition](currentAggregator: Aggregator,
		  name: String = null,
		  messageStore: MessageStore = null,
		  discardFlow: T = null) = {
    val enrichedAggregator = currentAggregator.copy(name = name, additionalAttributes = Map("messageStore" -> messageStore, "discardChannel" -> discardFlow))
    new SendingEndpointComposition(null, enrichedAggregator)
  }

  private def doOnTrait[T: Manifest, R: NotUnitType](currentAggregator: Aggregator, correlationFunction: Function1[_, R]) = {
    require(currentAggregator != null, "Trait 'On' can only be applied on composition with existing Aggregator instance")
    val enrichedAggregator = currentAggregator.copy(correlationFunction = correlationFunction)
    new SendingEndpointComposition(null, enrichedAggregator) {
      def until[T: Manifest](releaseFunction: Function1[Iterable[T], Boolean]) = doUntilTrait(enrichedAggregator, releaseFunction)

      def sendPartialResultOnExpiry = doSendPartialResultsOnExpiry(enrichedAggregator)

      def expireGroupsUponCompletion = doExpireGroupsUponCompletion(enrichedAggregator)

      def additionalAttributes[T <: BaseIntegrationComposition](name: String = "$aggr_" + UUID.randomUUID().toString.substring(0, 8),
                               messageStore: MessageStore = null,
                               discardFlow: T = null) = doAdditionalAttributes(enrichedAggregator, name, messageStore, discardFlow)
    }
  }

  private def doUntilTrait[T: Manifest](currentAggregator: Aggregator, releaseFunction: Function1[Iterable[T], Boolean]) = {
    require(currentAggregator != null, "Trait 'Until' can only be applied on composition with existing Aggregator instance")
    val enrichedAggregator = currentAggregator.copy(releaseFunction = new ReleaseFunctionWrapper(releaseFunction))
    new SendingEndpointComposition(null, enrichedAggregator) {

      def sendPartialResultOnExpiry = doSendPartialResultsOnExpiry(enrichedAggregator)

      def expireGroupsUponCompletion = doExpireGroupsUponCompletion(enrichedAggregator)

      def additionalAttributes[T <: BaseIntegrationComposition](name: String = "$aggr_" + UUID.randomUUID().toString.substring(0, 8),
                               messageStore: MessageStore = null,
                               discardFlow: T = null) = doAdditionalAttributes(enrichedAggregator, name, messageStore, discardFlow)
    }
  }

  private def doExpireGroupsUponCompletion(currentAggregator: Aggregator) = {
    val enrichedAggregator = currentAggregator.copy(expireGroupsUponCompletion = true)
    new SendingEndpointComposition(null, enrichedAggregator) {

      def sendPartialResultOnExpiry = {
        val enrichedAggregatorA = enrichedAggregator.copy(sendPartialResultOnExpiry = true)
        new SendingEndpointComposition(null, enrichedAggregatorA) {

          def additionalAttributes[T <: BaseIntegrationComposition](name: String = "$aggr_" + UUID.randomUUID().toString.substring(0, 8),
                                   messageStore: MessageStore = null,
                                   discardFlow: T = null) = doAdditionalAttributes(enrichedAggregatorA, name, messageStore, discardFlow)

        }
      }

      def additionalAttributes[T <: BaseIntegrationComposition](name: String = "$aggr_" + UUID.randomUUID().toString.substring(0, 8),
                               messageStore: MessageStore = null,
                               discardFlow: T = null) = doAdditionalAttributes(enrichedAggregator, name, messageStore, discardFlow)

    }
  }

  private def doSendPartialResultsOnExpiry(currentAggregator: Aggregator) = {
    val enrichedAggregator = currentAggregator.copy(sendPartialResultOnExpiry = true)
    new SendingEndpointComposition(null, enrichedAggregator) {

      def additionalAttributes[T <: BaseIntegrationComposition](name: String = "$aggr_" + UUID.randomUUID().toString.substring(0, 8),
                               messageStore: MessageStore = null,
                               discardFlow: T = null) = doAdditionalAttributes(enrichedAggregator, name, messageStore, discardFlow)

      def expireGroupsUponCompletion = {
        val enrichedAggregatorA = enrichedAggregator.copy(expireGroupsUponCompletion = true)
        new SendingEndpointComposition(null, enrichedAggregatorA) {

          def additionalAttributes[T <: BaseIntegrationComposition](name: String = "$aggr_" + UUID.randomUUID().toString.substring(0, 8),
                                   messageStore: MessageStore = null,
                                   discardFlow: T = null) = doAdditionalAttributes(enrichedAggregatorA, name, messageStore, discardFlow)
        }
      }

    }
  }

  private def doApply(currentAggregator: Aggregator) = {
    new SendingEndpointComposition(null, currentAggregator) {

      def on[T, R: NotUnitType](correlationFunction: Function1[_, R]) = doOnTrait(currentAggregator, correlationFunction)

      def until[T: Manifest](releaseFunction: Function1[Iterable[T], Boolean]) = doUntilTrait(currentAggregator, releaseFunction)

      def sendPartialResultOnExpiry = doSendPartialResultsOnExpiry(currentAggregator)

      def expireGroupsUponCompletion = doExpireGroupsUponCompletion(currentAggregator)

      def additionalAttributes[T <: BaseIntegrationComposition](name: String = "$aggr_" + UUID.randomUUID().toString.substring(0, 8),
                               messageStore: MessageStore = null,
                               discardFlow: T = null) = doAdditionalAttributes(currentAggregator, name, messageStore, discardFlow)
    }
  }
}

private[dsl] case class Aggregator(override val name: String = "$aggr_" + UUID.randomUUID().toString.substring(0, 8),
  val sendPartialResultOnExpiry: java.lang.Boolean = null,
  val expireGroupsUponCompletion: java.lang.Boolean = null,
  val aggregationFunction: SingleMessageScalaFunctionWrapper[_, _] = null,
  val correlationFunction: Function1[_, _] = null,
  val releaseFunction: ReleaseFunctionWrapper[_] = null,
  val additionalAttributes: Map[String, _] = null) extends SimpleEndpoint(name, null) {

  override def build(document: Document,
    targetDefinitionFunction: Function1[Any, Tuple2[String, String]],
    compositionInitFunction: Function2[BaseIntegrationComposition, AbstractChannel, Unit],
    inputChannel: AbstractChannel,
    outputChannel: AbstractChannel): Element = {

    require(inputChannel != null, "'inputChannel' must be provided")

    val element = document.createElement("int:aggregator")
    element.setAttribute("id", this.name)
    element.setAttribute("input-channel", inputChannel.name);
    if (outputChannel != null) {
      element.setAttribute("output-channel", outputChannel.name);
    }

    if (sendPartialResultOnExpiry != null)
      element.setAttribute(Conventions.propertyNameToAttributeName("sendPartialResultOnExpiry"), sendPartialResultOnExpiry.toString())

    if (expireGroupsUponCompletion != null)
      element.setAttribute(Conventions.propertyNameToAttributeName("expireGroupsUponCompletion"), expireGroupsUponCompletion.toString())

    if (aggregationFunction != null) {
      val aggregationFunctionDefinition = targetDefinitionFunction(aggregationFunction)
      element.setAttribute("expression", "@" + aggregationFunctionDefinition._1 + "." +
        aggregationFunctionDefinition._2 + "(#this)")
    }

    if (releaseFunction != null) {
      val releaseFunctionDefinition = targetDefinitionFunction(releaseFunction)
      element.setAttribute("release-strategy-expression", "@" + releaseFunctionDefinition._1 + "." +
        releaseFunctionDefinition._2 + "(#this)")
    }

    if (correlationFunction != null) {
      val correlationFunctionDefinition = targetDefinitionFunction(correlationFunction)

      //      element.setAttribute("correlation-strategy-expression", "@" + correlationFunctionDefinition._1 + "." +
      //        correlationFunctionDefinition._2 + "(#this)")

      element.setAttribute("correlation-strategy", correlationFunctionDefinition._1)
      element.setAttribute("correlation-strategy-method", correlationFunctionDefinition._2)
    }

    if (additionalAttributes != null) {
      DslUtils.setAdditionalAttributes(element, additionalAttributes, compositionInitFunction)
    }
    element
  }
}

private class ReleaseFunctionWrapper[T](val releaseFunction: Function1[Iterable[T], Boolean]) extends Function1[java.util.Collection[T], Boolean] {

  def apply(messages: java.util.Collection[T]): Boolean = {
    releaseFunction(JavaConversions.collectionAsScalaIterable[T](messages))
  }
}
