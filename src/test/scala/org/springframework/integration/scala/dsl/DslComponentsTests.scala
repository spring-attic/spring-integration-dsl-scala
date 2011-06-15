/*
 * Copyright 2002-2011 the original author or authors.
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
package org.springframework.integration.scala.dsl
import org.junit._

import java.util.concurrent._
/**
 * @author Oleg Zhurakousky
 *
 */
class DslComponentsTests{
  
  @Test
  def testChannel() {
    val anonymousDirectChannel = channel()
    assert(anonymousDirectChannel  != null)
    
    val directNamedChannel = channel("someChannel")
    assert(directNamedChannel  != null)
    
    val anonymousQueueChannel = channel(queue(5))
    assert(anonymousQueueChannel  != null)
    
    val namedQueueChannel = channel("someChannel", queue(5))
    assert(namedQueueChannel  != null)
    
    val anonymousAsyncChannelWithDefaultExecutor = channel(executor())
    assert(anonymousAsyncChannelWithDefaultExecutor  != null)
    
    val namedAsyncChannelWithDefaultExecutor = channel("someChannel", executor())
    assert(namedAsyncChannelWithDefaultExecutor  != null)
    
    val namedAsyncChannelWithProvidedExecutor = channel("someChannel", executor(Executors.newCachedThreadPool))
    assert(namedAsyncChannelWithProvidedExecutor  != null)
    
  }
  
  @Test
  def testChannelSubscriptions(){
    // identify parent ACs
    var integrationConfiguration = new SpringIntegrationContext()
   
    integrationConfiguration <= {
	    pub_sub_channel("channelB") >> (
	        transform("innerTransformerA"){"SpEL"}, 
	        
	        transform("innerTransformerB"){"SpEL"} >> 
	        channel("innerChannelA"), 
	        
	        transform("innerTransformerC"){"SpEL"} >> 
	        channel("innerChannelB")
	    )
    }

	//integrationConfiguration.init() -- issue explicit init() or
    
    //channel.send(Message) - will check if context is inited and will initialize it first
	
  }

}