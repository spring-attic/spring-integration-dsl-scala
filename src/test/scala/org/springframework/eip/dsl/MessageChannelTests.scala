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
import org.springframework.integration.store.SimpleMessageStore
import org.springframework.core.task.SimpleAsyncTaskExecutor
import org.junit.{Assert, Test}

/**
 * @author Oleg Zhurakousky
 */

class MessageChannelTests {
//  @Test
//  def validChannelConfigurationSyntax{
//
//    Channel("myChannel")
//     // Queue Channel
//    Channel("myChannel").withQueue
//
//    Channel("myChannel").withQueue()
//
//    Channel("myChannel") withQueue
//
//    Channel("myChannel") withQueue()
//
//    Channel("myChannel") withQueue(capacity = 2)
//
//    Channel("myChannel") withQueue(capacity = 2, messageStore = new SimpleMessageStore)
//
//    Channel("myChannel") withQueue(messageStore = new SimpleMessageStore)
//
//    Channel("myChannel") withQueue(messageStore = new SimpleMessageStore, capacity = 2)
//
//    //  Direct and Executable Channel
//    Channel("myChannel").withDispatcher(failover = false)
//
//    Channel("myChannel") withDispatcher(failover = false)
//
//    Channel("myChannel") withDispatcher(failover = false, loadBalancer = "round-robin", taskExecutor = new SimpleAsyncTaskExecutor)
//
//    Channel("myChannel") withDispatcher(taskExecutor = new SimpleAsyncTaskExecutor, failover = false)
//  }
//
//  @Test
//  def validateChannelConfiguration(){
//
//    val channelA = Channel("myChannel")
//
//    val targetChannelA:Channel = channelA.target.asInstanceOf[Channel]
//
//    Assert.assertEquals("myChannel", targetChannelA.name)
//    Assert.assertEquals(Integer.MIN_VALUE, targetChannelA.capacity)
//    Assert.assertTrue(targetChannelA.failover)
//    Assert.assertNull(targetChannelA.messageStore)
//    Assert.assertNull(targetChannelA.loadBalancer)
//    Assert.assertNull(targetChannelA.taskExecutor)
//
//    val channelB = Channel("myChannel") withQueue(capacity = 2, messageStore = new SimpleMessageStore)
//
//    val targetChannelB:Channel = channelB.target.asInstanceOf[Channel]
//
//    Assert.assertEquals("myChannel", targetChannelB.name)
//    Assert.assertEquals(2, targetChannelB.capacity)
//    Assert.assertTrue(targetChannelB.failover)
//    Assert.assertNotNull(targetChannelB.messageStore)
//    Assert.assertNull(targetChannelB.loadBalancer)
//    Assert.assertNull(targetChannelB.taskExecutor)
//
//    val channelC = Channel("myChannel") withDispatcher(failover = false, loadBalancer = "round-robin", taskExecutor = new SimpleAsyncTaskExecutor)
//
//    val targetChannelC:Channel = channelC.target.asInstanceOf[Channel]
//
//    Assert.assertEquals("myChannel", targetChannelC.name)
//    Assert.assertEquals(Integer.MIN_VALUE, targetChannelC.capacity)
//    Assert.assertFalse(targetChannelC.failover)
//    Assert.assertNull(targetChannelC.messageStore)
//    Assert.assertEquals("round-robin", targetChannelC.loadBalancer)
//    Assert.assertNotNull(targetChannelC.taskExecutor)
//  }
//
//  @Test
//  def validateMessagingBridgeComposition(){
//
//    val channelA = Channel("channelA")
//
//    val channelB = Channel("channelB")
//
//    val messageBridgeComposition = channelA --> channelB
//
//    Assert.assertTrue(messageBridgeComposition.target.isInstanceOf[Channel])
//    Assert.assertEquals("channelB", messageBridgeComposition.target.asInstanceOf[Channel].name)
//    Assert.assertEquals("channelA", messageBridgeComposition.parentComposition.target.asInstanceOf[Channel].name)
//    Assert.assertNull(messageBridgeComposition.parentComposition.parentComposition)
//  }

}