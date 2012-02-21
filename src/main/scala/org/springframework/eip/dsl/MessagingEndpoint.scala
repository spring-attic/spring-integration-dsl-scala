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
import java.util.UUID


/**
 * @author Oleg Zhurakousky
 */

/**
 * SERVICE ACTIVATOR
 */
object handle {

  def using(function:Function1[_,_]) = new IntegrationComposition(null, new ServiceActivator(target = function)) {
    
    def where(name:String)= new IntegrationComposition(null, new ServiceActivator(name = name, target = function))
  }

  def using(spelExpression:String) = new IntegrationComposition(null, new ServiceActivator(target = spelExpression))  {
    def where(name:String)= new IntegrationComposition(null, new ServiceActivator(name = name, target = spelExpression))
  }
}



/**
 * TRANSFORMER
 */
object transform {

  def using(function:Function1[_,AnyRef]) = new IntegrationComposition(null, new Transformer(target = function)) {
    def where(name:String)= new IntegrationComposition(null, new Transformer(name = name, target = function))
  }

  def using(spelExpression:String) = new IntegrationComposition(null, new Transformer(target = spelExpression)) {
    def where(name:String)= new IntegrationComposition(null, new Transformer(name = name, target = spelExpression))
  }
}

/**
 * FILTER
 */
object filter {

  def using(function:Function1[_,Boolean]) = new IntegrationComposition(null, new MessageFilter(target = function)) {
    def where(name:String = "$flt_" + UUID.randomUUID().toString.substring(0, 8), exceptionOnRejection:Boolean = false) =
      new IntegrationComposition(null, new MessageFilter(name = name, target = function, exceptionOnRejection = exceptionOnRejection))
  }

  def using(spelExpression:String) = new IntegrationComposition(null, new MessageFilter(target = spelExpression))  {
    def where(name:String = "$flt_" + UUID.randomUUID().toString.substring(0, 8), exceptionOnRejection:Boolean = false) =
      new IntegrationComposition(null, new MessageFilter(name = name, target = spelExpression, exceptionOnRejection = exceptionOnRejection))
  }
}


///**
// * ENRICHER (payload, header)
// */
//object enrich {
//  
//  def header = new {
//    def using(headerMap:(Tuple2[String, _])*)=
//      new IntegrationComposition(null, new Enricher(null, headerMap))
//    
//    def using(function:Function1[_,AnyRef])=
//      new IntegrationComposition(null, new Enricher(null, function))
//  }
//  
//  def payload = new {
//    def using(function:Function1[_,AnyRef])=
//      new IntegrationComposition(null, new Enricher(null, function))
//  }
//}

/**
 * SPLITTER
 */
object split {

  def using(function:Function1[_,Iterable[Any]]) = new IntegrationComposition(null, new MessageSplitter(target=function)) {
    def where(name:String = "$splt_" + UUID.randomUUID().toString.substring(0, 8), applySequence:Boolean = true) = 
      new IntegrationComposition(null, new MessageSplitter(name = name, target = function, applySequence = applySequence))
  }

  def using(spelExpression:String) = new IntegrationComposition(null, new MessageSplitter(null, target = spelExpression))  {
    def where(name:String = "$splt_" + UUID.randomUUID().toString.substring(0, 8), applySequence:Boolean = true) = 
      new IntegrationComposition(null, new MessageSplitter(name = name, target = spelExpression, applySequence = applySequence))
  }
}

/**
* AGGREGATOR
*/
object aggregate {
  /**
   * 
   */
  def apply() = new IntegrationComposition(null, new MessageAggregator()) {
    def where(name:String,
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
  /**
   * 
   */
  def on(correlationFunction:Function1[_,AnyRef]) = new IntegrationComposition(null, new MessageAggregator())  {
    def where(name:String,
              keepReleasedMessages:Boolean = false,
              messageStore:MessageStore = new SimpleMessageStore,
              sendPartialResultsOnExpiry:Boolean = true,
              expireGroupsUponCompletion:Boolean = false) =
      new IntegrationComposition(null, new MessageAggregator(name = name,
                                                        keepReleasedMessages = keepReleasedMessages,
                                                        messageStore = messageStore,
                                                        sendPartialResultsOnExpiry = sendPartialResultsOnExpiry,
                                                        expireGroupsUponCompletion = expireGroupsUponCompletion))

    def until(releaseFunction:Function1[_,Boolean]) = new IntegrationComposition(null, new MessageAggregator())  {
      def where(name:String,
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

    def until(releaseExpression:String) = new IntegrationComposition(null, new MessageAggregator())  {
      def where(name:String ,
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
  /**
   * 
   */
  def on(correlationKey:AnyRef) = new IntegrationComposition(null, new MessageAggregator())  {
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

    def until(releaseFunction:Function1[_,Boolean]) = new IntegrationComposition(null, new MessageAggregator())  {
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

    def until(releaseExpression:String) = new IntegrationComposition(null, new MessageAggregator())  {
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
  /**
   *  
   */
  def until(releaseFunction:Function1[_,Boolean]) = new IntegrationComposition(null, new MessageAggregator()) {
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
    
    def on(correlationKey:AnyRef) = new IntegrationComposition(null, new MessageAggregator())  {
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

  def until(releaseExpression:String) = new IntegrationComposition(null, new MessageAggregator())  {
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
    
    def on(correlationKey:AnyRef) = new IntegrationComposition(null, new MessageAggregator())  {
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
}

private[dsl] class ServiceActivator(name:String = "$sa_" + UUID.randomUUID().toString.substring(0, 8), target:Any)
            extends SimpleEndpoint(name, target)

private[dsl] class MessagingBridge(name:String = "$br_" + UUID.randomUUID().toString.substring(0, 8))
            extends SimpleEndpoint(name, null)

private[dsl] class Enricher(name:String = "$enr_" + UUID.randomUUID().toString.substring(0, 8), target:Any)
            extends SimpleEndpoint(name, null)

private[dsl] class Transformer(name:String = "$xfmr_" + UUID.randomUUID().toString.substring(0, 8), target:Any)
            extends SimpleEndpoint(name, target)

private[dsl] class MessageFilter(name:String = "$flt_" + UUID.randomUUID().toString.substring(0, 8), target:Any, val exceptionOnRejection:Boolean = false)
            extends SimpleEndpoint(name, target)

private[dsl] class MessageSplitter(name:String = "$splt_" + UUID.randomUUID().toString.substring(0, 8), target:Any, val applySequence:Boolean = false)
            extends SimpleEndpoint(name, target)

private[dsl] case class MessageAggregator(override val name:String = "$ag_" + UUID.randomUUID().toString.substring(0, 8),
                                          val keepReleasedMessages:Boolean = true,
                                          val messageStore:MessageStore = new SimpleMessageStore,
                                          val sendPartialResultsOnExpiry:Boolean = false,
                                          val expireGroupsUponCompletion:Boolean = false) extends IntegrationComponent(name)

private[dsl] abstract class SimpleEndpoint(name:String, val target:Any = null) extends IntegrationComponent(name)

