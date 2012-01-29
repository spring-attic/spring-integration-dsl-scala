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
package org.springframework.eip.dsl

import org.springframework.context.ApplicationContext
import org.springframework.context.support.GenericApplicationContext
import java.util.UUID
import java.lang.IllegalStateException
import org.springframework.integration.config.{ServiceActivatorFactoryBean, ConsumerEndpointFactoryBean}
import org.springframework.integration.Message
import java.lang.reflect.Method
import org.apache.log4j.Logger
import org.springframework.util.StringUtils
import org.springframework.beans.factory.support.{BeanDefinitionReaderUtils, BeanDefinitionBuilder}
import org.springframework.beans.factory.config.BeanDefinitionHolder
import org.springframework.integration.scheduling.PollerMetadata
import org.springframework.scheduling.support.PeriodicTrigger
import org.springframework.integration.context.IntegrationContextUtils
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler
import java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy
import org.springframework.integration.channel._

/**
 * @author Oleg Zhurakousky
 */
private[dsl] object ApplicationContextBuilder {
  /**
   *
   */
  def build(parentContext:ApplicationContext, compositions:CompletableEIPConfigurationComposition*):ApplicationContext= {
    implicit val applicationContext = new GenericApplicationContext()

    if (parentContext != null) {
      applicationContext.setParent(parentContext)
    }
    //TODO make it conditional based on what may already be registered in parent
    this.preProcess(applicationContext)

    for (composition <- compositions){
      this.init(composition.asInstanceOf[EIPConfigurationComposition], null)
    }
    applicationContext.refresh()
    applicationContext
  }

  /**
   *
   */
  private def init(composition:EIPConfigurationComposition, outputChannel:Channel)(implicit applicationContext:GenericApplicationContext):Unit = {

    val inputChannel:Channel = this.determineInputChannel(composition)
    if (inputChannel != null){
      this.buildChannel(inputChannel)
    }

    val nextOutputChannel:Channel = this.determineNextOutputChannel(composition, inputChannel)
    
    if (composition.parentComposition != null){
      composition.target match {
        case channel:Channel => {
          composition.parentComposition.target match {
            case parentChannel:Channel => {
              println(inputChannel.name +  " --> bridge --> " + composition.target.asInstanceOf[Channel].name)
            }
            case poller:Poller => {
              println(composition.parentComposition.parentComposition.target.asInstanceOf[Channel].name
                +  " --> pollable bridge --> " + composition.target.asInstanceOf[Channel].name)
            }
            case _ =>
          }
        }
        case endpoint:Endpoint => {
          composition.parentComposition.target match {
            case poller:Poller => {
              this.wireEndpoint(endpoint, inputChannel.name, (if (outputChannel != null) outputChannel.name else null), poller)
              println(inputChannel.name + " --> Polling(" + composition.target + ")" + (if (outputChannel != null) (" --> " + outputChannel.name) else ""))
            }
            case _ => {
              this.wireEndpoint(endpoint, inputChannel.name, (if (outputChannel != null) outputChannel.name else null))
              println(inputChannel.name + " --> " + composition.target + (if (outputChannel != null) (" --> " + outputChannel.name) else ""))
            }
          }
        }
        case _ =>
      }
    }


    if (composition.parentComposition != null){
      this.init(composition.parentComposition, nextOutputChannel)
    }
  }

  private def determineInputChannel(composition:EIPConfigurationComposition):Channel = {
    val inputChannel:Channel = if (composition.parentComposition != null) {
      composition.parentComposition.target match {
        case ch:Channel => {
          ch
        }
        case endpoint:Endpoint => {
          Channel("$ch_" + UUID.randomUUID().toString.substring(0,8))
        }
        case poller:Poller => {
          composition.parentComposition.parentComposition.target.asInstanceOf[Channel]
        }
        case _ => throw new IllegalStateException("unrecognized component " + composition)
      }
    }
    else {
      null
    }
    inputChannel
  }
  /**
   *
   */
  private def determineNextOutputChannel(composition:EIPConfigurationComposition, previousInputChannel:Channel):Channel = {
    composition.target match {
      case ch:Channel => {
        ch
      }
      case _ => {
        previousInputChannel
      }
    }
  }

  private def buildChannel(channelDefinition: Channel)(implicit applicationContext:GenericApplicationContext): Unit = {

    val channelBuilder: BeanDefinitionBuilder = 
      if (channelDefinition.capacity == Integer.MIN_VALUE){   // DirectChannel
        val builder = BeanDefinitionBuilder.rootBeanDefinition(classOf[DirectChannel])
        builder
      }
      else if (channelDefinition.capacity > Integer.MIN_VALUE){
        val builder = BeanDefinitionBuilder.rootBeanDefinition(classOf[QueueChannel])
        builder.addConstructorArgValue(channelDefinition.capacity)
        builder
      }
      else if (channelDefinition.taskExecutor != null){
        val builder = BeanDefinitionBuilder.rootBeanDefinition(classOf[ExecutorChannel])
        builder.addConstructorArgValue(channelDefinition.taskExecutor)
        builder
      }
      else {
        throw new IllegalArgumentException("Unsupported Channel type: " + channelDefinition)
      }

    channelBuilder.addPropertyValue("beanName", channelDefinition.name)
    applicationContext.registerBeanDefinition(channelDefinition.name, channelBuilder.getBeanDefinition)
  }

  /**
   *
   */
  private def wireEndpoint(endpoint: Endpoint, inputChannelName:String,  outputChannelName:String, poller:Poller = null)
                          (implicit applicationContext:GenericApplicationContext) {

    val consumerBuilder =
      BeanDefinitionBuilder.rootBeanDefinition(classOf[ConsumerEndpointFactoryBean])
    var handlerBuilder = this.getHandlerDefinitionBuilder(endpoint)

    consumerBuilder.addPropertyValue("inputChannelName", inputChannelName)

    if (poller != null) {
      this.configurePoller(endpoint, poller, consumerBuilder)
    }

//    if (endpoint.isInstanceOf[route]) {
//      if (endpoint.outputChannel != null) {
//        handlerBuilder.addPropertyReference(route.defaultOutputChannel, this.resolveChannelName(endpoint.outputChannel));
//      }
//    } else {
      if (outputChannelName != null) {
        handlerBuilder.addPropertyReference("outputChannel", outputChannelName);
      }
//    }

    consumerBuilder.addPropertyValue("handler", handlerBuilder.getBeanDefinition)
    val consumerName = endpoint.name
    if (StringUtils.hasText(consumerName)){
      BeanDefinitionReaderUtils.
        registerBeanDefinition(new BeanDefinitionHolder(consumerBuilder.getBeanDefinition, consumerName), applicationContext)
    }
    else {
      BeanDefinitionReaderUtils.registerWithGeneratedName(consumerBuilder.getBeanDefinition, applicationContext)
    }
  }

  private def configurePoller(endpoint: Endpoint, pollerConfig:Poller,  consumerBuilder: BeanDefinitionBuilder)
                             (implicit applicationContext:GenericApplicationContext) = {
    var pollerBuilder =
      BeanDefinitionBuilder.rootBeanDefinition(classOf[PollerMetadata])

      var triggerBuilder = BeanDefinitionBuilder.genericBeanDefinition(classOf[PeriodicTrigger])
      if (pollerConfig.fixedRate > Integer.MIN_VALUE) {
        triggerBuilder.addConstructorArgValue(pollerConfig.fixedRate);
        triggerBuilder.addPropertyValue("fixedRate", true);
      }

      //pollerBuilder.addPropertyValue("trigger", triggerBuilder.getBeanDefinition)
    //context.registerBeanDefinition(triggerBeanName, triggerBuilder.getBeanDefinition)
      val triggerBeanName = BeanDefinitionReaderUtils.registerWithGeneratedName(triggerBuilder.getBeanDefinition, applicationContext)
      pollerBuilder.addPropertyReference("trigger", triggerBeanName)

      if (pollerConfig.maxMessagesPerPoll > Integer.MIN_VALUE) {
        pollerBuilder.addPropertyValue("maxMessagesPerPoll", pollerConfig.maxMessagesPerPoll)
      }

      consumerBuilder.addPropertyValue("pollerMetadata", pollerBuilder.getBeanDefinition)
  }

  /**
   *
   */
  private def getHandlerDefinitionBuilder(endpoint: Endpoint): BeanDefinitionBuilder = {
    var handlerBuilder: BeanDefinitionBuilder = null

    endpoint match {
      case sa: ServiceActivator => {
        handlerBuilder = BeanDefinitionBuilder.rootBeanDefinition(classOf[ServiceActivatorFactoryBean])
        this.defineHandlerTarget(sa.target, handlerBuilder)
      }
//      case xfmr: transform => {
//        handlerBuilder = BeanDefinitionBuilder.rootBeanDefinition(classOf[TransformerFactoryBean])
//      }
//      case router: route => {
//        handlerBuilder = BeanDefinitionBuilder.rootBeanDefinition(classOf[RouterFactoryBean])
//        handlerBuilder.addPropertyValue(route.ignoreChannelNameResolutionFailures, true)
//        val channelMappings = router.configMap.get(route.channelIdentifierMap)
//        if (channelMappings != null) {
//          handlerBuilder.addPropertyValue(route.channelIdentifierMap, channelMappings)
//        }
//      }
//      case fltr: filter => {
//        handlerBuilder = BeanDefinitionBuilder.rootBeanDefinition(classOf[FilterFactoryBean])
//        val errorOnRejection = fltr.configMap.get(filter.throwExceptionOnRejection)
//        if (errorOnRejection != null) {
//          handlerBuilder.addPropertyValue(filter.throwExceptionOnRejection, errorOnRejection)
//        }
//      }
//      case splitter: split => {
//        handlerBuilder = BeanDefinitionBuilder.rootBeanDefinition(classOf[SplitterFactoryBean])
//      }
//      case aggregator: aggregate => {
//        handlerBuilder = BeanDefinitionBuilder.rootBeanDefinition(classOf[CorrelatingMessageHandler])
//        val processorBuilder = BeanDefinitionBuilder.genericBeanDefinition(classOf[DefaultAggregatingMessageGroupProcessor]);
//        handlerBuilder.addConstructorArgValue(processorBuilder.getBeanDefinition());
//      }
      case _ => {
        throw new IllegalArgumentException("handler is not currently supported: " + endpoint)
      }
    }
    handlerBuilder
  }

  private def defineHandlerTarget(target: Any, handlerBuilder: BeanDefinitionBuilder) = {

    target match {
      case function: Function[_, _] => {
        var functionInvoker = new FunctionInvoker(function)
        handlerBuilder.addPropertyValue("targetObject", functionInvoker);
        handlerBuilder.addPropertyValue("targetMethodName", functionInvoker.methodName);
      }
      case spel: String => {
        handlerBuilder.addPropertyValue("expressionString", spel);
      }
      case _ => {
        throw new IllegalArgumentException("Unsupported value for 'target' - " + target)
      }
    }
  }

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

  private def preProcess(applicationContext:GenericApplicationContext) {

    // taskScheduler
    var schedulerBuilder = BeanDefinitionBuilder
      .genericBeanDefinition(classOf[ThreadPoolTaskScheduler]);
    schedulerBuilder.addPropertyValue("poolSize", 10);
    schedulerBuilder.addPropertyValue("threadNamePrefix", "task-scheduler-");
    schedulerBuilder.addPropertyValue("rejectedExecutionHandler", new CallerRunsPolicy());
    var errorHandlerBuilder = BeanDefinitionBuilder.genericBeanDefinition(classOf[MessagePublishingErrorHandler]);
    errorHandlerBuilder.addPropertyReference("defaultErrorChannel", "errorChannel");
    schedulerBuilder.addPropertyValue("errorHandler", errorHandlerBuilder.getBeanDefinition());

    applicationContext.registerBeanDefinition(IntegrationContextUtils.TASK_SCHEDULER_BEAN_NAME, schedulerBuilder.getBeanDefinition)

    // default errorChannel
    var errorChannelBuilder =
      BeanDefinitionBuilder.rootBeanDefinition(classOf[PublishSubscribeChannel])
    applicationContext.registerBeanDefinition(IntegrationContextUtils.ERROR_CHANNEL_BEAN_NAME, errorChannelBuilder.getBeanDefinition)
  }
}


