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
import java.util.concurrent._
import java.util.concurrent.ThreadPoolExecutor._
import org.springframework.context._
import org.springframework.context.support._
import org.springframework.integration._
import org.springframework.scheduling.support._
import org.springframework.beans.factory.support._
import org.springframework.integration.channel._
import org.springframework.integration.scheduling._
import org.springframework.beans._
import org.springframework.beans.factory.config._
import org.springframework.util._
import org.springframework.integration.config._

/**
 * @author Oleg Zhurakousky
 *
 */
object SpringIntegrationContext {
  def apply(components:InitializedComponent*): SpringIntegrationContext = new SpringIntegrationContext(null, components:_*)
}
class SpringIntegrationContext(parentContext:ApplicationContext,  components:InitializedComponent*) {
  private val logger = Logger.getLogger(this.getClass)
  private var componentMap: java.util.Map[IntegrationComponent, IntegrationComponent] = null
  private[dsl] var context = new GenericApplicationContext()

  require(components != null)
  for (integrationComponent <- components) {
    if (integrationComponent.componentMap == null) {
      integrationComponent.componentMap = new java.util.HashMap[IntegrationComponent, IntegrationComponent]
    }
    else {
      if (this.componentMap == null){
        this.componentMap = integrationComponent.componentMap
      }
      else {
        this.componentMap.putAll(integrationComponent.componentMap)
      }
    }
    if (!integrationComponent.componentMap.containsKey(integrationComponent)) {
      integrationComponent.componentMap.put(integrationComponent, null)
    }
    if (logger isDebugEnabled) {
      logger debug "Adding: " + integrationComponent.componentMap + "' To: " + this
    }
  }
 
  def init(): Unit = {
    this.preProcess
    var iterator = componentMap.keySet().iterator
    while (iterator.hasNext) {
      var receivingDescriptor = iterator.next

      receivingDescriptor match {
        case ch: channel => {
          this.buildChannel(ch)
        }
        case endpoint: AbstractEndpoint => {
          val consumerBuilder =
            BeanDefinitionBuilder.rootBeanDefinition(classOf[ConsumerEndpointFactoryBean])
          var handlerBuilder: BeanDefinitionBuilder = null

          endpoint match {
            case sa: service => {
              handlerBuilder = BeanDefinitionBuilder.rootBeanDefinition(classOf[ServiceActivatorFactoryBean])
            }
            case xfmr: transform => {
              handlerBuilder = BeanDefinitionBuilder.rootBeanDefinition(classOf[TransformerFactoryBean])
            }
            case router: route => {
              handlerBuilder = BeanDefinitionBuilder.rootBeanDefinition(classOf[RouterFactoryBean])
              handlerBuilder.addPropertyValue("ignoreChannelNameResolutionFailures", true)
            }
            case _ => {
              throw new IllegalArgumentException("handler is not currently supported" + receivingDescriptor)
            }
          }

          if (endpoint.inputChannel.configMap.containsKey(IntegrationComponent.queueCapacity)) { // this means channel is Queue and we need polling consumer
            this.configurePoller(endpoint, consumerBuilder)
          }

          if (endpoint.configMap.containsKey(IntegrationComponent.using)) {
            val using = endpoint.configMap.get(IntegrationComponent.using)
            using match {
              case function: Function[_, _] => {
                var functionInvoker = new FunctionInvoker(function)
                handlerBuilder.addPropertyValue("targetObject", functionInvoker);
                handlerBuilder.addPropertyValue("targetMethodName", functionInvoker.methodName);
              }
              case spel: String => {
                handlerBuilder.addPropertyValue("expressionString", spel);
              }
              case _ => {
                throw new IllegalArgumentException("Unsupported value for 'using' - " + using)
              }
            }
          }
          this.ensureComponentIsNamed(endpoint.inputChannel)
          consumerBuilder.addPropertyValue("inputChannelName", endpoint.inputChannel.configMap.get(IntegrationComponent.name))

          endpoint match {
            case e: AbstractEndpoint => {
              var outputChannel = e.outputChannel
              if (outputChannel != null) {
                this.ensureComponentIsNamed(outputChannel)
                if (e.isInstanceOf[route]){
                  handlerBuilder.addPropertyReference("defaultOutputChannel", outputChannel.configMap.get(IntegrationComponent.name).asInstanceOf[String]);
                }
                else {
                  handlerBuilder.addPropertyReference("outputChannel", outputChannel.configMap.get(IntegrationComponent.name).asInstanceOf[String]);
                }       
              }
            }
          }
          if (!endpoint.configMap.containsKey(IntegrationComponent.name)) {
            endpoint.configMap.put(IntegrationComponent.name, endpoint.getClass().getSimpleName + "_" + endpoint.hashCode)
          }
          consumerBuilder.addPropertyValue("handler", handlerBuilder.getBeanDefinition)
          val name = endpoint.configMap.get(IntegrationComponent.name).asInstanceOf[String]
          context.registerBeanDefinition(name, consumerBuilder.getBeanDefinition)
        }
      }

    }
    context.refresh
  }
  /*
   * 
   */
  private def buildChannel(x: channel): Unit = {
    var channelBuilder: BeanDefinitionBuilder = null
    x.underlyingContext = context
    x match {
      case psChannel: pub_sub_channel => {
        channelBuilder =
          BeanDefinitionBuilder.rootBeanDefinition("org.springframework.integration.channel.PublishSubscribeChannel")
        if (psChannel.configMap.containsKey(IntegrationComponent.executor)) {
          channelBuilder.addConstructorArg(psChannel.configMap.get(IntegrationComponent.executor));
        }
      }
      case _ =>
        {
          if (x.configMap.containsKey(IntegrationComponent.queueCapacity)) {
            channelBuilder =
              BeanDefinitionBuilder.rootBeanDefinition("org.springframework.integration.channel.QueueChannel")
            var queueCapacity: Int = x.configMap.get(IntegrationComponent.queueCapacity).asInstanceOf[Int]
            if (queueCapacity > 0) {
              channelBuilder.addConstructorArg(queueCapacity)
            }
          } else if (x.configMap.containsKey(IntegrationComponent.executor)) {
            channelBuilder = BeanDefinitionBuilder.rootBeanDefinition("org.springframework.integration.channel.ExecutorChannel")
            channelBuilder.addConstructorArg(x.configMap.get(IntegrationComponent.executor))
          } else {
            channelBuilder =
              BeanDefinitionBuilder.rootBeanDefinition("org.springframework.integration.channel.DirectChannel")
          }
        }
    }
    this.ensureComponentIsNamed(x)
    channelBuilder.addPropertyValue("componentName", x.configMap.get(IntegrationComponent.name))
    context.registerBeanDefinition(x.configMap.get(IntegrationComponent.name).asInstanceOf[String], channelBuilder.getBeanDefinition)
  }
  /*
   * 
   */
  private def configurePoller(endpoint: AbstractEndpoint, consumerBuilder: BeanDefinitionBuilder) = {
    var pollerBuilder =
      BeanDefinitionBuilder.rootBeanDefinition(classOf[PollerMetadata])
    // check if poller config is provided
    if (endpoint.configMap.containsKey(IntegrationComponent.poller)) {
      var pollerConfig = endpoint.configMap.get(IntegrationComponent.poller).asInstanceOf[Map[Any, _]]

      var triggerBuilder = BeanDefinitionBuilder.genericBeanDefinition(classOf[PeriodicTrigger])
      if (pollerConfig.contains(IntegrationComponent.fixedRate)) {
        triggerBuilder.addConstructorArgValue(pollerConfig.get(IntegrationComponent.fixedRate).get);
        triggerBuilder.addPropertyValue(IntegrationComponent.fixedRate, true);
      }
      var triggerBeanName = "trtigger_" + triggerBuilder.hashCode

      context.registerBeanDefinition(triggerBeanName, triggerBuilder.getBeanDefinition)
      pollerBuilder.addPropertyReference("trigger", triggerBeanName)
      if (pollerConfig.contains(IntegrationComponent.maxMessagesPerPoll)) {
        pollerBuilder.addPropertyValue(IntegrationComponent.maxMessagesPerPoll, pollerConfig.get(IntegrationComponent.maxMessagesPerPoll).get)
      }

      consumerBuilder.addPropertyValue("pollerMetadata", pollerBuilder.getBeanDefinition)
    } else {
      context.registerBeanDefinition("org.springframework.integration.context.defaultPollerMetadata", pollerBuilder.getBeanDefinition)
    }
  }
  /*
   * 
   */
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
  /*
   * 
   */
  private def ensureComponentIsNamed(ic: IntegrationComponent) = {
    if (!ic.configMap.containsKey(IntegrationComponent.name)) {
      ic.configMap.put(IntegrationComponent.name, ic.getClass().getSimpleName + "_" + ic.hashCode)
    }
  }
}

protected class FunctionInvoker(f: Function[_, _]) {
  val function = f
  var method = function.getClass.getDeclaredMethod("apply", classOf[Message[Any]])
  var returnType = method.getReturnType
  var methodName = "send"
  if (!returnType.isAssignableFrom(Void.TYPE)) {
    methodName = "sendAndReceive"
  }

  def send(m: Message[Any]): Unit = {
    method.setAccessible(true)
    method.invoke(function, m)
  }
  def sendAndReceive(m: Message[Any]): Any = {
    var method = function.getClass.getDeclaredMethod("apply", classOf[Message[Any]])
    method.setAccessible(true)
    method.invoke(function, m)
  }
}