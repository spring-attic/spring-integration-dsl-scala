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
class filter extends AbstractEndpoint {
    
  override def toString = {
    var name = this.configMap.get(IntegrationComponent.name).asInstanceOf[String]
    if (StringUtils.hasText(name)) name else filter.filterPrefix + this.hashCode
  }
}
/**
 * 
 */
object filter {
  val throwExceptionOnRejection = "throwExceptionOnRejection"
  val filterPrefix =  "filter_"
    
  def withName(componentName: String) = new filter() with using with andPoller with andErrorOnRejection{
    require(StringUtils.hasText(componentName))
    this.configMap.put(IntegrationComponent.name, componentName)
  }

  def using(spel: String) = new filter() with AssembledComponent{
    require(StringUtils.hasText(spel))
    this.configMap.put(IntegrationComponent.using, spel)
  }
  
  def using(function: _ => _) = new filter() with AssembledComponent{
    this.configMap.put(IntegrationComponent.using, function)
  }

  def withPoller(maxMessagesPerPoll: Int, fixedRate: Int) = new filter() with using with andName {
    this.configMap.put(IntegrationComponent.poller, Map(IntegrationComponent.maxMessagesPerPoll -> maxMessagesPerPoll, IntegrationComponent.fixedRate -> fixedRate))
  }
  def withPoller(maxMessagesPerPoll: Int, cron: String) = new filter() with using with andName {
    require(StringUtils.hasText(cron))
    this.configMap.put(IntegrationComponent.poller, Map(IntegrationComponent.maxMessagesPerPoll -> maxMessagesPerPoll, IntegrationComponent.cron -> cron))
  }
  def withPoller(cron: String) = new filter() with using with andName {
    require(StringUtils.hasText(cron))
    this.configMap.put(IntegrationComponent.poller, Map(IntegrationComponent.cron -> cron))
  }
  def withPoller(fixedRate: Int) = new filter() with using with andName {
    this.configMap.put(IntegrationComponent.poller, Map(IntegrationComponent.fixedRate -> fixedRate))
  }
  
  trait andErrorOnRejection extends filter with using {
    def andErrorOnRejection(throwErrorOnRejection: Boolean): filter with using = {
	  this.configMap.put(throwExceptionOnRejection, throwErrorOnRejection)
	  this
	}
  }
}