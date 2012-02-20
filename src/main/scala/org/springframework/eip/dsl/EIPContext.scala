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
import org.springframework.integration.channel.QueueChannel

/**
 * @author Oleg Zhurakousky
 */
object $ {

  def apply(compositions:IntegrationComposition*) = new $(null, compositions: _*)

  def apply(parentApplicationContext:ApplicationContext)(compositions:IntegrationComposition*) =
            new $(parentApplicationContext, compositions: _*)
}

/**
 *
 */
class $(parentContext:ApplicationContext, compositions:IntegrationComposition*) {

  val applicationContext = ApplicationContextBuilder.build(parentContext, compositions: _*)
  /**
   *
   */
  def send(message:Any, timeout:Long = 0, headers:Map[String,  Any] = null):Boolean = {
    if (compositions.size > 1){
      throw new IllegalStateException("Can not determine starting point for thsi context since it contains multiple")
    }
    
//    val inputChannelName = DslUtils.getStartingComposition(compositions(0)).target match {
//      case ch:Channel => {
//        ch.name
//      }
//      case _ => throw new IllegalStateException("Can not determine starting channel for composition: " + compositions(0))
//    }
//
//    val inputChannel = this.applicationContext.getBean[MessageChannel](inputChannelName, classOf[MessageChannel])
//
//    val messageToSend = this.constructMessage(message, headers)
//    val sent = if (timeout > 0){
//      inputChannel.send(messageToSend, timeout)
//    }
//    else {
//      inputChannel.send(messageToSend)
//    }
//    sent
    false
  }

  def sendAndReceive(message:Any, timeout:Long = 0, headers:Map[String,  Any] = null):Message[_] = {
//    if (compositions.size > 1){
//      throw new IllegalStateException("Can not determine starting point for thsi context since it contains multiple")
//    }
//
//    val inputChannelName = compositions(0).asInstanceOf[EIPConfigurationComposition].getStartingComposition().target match {
//      case ch:Channel => {
//        ch.name
//      }
//      case _ => throw new IllegalStateException("Can not determine starting channel for composition: " + compositions(0))
//    }
//
//    val inputChannel = this.applicationContext.getBean[MessageChannel](inputChannelName, classOf[MessageChannel])
//
//    val replyChannel = new QueueChannel()
//    val messageToSend = MessageBuilder.
//      fromMessage(this.constructMessage(message, headers)).setReplyChannel(replyChannel).build()
//    val sent = if (timeout > 0){
//      inputChannel.send(messageToSend, timeout)
//    }
//    else {
//      inputChannel.send(messageToSend)
//    }
//    replyChannel.receive(1000)
    null
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