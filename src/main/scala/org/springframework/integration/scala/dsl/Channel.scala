/*
 * Copyright 2002-2011 the original author or authors.
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
package org.springframework.integration.scala.dsl
import java.util.concurrent._
import org.springframework.context._
import org.springframework.integration._
import org.springframework.integration.core._
import org.springframework.util._

/**
 * @author Oleg Zhurakousky
 *
 */
abstract class AbstractChannel extends InitializedComponent {

  private[dsl] var underlyingContext: ApplicationContext = null;

  override def toString = {
    this.configMap.get(IntegrationComponent.name).asInstanceOf[String]
  }

  def send(m: Message[Any]): Unit = {
    require(underlyingContext != null)
    var messageChannel = underlyingContext.getBean(this.configMap.get(IntegrationComponent.name).asInstanceOf[String])
    val mChannel = messageChannel.asInstanceOf[MessageChannel]
    mChannel.send(m)
    println()
  }
}

/**
 * Channel
 */
class channel extends AbstractChannel {
  this.configMap.put(IntegrationComponent.name, "generatedChannel_" + this.hashCode)
}
//
object channel {

  def apply(): channel = new channel()
  
  def apply(name: String): channel = {
    require(StringUtils.hasText(name))
    channel.withName(name)
  }

  def withQueue(capacity: Int) = new channel() with buffered {
    require(capacity > -1)
    this.configMap.put(IntegrationComponent.queueCapacity, capacity)
    
    def andName(name: String): channel with buffered = {
      require(StringUtils.hasText(name))
      this.configMap.put(IntegrationComponent.name, name)
      this
    }
  }

  def withQueue() = new channel() with buffered {
    this.configMap.put(IntegrationComponent.queueCapacity, 0)
    
    def andName(name: String): channel with buffered = {
      require(StringUtils.hasText(name))
      this.configMap.put(IntegrationComponent.name, name)
      this
    }
  }

  def withName(name: String) = new channel() with andQueue with andExecutor {
    require(StringUtils.hasText(name))
    this.configMap.put(IntegrationComponent.name, name)
  }

  def withExecutor(executor: Executor) = new channel() with andName {
    require(executor != null)
    this.configMap.put(IntegrationComponent.executor, executor)
  }

  def withExecutor() = new channel() {
    this.configMap.put(IntegrationComponent.executor, Executors.newCachedThreadPool)
    
    def andName(name: String): channel = {
      require(StringUtils.hasText(name))
      this.configMap.put(IntegrationComponent.name, name)
      this
    }
  }
}

/**
 * Pub-Sub Channel
 */
class pub_sub_channel extends channel {

}
//
object pub_sub_channel {
  def withName(name: String) = new pub_sub_channel() with andExecutor {
    require(StringUtils.hasText(name))
    this.configMap.put(IntegrationComponent.name, name)
  }
  def withExecutor(executor: Executor) = new pub_sub_channel() with andName {
    require(executor != null)
    this.configMap.put(IntegrationComponent.executor, executor)
  }
  def withApplySequence(applySequence: Boolean) = new pub_sub_channel() with andName {
    this.configMap.put(IntegrationComponent.applySequence, applySequence)
  }
}

// TRAITS related to Channels
/**
 *
 */
trait andQueue extends channel {
  def andQueue(capacity: Int): channel with buffered = {
    require(capacity > -1)
    val queueChannel = channel.withQueue(capacity)
    queueChannel.configMap.putAll(this.configMap)
    queueChannel
  }
  def andQueue(): channel with buffered = {
    val queueChannel = channel.withQueue(0)
    queueChannel.configMap.putAll(this.configMap)
    queueChannel
  }
}
/**
 *
 */
trait andExecutor extends channel {
  def andExecutor(executor: Executor): channel = {
    require(executor != null)
    this.configMap.put(IntegrationComponent.executor, executor)
    this
  }
  def andExecutor(): channel = {
    this.configMap.put(IntegrationComponent.executor, Executors.newCachedThreadPool)
    this
  }
}
/**
 *
 */
trait buffered extends channel {
  def receive(): Message[_] = {
    val underlyingChannelName = this.configMap.get(IntegrationComponent.name).asInstanceOf[String]
    var queueChannel = this.underlyingContext.getBean(underlyingChannelName, classOf[PollableChannel])
    queueChannel.receive
  }
}
