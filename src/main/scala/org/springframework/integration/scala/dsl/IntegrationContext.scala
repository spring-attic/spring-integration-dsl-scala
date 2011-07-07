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
import java.util.concurrent.ThreadPoolExecutor._
import scala.collection.mutable.ListBuffer
import scalaz._
import Scalaz._
import IntegrationComponent._
import org.apache.log4j._
import org.springframework.context._
import org.springframework.context.support._
import org.springframework.beans.factory.support._
import org.springframework.scheduling.support._
import org.springframework.scheduling.concurrent._
import org.springframework.integration.channel._
import org.springframework.integration.config._
import org.springframework.integration.context._
import org.springframework.integration.aggregator._
import org.springframework.integration.scheduling._

/**
 * @author Oleg Zhurakousky
 *
 */
object IntegrationContext {
  def apply(compositions: Kleisli[Responder, ListBuffer[Any], ListBuffer[Any]]*): IntegrationContext = new IntegrationContext(null, compositions:_*)
  def apply(parentContext: ApplicationContext, compositions: Kleisli[Responder, ListBuffer[Any], ListBuffer[Any]]*): IntegrationContext = 
	  	new IntegrationContext(parentContext, compositions:_*)
}
/**
 * 
 */
class IntegrationContext(parentContext: ApplicationContext, compositions: Kleisli[Responder, ListBuffer[Any], ListBuffer[Any]]*) {
  private val logger = Logger.getLogger(this.getClass)
  private[dsl] var context = new GenericApplicationContext()

  val compositionBuffer = new ListBuffer[Any]
  for (composition <- compositions){
    composition.apply(compositionBuffer).respond(r => r)
    process(null, compositionBuffer)
  }

  this.preProcess
  context.refresh

  /*
   * 
   */
  private def process(from: IntegrationComponent, lb: ListBuffer[Any]) {

    val endpoints = new ListBuffer[AbstractEndpoint]
    var _from = from

    for (compositionElement <- lb) {

      compositionElement match {
        case lBuf: ListBuffer[Any] => {

          process(_from, lBuf)

        }
        case toChannel: AbstractChannel => {
          this.ensureComponentIsNamed(toChannel)
          this.buildChannel(toChannel)
          if (_from != null) {
            _from match {
              case ep: AbstractEndpoint => {
                ep.outputChannel = toChannel
              }
            }
          }

          logWiring(_from, toChannel)
          
          _from = toChannel
        }
        case toEndpoint: AbstractEndpoint => {
          this.ensureComponentIsNamed(toEndpoint)
          endpoints += toEndpoint
          _from match {
            case ep: AbstractEndpoint => {
              val anonChannel = channel("anonChannel_" + ep.hashCode)
              this.buildChannel(anonChannel)
              toEndpoint.inputChannel = anonChannel
              ep.outputChannel = anonChannel
              logWiring(ep, anonChannel)
              logWiring(anonChannel, toEndpoint)
            }
            case ch: AbstractChannel => {
              toEndpoint.inputChannel = ch
              logWiring(_from, toEndpoint)
         
            }
          }
          
          _from = toEndpoint
        }
      }
    }
    
    for (endpoint <- endpoints){
      logger.debug("Building: " + endpoint + "(in:" + endpoint.asInstanceOf[AbstractEndpoint].inputChannel + ", out:" + endpoint.asInstanceOf[AbstractEndpoint].outputChannel + ")")
      this.wireEndpoint(endpoint)
    }
  }
  /*
   * 
   */
  private def wireEndpoint(endpoint: AbstractEndpoint) {
    val consumerBuilder =
      BeanDefinitionBuilder.rootBeanDefinition(classOf[ConsumerEndpointFactoryBean])
    var handlerBuilder = this.getHandlerDefinitionBuilder(endpoint)

    if (endpoint.inputChannel.configMap.containsKey(IntegrationComponent.queueCapacity)) {
      this.configurePoller(endpoint, consumerBuilder)
    }

    this.defineHandlerTarget(endpoint, handlerBuilder)

    val inChannelName = endpoint.inputChannel.configMap.get(IntegrationComponent.name)
    consumerBuilder.addPropertyValue(IntegrationComponent.inputChannelName, inChannelName)

    if (endpoint.isInstanceOf[route]) {
      if (endpoint.outputChannel != null){
        handlerBuilder.addPropertyReference(route.defaultOutputChannel, this.resolveChannelName(endpoint.outputChannel));
      }     
    } else {
      if (endpoint.outputChannel != null){
        handlerBuilder.addPropertyReference(IntegrationComponent.outputChannel, this.resolveChannelName(endpoint.outputChannel));
      }   
    }

    consumerBuilder.addPropertyValue(IntegrationComponent.handler, handlerBuilder.getBeanDefinition)
    val consumerName = endpoint.configMap.get(IntegrationComponent.name).asInstanceOf[String]
    context.registerBeanDefinition(consumerName, consumerBuilder.getBeanDefinition)
  }
  /*
   * 
   */
  private def logWiring(from: IntegrationComponent, to: IntegrationComponent) {
    if (from == null) {
      if (logger.isDebugEnabled) {
        logger.debug("Wiring: " + to)
      }
    } else {
      if (logger.isDebugEnabled) {
        if (from.isInstanceOf[AbstractChannel]) {
          logger.debug("Wiring: " + from + " -> " + to)
        } else {
          logger.debug("Wiring: " + from + "(in:" + from.asInstanceOf[AbstractEndpoint].inputChannel + ", out:" + from.asInstanceOf[AbstractEndpoint].outputChannel + ")")
        }
      }
    }
  }
  /*
   * 
   */
  private def buildChannel(x: AbstractChannel): Unit = {
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
  private def ensureComponentIsNamed(ic: IntegrationComponent) = {
    if (ic != null && !ic.configMap.containsKey(IntegrationComponent.name)) {
      ic.configMap.put(IntegrationComponent.name, ic.getClass().getSimpleName + "_" + ic.hashCode)
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
  private def defineHandlerTarget(endpoint: AbstractEndpoint, handlerBuilder: BeanDefinitionBuilder) = {
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
  private def resolveChannelName(ch: AbstractChannel): String = {
    ch.configMap.get(IntegrationComponent.name).asInstanceOf[String]
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
}