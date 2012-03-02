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
import org.springframework.integration.support.MessageBuilder
import scala.collection.JavaConversions
import java.lang.reflect.Method
import org.springframework.integration.Message
import org.apache.commons.logging.LogFactory
import org.springframework.util.ReflectionUtils
/**
 * @author Oleg Zhurakousky
 */
private final class FunctionInvoker(val f: Function[_, _], endpoint: IntegrationComponent) {
  private val logger = LogFactory.getLog(this.getClass());

  var methodName: String = ""

  val messageMethod = ReflectionUtils.findMethod(f.getClass(), "apply", classOf[Message[_]])
  val method = if (messageMethod != null) messageMethod else ReflectionUtils.findMethod(f.getClass(), "apply", classOf[Object])

  require(method != null, "Failed to find apply(..) method on the Function: " + f)
  
  val returnType = method.getReturnType()
  val inputParameter = method.getParameterTypes()(0)

  if (logger.isDebugEnabled) logger.debug("Selecting method: " + method)
 
  if (returnType.isAssignableFrom(Void.TYPE) && inputParameter.isAssignableFrom(classOf[Message[_]])) {
    methodName = "sendMessage"
  } else if (returnType.isAssignableFrom(Void.TYPE) && !classOf[Message[_]].isAssignableFrom(inputParameter)) {
    methodName = "sendPayload"
  } else if (classOf[Message[_]].isAssignableFrom(returnType) && classOf[Message[_]].isAssignableFrom(inputParameter)) {
    methodName = "sendMessageAndReceiveMessage"
  } else if (!classOf[Message[_]].isAssignableFrom(returnType) && classOf[Message[_]].isAssignableFrom(inputParameter)) {
    methodName = "sendMessageAndReceivePayload"
  } else if (classOf[Message[_]].isAssignableFrom(returnType) && !classOf[Message[_]].isAssignableFrom(inputParameter)) {
    methodName = "sendPayloadAndReceiveMessage"
  } else if (!classOf[Message[_]].isAssignableFrom(returnType) && !classOf[Message[_]].isAssignableFrom(inputParameter)) {
    methodName = "sendPayloadAndReceivePayload"
  }

  if (logger.isDebugEnabled) {
    logger.debug("FunctionInvoker method name: " + methodName)
  }
  
  def sendPayload(m: Object): Unit = {
    method.setAccessible(true)
    method.invoke(f, m)
  }
  
  def sendMessage(m: Message[_]): Unit = {
    method.setAccessible(true)
    method.invoke(f, m)
  }
  
  def sendPayloadAndReceivePayload(m: Object): Object = {
    var method = f.getClass.getDeclaredMethod("apply", classOf[Any])
    method.setAccessible(true)
    this.normalizeResult[Object](method.invoke(f, m))
  }
  
  def sendPayloadAndReceiveMessage(m: Object): Message[_] = {
    var method = f.getClass.getDeclaredMethod("apply", classOf[Any])
    method.setAccessible(true)
    this.normalizeResult[Message[_]](method.invoke(f, m).asInstanceOf[Message[_]])
  }
  
  def sendMessageAndReceivePayload(m: Message[_]): Object = {
    var method = f.getClass.getDeclaredMethod("apply", classOf[Any])
    method.setAccessible(true)
    this.normalizeResult[Object](method.invoke(f, m))
  }
  
  def sendMessageAndReceiveMessage(m: Message[_]): Message[_] = {
    var method = f.getClass.getDeclaredMethod("apply", classOf[Any])
    method.setAccessible(true)

    this.normalizeResult[Message[_]](method.invoke(f, m).asInstanceOf[Message[_]])
  }

  private def normalizeResult[T](result: Any): T = {
    endpoint match {
      case splitter: MessageSplitter => {
        result match {
          case message: Message[_] => {
            val payload = message.getPayload
            if (payload.isInstanceOf[Iterable[_]]) {
              MessageBuilder.withPayload(JavaConversions.asJavaCollection(payload.asInstanceOf[Iterable[_]])).
                copyHeaders(message.getHeaders).build().asInstanceOf[T]
            } else {
              message.asInstanceOf[T]
            }
          }
          case _ => {
            if (result.isInstanceOf[Iterable[_]]) {
              JavaConversions.asJavaCollection(result.asInstanceOf[Iterable[_]]).asInstanceOf[T]
            } else {
              result.asInstanceOf[T]
            }
          }
        }
      }
      case _ => {
        result.asInstanceOf[T]
      }
    }

  }
}