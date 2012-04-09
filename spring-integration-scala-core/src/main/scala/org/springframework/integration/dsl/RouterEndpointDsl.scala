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
import org.springframework.integration.dsl.utils.DslUtils
import org.springframework.util.StringUtils
import org.springframework.beans.factory.support.BeanDefinitionBuilder
import org.springframework.integration.router.PayloadTypeRouter
import org.springframework.integration.router.HeaderValueRouter
import org.springframework.integration.config.RouterFactoryBean
import java.util.HashMap
import org.apache.commons.logging.LogFactory

/**
 * This class provides DSL and related components to support "Message Router" pattern
 * 
 * @author Oleg Zhurakousky
 */
object route {

  def onPayloadType(conditionCompositions: PayloadTypeCondition*) = new SendingEndpointComposition(null, new Router()(conditionCompositions: _*)) {

    def where(name: String) = new SendingEndpointComposition(null, new Router(name, null, null)(conditionCompositions: _*))
  }

  def onValueOfHeader(headerName: String)(conditionCompositions: ValueCondition*) = {
    require(StringUtils.hasText(headerName), "'headerName' must not be empty")
    new SendingEndpointComposition(null, new Router(headerName = headerName)(conditionCompositions: _*)) {

      def where(name: String) = new SendingEndpointComposition(null, new Router(name = name, headerName = headerName)(conditionCompositions: _*))
    }
  }

  def using(target: String)(conditions: ValueCondition*) =
    new SendingEndpointComposition(null, new Router(target = target)(conditions: _*)) {
      def where(name: String) = new SendingEndpointComposition(null, new Router(name = name, target = target)(conditions: _*))
    }

  def using(target: Function1[_, String])(conditions: ValueCondition*) =
    new SendingEndpointComposition(null, new Router(target = target)(conditions: _*)) {
      def where(name: String) = new SendingEndpointComposition(null, new Router(name = name, target = target)(conditions: _*))
    }
}
/**
 *
 */
object when {
  def apply(payloadType: Class[_]) = new {
    def then(composition: BaseIntegrationComposition) = new PayloadTypeCondition(payloadType, composition)
  }

  def apply(value: Any) = new {
    def then(composition: BaseIntegrationComposition) = new ValueCondition(value, composition)
  }
}

private[dsl] class Router(name: String = "$rtr_" + UUID.randomUUID().toString.substring(0, 8), target: Any = null, val headerName: String = null)(val conditions: Condition*)
  extends SimpleEndpoint(name, target) {
  
  private val logger = LogFactory.getLog(this.getClass());
  
  override def build(targetDefFunction: Function2[SimpleEndpoint, BeanDefinitionBuilder, Unit],
                     compositionInitFunction: Function2[BaseIntegrationComposition, AbstractChannel, Unit]): BeanDefinitionBuilder = {
    
    val conditions = this.conditions
    
    require(conditions != null && conditions.size > 0, "Router without conditions is not supported")

    val handlerBuilder: BeanDefinitionBuilder = {
      conditions(0) match {
        case hv: ValueCondition =>
          if (this.headerName != null) {
            val builder = BeanDefinitionBuilder.rootBeanDefinition(classOf[HeaderValueRouter])
            builder.addConstructorArgValue(this.headerName)
            builder
          } else
            BeanDefinitionBuilder.rootBeanDefinition(classOf[RouterFactoryBean])

        case pt: PayloadTypeCondition =>
          BeanDefinitionBuilder.rootBeanDefinition(classOf[PayloadTypeRouter])

        case _ => throw new IllegalStateException("Unrecognized Router type: " + conditions(0))
      }
    }

    if (logger.isDebugEnabled)
      logger.debug("Creating Router of type: " + handlerBuilder.getBeanDefinition.getBeanClass.getSimpleName)

    if (this.target != null)
      targetDefFunction(this, handlerBuilder)

    val channelMappings = new HashMap[Any, Any]()

    for (condition <- conditions) {
      val composition = condition.integrationComposition.copy()
      val normailizedCompositon = composition.normalizeComposition()
      val channel = DslUtils.getStartingComposition(normailizedCompositon).target.asInstanceOf[AbstractChannel]
      channelMappings.put(condition.value, channel.name)
      compositionInitFunction(normailizedCompositon, null)
    }
    handlerBuilder.addPropertyValue("channelMappings", channelMappings)
    handlerBuilder
  }
}

private[dsl] abstract class Condition(val value: Any, val integrationComposition: BaseIntegrationComposition)

private[dsl] class PayloadTypeCondition(val payloadType: Class[_], override val integrationComposition: BaseIntegrationComposition)
  extends Condition(DslUtils.toJavaType(payloadType).getName(), integrationComposition)

private[dsl] class ValueCondition(override val value: Any, override val integrationComposition: BaseIntegrationComposition)
  extends Condition(value, integrationComposition)