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

import java.lang.IllegalStateException

import scala.collection.JavaConversions.asScalaBuffer
import scala.collection.JavaConversions.asScalaSet
import scala.collection.JavaConversions.mapAsScalaMap
import scala.collection.JavaConversions

import org.apache.commons.logging.LogFactory
import org.springframework.context.ApplicationContext
import org.springframework.integration.channel.QueueChannel
import org.springframework.integration.dsl.utils.DslUtils
import org.springframework.integration.support.MessageBuilder
import org.springframework.integration.Message
import org.springframework.integration.MessageChannel
import org.springframework.integration.MessagingException
import org.springframework.util.CollectionUtils
import org.springframework.util.StringUtils

/**
 * @author Oleg Zhurakousky
 */
private[dsl] class IntegrationContext(parentContext: ApplicationContext, composition: BaseIntegrationComposition) {

  private val logger = LogFactory.getLog(this.getClass());

  if (logger.isDebugEnabled) logger.debug("Creating new EIP context")

  val normalizedComposition = composition.normalizeComposition()

  normalizedComposition.target match {
    case poller: Poller => throw new IllegalStateException("The resulting message flow configuration ends with Poller which " +
      "has no consumer Consumer: " + poller)
    case ch: Channel =>
      throw new IllegalStateException("The resulting message flow configuration ends with " +
        "Direct Channel but no subscribers are configured: " + ch)
    case ch: PubSubChannel =>
      logger.warn("The resulting message flow configuration ends with " +
        "Publish Subscribe Channel but no subscribers are configured: " + ch)
    case _ =>
  }

  val inputChannelName: String = {

    val startComp = DslUtils.getStartingComposition(normalizedComposition)
    startComp.target match {
      case ch: AbstractChannel => ch.name
      case _ => null
    }
  }

  val applicationContext = ApplicationContextBuilder.build(parentContext, normalizedComposition)

  def start() = this.applicationContext.start()

  def stop() = this.applicationContext.stop()
  /**
   *
   */
  def send(message: Any, timeout: Long = 0, headers: Map[String, Any] = null): Boolean = {
    require(StringUtils.hasText(this.inputChannelName), "Can not determine Input Channel for this composition")
    val inputChannel = this.applicationContext.getBean[MessageChannel](this.inputChannelName, classOf[MessageChannel])

    val messageToSend = this.constructMessage(message, headers)

    if (timeout > 0) inputChannel.send(messageToSend, timeout)
    else inputChannel.send(messageToSend)

  }

  /**
   *
   */
  def sendAndReceive[T: Manifest](message: Any, timeout: Long = 0, headers: Map[String, Any] = null, errorFlow: SendingEndpointComposition = null): T = {
    require(StringUtils.hasText(this.inputChannelName), "Can not determine Input Channel for this composition")
    val inputChannel = this.applicationContext.getBean[MessageChannel](this.inputChannelName, classOf[MessageChannel])

    val replyChannel = new QueueChannel()

    val messageToSend = MessageBuilder.
      fromMessage(this.constructMessage(message, headers)).setReplyChannel(replyChannel).build()

    val reply =
      try {
        val sent =
          if (timeout > 0)
            inputChannel.send(messageToSend, timeout)
          else
            inputChannel.send(messageToSend)
 
        if (!sent)
          throw new MessagingException(messageToSend, "Failed to send message")

        val replyMessage = replyChannel.receive(timeout)
        
        this.convertReply(replyMessage)
      } catch {
        case ex: Exception => {
          if (errorFlow != null) {
            errorFlow.sendAndReceive(ex)
          } else throw ex
        }
      }

    reply.asInstanceOf[T]
  }

  private def convertReply[T: Manifest](replyMessage: Message[_]): Any = {

    if (manifest.erasure.isAssignableFrom(classOf[Message[_]]))
      replyMessage
    else {
      val reply = if (replyMessage == null) null else replyMessage.getPayload
      reply match {
        case list: java.util.List[_] => list.toList
        case set: java.util.Set[_] => set.toSet
        case map: java.util.Map[_, _] => map.toMap
        case _ => reply
      }
    }
  }

  private def constructMessage(message: Any, headers: Map[String, Any] = null): Message[_] = {
    val javaHeaders =
      if (headers != null) JavaConversions.mapAsJavaMap(headers) else null

    val messageToSend: Message[_] =
      message match {
        case msg: Message[_] => {
          if (!CollectionUtils.isEmpty(javaHeaders)) 
            MessageBuilder.fromMessage(msg).copyHeadersIfAbsent(javaHeaders).build()
          else 
            msg
        }
        case _ => {
          if (!CollectionUtils.isEmpty(javaHeaders)) 
            MessageBuilder.withPayload(message).copyHeaders(javaHeaders).build()
          else 
            MessageBuilder.withPayload(message).build()
        }
      }
    messageToSend
  }

}