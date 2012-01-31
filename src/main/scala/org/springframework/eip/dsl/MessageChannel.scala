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
import org.springframework.integration.message.GenericMessage
import org.springframework.util.StringUtils
import java.util.UUID

/**
 * @author Oleg Zhurakousky
 */
object Channel {

  def apply(name:String) = new SimpleComposition(null, new Channel(name = name))
    with WithQueue
    with WithDispatcher
    with ChannelComposition
    with CompletableEIPConfigurationComposition {

    override def -->(composition: SimpleComposition) = {
      new SimpleCompletableComposition(this, composition.target) with CompletableEIPConfigurationComposition
    }

    def withQueue(capacity: Int, messageStore: MessageStore) =
          new PollableComposition(null, doWithQueue(name, capacity, messageStore))
              with CompletableEIPConfigurationComposition

    def withQueue() = new PollableComposition(null, doWithQueue(name, Int.MaxValue, new SimpleMessageStore))
              with CompletableEIPConfigurationComposition

    def withDispatcher(failover: Boolean, loadBalancer:String, taskExecutor:Executor) =
          new SimpleComposition(null, doWithDispatcher(name, failover, loadBalancer, taskExecutor))
                    with CompletableEIPConfigurationComposition with ChannelComposition{

            override def -->(composition: SimpleComposition) =
              new SimpleCompletableComposition(this, composition.target) with CompletableEIPConfigurationComposition
    }
  }

  def withDispatcher(failover: Boolean = true, loadBalancer:String = null, taskExecutor:Executor = null) =
    new SimpleComposition(null, doWithDispatcher(null, failover, loadBalancer, taskExecutor))
      with CompletableEIPConfigurationComposition with ChannelComposition{

      override def -->(composition: SimpleComposition) =
        new SimpleCompletableComposition(this, composition.target) with CompletableEIPConfigurationComposition
    }

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

  private[Channel] trait WithQueue {
    def withQueue(capacity: Int = Int.MaxValue, messageStore: MessageStore = new SimpleMessageStore): PollableComposition
                  with CompletableEIPConfigurationComposition


    def withQueue(): PollableComposition with CompletableEIPConfigurationComposition
  }

  private[Channel] trait WithDispatcher {
    def withDispatcher(failover: Boolean = true, loadBalancer:String = "round-robin", taskExecutor:Executor = null): SimpleComposition
  }
}

/**
 *
 */
private[dsl] case class Channel(val name:String,
                           val failover: Boolean = true,
                           val loadBalancer:String = null,
                           val taskExecutor:Executor = null,
                           val capacity: Int = Integer.MIN_VALUE,
                           val messageStore: MessageStore = null)

private[dsl] trait Receivable extends Sendable{
  def receive(): Message[_]

  def receive(timeout:Int): Message[_]
}

private[dsl] trait Sendable {
  def send(message:Message[_]): Unit
}

private[dsl] trait ChannelComposition