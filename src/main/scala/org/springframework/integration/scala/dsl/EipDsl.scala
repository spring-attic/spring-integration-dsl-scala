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
import org.springframework.integration._
import org.springframework.integration.channel._
import org.springframework.context._
/**
 * @author Oleg Zhurakousky
 *
 */
abstract class IntegrationComponent {
  private[dsl] val logger = Logger.getLogger(this.getClass)
  private[dsl] var componentMap: HashMap[IntegrationComponent, IntegrationComponent] = null

  def >>(e: IntegrationComponent*): IntegrationComponent = {
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
      if (c == null){
        ic
      } 
      else {
        locateStartingComponent(c)
      }
//      c
    } else {
      ic
    }
  }
}

/**
 *
 */
abstract class AbstractChannel(n: String, c: ConfigurationParameter*) extends IntegrationComponent {
  val channelName = if (n != null) n else this.getClass().getSimpleName + "_" + this.hashCode
  val configParameters = c
  override def toString = channelName
  private[dsl] var underlyingContext: ApplicationContext = null;

  def send(m: Message[Any]): Unit = {
    require(underlyingContext != null)
    var messageChannel = underlyingContext.getBean(channelName)
    messageChannel.asInstanceOf[MessageChannel].send(m)
  }
}

abstract class AbstractEndpoint(n: String, c: ConfigurationParameter*)(r: AnyRef) extends IntegrationComponent {
  val targetObject = r
  if (targetObject.isInstanceOf[Function[Any, Any]]) {
    logger.debug("Target object is Scala function: " + r)
  } else if (targetObject.isInstanceOf[String]) {
    logger.debug("Target Object must be SpEL '" + r + "'")
  } else {
    throw new IllegalArgumentException("Function type is not supported: " + r)
  }
  val endpointName = if (n != null) n else this.getClass().getSimpleName + "_" + this.hashCode

  private[dsl] var inputChannel: channel = null;

  private[dsl] var outputChannel: channel = null;

  private[dsl] val configParameters = c

  override def toString = endpointName
}
/**
 *
 */
class channel(n: String, c: ConfigurationParameter*) extends AbstractChannel(n, c: _*) {

}
object channel {
  def apply(): channel = new channel(null)
  def apply(n: String): channel = new channel(n)
  def apply(c: ConfigurationParameter*): channel = new channel(null, c: _*)
  def apply(n: String, c: ConfigurationParameter*): channel = new channel(n, c: _*)
}

/**
 *
 */
class queue_channel(n: String, c: ConfigurationParameter*) extends channel(n, c: _*) {
  def recieve(): Message[_] = {
    require(underlyingContext != null)
    var messageChannel = underlyingContext.getBean(channelName)
    messageChannel.asInstanceOf[QueueChannel].receive
  }
}
object queue_channel {
  def apply(): queue_channel = new queue_channel(null)
  def apply(n: String): queue_channel = new queue_channel(n)
  def apply(c: ConfigurationParameter*): queue_channel = new queue_channel(null, c: _*)
  def apply(n: String, c: ConfigurationParameter*): queue_channel = new queue_channel(n, c: _*)
}
/**
 *
 */
class pub_sub_channel(n: String, c: ConfigurationParameter*) extends channel(n, c: _*) {

}
object pub_sub_channel {
  def apply(): pub_sub_channel = new pub_sub_channel(null)
  def apply(n: String): pub_sub_channel = new pub_sub_channel(n)
  def apply(c: ConfigurationParameter*): pub_sub_channel = new pub_sub_channel(null, c: _*)
  def apply(n: String, c: ConfigurationParameter*): pub_sub_channel = new pub_sub_channel(n, c: _*)
}

/**
 * Transformer
 */
class transform(n: String, c: ConfigurationParameter*)(r: AnyRef) extends AbstractEndpoint(n, c: _*)(r) {
}
object transform {
  def apply()(r: AnyRef): transform = new transform(null)(r)
  def apply(n: String)(r: AnyRef): transform = new transform(n)(r)
  def apply(c: ConfigurationParameter*)(r: AnyRef): transform = new transform(null, c: _*)(r)
  def apply(n: String, c: ConfigurationParameter*)(r: AnyRef): transform = new transform(n, c: _*)(r)
}

/**
 * Service Activator
 */
class activate(n: String, c: ConfigurationParameter*)(r: AnyRef) extends AbstractEndpoint(n, c: _*)(r) {
}
object activate {
  def apply()(r: AnyRef): activate = new activate(null)(r)
  def apply(n: String)(r: AnyRef): activate = new activate(n)(r)
  def apply(c: ConfigurationParameter*)(r: AnyRef): activate = new activate(null, c: _*)(r)
  def apply(n: String, c: ConfigurationParameter*)(r: AnyRef): activate = new activate(n, c: _*)(r)
}