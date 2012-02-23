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
package org.springframework.integration.dsl.builders
import org.springframework.beans.factory.support.BeanDefinitionBuilder
import org.springframework.integration.config.ServiceActivatorFactoryBean
import org.springframework.integration.dsl.utils.HttpRequestExecutingMessageHandlerWrapper
import org.springframework.integration.http.outbound.HttpRequestExecutingMessageHandler
/**
 * @author Oleg Zhurakousky
 */
object HttpOutboundGatewayBuilder {

  def buildHandler(gateway: HttpOutboundGateway): BeanDefinitionBuilder = {

    val handlerBuilder: BeanDefinitionBuilder =
      gateway.target match {
        case fn: Function[_, _] => {
          val handlerBuilder = BeanDefinitionBuilder.rootBeanDefinition(classOf[HttpRequestExecutingMessageHandlerWrapper])
          val functionInvoker = new FunctionInvoker(fn, gateway)
          handlerBuilder.addConstructorArgValue(functionInvoker)
          handlerBuilder.addConstructorArgValue(functionInvoker.methodName)
          handlerBuilder.addConstructorArgValue(gateway.httpMethod)
          handlerBuilder.addConstructorArgValue(gateway.expectedResponseType)
          handlerBuilder
        }
        case url: String => {
          val handlerBuilder = BeanDefinitionBuilder.rootBeanDefinition(classOf[HttpRequestExecutingMessageHandler])
          handlerBuilder.addConstructorArgValue(url)
          handlerBuilder.addPropertyValue("httpMethod", gateway.httpMethod)
          if (gateway.expectedResponseType != null) {
            handlerBuilder.addPropertyValue("expectedResponseType", gateway.expectedResponseType)
          }
          handlerBuilder
        }
        case _ => throw new IllegalArgumentException("Unsupported HTTP Outbound Gateway Target")
      }
    handlerBuilder
  }
}