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
import java.lang.reflect.Method

import scala.collection.mutable.ListBuffer
import scala.collection.JavaConversions

import org.apache.commons.logging.LogFactory
import org.springframework.integration.support.MessageBuilder
import org.springframework.integration.Message
/**
 * @author Oleg Zhurakousky
 */
private final class FunctionInvoker(val f: Function[_, _], val endpoint: IntegrationComponent) {
  private val logger = LogFactory.getLog(this.getClass());
  private val APPLY_METHOD = "apply"

  val applyMethod = this.findPropperApplyMethod

  require(applyMethod != null, "Failed to find " + APPLY_METHOD + "(..) method on the Function: " + f)

  val methodName = this.determineApplyWrapperName

  /**
   *
   */
  def sendPayload(payload: Object): Unit = {
    this.invokeMethod[Object](payload)
  }

  /**
   *
   */
  def sendMessage(message: Message[_]): Unit = {
    this.invokeMethod[Object](message)
  }

  /**
   *
   */
  def sendPayloadAndReceive(payload: Object) = {
    val result = this.invokeMethod[Object](payload)
    result
  }

  /**
   *
   */
  def sendMessageAndReceive(message: Message[_]) = {
    this.invokeMethod[Object](message)
  }

  /**
   *
   */
  private def findPropperApplyMethod: Method = {
    // TODO Make it more Scala-esque (looks like java)
    
    val methodBuffer = new ListBuffer[Method]

    val methods = f.getClass().getDeclaredMethods()
    
    for (method <- methods)
      if (method.getName == APPLY_METHOD) methodBuffer += method

    val applyMethods = methodBuffer.toList

    val messageMethod =
      if (applyMethods.size == 1) 
        applyMethods(0)
      else if (applyMethods(0).getReturnType().isAssignableFrom(classOf[Any]) &&
        applyMethods(0).getParameterTypes()(0).isAssignableFrom(classOf[Any])) 
        applyMethods(1)
      else 
        applyMethods(0)
    
    messageMethod
  }

  /*
   * 
   */
  private def invokeMethod[T](value: Object): T = {
    var method = f.getClass.getDeclaredMethod(APPLY_METHOD, classOf[Any])
    method.setAccessible(true)
    this.normalizeResult[T](method.invoke(f, value))
  }

  /*
   * 
   */
  private def normalizeResult[T](result: Any): T = {
    val normalizedResponse =
      endpoint match {
        case splitter: MessageSplitter => {
          result match {
            case message: Message[_] => {
              message.getPayload match {
                case it: Iterable[_] =>
                  MessageBuilder.withPayload(JavaConversions.asJavaCollection(it)).
                    copyHeaders(message.getHeaders).build()
                case _ => message
              }
            }
            case it: Iterable[_] =>
              JavaConversions.asJavaCollection(it)
            case _ =>
              result
          }
        }
        case _ =>
          result
      }
    normalizedResponse.asInstanceOf[T]
  }

  /*
   * 
   */
  private def determineApplyWrapperName: String = {

    val returnType = applyMethod.getReturnType()
    val inputParameter = applyMethod.getParameterTypes()(0)

    if (logger.isDebugEnabled) logger.debug("Selecting method: " + applyMethod)

    val methodName =
      if (Void.TYPE.isAssignableFrom(returnType)) {
        if (classOf[Message[_]].isAssignableFrom(inputParameter))
          "sendMessage"
        else
          "sendPayload"
      } else {
        if (classOf[Message[_]].isAssignableFrom(inputParameter))
          "sendMessageAndReceive"
        else
          "sendPayloadAndReceive"
      }

    if (logger.isDebugEnabled) logger.debug("FunctionInvoker method name: " + methodName)

    methodName
  }
}