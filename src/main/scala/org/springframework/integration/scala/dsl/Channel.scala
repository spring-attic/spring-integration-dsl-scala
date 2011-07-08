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
    var name = this.configMap.get(IntegrationComponent.name).asInstanceOf[String]
    if (StringUtils.hasText(name)) name else "channel_" + this.hashCode
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
  def apply(name:String): channel = channel.withName(name)

  def withQueue(capacity: Int) = new channel() with buffered {
    this.configMap.put("queueCapacity", capacity)
    def andName(componentName: String): channel with buffered = {
      this.configMap.put("andName", "andName")
      this
    }
  }
  def withQueue() = new channel() with buffered {
    this.configMap.put("queueCapacity", 0)
    def andName(componentName: String): channel with buffered = {
      this.configMap.put("andName", "andName")
      this
    }
  }
  def withName(name: String) = new channel() with andQueue with andExecutor {
    this.configMap.put(IntegrationComponent.name, name)
  }
  def withExecutor(executor: Executor) = new channel() with andName {
    this.configMap.put("executor", executor)
  }
   def withExecutor() = new channel() {
    this.configMap.put("executor", Executors.newCachedThreadPool)
    def andName(componentName: String): channel  = {
      this.configMap.put("andName", "andName")
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
    this.configMap.put(IntegrationComponent.name, name)
  }
  def withExecutor(executor: Executor) = new pub_sub_channel() with andName {
    this.configMap.put("executor", executor)
  }
  def withApplySequence(applySequence: Boolean) = new pub_sub_channel() with andName {
    this.configMap.put("applySequence", applySequence)
  }
}

// TRAITS related to Channels
/**
 *
 */
trait andQueue extends channel{
  def andQueue(capacity: Int): channel with buffered = {
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
  def andExecutor(executor:Executor): channel = {
    this.configMap.put("executor", executor)
    this
  }
  def andExecutor(): channel = {
    this.configMap.put("executor", Executors.newCachedThreadPool)
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
