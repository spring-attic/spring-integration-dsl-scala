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
import org.junit.Assert
import org.junit.Test
import org.springframework.core.task.SimpleAsyncTaskExecutor
import org.springframework.integration.store.SimpleMessageStore
import java.util.concurrent.Executors
import org.springframework.integration.Message

/**
 * @author Oleg Zhurakousky
 */

class MessageChannelTests {
 
  @Test
  def validateDirectAndExecutorChannelConfiguration {
    var directGeneratedName = Channel()
    Assert.assertNotNull(directGeneratedName.target.name)
    
    var directWithName = Channel("myChannel")
    Assert.assertEquals("myChannel", directWithName.target.name)
    
    val channelA = Channel("myChannel")

    val targetChannelA:Channel = channelA.target.asInstanceOf[Channel]

    Assert.assertEquals("myChannel", targetChannelA.name)
    Assert.assertTrue(targetChannelA.failover)
    Assert.assertNull(targetChannelA.loadBalancer)
    Assert.assertNull(targetChannelA.taskExecutor)

    val channelC = Channel("myChannel") withDispatcher(failover = false, loadBalancer = "round-robin", taskExecutor = new SimpleAsyncTaskExecutor)

    val targetChannelC:Channel = channelC.target.asInstanceOf[Channel]

    Assert.assertEquals("myChannel", targetChannelC.name)
    Assert.assertFalse(targetChannelC.failover)
    Assert.assertEquals("round-robin", targetChannelC.loadBalancer)
    Assert.assertNotNull(targetChannelC.taskExecutor)
  }
  
  @Test
  def validatePubSubChannelConfiguration {
    val pubSubGeneratedName = PubSubChannel()
    Assert.assertNotNull(pubSubGeneratedName.target.name)
    
    val pubSubWithName = Channel("myChannel")
    Assert.assertEquals("myChannel", pubSubWithName.target.name)
    
    val pubSubWithSequence = PubSubChannel.applyingSequence
    Assert.assertTrue(pubSubWithSequence.target.asInstanceOf[PubSubChannel].applySequence)
    
    val executor = Executors.newCachedThreadPool()
    val pubSubWithExecutor = PubSubChannel.withExecutor(executor)
    Assert.assertEquals(executor, pubSubWithExecutor.target.asInstanceOf[PubSubChannel].taskExecutor)
    
    val pubSubWithExecutorAndSequence = PubSubChannel.withExecutor(executor).applyingSequence
    Assert.assertEquals(executor, pubSubWithExecutorAndSequence.target.asInstanceOf[PubSubChannel].taskExecutor)
    Assert.assertTrue(pubSubWithExecutorAndSequence.target.asInstanceOf[PubSubChannel].applySequence)
    
    val pubSubWithSequenceAndExecutor = PubSubChannel.applyingSequence.withExecutor(executor)
    Assert.assertEquals(executor, pubSubWithSequenceAndExecutor.target.asInstanceOf[PubSubChannel].taskExecutor)
    Assert.assertTrue(pubSubWithSequenceAndExecutor.target.asInstanceOf[PubSubChannel].applySequence)
    
  }

  @Test
  def validateQueueChannelConfiguration(){
   
    val queueA = Channel.withQueue
    Assert.assertNotNull(queueA.target.name)
    Assert.assertEquals(Int.MaxValue, queueA.target.asInstanceOf[PollableChannel].capacity)
    Assert.assertNotNull(queueA.target.asInstanceOf[PollableChannel].messageStore)
    
    val queueB = Channel.withQueue(3)
    Assert.assertNotNull(queueB.target.name)
    Assert.assertEquals(3, queueB.target.asInstanceOf[PollableChannel].capacity)
    Assert.assertNotNull(queueB.target.asInstanceOf[PollableChannel].messageStore)
    
    val queueC = Channel.withQueue(capacity = 3)
    Assert.assertNotNull(queueC.target.name)
    Assert.assertEquals(3, queueC.target.asInstanceOf[PollableChannel].capacity)
    Assert.assertNotNull(queueC.target.asInstanceOf[PollableChannel].messageStore)
    
    val ms = new SimpleMessageStore
    val queueD = Channel.withQueue(capacity = 3, messageStore = ms)
    Assert.assertNotNull(queueD.target.name)
    Assert.assertEquals(3, queueD.target.asInstanceOf[PollableChannel].capacity)
    Assert.assertEquals(ms, queueD.target.asInstanceOf[PollableChannel].messageStore)
  }

  @Test
  def validateMessagingBridgeComposition(){

    val channelA = Channel("channelA")

    val channelB = Channel("channelB")

    val messageBridgeComposition = channelA --> channelB

    Assert.assertTrue(messageBridgeComposition.target.isInstanceOf[Channel])
    Assert.assertEquals("channelB", messageBridgeComposition.target.asInstanceOf[Channel].name)
    Assert.assertEquals("channelA", messageBridgeComposition.parentComposition.target.asInstanceOf[Channel].name)
    Assert.assertNull(messageBridgeComposition.parentComposition.parentComposition)
  }
  
  
  @Test
  def validateMessagingBridge = {
     val flowWithMessageBridge = 
       Channel("A") --> Channel("B") --> handle.using{m:Message[_] => m}
       
     Assert.assertEquals("hello", flowWithMessageBridge.sendAndReceive[String]("hello"))
  }

}