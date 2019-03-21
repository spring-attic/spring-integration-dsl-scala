/*
 * Copyright 2002-2013 the original author or authors.
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
import java.util.UUID
import org.springframework.integration.dsl.utils.DslUtils
import org.springframework.util.StringUtils
import org.springframework.beans.factory.support.BeanDefinitionBuilder
import org.springframework.integration.router.PayloadTypeRouter
import org.springframework.integration.router.HeaderValueRouter
import org.springframework.integration.config.RouterFactoryBean
import java.util.HashMap
import org.apache.commons.logging.LogFactory
import org.w3c.dom.Element
import org.w3c.dom.Document

/**
 * This class provides DSL and related components to support "Message Router" pattern
 *
 * @author Oleg Zhurakousky
 * @author Soby Chacko
 */
object route {

  def onPayloadType(conditionCompositions: PayloadTypeCondition*) = new SendingEndpointComposition(null, new Router()(conditionCompositions: _*)) {

    def additionalAttributes(name: String) = new SendingEndpointComposition(null, new Router(name, null, null)(conditionCompositions: _*))
  }

  def onValueOfHeader(headerName: String)(conditionCompositions: ValueCondition*) = {
    require(StringUtils.hasText(headerName), "'headerName' must not be empty")
    new SendingEndpointComposition(null, new Router(headerName = headerName)(conditionCompositions: _*)) {

      def additionalAttributes(name: String) = new SendingEndpointComposition(null, new Router(name = name, headerName = headerName)(conditionCompositions: _*))
    }
  }

  def apply(target: String)(conditions: ValueCondition*) =
    new SendingEndpointComposition(null, new Router(target = target)(conditions: _*)) {
      def additionalAttributes(name: String) = new SendingEndpointComposition(null, new Router(name = name, target = target)(conditions: _*))
    }

  def apply[I:Manifest](function: Function1[I, String])(conditions: ValueCondition*) =
    new SendingEndpointComposition(null, new Router(target = new SingleMessageScalaFunctionWrapper(function))(conditions: _*)) {
      def additionalAttributes(name: String) = new SendingEndpointComposition(null, new Router(name = name, target = new SingleMessageScalaFunctionWrapper(function))(conditions: _*))
    }
}
/**
 *
 */
object when {
  def apply(payloadType: Class[_]) = new {
    def andThen(composition: BaseIntegrationComposition) = new PayloadTypeCondition(payloadType, composition)
  }

  def apply(value: Any) = new {
    def andThen(composition: BaseIntegrationComposition) = new ValueCondition(value, composition)
  }
}

private[dsl] class Router(name: String = "$rtr_" + UUID.randomUUID().toString.substring(0, 8), target: Any = null, val headerName: String = null)(val conditions: Condition*)
  extends SimpleEndpoint(name, target) {

  private val logger = LogFactory.getLog(this.getClass());

  override def build(document: Document = null,
    targetDefinitionFunction: Function1[Any, Tuple2[String, String]],
    compositionInitFunction: Function2[BaseIntegrationComposition, AbstractChannel, Unit] = null,
    inputChannel:AbstractChannel,
    outputChannel:AbstractChannel): Element = {

    require(inputChannel != null, "'inputChannel' must be provided")

    require(this.conditions != null && this.conditions.size > 0, "Router without conditions is not supported")

    val routerElement: Element =
      this.conditions(0) match {
        case hv: ValueCondition =>
          if (this.headerName != null) {
            val hvrElement = document.createElement("int:header-value-router")
            hvrElement.setAttribute("header-name", this.headerName)
            hvrElement
          } else {
            document.createElement("int:router")
          }
        case ptr: PayloadTypeCondition =>
          document.createElement("int:payload-type-router")
        case _ => throw new IllegalStateException("Unrecognized Router type: " + conditions(0))
      }

    if (logger.isDebugEnabled)
      logger.debug("Creating Router of type: " + routerElement)

    val targetDefinnition:Tuple2[String, String] = if (this.target != null) targetDefinitionFunction(this.target) else null

    targetDefinnition match {
      case t:Tuple2[String, String] => {
        routerElement.setAttribute("ref", targetDefinnition._1);
        routerElement.setAttribute("method", targetDefinnition._2);
      }
      case _ =>
    }

    for (condition <- conditions) yield {
      val composition = condition.integrationComposition.copy()
      val normailizedCompositon = composition.normalizeComposition()
      val channel = DslUtils.getStartingComposition(normailizedCompositon).target.asInstanceOf[AbstractChannel]
      compositionInitFunction(normailizedCompositon, null)
      val mapping = document.createElement("int:mapping")
      val keyAttributeName =
        condition match {
          case _: PayloadTypeCondition => "type"
          case _ => "value"
        }
      mapping.setAttribute(keyAttributeName, condition.value.toString())
      mapping.setAttribute("channel", channel.name)
      routerElement.appendChild(mapping)
    }
    routerElement.setAttribute("input-channel", inputChannel.name);
    if (outputChannel != null){
      routerElement.setAttribute("default-output-channel", outputChannel.name);
    }
    routerElement
  }
}

private[dsl] abstract class Condition(val value: Any, val integrationComposition: BaseIntegrationComposition)

private[dsl] class PayloadTypeCondition(val payloadType: Class[_], override val integrationComposition: BaseIntegrationComposition)
  extends Condition(DslUtils.toJavaType(payloadType).getName(), integrationComposition)

private[dsl] class ValueCondition(override val value: Any, override val integrationComposition: BaseIntegrationComposition)
  extends Condition(value, integrationComposition)