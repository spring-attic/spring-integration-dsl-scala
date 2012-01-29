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
import org.springframework.beans.factory.support.BeanDefinitionBuilder
import org.springframework.integration.channel.{DirectChannel, ExecutorChannel, QueueChannel}

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
              println(inputChannel.name + " --> Polling(" + composition.target + ")" + (if (outputChannel != null) (" --> " + outputChannel.name) else ""))
            }
            case _ => {
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
}


