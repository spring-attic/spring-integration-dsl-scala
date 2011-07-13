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
  def apply(): aggregate with AssembledComponent = new aggregate() with AssembledComponent
  
  def apply(name:String): aggregate with AssembledComponent = {
    require(StringUtils.hasText(name))
    aggregate.withName(name)
  }
  
  def withName(componentName: String) = new aggregate() with using with andPoller with AssembledComponent{
    this.configMap.put(IntegrationComponent.name, componentName)
  }

  def using(spel: String) = new aggregate() with AssembledComponent{
    require(StringUtils.hasText(spel))
    this.configMap.put(IntegrationComponent.using, spel)
  }
  
  def using(function: _ => _) = new aggregate() with AssembledComponent{
    this.configMap.put(IntegrationComponent.using, function)
  }

  def withPoller(fixedRate: Int, maxMessagesPerPoll: Int) = new aggregate() with using with andName with AssembledComponent{
    this.configMap.put(IntegrationComponent.poller, Map(IntegrationComponent.maxMessagesPerPoll -> maxMessagesPerPoll, IntegrationComponent.fixedRate -> fixedRate))
  }
  def withPoller(fixedRate: Int) = new aggregate() with using with andName with AssembledComponent{
    this.configMap.put(IntegrationComponent.poller, Map(IntegrationComponent.fixedRate -> fixedRate))
  }
}