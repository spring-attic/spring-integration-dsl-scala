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

import org.springframework.integration.Message
import java.util.concurrent.Executor
import org.springframework.integration.store.{SimpleMessageStore, MessageStore}
import org.springframework.util.StringUtils
import java.util.UUID

/**
 * @author Oleg Zhurakousky
 */
object Channel {
  
  /**
   * 
   */
  def apply(name:String) = new ChannelIntegrationComposition(null, new Channel(name = name)) {

    def withQueue(capacity: Int = Int.MaxValue, messageStore: MessageStore = null) =
          new PollableChannelIntegrationComposition(null, doWithQueue(name, capacity, messageStore))

    def withQueue() = new PollableChannelIntegrationComposition(null, doWithQueue(name, Int.MaxValue, new SimpleMessageStore))

    def withDispatcher(failover: Boolean = false, loadBalancer:String = null, taskExecutor:Executor = null) =
          new ChannelIntegrationComposition(null, doWithDispatcher(name, failover, loadBalancer, taskExecutor))
  }
  
  /**
   * 
   */
  def withDispatcher(failover: Boolean = true, loadBalancer:String = null, taskExecutor:Executor = null) =
    new ChannelIntegrationComposition(null, doWithDispatcher(null, failover, loadBalancer, taskExecutor))
  
  /**
   * 
   */
  def withQueue = new PollableChannelIntegrationComposition(null, doWithQueue(null, Int.MaxValue, new SimpleMessageStore))
  
  /**
   * 
   */
  def withQueue(capacity:Int = Int.MaxValue, messageStore: MessageStore = new SimpleMessageStore) = 
    	new PollableChannelIntegrationComposition(null, doWithQueue(null, capacity, messageStore))

  private def doWithQueue(name:String,  capacity: Int, messageStore: MessageStore): Channel  = {
    val channelName:String = if (!StringUtils.hasText(name)){
      "$ch_" + UUID.randomUUID().toString.substring(0,8)
    }
    else {
      name
    }
    new Channel(name = channelName, capacity = capacity, messageStore = messageStore)
  }

  private def doWithDispatcher(name:String,  failover: Boolean, loadBalancer:String, taskExecutor:Executor): Channel = {
    val channelName:String = if (!StringUtils.hasText(name)){
      "$ch_" + UUID.randomUUID().toString.substring(0,8)
    }
    else {
      name
    }
    new Channel(name = channelName, failover = failover, loadBalancer = loadBalancer, taskExecutor = taskExecutor)
  }
}
/**
 * 
 */
object PubSubChannel {
  def apply() = new ChannelIntegrationComposition(null, new PubSubChannel(name = null)) {
    def applyingSequence = new ChannelIntegrationComposition(null, new PubSubChannel(name = null, applySequence=true))
  }
  
  def apply(name:String) = new ChannelIntegrationComposition(null, new PubSubChannel(name = name)) {
    def applyingSequence = new ChannelIntegrationComposition(null, new PubSubChannel(name = name, applySequence=true))
  }
  
  def applyingSequence = new ChannelIntegrationComposition(null, new PubSubChannel(name = null, applySequence=true))
}

private[dsl] abstract class AbstractChannel(override val name:String) extends IntegrationComponent(name)

/**
 *
 */
private[dsl] case class Channel(override val name:String,
                           val failover: Boolean = true,
                           val loadBalancer:String = null,
                           val taskExecutor:Executor = null,
                           val capacity: Int = Integer.MIN_VALUE,
                           val messageStore: MessageStore = null) extends AbstractChannel(name)
/**
 *                            
 */
private[dsl] case class PubSubChannel(override val name:String,
                           val applySequence: Boolean = false,
                           val taskExecutor:Executor = null) extends AbstractChannel(name)
