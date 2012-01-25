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

import org.springframework.context.ApplicationContext
import org.springframework.integration.channel.{QueueChannel, DirectChannel}
import java.util.UUID
import org.springframework.context.support.GenericApplicationContext

/**
 * @author Oleg Zhurakousky
 */
object EIPContext {

  def apply(compositions:CompletableEIPConfigurationComposition*) = new EIPContext(null, compositions: _*)

  def apply(parentApplicationContext:ApplicationContext)(compositions:CompletableEIPConfigurationComposition*) =
            new EIPContext(parentApplicationContext, compositions: _*)
}

/**
 *
 */
class EIPContext(parentContext:ApplicationContext, compositions:CompletableEIPConfigurationComposition*) {

  val applicationContext = ApplicationContextBuilder.build(parentContext, compositions: _*)

  /**
   *
   */
  def channel(name:String) = new DirectChannel() with SimpeSendable

  /**
   *
   */
  def channel(name:ChannelComposition) = new DirectChannel() with SimpeSendable

  /**
   *
   */
  def channel(name:PollableComposition) = new QueueChannel() with SimpeSendable

  private[EIPContext] trait SimpeSendable {
    def send(payload:AnyRef):Boolean = {
       true
    }

    def send(payload:String, timeout:Long):Boolean = {
       true
    }
  }

}