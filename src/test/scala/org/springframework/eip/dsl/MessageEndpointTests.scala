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
import org.springframework.scheduling.support.PeriodicTrigger
import org.springframework.core.task.{SyncTaskExecutor, SimpleAsyncTaskExecutor, AsyncTaskExecutor}

/**
 * @author Oleg Zhurakousky
 */

class MessageEndpointTests {

  @Test
  def validServiceActivatorConfigurationSyntax{

    // with Function
    handle.using{s:String => s}

    handle using {s:String => s}

    handle.using{s:String => s}.where(name = "myService")

    handle.using{s:String => s} where(name = "myService")

    handle using{s:String => s} where(name = "myService")

    // with SpEL
    handle.using("'foo'")

    handle using("'foo'")

    handle.using("'foo'").where(name = "myService")

    handle.using("'foo'") where(name = "myService")

    handle using("'foo'") where(name = "myService")

    val serviceActivator = handle.using{s:String => s}.where(name = "aService")

    Assert.assertNull(serviceActivator.parentComposition)
    Assert.assertEquals("aService", serviceActivator.target.asInstanceOf[ServiceActivator].name)

    val anotherServiceActivator =
        serviceActivator -->
        handle.using{s:String => s}.where(name = "bService") -->
        handle.using{s:String => s}.where(name = "cService")

    Assert.assertNotNull(anotherServiceActivator.parentComposition)
    Assert.assertEquals("bService", anotherServiceActivator.parentComposition.target.asInstanceOf[ServiceActivator].name)
    Assert.assertEquals("cService", anotherServiceActivator.target.asInstanceOf[ServiceActivator].name)
  }

  @Test
  def validTransformerConfigurationSyntax{
    // with Function
    transform.using{s:String => s}

    // the below is invalid (will throw compilation error) since transformer must return non-null
    //transform.using{s:String => println(s)}

    transform using {s:String => s}

    transform.using{s:String => s}.where(name = "myService")

    transform.using{s:String => s} where(name = "myService")

    transform using{s:String => s} where(name = "myService")

    // with SpEL
    // with SpEL
    transform.using("'foo'")

    transform using("'foo'")

    transform.using("'foo'").where(name = "myService")

    transform.using("'foo'") where(name = "myService")

    transform using("'foo'") where(name = "myService")

    val transformer = transform.using{s:String => s}.where(name = "aTransformer")

    Assert.assertNull(transformer.parentComposition)
    Assert.assertEquals("aTransformer", transformer.target.asInstanceOf[Transformer].name)

    val anotherTransformer =
        transformer -->
        transform.using{s:String => s}.where(name = "bTransformer") -->
        transform.using{s:String => s}.where(name = "cTransformer")

    Assert.assertNotNull(anotherTransformer.parentComposition)
    Assert.assertEquals("bTransformer", anotherTransformer.parentComposition.target.asInstanceOf[Transformer].name)
    Assert.assertEquals("cTransformer", anotherTransformer.target.asInstanceOf[Transformer].name)
  }

  @Test
  def integrationCompositionDemo: Unit = {

    val pollableChannel = Channel("pol").withQueue()



    val v =
      Channel("directChannel") -->
      handle.using{s:String => s}  -->
      pollableChannel --> poll.usingTrigger(new PeriodicTrigger(5)) -->
      transform.using{s:String => s}

    println(v)

  }
}