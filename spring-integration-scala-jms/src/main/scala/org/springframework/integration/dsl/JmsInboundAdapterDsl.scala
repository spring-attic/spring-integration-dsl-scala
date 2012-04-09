package org.springframework.integration.dsl
import org.springframework.beans.factory.support.BeanDefinitionBuilder
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils
import org.springframework.beans.factory.support.BeanDefinitionRegistry
import org.springframework.integration.jms.ChannelPublishingJmsMessageListener
import org.springframework.integration.jms.JmsMessageDrivenEndpoint
import org.springframework.jms.listener.DefaultMessageListenerContainer
import javax.jms.ConnectionFactory
import java.util.UUID

object jms {
  
  def listen(requestDestinationName:String, connectionFactory:ConnectionFactory) = 
		  	new ListeningIntegrationComposition(null, new JmsInboundAdapter(target = requestDestinationName, connectionFactory = connectionFactory)) 
  
  
}

private[dsl] class JmsInboundAdapter(name: String = "$jms_in_" + UUID.randomUUID().toString.substring(0, 8),
  								     target: String,
  								     val connectionFactory:ConnectionFactory) extends InboundMessageSource(name, target) {
  
  override def build(beanDefinitionRegistry: BeanDefinitionRegistry,
                     requestChannelName: String): BeanDefinitionBuilder = {
    
    val mlcBuilder = BeanDefinitionBuilder.rootBeanDefinition(classOf[DefaultMessageListenerContainer])
    mlcBuilder.addPropertyValue("destinationName", this.target)
    mlcBuilder.addPropertyValue("connectionFactory", this.connectionFactory)
    mlcBuilder.addPropertyValue("autoStartup", false);
    val mlcName =  BeanDefinitionReaderUtils.registerWithGeneratedName(mlcBuilder.getBeanDefinition(), beanDefinitionRegistry)
    
    val cpmlBuilder = BeanDefinitionBuilder.rootBeanDefinition(classOf[ChannelPublishingJmsMessageListener])
    cpmlBuilder.addPropertyReference("requestChannel", requestChannelName)
    cpmlBuilder.addPropertyValue("expectReply", true)
   
    val cpmlName =  BeanDefinitionReaderUtils.registerWithGeneratedName(cpmlBuilder.getBeanDefinition(), beanDefinitionRegistry)
    
    val mdeBuilder = BeanDefinitionBuilder.rootBeanDefinition(classOf[JmsMessageDrivenEndpoint])
    mdeBuilder.addConstructorArgReference(mlcName);
	mdeBuilder.addConstructorArgReference(cpmlName);
		
    mdeBuilder
  }
}
