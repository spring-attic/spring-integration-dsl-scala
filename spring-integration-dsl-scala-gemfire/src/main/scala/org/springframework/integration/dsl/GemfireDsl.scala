/*
 * Copyright 2002-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.integration.dsl
import com.gemstone.gemfire.cache.Cache
import com.gemstone.gemfire.cache.Region
import com.gemstone.gemfire.cache.EntryEvent

/**
 * @author Oleg Zhurakousky
 */
private[dsl] object GemfireDsl {
  val gemfireSchema = " http://www.springframework.org/schema/integration/gemfire " +
    "https://www.springframework.org/schema/integration/gemfire/spring-integration-gemfire.xsd"
}

class GemfireRegion(val region: Region[_, _], private val cache: Cache) {

  require(region != null, "'region' must be provided")
  require(cache != null, "'cache' must be provided")
  
  def store = new SendingIntegrationComposition(null, new GemfireOutboundConfig(target = null, region = region, cache = cache))

  def store(function: Function1[_, Map[_, _]]) = new SendingIntegrationComposition(null, new GemfireOutboundConfig(target = function, region = region, cache = cache))

  def store(function: (_, Map[String, _]) => Map[_, _]) = new SendingIntegrationComposition(null, new GemfireOutboundConfig(target = function, region = region, cache = cache))

//  def receive[V](function: Function1[EntryEvent[Any, V], Any]) = new ListeningIntegrationComposition(null, new GemfireInboundConfig(target = function, region = region)()) {
//    def on(events: GemfireEvents.EventType*) = new ListeningIntegrationComposition(null, new GemfireInboundConfig(target = function, region = region)(events: _*)) 
//  } will be re-enabled once INT-2602 is resolved
  
  def receive = new ListeningIntegrationComposition(null, new GemfireInboundConfig(region = region)()) {
    def on(events: GemfireEvents.EventType*) = new ListeningIntegrationComposition(null, new GemfireInboundConfig(region = region)(events: _*)) 
  }
}

object GemfireEvents extends Enumeration {
      type EventType = Value
      val CREATED, UPDATED, DESTROYED, INVALIDATED = Value
}

