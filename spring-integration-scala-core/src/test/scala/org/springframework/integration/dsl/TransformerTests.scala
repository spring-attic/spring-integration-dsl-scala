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
package org.springframework.integration.dsl
import org.junit.{ Assert, Test }
import java.util.HashMap
/**
 * @author Oleg Zhurakousky
 */
class TransformerTests {

  @Test
  def validateTransformerWithFunctionAsTargetInvokingJavaObject {

    val service = new SimpleTransformer

    val messageFlow = transform.using { payload: String => service.transform(payload) }

    val reply = messageFlow.sendAndReceive[String]("Hello Java")
    Assert.assertEquals(reply, "HELLO JAVA")
  }

  @Test
  def validateTransformerWithNonUnitReturnTypes {
    val reply1 = transform.using { s: String => 23 }.sendAndReceive[Int]("hello")
    Assert.assertTrue(reply1.isInstanceOf[Int]) // AnyVal
    val reply2 = transform.using { s: String => "Hello" }.sendAndReceive[String]("hello")
    Assert.assertTrue(reply2.isInstanceOf[String]) // Any
    val reply3 = transform.using { s: String => new Object }.sendAndReceive[Object]("hello")
    Assert.assertTrue(reply3.isInstanceOf[Object]) // Any
    
    //transform.using { s: String => println }.sendAndReceive[Object]("hello") // should not compile
  }

  @Test
  def validateTransformerWithPayloadAndHeaders {
    val reply = transform.using { (_: String, headers: Map[String, _]) => headers.contains("foo") }.
      sendAndReceive[Boolean]("Hello Java", headers = Map("foo" -> "foo"))
    Assert.assertTrue(reply)
  }
}