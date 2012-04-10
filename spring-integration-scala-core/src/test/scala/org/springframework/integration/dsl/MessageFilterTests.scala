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
/**
 * @author Oleg Zhurakousky
 */
class MessageFilterTests {

  @Test
  def validateMessageFilterWithPayloadAndHeaders {
    val reply1 = filter.using { (_: String, headers: Map[String, _]) => headers.contains("foo") }.
      sendAndReceive[String]("Hello Scala", headers = Map("foo" -> "foo"))
    Assert.assertNotNull(reply1)
    Assert.assertEquals(reply1, "Hello Scala")
    
    val reply2 = filter.using { (_: String, headers: Map[String, _]) => headers.contains("bar") }.
      sendAndReceive[String]("Hello Scala", headers = Map("foo" -> "foo"), timeout=1000)
    Assert.assertNull(reply2)
  }
}