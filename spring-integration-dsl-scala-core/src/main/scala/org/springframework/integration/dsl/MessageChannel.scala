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

import org.springframework.integration.Message
import java.util.concurrent.Executor
import org.springframework.integration.store.{ SimpleMessageStore, MessageStore }
import org.springframework.util.StringUtils
import java.util.UUID

/**
 * @author Oleg Zhurakousky
 */
object Channel {

  /**
   *
   */
  def apply(name: String) = new ChannelIntegrationComposition(null, new Channel(name = name)) {
    require(StringUtils.hasText(name), "'name' must not be empty")
    def withQueue(capacity: Int = Int.MaxValue, messageStore: MessageStore = null) =
      new PollableChannelIntegrationComposition(null, doWithQueue(name = name, capacity = capacity, messageStore = messageStore))

    def withQueue() = new PollableChannelIntegrationComposition(null, doWithQueue(name = name))

    def withDispatcher(failover: Boolean = false, loadBalancer: String = null, taskExecutor: Executor = null) =
      new ChannelIntegrationComposition(null, doWithDispatcher(name = name, failover = failover, loadBalancer = loadBalancer, taskExecutor = taskExecutor))
  }
  
  /**
   * 
   */
  def apply() = new ChannelIntegrationComposition(null, new Channel()) {
    def withQueue(capacity: Int = Int.MaxValue, messageStore: MessageStore = null) =
      new PollableChannelIntegrationComposition(null, doWithQueue(capacity = capacity, messageStore = messageStore))

    def withQueue() = new PollableChannelIntegrationComposition(null, doWithQueue())

    def withDispatcher(failover: Boolean = false, loadBalancer: String = null, taskExecutor: Executor = null) =
      new ChannelIntegrationComposition(null, doWithDispatcher(failover = failover, loadBalancer = loadBalancer, taskExecutor = taskExecutor))
  }

  /**
   *
   */
  def withDispatcher(failover: Boolean = true, loadBalancer: String = null, taskExecutor: Executor = null) =
    new ChannelIntegrationComposition(null,
      doWithDispatcher(failover = failover, loadBalancer = loadBalancer, taskExecutor = taskExecutor))

  /**
   *
   */
  def withQueue = new PollableChannelIntegrationComposition(null, doWithQueue())

  /**
   *
   */
  def withQueue(capacity: Int = Int.MaxValue, messageStore: MessageStore = new SimpleMessageStore) =
    new PollableChannelIntegrationComposition(null, doWithQueue(capacity = capacity, messageStore = messageStore)) {
      require(messageStore != null, "'messageStore' must not be null")
    }

  private def doWithQueue(name: String = "$queue_ch_" + UUID.randomUUID().toString.substring(0, 8), capacity: Int = Int.MaxValue, messageStore: MessageStore = new SimpleMessageStore): PollableChannel = {
    new PollableChannel(name = name, capacity = capacity, messageStore = messageStore)
  }

  private def doWithDispatcher(name: String = "$ch_" + UUID.randomUUID().toString.substring(0, 8), failover: Boolean = true, loadBalancer: String = null, taskExecutor: Executor = null): Channel = {
    new Channel(name = name, failover = failover, loadBalancer = loadBalancer, taskExecutor = taskExecutor)
  }
}
/**
 *
 */
object PubSubChannel {
  def apply() = new ChannelIntegrationComposition(null, new PubSubChannel) {
    def applyingSequence = new ChannelIntegrationComposition(null, new PubSubChannel(applySequence = true)) {
      def withExecutor(taskExecutor: Executor) =
        new ChannelIntegrationComposition(null, new PubSubChannel(applySequence = true, taskExecutor = taskExecutor))
    }
  }

  def apply(name: String) = new ChannelIntegrationComposition(null, new PubSubChannel(name = name)) {
    require(StringUtils.hasText(name), "'name' must not be empty")
    def applyingSequence = new ChannelIntegrationComposition(null, new PubSubChannel(name = name, applySequence = true)) {
      def withExecutor(taskExecutor: Executor) =
        new ChannelIntegrationComposition(null, new PubSubChannel(name = name, applySequence = true, taskExecutor = taskExecutor))
    }
  }

  def applyingSequence = new ChannelIntegrationComposition(null, new PubSubChannel(applySequence = true)) {
    def withExecutor(taskExecutor: Executor) =
      new ChannelIntegrationComposition(null, new PubSubChannel(applySequence = true, taskExecutor = taskExecutor))
  }
  
  def withExecutor(taskExecutor: Executor) = new ChannelIntegrationComposition(null, new PubSubChannel(taskExecutor = taskExecutor)) {
    def applyingSequence =
      new ChannelIntegrationComposition(null, new PubSubChannel(applySequence = true, taskExecutor = taskExecutor))
  }
}

private[dsl] abstract class AbstractChannel(name: String) extends IntegrationComponent(name)

/**
 *
 */
private[dsl] class Channel(name: String = "$ch_" + UUID.randomUUID().toString.substring(0, 8),
  val failover: Boolean = true,
  val loadBalancer: String = null,
  val taskExecutor: Executor = null) extends AbstractChannel(name)

/**
 *
 */
private[dsl] class PollableChannel(name: String = "$queue_ch_" + UUID.randomUUID().toString.substring(0, 8),
  val capacity: Int = Int.MaxValue,
  val messageStore: MessageStore = null) extends AbstractChannel(name)
/**
 *
 */
private[dsl] class PubSubChannel(name: String = "$pub_sub_ch_" + UUID.randomUUID().toString.substring(0, 8),
  val applySequence: Boolean = false,
  val taskExecutor: Executor = null) extends AbstractChannel(name) 
