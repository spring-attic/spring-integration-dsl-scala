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

object jms {

  def listen(requestDestinationName: String, connectionFactory: ConnectionFactory) =
    new ListeningIntegrationComposition(null, new JmsInboundGateway(target = requestDestinationName, connectionFactory = connectionFactory))

}

private[dsl] class JmsInboundGateway(name: String = "$jms_in_" + UUID.randomUUID().toString.substring(0, 8),
  target: String,
  val connectionFactory: ConnectionFactory) extends InboundMessageSource(name, target) {

  override def build(document: Document = null, beanInstancesToRegister:scala.collection.mutable.Map[String, Any], requestChannelName: String): Element = {
    val beansElement = document.getElementsByTagName("beans").item(0).asInstanceOf[Element]
    beansElement.setAttribute("xmlns:int-jms", "http://www.springframework.org/schema/integration/jms")
    val schemaLocation = beansElement.getAttribute("xsi:schemaLocation")
    beansElement.setAttribute("xsi:schemaLocation", "http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd " +
    "http://www.springframework.org/schema/integration http://www.springframework.org/schema/integration/spring-integration.xsd " +
    "http://www.springframework.org/schema/integration/jms http://www.springframework.org/schema/integration/jms/spring-integration-jms.xsd")

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
