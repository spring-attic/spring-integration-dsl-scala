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

/**
 * @author Oleg Zhurakousky
 */
private[dsl] object ApplicationContextBuilder {
  /**
   *
   */
  def build(parentContext:ApplicationContext, compositions:CompletableEIPConfigurationComposition*):ApplicationContext= {
    val applicationContext = new GenericApplicationContext()

    if (parentContext != null) {
      applicationContext.setParent(parentContext)
    }

    for (composition <- compositions){
      this.init(composition.asInstanceOf[EIPConfigurationComposition], null)
    }
    applicationContext
  }

  /**
   *
   */
  private def init(composition:EIPConfigurationComposition, outputChannel:Channel):Unit = {

    val inputChannel:Channel = this.determineInputChannel(composition)
    if (inputChannel != null){
      // TODO: CREATE CHANNEL in BeanFactory
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
          println(inputChannel.name + " --> " + composition.target + (if (outputChannel != null) (" --> " + outputChannel.name) else ""))
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
        case _ => null
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
}

//  private def buildChannel(x: AbstractChannel): Unit = {
//    var channelBuilder: BeanDefinitionBuilder = null
//    x.underlyingContext = context
//    x match {
//      case psChannel: pub_sub_channel => {
//        channelBuilder =
//          BeanDefinitionBuilder.rootBeanDefinition(classOf[PublishSubscribeChannel])
//        if (psChannel.configMap.containsKey(IntegrationComponent.executor)) {
//          channelBuilder.addConstructorArg(psChannel.configMap.get(IntegrationComponent.executor));
//        }
//        if (psChannel.configMap.containsKey("applySequence")) {
//          channelBuilder.addPropertyValue("applySequence", psChannel.configMap.get("applySequence"));
//        }
//      }
//      case _ =>
//      {
//        if (x.configMap.containsKey(IntegrationComponent.queueCapacity)) {
//          channelBuilder =
//            BeanDefinitionBuilder.rootBeanDefinition(classOf[QueueChannel])
//          var queueCapacity: Int = x.configMap.get(IntegrationComponent.queueCapacity).asInstanceOf[Int]
//          if (queueCapacity > 0) {
//            channelBuilder.addConstructorArg(queueCapacity)
//          }
//        } else if (x.configMap.containsKey(IntegrationComponent.executor)) {
//          channelBuilder = BeanDefinitionBuilder.rootBeanDefinition(classOf[ExecutorChannel])
//          channelBuilder.addConstructorArg(x.configMap.get(IntegrationComponent.executor))
//        } else {
//          channelBuilder =
//            BeanDefinitionBuilder.rootBeanDefinition(classOf[DirectChannel])
//        }
//      }
//    }
//    channelBuilder.addPropertyValue(IntegrationComponent.name, x.configMap.get(IntegrationComponent.name))
//    context.registerBeanDefinition(x.configMap.get(IntegrationComponent.name).asInstanceOf[String], channelBuilder.getBeanDefinition)
//  }
