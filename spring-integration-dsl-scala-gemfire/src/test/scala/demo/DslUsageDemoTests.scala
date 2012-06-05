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
package demo

import org.junit.Test
import org.mockito.Mockito
import org.springframework.data.gemfire.CacheFactoryBean
import org.springframework.integration.dsl.GemfireEvents
import org.springframework.integration.dsl.GemfireRegion
import org.springframework.integration.dsl.GemfireRegion
import org.springframework.integration.dsl.handle

import com.gemstone.gemfire.cache.Cache
import com.gemstone.gemfire.cache.EntryEvent

/**
 * @author Oleg Zhurakousky
 */
class DSLUsageDemoTests {

  @Test
  def compilationTest = {

    val region = Mockito.mock(classOf[GemfireRegion])

    region.store

    region.store { p: Any => Map() }

    region.store { p: Map[_, _] => p }

    region.store { (p: Any, h: Map[_, _]) => Map() }

    println

  }

  @Test
  def storeScalaMapMessage = {

    val region = this.createDefaultGemfireRegion

    val messageFlow = region.store

    messageFlow.send(Map("foo" -> "FOO", "bar" -> "BAR"))

    println("done");
  }

  @Test
  def storeJavaMapMessage = {

    val region = this.createDefaultGemfireRegion

    val messageFlow = region.store

    val map = new java.util.HashMap[String, String]()
    map.put("foo", "FOO")
    map.put("bar", "Bar")

    messageFlow.send(map)

    println("done");
  }

  @Test
  def defaultReceiveCompilation = {
    val region = this.createDefaultGemfireRegion

    region.receive -->
      handle { e: EntryEvent[_, _] => println(e.getNewValue()) }
    println
  }

//  @Test
//  def receiveWithExtractionCompilation = {
//    val region = this.createDefaultGemfireRegion
//
//    region.receive[String](_.getNewValue()) -->
//      handle { s: String => println(s) }
//    println
//  }

  @Test
  def simpleReceiveOnEventCompilation = {
    val region = this.createDefaultGemfireRegion

    region.receive.on(GemfireEvents.CREATED, GemfireEvents.UPDATED) -->
      handle { s: String => println(s) }
    println
  }

//  @Test
//  def receiveWithExtractionOnEventCompilation = {
//    val region = this.createDefaultGemfireRegion
//
//    region.receive[String] { _.getNewValue }.on(GemfireEvents.CREATED, GemfireEvents.UPDATED) -->
//      handle { s: String => println(s) }
//    println
//  }

  @Test
  def defaultSendReceive = {
    val region = this.createDefaultGemfireRegion

    val receivingFlow =
      region.receive -->
      handle { e: EntryEvent[_, _] => println("RECEIVING: " + e.getNewValue) }
      
    receivingFlow.start
    
    val messageFlow = region.store

    messageFlow.send(Map("foo" -> "FOO", "bar" -> "BAR"))
    
    Thread.sleep(2000)

    println
  }
  
//  @Test see INT-2602 and INTSCALA-45
//  def sendReceiveWithExtraction = {
//    val region = this.createDefaultGemfireRegion
//
//	    val receivingFlow =
//	      region.receive[String](_.getNewValue()) -->
//	      handle { v: String => println("RECEIVING: " + v) }
//      
//    receivingFlow.start
//    
//    val messageFlow = region.store
//
//    messageFlow.send(Map("foo" -> "FOO", "bar" -> "BAR"))
//    
//    Thread.sleep(2000)
//
//    println
//  }
  
  @Test
  def sendReceiveWithEvents = {
    val region = this.createDefaultGemfireRegion

    val receivingFlow =
      region.receive.on(GemfireEvents.CREATED, GemfireEvents.UPDATED) -->
      handle { e: EntryEvent[_, _] => println("RECEIVING: " + e.getNewValue) }
      
    receivingFlow.start
    
    val messageFlow = region.store

    messageFlow.send(Map("foo" -> "FOO", "bar" -> "BAR"))
    
    Thread.sleep(2000)

    println
  }

  private def createDefaultGemfireRegion: GemfireRegion = {
    val cacheFactoryBean = new CacheFactoryBean
    cacheFactoryBean.afterPropertiesSet
    val cache = cacheFactoryBean.getObject.asInstanceOf[Cache]
    val region = if (cache.getRegion("$") == null)
      cache.createRegionFactory[String, String]().create("$") else cache.getRegion("$")
    new GemfireRegion(region, cache)
  }
}