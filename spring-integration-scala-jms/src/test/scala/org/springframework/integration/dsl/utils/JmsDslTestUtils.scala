package org.springframework.integration.dsl.utils
import org.springframework.jms.connection.CachingConnectionFactory

object JmsDslTestUtils {

  def localConnectionFactory = {
    val taretConnectionFactory = new org.apache.activemq.ActiveMQConnectionFactory
    taretConnectionFactory.setBrokerURL("vm://localhost")
    val connectionFactory = new CachingConnectionFactory
    connectionFactory.setTargetConnectionFactory(taretConnectionFactory)
    connectionFactory.setSessionCacheSize(10)
    connectionFactory.setCacheProducers(false)
    connectionFactory.setCacheConsumers(false)
    connectionFactory
  }
}