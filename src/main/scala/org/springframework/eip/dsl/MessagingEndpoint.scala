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

import org.springframework.integration.store.{SimpleMessageStore, MessageStore}


/**
 * @author Oleg Zhurakousky
 */

/**
 * SERVICE ACTIVATOR
 */
object handle {

  def using(function:Function1[_,_]) = new IntegrationComposition(null, new ServiceActivator(null, function)) {
    
    def where(name:String)= new IntegrationComposition(null, new ServiceActivator(name, function))
  }

  def using(spelExpression:String) = new IntegrationComposition(null, new ServiceActivator(null, spelExpression))  {
    def where(name:String)= new IntegrationComposition(null, new ServiceActivator(name, spelExpression))
  }
}



/**
 * TRANSFORMER
 */
object transform {

  def using(function:Function1[_,AnyRef]) = new IntegrationComposition(null, new Transformer(null, function)) {
    def where(name:String)= new IntegrationComposition(null, new Transformer(name, function))
  }

  def using(spelExpression:String) = new IntegrationComposition(null, new Transformer(null, spelExpression)) {
    def where(name:String)= new IntegrationComposition(null, new Transformer(name, spelExpression))
  }
}

/**
 * FILTER
 */
object filter {

  def using(function:Function1[_,Boolean]) = new IntegrationComposition(null, new MessageFilter(null, function)) {
    def where(name:String = null, exceptionOnRejection:Boolean = false)=
      new IntegrationComposition(null, new MessageFilter(name, function, exceptionOnRejection))
  }

  def using(spelExpression:String) = new IntegrationComposition(null, new MessageFilter(null, spelExpression))  {
    def where(name:String = null, exceptionOnRejection:Boolean = false) =
      new IntegrationComposition(null, new MessageFilter(name, spelExpression, exceptionOnRejection))
  }
}

/**
 * SPLITTER
 */
object split {

  def using(function:Function1[_,Iterable[Any]]) = new IntegrationComposition(null, new MessageSplitter(null, target=function)) {
    def where(name:String = null, applySequence:Boolean = true)= new IntegrationComposition(null, new MessageSplitter(name, function, applySequence))
  }

  def using(spelExpression:String) = new IntegrationComposition(null, new MessageSplitter(null, target = spelExpression))  {
    def where(name:String= null, applySequence:Boolean = true)= new IntegrationComposition(null, new MessageSplitter(name, spelExpression, applySequence))
  }
}

/**
* AGGREGATOR
*/
object aggregate {

  def apply() = new IntegrationComposition(null, new MessageAggregator()) {
    def where(name:String = null,
              keepReleasedMessages:Boolean = false,
              messageStore:MessageStore = new SimpleMessageStore,
              sendPartialResultsOnExpiry:Boolean = true,
              expireGroupsUponCompletion:Boolean = false) =
      new IntegrationComposition(null, new MessageAggregator(name = name,
                                                        keepReleasedMessages = keepReleasedMessages,
                                                        messageStore = messageStore,
                                                        sendPartialResultsOnExpiry = sendPartialResultsOnExpiry,
                                                        expireGroupsUponCompletion = expireGroupsUponCompletion))
  }

  def on(correlationFunction:Function1[_,AnyRef]) = new IntegrationComposition(null, new MessageAggregator(null))  {
    def where(name:String = null,
              keepReleasedMessages:Boolean = false,
              messageStore:MessageStore = new SimpleMessageStore,
              sendPartialResultsOnExpiry:Boolean = true,
              expireGroupsUponCompletion:Boolean = false) =
      new IntegrationComposition(null, new MessageAggregator(name = name,
                                                        keepReleasedMessages = keepReleasedMessages,
                                                        messageStore = messageStore,
                                                        sendPartialResultsOnExpiry = sendPartialResultsOnExpiry,
                                                        expireGroupsUponCompletion = expireGroupsUponCompletion))

    def until(releaseFunction:Function1[_,Boolean]) = new IntegrationComposition(null, new MessageAggregator(null))  {
      def where(name:String = null,
              keepReleasedMessages:Boolean = false,
              messageStore:MessageStore = new SimpleMessageStore,
              sendPartialResultsOnExpiry:Boolean = true,
              expireGroupsUponCompletion:Boolean = false) =
        new IntegrationComposition(null, new MessageAggregator(name = name,
                                                          keepReleasedMessages = keepReleasedMessages,
                                                          messageStore = messageStore,
                                                          sendPartialResultsOnExpiry = sendPartialResultsOnExpiry,
                                                          expireGroupsUponCompletion = expireGroupsUponCompletion))
    }

    def until(releaseExpression:String) = new IntegrationComposition(null, new MessageAggregator(null))  {
      def where(name:String = null,
              keepReleasedMessages:Boolean = false,
              messageStore:MessageStore = new SimpleMessageStore,
              sendPartialResultsOnExpiry:Boolean = true,
              expireGroupsUponCompletion:Boolean = false) =
        new IntegrationComposition(null, new MessageAggregator(name = name,
                                                          keepReleasedMessages = keepReleasedMessages,
                                                          messageStore = messageStore,
                                                          sendPartialResultsOnExpiry = sendPartialResultsOnExpiry,
                                                          expireGroupsUponCompletion = expireGroupsUponCompletion))
    }
  }

  def on(correlationKey:AnyRef) = new IntegrationComposition(null, new MessageAggregator(null))  {
    def where(name:String = null,
              keepReleasedMessages:Boolean = false,
              messageStore:MessageStore = new SimpleMessageStore,
              sendPartialResultsOnExpiry:Boolean = true,
              expireGroupsUponCompletion:Boolean = false) =
      new IntegrationComposition(null, new MessageAggregator(name = name,
                                                        keepReleasedMessages = keepReleasedMessages,
                                                        messageStore = messageStore,
                                                        sendPartialResultsOnExpiry = sendPartialResultsOnExpiry,
                                                        expireGroupsUponCompletion = expireGroupsUponCompletion))

    def until(releaseFunction:Function1[_,Boolean]) = new IntegrationComposition(null, new MessageAggregator(null))  {
      def where(name:String = null,
              keepReleasedMessages:Boolean = false,
              messageStore:MessageStore = new SimpleMessageStore,
              sendPartialResultsOnExpiry:Boolean = true,
              expireGroupsUponCompletion:Boolean = false) =
        new IntegrationComposition(null, new MessageAggregator(name = name,
                                                          keepReleasedMessages = keepReleasedMessages,
                                                          messageStore = messageStore,
                                                          sendPartialResultsOnExpiry = sendPartialResultsOnExpiry,
                                                           expireGroupsUponCompletion = expireGroupsUponCompletion))
    }

    def until(releaseExpression:String) = new IntegrationComposition(null, new MessageAggregator(null))  {
      def where(name:String = null,
              keepReleasedMessages:Boolean = false,
              messageStore:MessageStore = new SimpleMessageStore,
              sendPartialResultsOnExpiry:Boolean = true,
              expireGroupsUponCompletion:Boolean = false) =
        new IntegrationComposition(null, new MessageAggregator(name = name,
                                                          keepReleasedMessages = keepReleasedMessages,
                                                          messageStore = messageStore,
                                                          sendPartialResultsOnExpiry = sendPartialResultsOnExpiry,
                                                          expireGroupsUponCompletion = expireGroupsUponCompletion))
    }
  }

  def until(releaseFunction:Function1[_,Boolean]) = new IntegrationComposition(null, new MessageAggregator(null)) {
    def where(name:String = null,
              keepReleasedMessages:Boolean = false,
              messageStore:MessageStore = new SimpleMessageStore,
              sendPartialResultsOnExpiry:Boolean = true,
              expireGroupsUponCompletion:Boolean = false) =
      new IntegrationComposition(null, new MessageAggregator(name = name,
                                                        keepReleasedMessages = keepReleasedMessages,
                                                        messageStore = messageStore,
                                                        sendPartialResultsOnExpiry = sendPartialResultsOnExpiry,
                                                        expireGroupsUponCompletion = expireGroupsUponCompletion))
  }

  def until(releaseExpression:String) = new IntegrationComposition(null, new MessageAggregator(null))  {
    def where(name:String,
              keepReleasedMessages:Boolean,
              messageStore:MessageStore,
              sendPartialResultsOnExpiry:Boolean,
              expireGroupsUponCompletion:Boolean) =
      new IntegrationComposition(null, new MessageAggregator(name = name,
                                                        keepReleasedMessages = keepReleasedMessages,
                                                        messageStore = messageStore,
                                                        sendPartialResultsOnExpiry = sendPartialResultsOnExpiry,
                                                        expireGroupsUponCompletion = expireGroupsUponCompletion))

  }

  def where(name:String = null,
            keepReleasedMessages:Boolean = true,
            messageStore:MessageStore = new SimpleMessageStore,
            sendPartialResultsOnExpiry:Boolean = false,
            expireGroupsUponCompletion:Boolean = false) =
    new IntegrationComposition(null, new MessageAggregator(name = name,
                                                      keepReleasedMessages = keepReleasedMessages,
                                                      messageStore = messageStore,
                                                      sendPartialResultsOnExpiry = sendPartialResultsOnExpiry,
                                                      expireGroupsUponCompletion = expireGroupsUponCompletion))

//  private[aggregate] trait Where {
//    def where(name:String = null,
//              keepReleasedMessages:Boolean = true,
//              messageStore:MessageStore = new SimpleMessageStore,
//              sendPartialResultsOnExpiry:Boolean = false,
//              expireGroupsUponCompletion:Boolean = false): IntegrationComposition
//  }

//  private[aggregate] trait ReleaseStrategy {
//    def until(releaseFunction:Function1[_,Boolean]): IntegrationComposition
//
//    def until(releaseExpression:String): IntegrationComposition
//  }
//
//  private[aggregate] trait CorrelationStrategy {
//    def on(correlationKey:AnyRef): SimpleComposition
//
//    def on(correlationFunction:Function1[_,AnyRef]): IntegrationComposition
//  }
}

private[dsl] case class ServiceActivator(override val name:String, override val target:Any)
            extends SimpleEndpoint(name, target)

private[dsl] case class Transformer( override val name:String, override val target:Any)
            extends SimpleEndpoint(name, target)

private[dsl] case class MessageFilter(override val name:String, override val target:Any, exceptionOnRejection:Boolean = false)
            extends SimpleEndpoint(name, target)

private[dsl] case class MessageSplitter(override val name:String, override val target:Any, val applySequence:Boolean = false)
            extends SimpleEndpoint(name, target)

private[dsl] case class MessageAggregator(override val name:String = null,
                                          val keepReleasedMessages:Boolean = true,
                                          val messageStore:MessageStore = new SimpleMessageStore,
                                          val sendPartialResultsOnExpiry:Boolean = false,
                                          val expireGroupsUponCompletion:Boolean = false)
            extends Endpoint(name)

private[dsl] abstract class Endpoint(val name:String = null)

private[dsl] abstract class SimpleEndpoint(override val name:String = null, val target:Any) extends Endpoint(name)

