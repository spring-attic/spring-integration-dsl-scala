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
import org.springframework.integration.dsl.GemfireRegion
import com.gemstone.gemfire.cache.Cache
import org.springframework.integration.dsl.filter
import org.springframework.integration.Message
import scala.collection.JavaConversions

/**
 * @author Oleg Zhurakousky
 */
class DSLUsageDemoTests {

  @Test
  def compilationTest = {

    val region = Mockito.mock(classOf[GemfireRegion])
    
    region.store
    
    region.store{p:Any => Map()}
    
    region.store{p:Map[_,_] => p}
    
    region.store{(p:Any, h:Map[_,_]) => Map()}
    
    println
    
  }
  
  @Test
  def storeScalaMapMessage = {
    println("hello")
    val region = this.createDefaultGemfireRegion
    
    val messageFlow = region.store
      
    messageFlow.send(Map("foo" -> "FOO", "bar" -> "BAR"))
    
    println("Result: " + region.read("foo"));
  }
  
  @Test
  def storeJavaMapMessage = {
    println("hello")
    val region = this.createDefaultGemfireRegion
    
    val messageFlow = region.store
    
    val map = new java.util.HashMap[String, String]()
    map.put("foo", "FOO")
    map.put("bar", "Bar")
      
    messageFlow.send(map)
    
    println("Result: " + region.read("foo"));
  }
  
  private def createDefaultGemfireRegion:GemfireRegion = {
    val cacheFactoryBean = new CacheFactoryBean
    cacheFactoryBean.afterPropertiesSet
    val cache = cacheFactoryBean.getObject.asInstanceOf[Cache]
    val region = cache.createRegionFactory[String, String]().create("$")
    new GemfireRegion(region, cache)
  } 
}