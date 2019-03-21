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
import java.util.UUID
import org.w3c.dom.Document
import org.w3c.dom.Element
import javax.jms.ConnectionFactory
import org.springframework.integration.dsl.utils.Conventions
import org.springframework.util.StringUtils
import org.springframework.integration.dsl.utils.DslUtils
/**
 * @author Oleg Zhurakousky
 */
private[dsl] class JmsInboundGatewayConfig(name: String = "$jms_in_" + UUID.randomUUID().toString.substring(0, 8),
  target: String,
  val connectionFactory: ConnectionFactory,
  val additionalAttributes: Map[String, _]) extends InboundMessageSource(name, target) {

  def build(document: Document = null,
    targetDefinitionFunction: Function1[Any, Tuple2[String, String]],
    pollerDefinitionFunction: Function3[IntegrationComponent, Poller, Element, Unit],
    requestChannelName: String): Element = {

    val beansElement = document.getElementsByTagName("beans").item(0).asInstanceOf[Element]
    if (!beansElement.hasAttribute("xmlns:int-jms")) {
      beansElement.setAttribute("xmlns:int-jms", "http://www.springframework.org/schema/integration/jms")
      val schemaLocation = beansElement.getAttribute("xsi:schemaLocation")
      beansElement.setAttribute("xsi:schemaLocation", schemaLocation + JmsDsl.jmsSchema);
    }

    val element = document.createElement("int-jms:inbound-gateway")
    element.setAttribute("id", this.name)
    element.setAttribute("request-destination-name", this.target)
    element.setAttribute("request-channel", requestChannelName)

    val connectionFactoryName = targetDefinitionFunction(Some(this.connectionFactory))._1

    element.setAttribute("connection-factory", connectionFactoryName)
    element.setAttribute("auto-startup", "false")
    if (additionalAttributes != null){
      DslUtils.setAdditionalAttributes(element, additionalAttributes, null)
    }
    element
  }
}
