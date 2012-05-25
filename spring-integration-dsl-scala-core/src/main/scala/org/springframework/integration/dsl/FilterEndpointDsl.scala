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
import java.util.UUID
import org.springframework.beans.factory.support.BeanDefinitionBuilder
import org.springframework.integration.config.FilterFactoryBean
import org.springframework.util.StringUtils
import org.w3c.dom.Element
import org.w3c.dom.Document
/**
 * This class provides DSL and related components to support "Message Filter" pattern
 *
 * @author Oleg Zhurakousky
 */
object filter {

  def apply(function: Function1[_, Boolean]) = new SendingEndpointComposition(null, new MessageFilter(target = function)) {
    def additionalAttributes(name: String = "$flt_" + UUID.randomUUID().toString.substring(0, 8), exceptionOnRejection: Boolean = false) = {
      new SendingEndpointComposition(null, new MessageFilter(name = name, target = function, exceptionOnRejection = exceptionOnRejection))
    }
  }

  def apply(function: (_, Map[String, _]) => Boolean) = new SendingEndpointComposition(null, new MessageFilter(target = function)) {
    def additionalAttributes(name: String) = {

      require(StringUtils.hasText(name), "'name' must not be empty")
      new SendingEndpointComposition(null, new MessageFilter(name = name, target = function))
    }
  }
}

private[dsl] class MessageFilter(name: String = "$flt_" + UUID.randomUUID().toString.substring(0, 8), target: Any, val exceptionOnRejection: Boolean = false)
  extends SimpleEndpoint(name, target) {

  override def toMapOfProperties:Map[String, _] = super.toMapOfProperties + ("eipName" -> "FILTER")

  override def build(document: Document = null,
    targetDefinitionFunction: Function1[Any, Tuple2[String, String]],
    compositionInitFunction: Function2[BaseIntegrationComposition, AbstractChannel, Unit] = null,
    inputChannel:AbstractChannel,
    outputChannel:AbstractChannel): Element = {

    require(inputChannel != null, "'inputChannel' must be provided")

    val element = document.createElement("int:filter")
    element.setAttribute("id", this.name)
    if (this.exceptionOnRejection) {
      element.setAttribute("throw-exception-on-rejection", this.exceptionOnRejection.toString())
    }
    val targetDefinnition = targetDefinitionFunction.apply(this.target)
    element.setAttribute("ref", targetDefinnition._1);
    element.setAttribute("method", targetDefinnition._2);
    element.setAttribute("input-channel", inputChannel.name);
    if (outputChannel != null){
      element.setAttribute("output-channel", outputChannel.name);
    }
    element
  }
}