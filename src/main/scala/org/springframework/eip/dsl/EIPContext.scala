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
import java.lang.IllegalStateException
import org.springframework.integration.{Message, MessageChannel}
import org.springframework.util.CollectionUtils
import org.springframework.integration.support.MessageBuilder
import collection.JavaConversions

/**
 * @author Oleg Zhurakousky
 */
object EIPContext {

  def apply(compositions:(EIPConfigurationComposition with CompletableEIPConfigurationComposition)*) = new EIPContext(null, compositions: _*)

  def apply(parentApplicationContext:ApplicationContext)(compositions:(EIPConfigurationComposition with CompletableEIPConfigurationComposition)*) =
            new EIPContext(parentApplicationContext, compositions: _*)
}

/**
 *
 */
class EIPContext(parentContext:ApplicationContext, compositions:(EIPConfigurationComposition with CompletableEIPConfigurationComposition)*) {

  val applicationContext = ApplicationContextBuilder.build(parentContext, compositions: _*)
  /**
   *
   */
  def send(message:Any, timeout:Long = 0, headers:Map[String,  Any] = null):Boolean = {
    if (compositions.size > 1){
      throw new IllegalStateException("Can not determine starting point for thsi context since it contains multiple")
    }
    
    val inputChannelName = compositions(0).asInstanceOf[EIPConfigurationComposition].getStartingComposition().target match {
      case ch:Channel => {
        ch.name
      }
      case _ => throw new IllegalStateException("Can not determine starting channel for composition: " + compositions(0))
    }

    val inputChannel = this.applicationContext.getBean[MessageChannel](inputChannelName, classOf[MessageChannel])

    val messageToSend = this.constructMessage(message, headers)
    val sent = if (timeout > 0){
      inputChannel.send(messageToSend, timeout)
    }
    else {
      inputChannel.send(messageToSend)
    }
    sent
  }

  private def constructMessage(message:Any, headers:Map[String,  Any] = null):Message[_] = {
    val javaHeaders = if (headers != null){
      JavaConversions.mapAsJavaMap(headers)
    }
    else {
      null
    }
    val messageToSend:Message[_] = message match {
      case msg:Message[_] => {
        if (!CollectionUtils.isEmpty(javaHeaders)){
          MessageBuilder.fromMessage(msg).copyHeadersIfAbsent(javaHeaders).build()
        }
        else {
          msg
        }
      }
      case _ => {
        if (!CollectionUtils.isEmpty(javaHeaders)){
          MessageBuilder.withPayload(message).copyHeaders(javaHeaders).build()
        }
        else {
          MessageBuilder.withPayload(message).build()
        }
      }
    }
    messageToSend
  }

}