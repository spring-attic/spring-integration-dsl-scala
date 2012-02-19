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

import org.springframework.scheduling.Trigger
import java.util.concurrent.Executor
import org.springframework.core.task.SyncTaskExecutor

/**
 * @author Oleg Zhurakousky
 */

object poll {
  /**
   * Will define poller which takes a reference to an instance of
   * org.springframework.scheduling.Trigger
   */
  def usingTrigger(trigger:Trigger) = new Poller()  {
    def withExecutor(taskExecutor:Executor = new SyncTaskExecutor) = new Poller(taskExecutor = taskExecutor)
  }

  /**
   *
   */
  def usingFixedRate(fixedRate:Int) = new Poller(fixedRate = fixedRate)  {

    def withExecutor(taskExecutor:Executor) = new Poller(fixedRate = fixedRate)  {
      def withMaxMessagesPerPoll(maxMessagesPerPoll:Int) =
        new Poller(fixedRate = fixedRate, maxMessagesPerPoll = maxMessagesPerPoll)
    }

    def withMaxMessagesPerPoll(maxMessagesPerPoll:Int) =
      new Poller(fixedRate = fixedRate, maxMessagesPerPoll = maxMessagesPerPoll)  {

      def withExecutor(taskExecutor:Executor = new SyncTaskExecutor) =
        new Poller(fixedRate = fixedRate, maxMessagesPerPoll = maxMessagesPerPoll, taskExecutor = taskExecutor)
    }
  }

  /**
   *
   */
  def usingFixedDelay(fixedDelay:Int) = new Poller(fixedDelay = fixedDelay)  {

    def withExecutor(taskExecutor:Executor) = new Poller(fixedDelay = fixedDelay)  {
      def withMaxMessagesPerPoll(maxMessagesPerPoll:Int) =
        new Poller(fixedDelay = fixedDelay, maxMessagesPerPoll = maxMessagesPerPoll)
    }

    def withMaxMessagesPerPoll(maxMessagesPerPoll:Int) =
      new Poller(fixedDelay = fixedDelay, maxMessagesPerPoll = maxMessagesPerPoll) {

      def withExecutor(taskExecutor:Executor = new SyncTaskExecutor) =
        new Poller(fixedDelay = fixedDelay, maxMessagesPerPoll = maxMessagesPerPoll, taskExecutor = taskExecutor)
      }
  }

//  private[poll] trait WithExecutor {
//    def withExecutor(taskExecutor:Executor = new SyncTaskExecutor): Poller
//  }
//  private[poll] trait WithMaxMessagesPerPoll {
//    def withMaxMessagesPerPoll(maxMessagesPerPoll:Int): Poller
//  }
}
/**
 * 
 */
case class Poller(val fixedRate:Int = Integer.MIN_VALUE,
                               val fixedDelay:Int = Integer.MIN_VALUE,
                               val maxMessagesPerPoll:Int = Integer.MIN_VALUE,
                               val taskExecutor:Executor = new SyncTaskExecutor,
                               val trigger:Trigger = null){

  def -->[T](a: T)(implicit g :ComposableIntegrationComponent[T]):IntegrationComposition = null
}