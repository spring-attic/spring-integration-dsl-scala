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
import java.lang.reflect._
import org.apache.log4j._
import java.util._
import java.util.concurrent._
import org.springframework.integration._
import org.springframework.integration.channel._
import org.springframework.context._
/**
 * @author Oleg Zhurakousky
 *
 */
object IntegrationComponent {
  val name = "name"
  val queueCapacity = "queueCapacity"
  val executor = "executor"
  val poller = "poller"
  val using = "using"
    
  // POLLER Constants
  val maxMessagesPerPoll = "maxMessagesPerPoll"
  val fixedRate = "fixedRate"
  val cron = "cron"
}
//
abstract class IntegrationComponent {
  private[dsl] val logger = Logger.getLogger(this.getClass)
  
  private[dsl] val configMap = new HashMap[Any, Any]

  private[dsl] var componentMap: HashMap[IntegrationComponent, IntegrationComponent] = null
}
//
trait InitializedComponent extends IntegrationComponent {
  def >>(e: InitializedComponent*): InitializedComponent = {

    require(e.size > 0)

    for (element <- e) {
      if (this.componentMap == null) {
        this.componentMap = new HashMap[IntegrationComponent, IntegrationComponent]
      }
      if (element.componentMap == null) {
        element.componentMap = this.componentMap
      } else {
        element.componentMap.putAll(this.componentMap)
        this.componentMap = element.componentMap
      }

      val startingComponent = this.locateStartingComponent(element)
      element.componentMap.put(startingComponent, this)
      if (!element.componentMap.containsKey(this)) {
        element.componentMap.put(this, null)
      }

      if (this.isInstanceOf[AbstractEndpoint] && element.isInstanceOf[channel]) {
        // add startingComponent as output channel
        this.asInstanceOf[AbstractEndpoint].outputChannel = element.asInstanceOf[channel]
      } else if (this.isInstanceOf[channel] && startingComponent.isInstanceOf[AbstractEndpoint]) {
        // add channel as input channel to this
        startingComponent.asInstanceOf[AbstractEndpoint].inputChannel = this.asInstanceOf[channel]
      }

      if (logger isDebugEnabled) {
        logger debug "From: '" + this + "' To: " + startingComponent

        println(this.componentMap);
      }
      if (e.size == 1) {
        return element
      }
    }
    this
  }
  /*
   * 
   */
  private def locateStartingComponent(ic: IntegrationComponent): IntegrationComponent = {
    if (ic.componentMap.containsKey(ic)) {
      var c: IntegrationComponent = ic.componentMap.get(ic);
      if (c == null) {
        ic
      } else {
        locateStartingComponent(c)
      }
      //      c
    } else {
      ic
    }
  }
}
trait andName extends IntegrationComponent with using {
  def andName(componentName: String): IntegrationComponent with using = {
    this.configMap.put("andName", "andName")
    println(this.configMap)
    this
  }
}