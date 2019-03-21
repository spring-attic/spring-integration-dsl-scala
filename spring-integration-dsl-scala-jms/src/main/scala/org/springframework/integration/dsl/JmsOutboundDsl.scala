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
import javax.jms.ConnectionFactory
import java.util.UUID
import org.w3c.dom.Element
import org.w3c.dom.Document
/**
 * @author Oleg Zhurakousky
 */
private[dsl] class JmsOutboundGatewayConfig(name: String = "$jms_out_" + UUID.randomUUID().toString.substring(0, 8),
  target: String,
  oneway: Boolean,
  val connectionFactory: ConnectionFactory) extends SimpleEndpoint(name, target) with OutboundAdapterEndpoint {

  override def build(document: Document = null,
    targetDefinitionFunction: Function1[Any, Tuple2[String, String]],
    compositionInitFunction: Function2[BaseIntegrationComposition, AbstractChannel, Unit] = null,
    inputChannel: AbstractChannel,
    outputChannel: AbstractChannel): Element = {

    require(inputChannel != null, "'inputChannel' must be provided")

    val connectionFactoryName = targetDefinitionFunction.apply(Some(connectionFactory))._1

    val beansElement = document.getElementsByTagName("beans").item(0).asInstanceOf[Element]
    if (!beansElement.hasAttribute("xmlns:int-jms")){
       beansElement.setAttribute("xmlns:int-jms", "http://www.springframework.org/schema/integration/jms")
       val schemaLocation = beansElement.getAttribute("xsi:schemaLocation")
       beansElement.setAttribute("xsi:schemaLocation", schemaLocation + JmsDsl.jmsSchema);
    }

    val element: Element =
      if (oneway) {
        val outboundAdapterElement = document.createElement("int-jms:outbound-channel-adapter")

        outboundAdapterElement.setAttribute("destination-name", this.target)
        outboundAdapterElement.setAttribute("channel", inputChannel.name)

        outboundAdapterElement
      } else {

        val outboundGatewayElement = document.createElement("int-jms:outbound-gateway")

        outboundGatewayElement.setAttribute("request-destination-name", this.target)
        outboundGatewayElement.setAttribute("request-channel", inputChannel.name)
        if (outputChannel != null) {
          outboundGatewayElement.setAttribute("reply-channel", outputChannel.name)
        }
        outboundGatewayElement
      }
    element.setAttribute("id", this.name)
    element.setAttribute("connection-factory", connectionFactoryName)
    element
  }
}