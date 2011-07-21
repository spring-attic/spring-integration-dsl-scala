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
class aggregate extends AbstractEndpoint {
  override def toString = {
    var name = this.configMap.get(IntegrationComponent.name).asInstanceOf[String]
    if (StringUtils.hasText(name)) name else "aggregate_" + this.hashCode
  }
} 
/**
 * 
 */
object aggregate {
  def apply():Composable = {
    val a = new aggregate()   
    new ComposableEndpoint(a)
  }
  
  def apply(name:String):Composable = {
    require(StringUtils.hasText(name))
    val a = aggregate.withName(name)
    new ComposableEndpoint(a)
  }
  
  def withName(componentName: String) = new aggregate() with using with andPoller{
    this.configMap.put(IntegrationComponent.name, componentName)
  }

  def using(spel: String):Composable = {
    require(StringUtils.hasText(spel))
    val a = new aggregate()
    a.configMap.put(IntegrationComponent.using, spel)
    new ComposableEndpoint(a)
  }
  
  def using(function: _ => _):Composable = {
    val a = new aggregate()
    a.configMap.put(IntegrationComponent.using, function)
    new ComposableEndpoint(a)
  }

  def withPoller(fixedRate: Int, maxMessagesPerPoll: Int) = new aggregate() with using with andName {
    this.configMap.put(IntegrationComponent.poller, Map(IntegrationComponent.maxMessagesPerPoll -> maxMessagesPerPoll, IntegrationComponent.fixedRate -> fixedRate))
  }
  def withPoller(fixedRate: Int) = new aggregate() with using with andName {
    this.configMap.put(IntegrationComponent.poller, Map(IntegrationComponent.fixedRate -> fixedRate))
  }
}