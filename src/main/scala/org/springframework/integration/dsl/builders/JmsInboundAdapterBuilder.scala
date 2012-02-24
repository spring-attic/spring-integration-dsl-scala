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
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils
import org.springframework.context.support.GenericApplicationContext
import org.springframework.integration.channel.QueueChannel
import org.springframework.integration.jms.ChannelPublishingJmsMessageListener
import org.springframework.integration.jms.JmsMessageDrivenEndpoint
import org.springframework.jms.listener.DefaultMessageListenerContainer
/**
 * @author Oleg Zhurakousky
 */
private object JmsInboundAdapterBuilder {
  
  def buildHandler(gateway: JmsInboundAdapter, requestChannelName: String, applicationContext:GenericApplicationContext): BeanDefinitionBuilder = {
    
    val mlcBuilder = BeanDefinitionBuilder.rootBeanDefinition(classOf[DefaultMessageListenerContainer])
    mlcBuilder.addPropertyValue("destinationName", gateway.target)
    mlcBuilder.addPropertyValue("connectionFactory", gateway.connectionFactory)
    mlcBuilder.addPropertyValue("autoStartup", false);
    val mlcName =  BeanDefinitionReaderUtils.registerWithGeneratedName(mlcBuilder.getBeanDefinition(), applicationContext)
    
    val cpmlBuilder = BeanDefinitionBuilder.rootBeanDefinition(classOf[ChannelPublishingJmsMessageListener])
    cpmlBuilder.addPropertyReference("requestChannel", requestChannelName)
    cpmlBuilder.addPropertyValue("expectReply", true)
   
    val cpmlName =  BeanDefinitionReaderUtils.registerWithGeneratedName(cpmlBuilder.getBeanDefinition(), applicationContext)
    
    val mdeBuilder = BeanDefinitionBuilder.rootBeanDefinition(classOf[JmsMessageDrivenEndpoint])
    mdeBuilder.addConstructorArgReference(mlcName);
	mdeBuilder.addConstructorArgReference(cpmlName);
		
    mdeBuilder
  }
}