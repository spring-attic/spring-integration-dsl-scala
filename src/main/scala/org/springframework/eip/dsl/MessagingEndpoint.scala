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

  def using(function:Function1[_,_]) = new SimpleComposition(null, new ServiceActivator(null, function)) with Where {
    def where(name:String)= new SimpleComposition(null, new ServiceActivator(name, function))
  }

  def using(spelExpression:String) = new SimpleComposition(null, new ServiceActivator(null, spelExpression)) with Where {
    def where(name:String)= new SimpleComposition(null, new ServiceActivator(name, spelExpression))
  }

  private[handle] trait Where {
    def where(name:String): SimpleComposition
  }
}

/**
 * TRANSFORMER
 */
object transform {

  def using(function:Function1[_,AnyRef]) = new SimpleComposition(null, new Transformer(null, function)) with Where {
    def where(name:String)= new SimpleComposition(null, new Transformer(name, function))
  }

  def using(spelExpression:String) = new SimpleComposition(null, new Transformer(null, spelExpression)) with Where {
    def where(name:String)= new SimpleComposition(null, new Transformer(name, spelExpression))
  }

  private[transform] trait Where {
    def where(name:String): SimpleComposition
  }
}

/**
 * FILTER
 */
object filter {

  def using(function:Function1[_,Boolean]) = new SimpleComposition(null, new MessageFilter(null, function)) with Where {
    def where(name:String)= new SimpleComposition(null, new MessageFilter(name, function))
  }

  def using(spelExpression:String) = new SimpleComposition(null, new MessageFilter(null, spelExpression)) with Where {
    def where(name:String)= new SimpleComposition(null, new MessageFilter(name, spelExpression))
  }

  private[filter] trait Where {
    def where(name:String): SimpleComposition
  }
}

/**
 * SPLITTER
 */
object split {

  def using(function:Function1[_,List[_]]) = new SimpleComposition(null, new MessageSplitter(null, target=function)) with Where {
    def where(name:String, applySequence:Boolean)= new SimpleComposition(null, new MessageSplitter(name, applySequence, function))
  }

  def using(spelExpression:String) = new SimpleComposition(null, new MessageSplitter(null, target = spelExpression)) with Where {
    def where(name:String, applySequence:Boolean)= new SimpleComposition(null, new MessageSplitter(name, applySequence, spelExpression))
  }

  private[split] trait Where {
    def where(name:String, applySequence:Boolean = true): SimpleComposition
  }
}

/**
* AGGREGATOR
*/
object aggregate {

  def apply() = new SimpleComposition(null, new MessageAggregator()) with Where {
    def where(name:String, keepReleasedMessages:Boolean)= new SimpleComposition(null, new MessageAggregator(name = name, keepReleasedMessages = keepReleasedMessages))
  }

  def correlatingOn(correlationFunction:Function1[_,AnyRef]) = new SimpleComposition(null, new MessageAggregator(null)) with ReleaseStrategy with Where {
    def where(name:String, keepReleasedMessages:Boolean)= new SimpleComposition(null, new MessageAggregator(name = name, keepReleasedMessages = keepReleasedMessages))

    def releasingWhen(releaseFunction:Function1[_,Boolean]) = new SimpleComposition(null, new MessageAggregator(null)) with Where {
      def where(name:String, keepReleasedMessages:Boolean)= new SimpleComposition(null, new MessageAggregator(name = name, keepReleasedMessages = keepReleasedMessages))
    }

    def releasingWhen(releaseExpression:String) = new SimpleComposition(null, new MessageAggregator(null)) with Where {
      def where(name:String, keepReleasedMessages:Boolean)= new SimpleComposition(null, new MessageAggregator(name = name, keepReleasedMessages = keepReleasedMessages))
    }
  }

  def correlatingOn(correlationKey:AnyRef) = new SimpleComposition(null, new MessageAggregator(null)) with ReleaseStrategy with Where {
    def where(name:String, keepReleasedMessages:Boolean)= new SimpleComposition(null, new MessageAggregator(name = name, keepReleasedMessages = keepReleasedMessages))

    def releasingWhen(releaseFunction:Function1[_,Boolean]) = new SimpleComposition(null, new MessageAggregator(null)) with Where {
      def where(name:String, keepReleasedMessages:Boolean)= new SimpleComposition(null, new MessageAggregator(name = name, keepReleasedMessages = keepReleasedMessages))
    }

    def releasingWhen(releaseExpression:String) = new SimpleComposition(null, new MessageAggregator(null)) with Where {
      def where(name:String, keepReleasedMessages:Boolean)= new SimpleComposition(null, new MessageAggregator(name = name, keepReleasedMessages = keepReleasedMessages))
    }
  }

  def releasingWhen(releaseFunction:Function1[_,Boolean]) = new SimpleComposition(null, new MessageAggregator(null)) with CorrelationStrategy with Where {
    def where(name:String, keepReleasedMessages:Boolean)= new SimpleComposition(null, new MessageAggregator(name = name, keepReleasedMessages = keepReleasedMessages))

    def correlatingOn(correlationFunction:Function1[_,AnyRef]) = new SimpleComposition(null, new MessageAggregator(null))  with Where {
      def where(name:String, keepReleasedMessages:Boolean)= new SimpleComposition(null, new MessageAggregator(name = name, keepReleasedMessages = keepReleasedMessages))
    }

    def correlatingOn(correlationKey:AnyRef) = new SimpleComposition(null, new MessageAggregator(null))  with Where {
      def where(name:String, keepReleasedMessages:Boolean)= new SimpleComposition(null, new MessageAggregator(name = name, keepReleasedMessages = keepReleasedMessages))
    }
  }

  def releasingWhen(releaseExpression:String) = new SimpleComposition(null, new MessageAggregator(null)) with Where {
    def where(name:String, keepReleasedMessages:Boolean)= new SimpleComposition(null, new MessageAggregator(name = name, keepReleasedMessages = keepReleasedMessages))

    def correlatingOn(correlationFunction:Function1[_,AnyRef]) = new SimpleComposition(null, new MessageAggregator(null))  with Where {
      def where(name:String, keepReleasedMessages:Boolean)= new SimpleComposition(null, new MessageAggregator(name = name, keepReleasedMessages = keepReleasedMessages))
    }

    def correlatingOn(correlationKey:AnyRef) = new SimpleComposition(null, new MessageAggregator(null))  with Where {
      def where(name:String, keepReleasedMessages:Boolean)= new SimpleComposition(null, new MessageAggregator(name = name, keepReleasedMessages = keepReleasedMessages))
    }
  }

  def where(name:String, keepReleasedMessages:Boolean = true)= new SimpleComposition(null, new MessageAggregator(name = name, keepReleasedMessages = keepReleasedMessages))

  private[aggregate] trait Where {
    def where(name:String, keepReleasedMessages:Boolean = true): SimpleComposition
  }

  private[aggregate] trait ReleaseStrategy {
    def releasingWhen(releaseFunction:Function1[_,Boolean]): SimpleComposition

    def releasingWhen(releaseExpression:String): SimpleComposition
  }

  private[aggregate] trait CorrelationStrategy {
    def correlatingOn(correlationKey:AnyRef): SimpleComposition

    def correlatingOn(correlationFunction:Function1[_,AnyRef]): SimpleComposition
  }
}

private[dsl] case class ServiceActivator(val name:String, val target:Any)

private[dsl] case class Transformer(val name:String, val target:Any)

private[dsl] case class MessageFilter(val name:String, val target:Any)

private[dsl] case class MessageSplitter(val name:String, val applySequence:Boolean = false, val target:Any)

private[dsl] case class MessageAggregator(val name:String = null,
                                          val keepReleasedMessages:Boolean = true,
                                          val messageStore:MessageStore = new SimpleMessageStore,
                                          val sendPartialResultsOnExpiry:Boolean = false,
                                          val expireGroupsUponCompletion:Boolean = false)

