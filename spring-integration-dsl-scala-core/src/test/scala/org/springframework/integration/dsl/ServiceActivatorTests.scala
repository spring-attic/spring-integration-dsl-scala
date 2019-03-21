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
import org.junit.{Assert, Test}
/**
 * @author Oleg Zhurakousky
 */
class ServiceActivatorTests {
  
  @Test
  def validateServiceActivatorWithPayloadAndHeaders {
   val reply = handle{(_:String, headers:Map[String, _]) => headers.contains("foo")}.
                            sendAndReceive[Boolean]("Hello Java", headers = Map("foo" -> "foo"))
   Assert.assertTrue(reply)
  }
  
  
  @Test
  def validateServiceActivatorWithFunctionAsTargetInvokingJavaObject {

    val service = new SimpleService
    
    val reply1 = handle{payload:String => service.echo(payload)}.sendAndReceive[String]("Hello Java")
    Assert.assertEquals(reply1, "HELLO JAVA")
    val reply2 = handle{service.echo(_:String)}.sendAndReceive[String]("Hello Java")
    Assert.assertEquals(reply2, "HELLO JAVA")
  }
  
  @Test
  def validateServiceActivatorWithPayloadAndHeadersFunction {

    val service = new SimpleService
    
    val messageFlow = handle{println(_:Any)}
  }
}