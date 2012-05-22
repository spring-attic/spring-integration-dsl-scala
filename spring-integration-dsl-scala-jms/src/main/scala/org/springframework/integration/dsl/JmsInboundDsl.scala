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
import org.springframework.beans.factory.support.BeanDefinitionBuilder
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils
import org.springframework.beans.factory.support.BeanDefinitionRegistry
import org.springframework.integration.jms.ChannelPublishingJmsMessageListener
import org.springframework.integration.jms.JmsMessageDrivenEndpoint
import org.springframework.jms.listener.DefaultMessageListenerContainer
import javax.jms.ConnectionFactory
import java.util.UUID
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.beans.factory.config.ConfigurableBeanFactory

/**
 * @author Oleg Zhurakousky
 */
private[dsl] class JmsInboundGateway(name: String = "$jms_in_" + UUID.randomUUID().toString.substring(0, 8),
  target: String,
  val connectionFactory: ConnectionFactory) extends InboundMessageSource(name, target) {

  override def build(document: Document = null, beanInstancesToRegister:scala.collection.mutable.Map[String, Any],
      requestChannelName: String): Element = {


    val beansElement = document.getElementsByTagName("beans").item(0).asInstanceOf[Element]
    beansElement.setAttribute("xmlns:int-jms", "http://www.springframework.org/schema/integration/jms")
    val schemaLocation = beansElement.getAttribute("xsi:schemaLocation")
    beansElement.setAttribute("xsi:schemaLocation", JmsDsl.jmsSchema)

    val element = document.createElement("int-jms:inbound-gateway")
    element.setAttribute("id", this.name)
    element.setAttribute("request-destination-name", this.target)
    element.setAttribute("request-channel", requestChannelName)
    val connectionFactoryName = "jms_cf_" + this.connectionFactory.hashCode()
    if (!beanInstancesToRegister.contains(connectionFactoryName)) {
      beanInstancesToRegister += (connectionFactoryName -> this.connectionFactory)
    }

    element.setAttribute("connection-factory", connectionFactoryName)
    element.setAttribute("auto-startup", "false")
    element
  }
}
