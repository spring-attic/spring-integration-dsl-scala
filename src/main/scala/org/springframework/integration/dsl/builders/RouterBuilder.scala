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
import org.springframework.integration.config.TransformerFactoryBean
import org.springframework.integration.router.HeaderValueRouter
import org.springframework.integration.config.RouterFactoryBean
import org.springframework.integration.router.PayloadTypeRouter
import org.springframework.integration.dsl.utils.DslUtils
import java.util.HashMap
import org.springframework.util.StringUtils
import org.apache.commons.logging.LogFactory
/**
 *
 * @author Oleg Zhurakousky
 */
object RouterBuilder {

  private val logger = LogFactory.getLog(this.getClass());

  def build(router: Router, targetDefFunction: Function2[SimpleEndpoint, BeanDefinitionBuilder, Unit],
    compositionInitFunction: Function2[BaseIntegrationComposition, AbstractChannel, Unit]): BeanDefinitionBuilder = {

    val conditions = router.conditions
    require(conditions != null && conditions.size > 0, "Router without conditions is not supported")

    val handlerBuilder: BeanDefinitionBuilder = {
      conditions(0) match {
        case hv: ValueCondition =>
          if (router.headerName != null) {
            val builder = BeanDefinitionBuilder.rootBeanDefinition(classOf[HeaderValueRouter])
            builder.addConstructorArgValue(router.headerName)
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

    targetDefFunction(router, handlerBuilder)

    val channelMappings = new HashMap[Any, Any]()

    for (condition <- conditions) {
      val composition = condition.integrationComposition.copy()
      val normailizedCompositon = composition.normalizeComposition()
      val channel = DslUtils.getStartingComposition(normailizedCompositon).target.asInstanceOf[AbstractChannel]
      channelMappings.put(condition.value, channel.name)
      compositionInitFunction(normailizedCompositon, null)
    }
    handlerBuilder.addPropertyValue("channelMappings", channelMappings)
  }
}