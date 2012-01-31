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
import org.springframework.integration.router.{PayloadTypeRouter, HeaderValueRouter}
import java.util.{HashMap, UUID}
import java.lang.IllegalStateException
import org.springframework.integration.config._
import org.springframework.integration.aggregator.{AggregatingMessageHandler, DefaultAggregatingMessageGroupProcessor}
import collection.JavaConversions
import org.springframework.integration.support.MessageBuilder

/**
 * @author Oleg Zhurakousky
 */
private[dsl] object ApplicationContextBuilder {

  private val logger = Logger.getLogger(this.getClass)
  

  /**
   *
   */
  def build(parentContext:ApplicationContext,
            compositions:(EIPConfigurationComposition with CompletableEIPConfigurationComposition)*):ApplicationContext= {
    implicit val applicationContext = new GenericApplicationContext()

    if (parentContext != null) {
      applicationContext.setParent(parentContext)
    }
    //TODO make it conditional based on what may already be registered in parent
    this.preProcess(applicationContext)

    if (compositions.size > 0){
      this.init(compositions(0), null)
    }
    applicationContext.refresh()
    logger.info("\n*** Spring Integration Message Flow composition was initialized successfully ***\n")
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
              if (logger.isTraceEnabled) {
                logger.trace("[" + inputChannel.name +  " --> bridge --> " + composition.target.asInstanceOf[Channel].name + "]")
              }
            }
            case poller:Poller => {
              if (logger.isTraceEnabled){
                logger.trace("[" + composition.parentComposition.parentComposition.target.asInstanceOf[Channel].name
                  +  " --> pollable bridge --> " + composition.target.asInstanceOf[Channel].name + "]")
              }
            }
            case _ =>
          }
        }
        case endpoint:Endpoint => {
          composition.parentComposition.target match {
            case poller:Poller => {
              if (logger.isTraceEnabled){
                logger.trace("[" + inputChannel.name + " --> Polling(" + composition.target + ")" +
                  (if (outputChannel != null) (" --> " + outputChannel.name) else "") + "]")
              }
              this.wireEndpoint(endpoint, inputChannel, (if (outputChannel != null) outputChannel else null), poller)
            }
            case _ => {
              if (logger.isTraceEnabled){
                logger.trace("[" + inputChannel.name + " --> " + composition.target +
                  (if (outputChannel != null) (" --> " + outputChannel.name) else "") + "]")
              }

              this.wireEndpoint(endpoint, inputChannel, (if (outputChannel != null) outputChannel else null))
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
        case _ => throw new IllegalStateException("Unrecognized component " + composition)
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
        if (logger.isDebugEnabled){
          logger.debug("Creating DirectChannel from: " + channelDefinition)
        }
        val builder = BeanDefinitionBuilder.rootBeanDefinition(classOf[DirectChannel])
        builder
      }
      else if (channelDefinition.capacity > Integer.MIN_VALUE){
        if (logger.isDebugEnabled){
          logger.debug("Creating QueueChannel from: " + channelDefinition)
        }
        val builder = BeanDefinitionBuilder.rootBeanDefinition(classOf[QueueChannel])
        builder.addConstructorArgValue(channelDefinition.capacity)
        builder
      }
      else if (channelDefinition.taskExecutor != null){
        if (logger.isDebugEnabled){
          logger.debug("Creating ExecutorChannel from: " + channelDefinition)
        }
        val builder = BeanDefinitionBuilder.rootBeanDefinition(classOf[ExecutorChannel])
        builder.addConstructorArgValue(channelDefinition.taskExecutor)
        builder
      }
      else {
        throw new IllegalArgumentException("Unsupported Channel type: " + channelDefinition)
      }

    applicationContext.registerBeanDefinition(channelDefinition.name, channelBuilder.getBeanDefinition)
  }

  /**
   *
   */
  private def wireEndpoint(endpoint: Endpoint, inputChannel:Channel,  outputChannel:Channel, poller:Poller = null)
                          (implicit applicationContext:GenericApplicationContext) {

    if (endpoint.isInstanceOf[Router] && outputChannel != null){
       print()
    }

    if (logger.isDebugEnabled){
      logger.debug("Creating " + endpoint)
    }
    val consumerBuilder =
      BeanDefinitionBuilder.rootBeanDefinition(classOf[ConsumerEndpointFactoryBean])
    var handlerBuilder = this.getHandlerDefinitionBuilder(endpoint, outputChannel)

    consumerBuilder.addPropertyValue("inputChannelName", inputChannel.name)

    if (poller != null) {
      this.configurePoller(endpoint, poller, consumerBuilder)
    }

    if (outputChannel != null) {
      endpoint match {
        case rt:Router => {
          handlerBuilder.addPropertyReference("defaultOutputChannel", outputChannel.name);
        }
        case _ => {
          handlerBuilder.addPropertyReference("outputChannel", outputChannel.name);
        }
      }
    }

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

  /**
   *
   */
  private def configurePoller(endpoint: Endpoint, pollerConfig:Poller,  consumerBuilder: BeanDefinitionBuilder)
                             (implicit applicationContext:GenericApplicationContext) = {
    if (logger.isDebugEnabled){
      logger debug "Creating Polling consumer using " + pollerConfig
    }
    var pollerBuilder =
      BeanDefinitionBuilder.rootBeanDefinition(classOf[PollerMetadata])

      var triggerBuilder = BeanDefinitionBuilder.genericBeanDefinition(classOf[PeriodicTrigger])
      if (pollerConfig.fixedRate > Integer.MIN_VALUE) {
        triggerBuilder.addConstructorArgValue(pollerConfig.fixedRate);
        triggerBuilder.addPropertyValue("fixedRate", true);
      }

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
  private def getHandlerDefinitionBuilder(endpoint: Endpoint, outputChannel:Channel = null)
                           (implicit applicationContext:GenericApplicationContext): BeanDefinitionBuilder = {
    var handlerBuilder: BeanDefinitionBuilder = null

    endpoint match {
      case sa: ServiceActivator => {
        handlerBuilder = BeanDefinitionBuilder.rootBeanDefinition(classOf[ServiceActivatorFactoryBean])
        this.defineHandlerTarget(sa, handlerBuilder)
      }
      case xfmr: Transformer => {
        handlerBuilder = BeanDefinitionBuilder.rootBeanDefinition(classOf[TransformerFactoryBean])
        this.defineHandlerTarget(xfmr, handlerBuilder)
      }
      case router: Router => {
        val conditionCompositions = router.compositions
        if (conditionCompositions.size > 0) {
          handlerBuilder = conditionCompositions(0) match {
            case hv:ValueConditionComposition => {
              if (router.headerName != null){
                if (logger.isDebugEnabled){
                  logger.debug("Router is HeaderValueRouter")
                }
                BeanDefinitionBuilder.rootBeanDefinition(classOf[HeaderValueRouter])
              }
              else {
                if (logger.isDebugEnabled){
                  logger.debug("Router is MethodInvoking")
                }
                val hBuilder = BeanDefinitionBuilder.rootBeanDefinition(classOf[RouterFactoryBean])
                this.defineHandlerTarget(router, hBuilder)
                hBuilder
              }
            }
            case pt:PayloadTypeConditionComposition => {
              if (logger.isDebugEnabled){
                logger.debug("Router is PayloadTypeRouter")
              }
              BeanDefinitionBuilder.rootBeanDefinition(classOf[PayloadTypeRouter])
            }
            case _ => throw new IllegalStateException("Unrecognized Router type: " + conditionCompositions(0))
          }
        }

        if (StringUtils.hasText(router.headerName)){
          handlerBuilder.addConstructorArgValue(router.headerName)
        }

        val channelMappings = new HashMap[Any,  Any]()

        for(conditionComposition <- conditionCompositions){
          conditionComposition.target match {
            case hv:ValueCondition => {
              var starting  = hv.composition.getStartingComposition()
              starting match {
                case ch:Channel => {

                  //val compositionToInitialize =
                  //this.init(new SimpleComposition(hv.composition.parentComposition, hv.composition.target), null)
                  this.init(hv.composition, null)
                  channelMappings.put(hv.headerValue, ch.name)
                }
                case _ => {
                  val normalizedComp = hv.composition.normalizeComposition()
                  val parentComposition = new SimpleComposition(normalizedComp.parentComposition, normalizedComp.target)

                  this.init(parentComposition --> new SimpleComposition(null, outputChannel) , null)
                  channelMappings.put(hv.headerValue, normalizedComp.getStartingComposition().target.asInstanceOf[Channel].name)
                }
              }
            }
            case pt:PayloadTypeCondition => {
              var starting  = pt.composition.getStartingComposition()
              starting match {
                case ch:Channel => {
                  this.init(new SimpleComposition(pt.composition.parentComposition, pt.composition.target), null)
                  channelMappings.put(pt.payloadType.getName, ch.name)
                }
                case _ => {
                  val normalizedComp = pt.composition.normalizeComposition()
                  this.init(new SimpleComposition(normalizedComp.parentComposition, normalizedComp.target), null)
                  channelMappings.put(pt.payloadType.getName, normalizedComp.getStartingComposition().target.asInstanceOf[Channel].name)
                }
              }
            }
            case _ =>
          }
        }
        handlerBuilder.addPropertyValue("channelMappings", channelMappings)
      }
      case fltr: MessageFilter => {
        handlerBuilder = BeanDefinitionBuilder.rootBeanDefinition(classOf[FilterFactoryBean])
        if (fltr.exceptionOnRejection) {
          handlerBuilder.addPropertyValue("throwExceptionOnRejection", fltr.exceptionOnRejection)
        }
        this.defineHandlerTarget(fltr, handlerBuilder)
      }
      case splitter: MessageSplitter => {
        handlerBuilder = BeanDefinitionBuilder.rootBeanDefinition(classOf[SplitterFactoryBean])
        this.defineHandlerTarget(splitter, handlerBuilder)
      }
      case aggregator: MessageAggregator => {
        handlerBuilder = BeanDefinitionBuilder.rootBeanDefinition(classOf[AggregatingMessageHandler])
        val processorBuilder = BeanDefinitionBuilder.genericBeanDefinition(classOf[DefaultAggregatingMessageGroupProcessor]);
        handlerBuilder.addConstructorArgValue(processorBuilder.getBeanDefinition());
      }
      case _ => {
        throw new IllegalArgumentException("handler is not currently supported: " + endpoint)
      }
    }
    handlerBuilder
  }

  /**
   *
   */
  private def defineHandlerTarget(endpoint: SimpleEndpoint, handlerBuilder: BeanDefinitionBuilder) = {

    endpoint.target match {
      case function: Function[_, _] => {
        val functionInvoker = new FunctionInvoker(function, endpoint)
        handlerBuilder.addPropertyValue("targetObject", functionInvoker);
        handlerBuilder.addPropertyValue("targetMethodName", functionInvoker.methodName);
      }
      case spel: String => {
        handlerBuilder.addPropertyValue("expressionString", spel);
      }
      case _ => {
        throw new IllegalArgumentException("Unsupported value for 'target' - " + endpoint.target)
      }
    }
  }

  private[dsl] final class FunctionInvoker(val f: Function[_, _], endpoint:Endpoint) {
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
      this.normalizeResult[Object](method.invoke(f, m))
    }
    def sendPayloadAndReceiveMessage(m: Object): Message[_] = {
      var method = f.getClass.getDeclaredMethod("apply", classOf[Any])
      method.setAccessible(true)
      this.normalizeResult[Message[_]](method.invoke(f, m).asInstanceOf[Message[_]])
    }
    def sendMessageAndReceivePayload(m: Message[_]): Object = {
      var method = f.getClass.getDeclaredMethod("apply", classOf[Any])
      method.setAccessible(true)
      this.normalizeResult[Object](method.invoke(f, m))
    }
    def sendMessageAndReceiveMessage(m: Message[_]): Message[_] = {
      var method = f.getClass.getDeclaredMethod("apply", classOf[Any])
      method.setAccessible(true)

      this.normalizeResult[Message[_]](method.invoke(f, m).asInstanceOf[Message[_]])
    }
    
    private def normalizeResult[T](result:Any):T = {
      endpoint match {
        case splitter:MessageSplitter => {
          result match {
            case message:Message[_] => {
              val payload = message.getPayload
              if (payload.isInstanceOf[Iterable[_]]){
                MessageBuilder.withPayload(JavaConversions.asJavaCollection(payload.asInstanceOf[Iterable[_]])).
                  copyHeaders(message.getHeaders).build().asInstanceOf[T]
              }
              else {
                message.asInstanceOf[T]
              }
            }
            case _ => {
              if (result.isInstanceOf[Iterable[_]]){
                JavaConversions.asJavaCollection(result.asInstanceOf[Iterable[_]]).asInstanceOf[T]
              }
              else {
                result.asInstanceOf[T]
              }
            }
          }
        }
        case _ => {
          result.asInstanceOf[T]
        }
      }
      
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


