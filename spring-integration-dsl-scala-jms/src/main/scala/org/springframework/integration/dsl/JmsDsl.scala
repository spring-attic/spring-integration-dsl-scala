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
import javax.jms.ConnectionFactory
/**
 * @author Oleg Zhurakousky
 */
private[dsl] object JmsDsl {

  val jmsSchema = "http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd " +
    "http://www.springframework.org/schema/integration http://www.springframework.org/schema/integration/spring-integration.xsd " +
    "http://www.springframework.org/schema/integration/jms http://www.springframework.org/schema/integration/jms/spring-integration-jms.xsd"

}
object jms {

  def listen(requestDestinationName: String, connectionFactory: ConnectionFactory) =
    new ListeningIntegrationComposition(null, new JmsInboundGatewayConfig(target = requestDestinationName, connectionFactory = connectionFactory))

  def sendAndReceive(requestDestinationName: String, connectionFactory: ConnectionFactory) =
    new SendingEndpointComposition(null, new JmsOutboundGatewayConfig(target = requestDestinationName, oneway = false, connectionFactory = connectionFactory))

   def send(requestDestinationName: String, connectionFactory: ConnectionFactory) =
    new SendingEndpointComposition(null, new JmsOutboundGatewayConfig(target = requestDestinationName, oneway = true, connectionFactory = connectionFactory))
}