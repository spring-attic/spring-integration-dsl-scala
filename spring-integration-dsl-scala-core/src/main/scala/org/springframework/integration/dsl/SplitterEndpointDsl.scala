/*
 * Copyright 2002-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.integration.dsl

import org.springframework.integration.store.{ SimpleMessageStore, MessageStore }
import java.util.UUID
import org.springframework.util.StringUtils
import org.springframework.beans.factory.support.BeanDefinitionBuilder
import org.springframework.integration.config.SplitterFactoryBean
import org.w3c.dom.Element
import org.w3c.dom.Document

/**
 * This class provides DSL and related components to support "Message Splitter" pattern
 *
 * @author Oleg Zhurakousky
 */
object split {

  def apply[I: Manifest](function: Function1[I, Iterable[Any]]) = new SendingEndpointComposition(null, new Splitter(target = new SingleMessageScalaFunctionWrapper(function))) {
    def additionalAttributes(name: String = "$split_" + UUID.randomUUID().toString.substring(0, 8), applySequence: Boolean = true) =
      new SendingEndpointComposition(null, new Splitter(name = name, target = new SingleMessageScalaFunctionWrapper(function), applySequence = applySequence))
  }

  def apply[I: Manifest](function: (I, Map[String, _]) => Iterable[Any]) = new SendingEndpointComposition(null, new Splitter(target = new ParsedMessageScalaFunctionWrapper(function))) {
    def additionalAttributes(name: String) = {

      require(StringUtils.hasText(name), "'name' must not be empty")
      new SendingEndpointComposition(null, new Splitter(name = name, target = new ParsedMessageScalaFunctionWrapper(function)))
    }
  }
}

private[dsl] class Splitter(name: String = "$splt_" + UUID.randomUUID().toString.substring(0, 8), target: Any, val applySequence: Boolean = false)
  													extends SimpleEndpoint(name, target) {

  override def build(document: Document = null,
    targetDefinitionFunction: Function1[Any, Tuple2[String, String]],
    compositionInitFunction: Function2[BaseIntegrationComposition, AbstractChannel, Unit] = null,
    inputChannel:AbstractChannel,
    outputChannel:AbstractChannel): Element = {

    require(inputChannel != null, "'inputChannel' must be provided")

    val element = document.createElement("int:splitter")
    element.setAttribute("id", this.name)
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