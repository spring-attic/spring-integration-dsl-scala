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

import org.junit.{Assert, Test}
import org.springframework.integration.Message
import org.springframework.core.task.SimpleAsyncTaskExecutor
import org.springframework.eip.dsl.DSL._

/**
 * @author Oleg Zhurakousky
 * Date: 1/18/12
 */

class MessageRouterTests {

  @Test
  def validateConditionComposition(){

    val typeCondition = when(classOf[String]).then(Channel("hello"))
    Assert.assertTrue(typeCondition.isInstanceOf[PayloadTypeCondition])
    
    val valueCondition = when("foo").then(Channel("hello"))
    Assert.assertTrue(valueCondition.isInstanceOf[ValueCondition])
  }

  /**
   * Demonstrates PayloadTypeRouter
   */
  @Test
  def validatePayloadTypeRouterConfig(){

    val routerA =  route.onPayloadType(

      when(classOf[String]) then Channel("StringChannel"),
      when(classOf[Int]) then Channel("IntChannel")

    ).where(name = "myRouter")
    
    Assert.assertTrue(routerA.isInstanceOf[IntegrationComposition])
    val targetRouter = routerA.target.asInstanceOf[Router]
    Assert.assertTrue(targetRouter.name equals "myRouter")
//    Assert.assertNotNull(targetRouter.compositions)
//    Assert.assertEquals(2, targetRouter.compositions.size)

    // infix notation
    route onPayloadType(

      when(classOf[String]) then Channel("StringChannel")
       

    ) where(name = "myRouter")
  }

  /**
   * Demonstrates HeaderValueRouter
   */
  @Test
  def validateHeaderValueRouterConfig(){

    val sChannel = Channel("stringChannel")

    Channel("A") -->
    route.onValueOfHeader("someHeaderName") (

      when("foo") then Channel("stringChannel"),
      when("bar") then Channel("intChannel") 
    )
  }

  /**
   * Demonstrates SpEL Router
   */
  @Test
  def validateSpELRouterConfig(){
    
    route.using("'someChannelName'")_ // if no condition

    route.using("'someChannelName'")(
      when(1) then Channel("1"),
      when(2) then Channel("2")
    ) where(name = "myRouter")
    
    (route using("'someChannelName'"))(
      when(1) then Channel("1"),
      when(2) then Channel("2")
    ) where(name = "myRouter")
    
    
  }

  /**
   * Demonstrates Function based  Router
   */
  @Test
  def validateFunctionRouterConfig(){
    
    route.using{m:Message[_] => m.getHeaders.get("routeToChannel")}(
      when(1) then Channel("1"),
      when(2) then Channel("2")
    ) where(name = "myRouter")
  }
}