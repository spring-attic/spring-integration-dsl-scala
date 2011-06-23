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
import java.lang.reflect._
import java.util.concurrent._
import java.util.concurrent.ThreadPoolExecutor._
import org.springframework.context._
import org.springframework.context.support._
import org.springframework.integration._
import org.springframework.integration.gateway._
import org.springframework.scheduling.support._
import org.springframework.beans.factory.support._
import org.springframework.integration.channel._
import org.springframework.integration.scheduling._
import org.springframework.beans._
import org.springframework.beans.factory.config._
import org.springframework.util._
import org.springframework.integration.config._
import org.springframework.integration.context._
import org.springframework.integration.aggregator._
import org.springframework.scheduling.concurrent._

/**
 * @author Oleg Zhurakousky
 *
 */
object SpringIntegrationContext {
  def apply(components: InitializedComponent*): SpringIntegrationContext = new SpringIntegrationContext(null, components: _*)
}
/**
 *
 */
class SpringIntegrationContext(parentContext: ApplicationContext, components: InitializedComponent*) {
  private val logger = Logger.getLogger(this.getClass)
  private var componentMap: java.util.Map[IntegrationComponent, IntegrationComponent] = null
  private[dsl] var context = new GenericApplicationContext()

  require(components != null)
  
  for (integrationComponent <- components) {
    if (integrationComponent.componentMap == null) {
      integrationComponent.componentMap = new java.util.HashMap[IntegrationComponent, IntegrationComponent]
    } 
    else {
      if (this.componentMap == null) {
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
  this.init()
  
  def stop() = {
    context.destroy
  }
  /*
   * 
   */
  private def init(): Unit = {
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

          var handlerBuilder = this.getHandlerDefinitionBuilder(endpoint)

          // Determine if Polling Consumer and define poller
          if (endpoint.inputChannel.configMap.containsKey(IntegrationComponent.queueCapacity)) {
            this.configurePoller(endpoint, consumerBuilder)
          }
          // Build Target Object
          this.defineHandlerTarget(endpoint,handlerBuilder)

          this.ensureComponentIsNamed(endpoint.inputChannel)
          
          // Set Input Channel
          consumerBuilder.addPropertyValue(IntegrationComponent.inputChannelName, endpoint.inputChannel.configMap.get(IntegrationComponent.name))

          // Identify and set Output Channel
          endpoint match {
            case e: AbstractEndpoint => {
              var outputChannel = e.outputChannel
              if (outputChannel != null) {
                this.ensureComponentIsNamed(outputChannel)
                if (e.isInstanceOf[route]) {
                  handlerBuilder.addPropertyReference(route.defaultOutputChannel, outputChannel.configMap.get(IntegrationComponent.name).asInstanceOf[String]);
                } else {
                  handlerBuilder.addPropertyReference(IntegrationComponent.outputChannel, outputChannel.configMap.get(IntegrationComponent.name).asInstanceOf[String]);
                }
              }
            }
          }
          if (!endpoint.configMap.containsKey(IntegrationComponent.name)) {
            endpoint.configMap.put(IntegrationComponent.name, endpoint.getClass().getSimpleName + "_" + endpoint.hashCode)
          }
          consumerBuilder.addPropertyValue(IntegrationComponent.handler, handlerBuilder.getBeanDefinition)
          val name = endpoint.configMap.get(IntegrationComponent.name).asInstanceOf[String]
          context.registerBeanDefinition(name, consumerBuilder.getBeanDefinition)
        }
        case gw: gateway => {
          var gatewayDefinition = gateway.buildGateway(gw)
          gw.underlyingContext = context
          context.registerBeanDefinition(gw.configMap.get(IntegrationComponent.name).asInstanceOf[String], gatewayDefinition)    
        }
      }
    }
    context.refresh
  }
  /*
   * 
   */
  private def defineHandlerTarget(endpoint:AbstractEndpoint, handlerBuilder:BeanDefinitionBuilder) = {
    if (endpoint.configMap.containsKey(IntegrationComponent.using)) {
      val using = endpoint.configMap.get(IntegrationComponent.using)
      using match {
        case function: Function[_, _] => {
          var functionInvoker = new FunctionInvoker(function)
          handlerBuilder.addPropertyValue(IntegrationComponent.targetObject, functionInvoker);
          println(functionInvoker.methodName)
          handlerBuilder.addPropertyValue(IntegrationComponent.targetMethodName, functionInvoker.methodName);
        }
        case spel: String => {
          handlerBuilder.addPropertyValue(IntegrationComponent.expressionString, spel);
        }
        case _ => {
          throw new IllegalArgumentException("Unsupported value for 'using' - " + using)
        }
      }
    }
  }
  /*
   * 
   */
  private def getHandlerDefinitionBuilder(endpoint: AbstractEndpoint): BeanDefinitionBuilder = {
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
        handlerBuilder.addPropertyValue(route.ignoreChannelNameResolutionFailures, true)
        val channelMappings = router.configMap.get(route.channelIdentifierMap)
        if (channelMappings != null) {
          handlerBuilder.addPropertyValue(route.channelIdentifierMap, channelMappings)
        }
      }
      case fltr: filter => {
        handlerBuilder = BeanDefinitionBuilder.rootBeanDefinition(classOf[FilterFactoryBean])
        val errorOnRejection = fltr.configMap.get(filter.throwExceptionOnRejection)
        if (errorOnRejection != null) {
          handlerBuilder.addPropertyValue(filter.throwExceptionOnRejection, errorOnRejection)
        }
      }
      case splitter: split => {
        handlerBuilder = BeanDefinitionBuilder.rootBeanDefinition(classOf[SplitterFactoryBean])
      }
      case aggregator: aggregate => {
        handlerBuilder = BeanDefinitionBuilder.rootBeanDefinition(classOf[CorrelatingMessageHandler])
        val processorBuilder = BeanDefinitionBuilder.genericBeanDefinition(classOf[DefaultAggregatingMessageGroupProcessor]);
        handlerBuilder.addConstructorArgValue(processorBuilder.getBeanDefinition());
      }
      case _ => {
        throw new IllegalArgumentException("handler is not currently supported: " + endpoint)
      }
    }
    handlerBuilder
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
          BeanDefinitionBuilder.rootBeanDefinition(classOf[PublishSubscribeChannel])
        if (psChannel.configMap.containsKey(IntegrationComponent.executor)) {
          channelBuilder.addConstructorArg(psChannel.configMap.get(IntegrationComponent.executor));
        }
        if (psChannel.configMap.containsKey("applySequence")) {
          channelBuilder.addPropertyValue("applySequence", psChannel.configMap.get("applySequence"));
        }
      }
      case _ =>
        {
          if (x.configMap.containsKey(IntegrationComponent.queueCapacity)) {
            channelBuilder =
              BeanDefinitionBuilder.rootBeanDefinition(classOf[QueueChannel])
            var queueCapacity: Int = x.configMap.get(IntegrationComponent.queueCapacity).asInstanceOf[Int]
            if (queueCapacity > 0) {
              channelBuilder.addConstructorArg(queueCapacity)
            }
          } else if (x.configMap.containsKey(IntegrationComponent.executor)) {
            channelBuilder = BeanDefinitionBuilder.rootBeanDefinition(classOf[ExecutorChannel])
            channelBuilder.addConstructorArg(x.configMap.get(IntegrationComponent.executor))
          } else {
            channelBuilder =
              BeanDefinitionBuilder.rootBeanDefinition(classOf[DirectChannel])
          }
        }
    }
    this.ensureComponentIsNamed(x)
    channelBuilder.addPropertyValue(IntegrationComponent.name, x.configMap.get(IntegrationComponent.name))
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
     
      var triggerBeanName = IntegrationComponent.trigger + "_" + triggerBuilder.hashCode

      context.registerBeanDefinition(triggerBeanName, triggerBuilder.getBeanDefinition)
      pollerBuilder.addPropertyReference(IntegrationComponent.trigger, triggerBeanName)
      if (pollerConfig.contains(IntegrationComponent.maxMessagesPerPoll)) {
        pollerBuilder.addPropertyValue(IntegrationComponent.maxMessagesPerPoll, pollerConfig.get(IntegrationComponent.maxMessagesPerPoll).get)
      }

      consumerBuilder.addPropertyValue(IntegrationComponent.pollerMetadata, pollerBuilder.getBeanDefinition)
    } else {
      
      context.registerBeanDefinition(IntegrationContextUtils.DEFAULT_POLLER_METADATA_BEAN_NAME, pollerBuilder.getBeanDefinition)
    }
  }
  /*
   * 
   */
  private def preProcess() {

    // taskScheduler
    var schedulerBuilder = BeanDefinitionBuilder
      .genericBeanDefinition(classOf[ThreadPoolTaskScheduler]);
    schedulerBuilder.addPropertyValue("poolSize", 10);
    schedulerBuilder.addPropertyValue("threadNamePrefix", "task-scheduler-");
    schedulerBuilder.addPropertyValue("rejectedExecutionHandler", new CallerRunsPolicy());
    var errorHandlerBuilder = BeanDefinitionBuilder.genericBeanDefinition(classOf[MessagePublishingErrorHandler]);
    errorHandlerBuilder.addPropertyReference("defaultErrorChannel", "errorChannel");
    schedulerBuilder.addPropertyValue("errorHandler", errorHandlerBuilder.getBeanDefinition());

    context.registerBeanDefinition(IntegrationContextUtils.TASK_SCHEDULER_BEAN_NAME, schedulerBuilder.getBeanDefinition)

    // default errorChannel
    var errorChannelBuilder =
      BeanDefinitionBuilder.rootBeanDefinition(classOf[PublishSubscribeChannel])
    context.registerBeanDefinition(IntegrationContextUtils.ERROR_CHANNEL_BEAN_NAME, errorChannelBuilder.getBeanDefinition)
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
/**
 *
 */
private[dsl] final class FunctionInvoker(val f: Function[_, _]) {
  private val logger = Logger.getLogger(this.getClass)
  var methodName: String = ""

  var method: Method = null
  val methods = f.getClass().getDeclaredMethods()
  if (methods.size > 1) {
    for (m <- f.getClass().getDeclaredMethods()) {
      var returnType = m.getReturnType()
      val inputParameter = m.getParameterTypes()(0)
      if (!(returnType.isAssignableFrom(classOf[Object]) && inputParameter.isAssignableFrom(classOf[Object]))) {
        if (logger.isDebugEnabled) {
          logger.debug("Selecting method: " + m)
        }
        method = m
        if (returnType.isAssignableFrom(Void.TYPE) && inputParameter.isAssignableFrom(classOf[Message[_]])) {
          methodName = "sendMessage"
        } else if (returnType.isAssignableFrom(Void.TYPE) && !inputParameter.isAssignableFrom(classOf[Message[_]])) {
          methodName = "sendPayload"
        } else if (returnType.isAssignableFrom(classOf[Message[_]]) && inputParameter.isAssignableFrom(classOf[Message[_]])) {
          methodName = "sendMessageAndReceiveMessage"
        } else if (!returnType.isAssignableFrom(classOf[Message[_]]) && inputParameter.isAssignableFrom(classOf[Message[_]])) {
          methodName = "sendMessageAndReceivePayload"
        } else if (returnType.isAssignableFrom(classOf[Message[_]]) && !inputParameter.isAssignableFrom(classOf[Message[_]])) {
          methodName = "sendPayloadAndReceiveMessage"
        } else if (!returnType.isAssignableFrom(classOf[Message[_]]) && !inputParameter.isAssignableFrom(classOf[Message[_]])) {
          methodName = "sendPayloadAndReceivePayload"
        }
      }
    }
  } else {
    method = f.getClass.getDeclaredMethod("apply", classOf[Object])
    methodName = "sendPayoadAndReceive"
    if (logger.isDebugEnabled) {
      logger.debug("Selecting method: " + method)
    }
  }
  if (logger.isDebugEnabled) {
    logger.debug("FunctionInvoker method name: " + methodName)
  }
  def sendPayload(m: Object): Unit = {
    method.setAccessible(true)
    method.invoke(f, m)
  }
  def sendMessage(m: Message[_]): Unit = {
    method.setAccessible(true)
    method.invoke(f, m)
  }
  def sendPayloadAndReceivePayload(m: Object): Object = {
    var method = f.getClass.getDeclaredMethod("apply", classOf[Any])
    method.setAccessible(true)
    method.invoke(f, m)
  }
  def sendPayloadAndReceiveMessage(m: Object): Message[_] = {
    var method = f.getClass.getDeclaredMethod("apply", classOf[Any])
    method.setAccessible(true)
    method.invoke(f, m).asInstanceOf[Message[_]]
  }
  def sendMessageAndReceivePayload(m: Message[_]): Object = {
    var method = f.getClass.getDeclaredMethod("apply", classOf[Any])
    method.setAccessible(true)
    method.invoke(f, m)
  }
  def sendMessageAndReceiveMessage(m: Message[_]): Message[_] = {
    var method = f.getClass.getDeclaredMethod("apply", classOf[Any])
    method.setAccessible(true)
    method.invoke(f, m).asInstanceOf[Message[_]]
  }
}