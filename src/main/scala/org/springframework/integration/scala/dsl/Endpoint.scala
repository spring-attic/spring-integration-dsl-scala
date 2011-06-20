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
import org.springframework.util._
/**
 * @author Oleg Zhurakousky
 *
 */
private[dsl] class AbstractEndpoint extends IntegrationComponent {

  private[dsl] var inputChannel: channel = null;

  private[dsl] var outputChannel: channel = null;
}

/**
 * Transformer
 */
class transform extends AbstractEndpoint {
  override def toString = {
    var name = this.configMap.get(IntegrationComponent.name).asInstanceOf[String]
    if (StringUtils.hasText(name)) name else "transforml_" + this.hashCode
  }
}
object transform {
  def withName(componentName: String) = new transform() with using with andPoller {
    this.configMap.put(IntegrationComponent.name, componentName)
    println(this.configMap)
  }

  def using(usingCode: AnyRef) = new transform() with InitializedComponent{
    this.configMap.put(IntegrationComponent.using, usingCode)
    println(this.configMap)
  }

  def withPoller(fixedRate: Int, maxMessagesPerPoll: Int) = new transform() with using with andName {
    this.configMap.put(IntegrationComponent.poller, Map(IntegrationComponent.maxMessagesPerPoll -> maxMessagesPerPoll, IntegrationComponent.fixedRate -> fixedRate))
    println(this.configMap)
  }
  def withPoller(fixedRate: Int) = new transform() with using with andName {
    this.configMap.put(IntegrationComponent.poller, Map(IntegrationComponent.fixedRate -> fixedRate))
    println(this.configMap)
  }
}

/**
 * Service Activator
 */
class service extends AbstractEndpoint {
  override def toString = {
    var name = this.configMap.get(IntegrationComponent.name).asInstanceOf[String]
    if (StringUtils.hasText(name)) name else "activate_" + this.hashCode
  }
}


object service {
  def withName(componentName: String) = new service() with using with andPoller {
    require(StringUtils.hasText(componentName))
    this.configMap.put(IntegrationComponent.name, componentName)
    println(this.configMap)
  }

  def using(usingCode: AnyRef) = new service() with InitializedComponent{
    this.configMap.put(IntegrationComponent.using, usingCode)
    println(this.configMap)
  }

  def withPoller(maxMessagesPerPoll: Int, fixedRate: Int) = new service() with using with andName {
    this.configMap.put(IntegrationComponent.poller, Map(IntegrationComponent.maxMessagesPerPoll -> maxMessagesPerPoll, IntegrationComponent.fixedRate -> fixedRate))
    println(this.configMap)
  }
  def withPoller(maxMessagesPerPoll: Int, cron: String) = new service() with using with andName {
    this.configMap.put(IntegrationComponent.poller, Map(IntegrationComponent.maxMessagesPerPoll -> maxMessagesPerPoll, IntegrationComponent.cron -> cron))
    println(this.configMap)
  }
  def withPoller(cron: String) = new service() with using with andName {
    this.configMap.put(IntegrationComponent.poller, Map(IntegrationComponent.cron -> cron))
    println(this.configMap)
  }
  def withPoller(fixedRate: Int) = new service() with using with andName {
    this.configMap.put(IntegrationComponent.poller, Map(IntegrationComponent.fixedRate -> fixedRate))
    println(this.configMap)
  }
}
/**
 * ROUTER (work in progress)
 */
class route extends AbstractEndpoint {
  override def toString = {
    var name = this.configMap.get("name").asInstanceOf[String]
    if (StringUtils.hasText(name)) name else "route_" + this.hashCode
  }
}
object route {

  def withName(componentName: String) = new route() with using with andPoller {
    require(StringUtils.hasText(componentName))
    this.configMap.put(IntegrationComponent.name, componentName)
    println(this.configMap)
  }

  def using(usingCode: AnyRef) = new route() with InitializedComponent{
    this.configMap.put(IntegrationComponent.using, usingCode)
    println(this.configMap)
  }

  def withPoller(maxMessagesPerPoll: Int, fixedRate: Int) = new route() with using with andName {
    this.configMap.put(IntegrationComponent.poller, Map(IntegrationComponent.maxMessagesPerPoll -> maxMessagesPerPoll, IntegrationComponent.fixedRate -> fixedRate))
    println(this.configMap)
  }
  def withPoller(maxMessagesPerPoll: Int, cron: String) = new route() with using with andName {
    this.configMap.put(IntegrationComponent.poller, Map(IntegrationComponent.maxMessagesPerPoll -> maxMessagesPerPoll, IntegrationComponent.cron -> cron))
    println(this.configMap)
  }
  def withPoller(cron: String) = new route() with using with andName {
    this.configMap.put(IntegrationComponent.poller, Map(IntegrationComponent.cron -> cron))
    println(this.configMap)
  }
  def withPoller(fixedRate: Int) = new route() with using with andName {
    this.configMap.put(IntegrationComponent.poller, Map(IntegrationComponent.fixedRate -> fixedRate))
    println(this.configMap)
  }
}

trait andMappings extends route with using {
  def andMappings(r: AnyRef): route with using = {
    this.configMap.put("andMappings", "andMappings")
    println(this.configMap)
    this
  }
}
//trait andDefaultOutputChannel extends route with using {
//  def andDefaultOutputChannel(defaultOutputChannel: String): route with using = {
//    this.configMap.put("defaultOutputChannel", defaultOutputChannel)
//    println(this.configMap)
//    this
//  }
//}

/**
 * Common Traits
 */
trait using extends IntegrationComponent {
  def using(usingCode: AnyRef): InitializedComponent = { 
    this match {
      case act:service => {
        val activator = new service() with InitializedComponent
        activator.configMap.putAll(this.configMap)
        activator.configMap.put(IntegrationComponent.using, usingCode)
        activator
      }
      case act:transform => {
        val transformer = new transform() with InitializedComponent
        transformer.configMap.putAll(this.configMap)
        transformer.configMap.put(IntegrationComponent.using, usingCode)
        transformer
      }
      case _ => {
        throw new IllegalArgumentException("'using' trait is unsupported for this pattern: " + this)
      }
    }
  }
}
trait andPoller extends AbstractEndpoint with using with andName{
  def andPoller(maxMessagesPerPoll: Int, fixedRate: Int): AbstractEndpoint with using with andName = {
    this.configMap.put(IntegrationComponent.poller, Map(IntegrationComponent.maxMessagesPerPoll -> maxMessagesPerPoll, IntegrationComponent.fixedRate -> fixedRate))
    this
  }
  def andPoller(maxMessagesPerPoll: Int, cron: String):AbstractEndpoint with using with andName = {
    this.configMap.put(IntegrationComponent.poller, Map(IntegrationComponent.maxMessagesPerPoll -> maxMessagesPerPoll, IntegrationComponent.cron -> cron))
    this
  }
  def andPoller(cron: String): AbstractEndpoint with using with andName =  {
    this.configMap.put(IntegrationComponent.poller, Map(IntegrationComponent.cron -> cron))
    this
  }
  def andPoller(fixedRate: Int):AbstractEndpoint with using with andName = {
    this.configMap.put(IntegrationComponent.poller, Map(IntegrationComponent.fixedRate -> fixedRate))
    this
  }
}