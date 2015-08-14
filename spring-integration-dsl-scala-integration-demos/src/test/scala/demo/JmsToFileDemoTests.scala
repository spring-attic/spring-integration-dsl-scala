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
import org.junit.Assert._
import org.springframework.integration.dsl._
import org.springframework.messaging.Message
import org.junit.Ignore
import java.io.File
import org.junit.Before
import org.junit.After

/**
 * @author Oleg Zhurakousky
 */
class JmsToFileDemoTests {

  @Test
  def demo = {

	val connectionFactory =	IntegrationDemoUtils.localConnectionFactory

    val sendingFlow =
      transform{p:String => p.toUpperCase} -->
      jms.send(requestDestinationName="myQueue", connectionFactory = connectionFactory)

    val receivingFlow =
      jms.listen(requestDestinationName = "myQueue", connectionFactory = connectionFactory) -->
      handle{p:String => println("Received Message with payload: " + p);p} -->
      file.write.asFileName{_:Any => "JmsToFileDemoTests.txt"} // will write to current directory
      // use file.write("foo/bar"). . . to specify directory

    receivingFlow.start()

    sendingFlow.send("Hello JMS and File support")
    Thread.sleep(2000)
    assertTrue(new File("JmsToFileDemoTests.txt").exists)
    println("done")
  }

  @Before
  def before = {
    val file = new File("JmsToFileDemoTests.txt")
    if (file.exists()){
      file.delete()
    }
    IntegrationDemoUtils.before
  }
  @After
  def after = IntegrationDemoUtils.after

}