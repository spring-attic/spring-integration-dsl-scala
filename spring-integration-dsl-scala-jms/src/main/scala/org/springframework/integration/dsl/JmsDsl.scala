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
import org.springframework.transaction.PlatformTransactionManager
/**
 * @author Oleg Zhurakousky
 */
private[dsl] object JmsDsl {

  val jmsSchema = " http://www.springframework.org/schema/integration/jms " +
  		"https://www.springframework.org/schema/integration/jms/spring-integration-jms.xsd"

}
object jms {

  def listen(requestDestinationName: String, connectionFactory: ConnectionFactory) =
    new ListeningIntegrationComposition(null, new JmsInboundGatewayConfig(target = requestDestinationName, connectionFactory = connectionFactory, additionalAttributes = null)) {

    /**
     * Below are the docs for each attribute:
     *
     * '''correlationKey'''
     *
     * ''Provide the name of a JMS property that should be copied from the request
	 * Message to the reply Message. If NO value is provided for this attribute then the
	 * JMSMessageID from the request will be copied into the JMSCorrelationID of the reply
	 * unless there is already a value in the JMSCorrelationID property of the newly created
	 * reply Message in which case nothing will be copied. If the JMSCorrelationID of the
	 * request Message should be copied into the JMSCorrelationID of the reply Message
	 * instead, then this value should be set to "JMSCorrelationID".
	 * Any other value will be treated as a JMS String Property to be copied as-is
	 * from the request Message into the reply Message with the same property name.''
	 *
	 * '''errorFlow'''
	 *
	 * ''An instance of a SendingEndpointComposition that represents a Message flow that will be invoked in case of error ''
	 *
	 * '''subscriptionDurable'''
	 *
	 * ''Boolean property indicating whether to make the subscription durable. The durable subscription name to be used can be specified
	 * through the "durableSubscriptionName" property. Default is "false". Set this to "true" to register a durable subscription,
	 * typically in combination with a "durableSubscriptionName" value (unless your message listener class name is good enough as
	 * subscription name). Only makes sense when listening to a topic (pub-sub domain)''
     */
    def additionalAttributes(requestPubSubDomain:java.lang.Boolean = null,
    			       correlationKey:String = null,
    			       requestTimeout:java.lang.Integer = null,
    			       replyTimeout:java.lang.Integer = null,
    			       errorFlow:SendingEndpointComposition = null,
    			       transactionManager:PlatformTransactionManager = null,
    			       subscriptionDurable:java.lang.Boolean = null,
    			       durableSubscriptionName:String = null,
    			       clientId:String = null,
    			       concurrentConsumers:java.lang.Integer = null,
    			       maxConcurrentConsumers:java.lang.Integer = null,
    			       cacheLevel:CacheLevel.CacheLevel = null,
    			       maxMessagesPerTask:java.lang.Integer = null):ListeningIntegrationComposition = {

      val attributesMap = Map[String, Any]("requestPubSubDomain" -> requestPubSubDomain,
                              "correlationKey" -> correlationKey,
                              "requestTimeout" -> requestTimeout,
                              "replyTimeout" -> replyTimeout,
                              "errorFlow" -> errorFlow,
                              "transactionManager" -> transactionManager,
                              "subscriptionDurable" -> subscriptionDurable,
                              "durableSubscriptionName" -> durableSubscriptionName,
                              "clientId" -> clientId,
                              "concurrentConsumers" -> concurrentConsumers,
                              "maxConcurrentConsumers" -> maxConcurrentConsumers,
                              "cacheLevel" -> cacheLevel,
                              "maxMessagesPerTask" -> maxMessagesPerTask)
      new ListeningIntegrationComposition(null, new JmsInboundGatewayConfig(target = requestDestinationName, connectionFactory = connectionFactory, additionalAttributes = attributesMap))
    }
  }

  def sendAndReceive(requestDestinationName: String, connectionFactory: ConnectionFactory) =
    new SendingEndpointComposition(null, new JmsOutboundGatewayConfig(target = requestDestinationName, oneway = false, connectionFactory = connectionFactory))

   def send(requestDestinationName: String, connectionFactory: ConnectionFactory) =
    new SendingEndpointComposition(null, new JmsOutboundGatewayConfig(target = requestDestinationName, oneway = true, connectionFactory = connectionFactory))
}

object CacheLevel extends Enumeration {
     type CacheLevel = Value
     val CACHE_NONE, CACHE_CONNECTION, CACHE_SESSION, CACHE_CONSUMER = Value
}