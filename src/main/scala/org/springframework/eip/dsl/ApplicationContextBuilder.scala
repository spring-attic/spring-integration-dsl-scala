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

    val inputChannel:Channel = if (composition.parentComposition == null) {
      null
    }
    else {
      composition.parentComposition.target match {
        case ch:Channel => {
          ch
        }
        case _ => {
          Channel("$ch_" + UUID.randomUUID().toString.substring(0,8))
        }
      }
    }
    // TODO CREATE CHANNEL

    val nextOutputChannel = composition.target match {
      case ch:Channel => {
        ch
      }
      case _ => {
        // TODO CREATE ENDPOINT (with input/output-channel)
        println(inputChannel.name + " --> " + composition.target + (if (outputChannel != null) (" --> " + outputChannel.name) else ""))
        inputChannel
      }
    }

    if (composition.parentComposition != null){
      this.init(composition.parentComposition, nextOutputChannel)
    }
  }

}