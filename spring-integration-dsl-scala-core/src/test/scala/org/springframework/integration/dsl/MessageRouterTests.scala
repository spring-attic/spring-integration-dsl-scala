/*
 * Copyright 2002-2013 the original author or authors.
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
package org.springframework.integration.dsl

import org.junit.{Assert, Test}
import org.springframework.integration.Message
import org.springframework.core.task.SimpleAsyncTaskExecutor

/**
 * @author Oleg Zhurakousky
 * @author Soby Chacko
 */
class MessageRouterTests {

  @Test
  def validateConditionComposition(){

    val typeCondition = when(classOf[String]).andThen(Channel("hello"))
    Assert.assertTrue(typeCondition.isInstanceOf[PayloadTypeCondition])

    val valueCondition = when("foo").andThen(Channel("hello"))
    Assert.assertTrue(valueCondition.isInstanceOf[ValueCondition])
  }

  /**
   * Demonstrates PayloadTypeRouter
   */
  @Test
  def validatePayloadTypeRouterConfig(){

    val routerA =  route.onPayloadType(

      when(classOf[String]) andThen Channel("StringChannel"),
      when(classOf[Int]) andThen Channel("IntChannel")

    ).additionalAttributes(name = "myRouter")

    val targetRouter = routerA.target.asInstanceOf[Router]
    Assert.assertTrue(targetRouter.name equals "myRouter")
    // infix notation
    route onPayloadType(

      when(classOf[String]) andThen Channel("StringChannel")


    ) additionalAttributes(name = "myRouter")
  }

  /**
   * Demonstrates HeaderValueRouter
   */
  @Test
  def validateHeaderValueRouterConfig(){

    val sChannel = Channel("stringChannel")

    Channel("A") -->
    route.onValueOfHeader("someHeaderName") (

      when("foo") andThen Channel("stringChannel"),
      when("bar") andThen Channel("intChannel")
    )
  }

  /**
   * Demonstrates SpEL Router
   */
  @Test
  def validateSpELRouterConfig(){

    route("'someChannelName'")_ // if no condition

    route("'someChannelName'")(
      when(1) andThen Channel("1"),
      when(2) andThen Channel("2")
    ) additionalAttributes(name = "myRouter")

    (route ("'someChannelName'"))(
      when(1) andThen Channel("1"),
      when(2) andThen Channel("2")
    ) additionalAttributes(name = "myRouter")


  }

  /**
   * Demonstrates Function based  Router
   */
  @Test
  def validateFunctionRouterConfig(){

    route{m:Message[String] => m.getHeaders.get("routeToChannel").asInstanceOf[String]}(
      when(1) andThen Channel("1"),
      when(2) andThen Channel("2")
    ) additionalAttributes(name = "myRouter")
  }
}