/*
 * Copyright 2002-2011 the original author or authors.
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
package org.springframework.integration.scala.dsl
import org.apache.log4j._
import java.util._
import java.util.concurrent.ThreadPoolExecutor._
import org.springframework.context.support._
import org.springframework.integration._
import org.springframework.beans.factory.support._
import org.springframework.integration.channel._
import org.springframework.integration.scheduling._
import org.springframework.beans._
import org.springframework.beans.factory.config._
import org.springframework.util._

/**
 * @author Oleg Zhurakousky
 *
 */
object SpringIntegrationContext {
  def apply(): SpringIntegrationContext = new SpringIntegrationContext()
}
class SpringIntegrationContext {
  private val logger = Logger.getLogger(this.getClass)
  private var componentMap: Map[IntegrationComponent, IntegrationComponent] = null
  private[dsl] var context = new GenericApplicationContext()

  def <=(e: IntegrationComponent): IntegrationComponent = {
    require(e != null)
    if (e.componentMap == null) {
      e.componentMap = new HashMap[IntegrationComponent, IntegrationComponent]
    }
    this.componentMap = e.componentMap
    if (!e.componentMap.containsKey(e)) {
      e.componentMap.put(e, null)
    }
    if (logger isDebugEnabled) {
      logger debug "Adding: " + e.componentMap + "' To: " + this
    }
    e
  }

  def init(): Unit = {
    this.preProcess
    var iterator = componentMap.keySet().iterator
    while (iterator.hasNext) {
      var recievingDescriptor = iterator.next

      if (recievingDescriptor.isInstanceOf[channel]) {
        this.buildChannel(recievingDescriptor.asInstanceOf[channel])
        recievingDescriptor.asInstanceOf[channel].underlyingContext = context
      } else {
        var consumerBuilder =
          BeanDefinitionBuilder.rootBeanDefinition("org.springframework.integration.config.ConsumerEndpointFactoryBean")

        var handlerBuilder: BeanDefinitionBuilder = null
        if (recievingDescriptor.isInstanceOf[activate]) {
          handlerBuilder = BeanDefinitionBuilder.rootBeanDefinition("org.springframework.integration.config.ServiceActivatorFactoryBean")
        } else if (recievingDescriptor.isInstanceOf[transform]) {
          handlerBuilder = BeanDefinitionBuilder.rootBeanDefinition("org.springframework.integration.config.TransformerFactoryBean")
        } else {
          throw new IllegalArgumentException("handler is not currently supported" + recievingDescriptor)
        }

        var handler = recievingDescriptor.asInstanceOf[AbstractEndpoint]

        if (handler.inputChannel.isInstanceOf[queue_channel]) {
          var poller = this.getPollerConfiguration(handler)
          var pollerBuilder =
            BeanDefinitionBuilder.rootBeanDefinition("org.springframework.integration.scheduling.PollerMetadata")
          if (poller == null) {
            context.registerBeanDefinition("org.springframework.integration.context.defaultPollerMetadata", pollerBuilder.getBeanDefinition)
          } else {
            var triggerBuilder = BeanDefinitionBuilder.genericBeanDefinition("org.springframework.scheduling.support.PeriodicTrigger")
            triggerBuilder.addConstructorArgValue(poller.fixedRate);
            triggerBuilder.addPropertyValue("fixedRate", true);
            var triggerBeanName = poller.getClass().getSimpleName + "_" + poller.hashCode
            context.registerBeanDefinition(triggerBeanName, triggerBuilder.getBeanDefinition)
            pollerBuilder.addPropertyReference("trigger", triggerBeanName)
            pollerBuilder.addPropertyValue("maxMessagesPerPoll", poller.maxMessagesPerPoll)
            consumerBuilder.addPropertyValue("pollerMetadata", pollerBuilder.getBeanDefinition)
          }
        }

        if (handler.targetObject != null) {
          if (!handler.targetObject.isInstanceOf[String]) { // Scala function
            var functionInvoker = new FunctionInvoker(handler.targetObject)
            handlerBuilder.addPropertyValue("targetObject", functionInvoker);
//            handlerBuilder.addPropertyValue("requiresReply", false);
            handlerBuilder.addPropertyValue("targetMethodName", functionInvoker.methodName);
          } else {
            handlerBuilder.addPropertyValue("expressionString", handler.targetObject);
          }
        }

        var inputChannelName = recievingDescriptor.asInstanceOf[AbstractEndpoint].inputChannel.channelName
        var outputChannel = recievingDescriptor.asInstanceOf[AbstractEndpoint].outputChannel
        if (StringUtils.hasText(inputChannelName)) {
          consumerBuilder.addPropertyValue("inputChannelName", inputChannelName)
        }
        if (outputChannel != null && StringUtils.hasText(outputChannel.channelName)) {
          handlerBuilder.addPropertyReference("outputChannel", outputChannel.channelName);
        }
        consumerBuilder.addPropertyValue("handler", handlerBuilder.getBeanDefinition)
        context.registerBeanDefinition(handler.endpointName, consumerBuilder.getBeanDefinition)
      }
    }
    context.refresh
  }

  private def getPollerConfiguration(e: AbstractEndpoint): poller = {
    if (e.configParameters != null) {
      for (val configurationParameter <- e.configParameters) {
        if (configurationParameter.isInstanceOf[poller]) {
          return configurationParameter.asInstanceOf[poller]
        }
      }
    }
    null
  }
  /*
   * 
   */
  private def buildChannel(x: channel): Unit = {
    var channelBuilder: BeanDefinitionBuilder = null

    if (x.isInstanceOf[pub_sub_channel]) {
      channelBuilder =
        BeanDefinitionBuilder.rootBeanDefinition("org.springframework.integration.channel.PublishSubscribeChannel")
      for (val configParameter <- x.configParameters) {
        if (configParameter.isInstanceOf[executor]) {
          val exectr = configParameter.asInstanceOf[executor].threadExecutor
          channelBuilder.addConstructorArg(exectr);
        }
      }
    } else if (x.isInstanceOf[queue_channel]) {
      channelBuilder =
        BeanDefinitionBuilder.rootBeanDefinition("org.springframework.integration.channel.QueueChannel")
      for (val configParameter <- x.configParameters) {
        if (configParameter.isInstanceOf[queue]) {
          val q = configParameter.asInstanceOf[queue]
          if (q.queueSize > 0) {
            channelBuilder.addConstructorArg(q.queueSize)
          }
        }
      }
    } else {
      for (val configParameter <- x.configParameters) {
        if (configParameter.isInstanceOf[executor]) {
          val exectr = configParameter.asInstanceOf[executor].threadExecutor
          channelBuilder = BeanDefinitionBuilder.rootBeanDefinition("org.springframework.integration.channel.ExecutorChannel")
          channelBuilder.addConstructorArg(exectr)
        }
      }
    }
    if (channelBuilder == null) {
      channelBuilder =
        BeanDefinitionBuilder.rootBeanDefinition("org.springframework.integration.channel.DirectChannel")
    }
    channelBuilder.addPropertyValue("componentName", x.channelName)
    context.registerBeanDefinition(x.channelName, channelBuilder.getBeanDefinition)
  }

  private def preProcess() {

    // taskScheduler
    var schedulerBuilder = BeanDefinitionBuilder
      .genericBeanDefinition("org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler");
    schedulerBuilder.addPropertyValue("poolSize", 10);
    schedulerBuilder.addPropertyValue("threadNamePrefix", "task-scheduler-");
    schedulerBuilder.addPropertyValue("rejectedExecutionHandler", new CallerRunsPolicy());
    var errorHandlerBuilder = BeanDefinitionBuilder
      .genericBeanDefinition("org.springframework.integration.channel.MessagePublishingErrorHandler");
    errorHandlerBuilder.addPropertyReference("defaultErrorChannel", "errorChannel");
    schedulerBuilder.addPropertyValue("errorHandler", errorHandlerBuilder.getBeanDefinition());

    context.registerBeanDefinition("taskScheduler", schedulerBuilder.getBeanDefinition)

    // default errorChannel
    var errorChannelBuilder =
      BeanDefinitionBuilder.rootBeanDefinition("org.springframework.integration.channel.PublishSubscribeChannel")
    context.registerBeanDefinition("errorChannel", errorChannelBuilder.getBeanDefinition)
  }

}

protected class FunctionInvoker(f: AnyRef) {
  val function = f
  var method = function.getClass.getDeclaredMethod("apply", classOf[Message[Any]])
  var returnType = method.getReturnType
  var methodName = "send"
  if (!returnType.isAssignableFrom(Void.TYPE)) {
    methodName = "sendAndRecieve"
  }

  def send(m: Message[Any]): Unit = {
    method.setAccessible(true)
    method.invoke(function, m)
  }
  def sendAndRecieve(m: Message[Any]): Any = {
    var method = function.getClass.getDeclaredMethod("apply", classOf[Message[Any]])
    method.setAccessible(true)
    method.invoke(function, m)
  }
}