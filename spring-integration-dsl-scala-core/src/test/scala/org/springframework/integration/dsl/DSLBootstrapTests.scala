///*
// * Copyright 2002-2012 the original author or authors.
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *      https://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//package org.springframework.integration.dsl
//
//import org.junit._
//import org.junit.Assert._
//import org.junit.Test
//import org.springframework.integration.dsl.utils.DslUtils
//import scala.collection.immutable.WrappedString
//import utils.DslUtils
//import org.springframework.integration.Message
//import org.springframework.integration.message.GenericMessage
///**
// * @author Oleg Zhurakousky
// */
//class DslBootstrapTests {
//  
//  @Test
//  def sendAndProcessMessageNoReply = {
//
//    val messageFlow =
//      handle.using { m: Message[_] => println(m) }
//    
//    assertTrue(messageFlow.send("hello"))
//  }
//  
//  @Test
//  def sendAndProcessPayloadNoReplyA= {
//
//    val messageFlow =
//      handle.using { m: Any => println(m) }
//
//    assertTrue(messageFlow.send("hello"))
//  }
//  
//  @Test
//  def sendAndProcessPayloadNoReplyB= {
//
//    val messageFlow =
//      handle.using { m: String => println(m) }
//
//    assertTrue(messageFlow.send("hello"))
//  }
//  
//  @Test
//  def sendAndReceive_ProcessMessageReplyWithMessageAndReceiveMessage = {
//    val messageFlow = 
//      handle.using { m: Message[_] => m }
//
//    assertTrue(messageFlow.sendAndReceive("hello").isInstanceOf[Message[_]])
//  }
//  
//  @Test
//  def sendAndReceive_ProcessMessageReplyWithMessageAndReceivePayload = {
//    val messageFlow = 
//      handle.using { m: Message[_] => m }
//
//    assertTrue(messageFlow.sendAndReceive[String]("hello").isInstanceOf[String])
//  }
//  
//  @Test
//  def sendAndReceive_ProcessMessageReplyWithPayloadAndReceiveMessage = {
//    val messageFlow = 
//      handle.using { m: Message[_] => m.getPayload }
//
//    assertTrue(messageFlow.sendAndReceive("hello").isInstanceOf[Message[_]])
//  }
//  
//  @Test
//  def sendAndReceive_ProcessMessageReplyWithPayloadAndReceivePayload = {
//    val messageFlow = 
//      handle.using { m: Message[_] => m.getPayload }
//
//    assertTrue(messageFlow.sendAndReceive[String]("hello").isInstanceOf[String])
//  }
//  
//  @Test
//  def sendAndReceive_ProcessPayloadReplyWithMessageAndReceiveMessage= {
//    val messageFlow = 
//      handle.using { m: String => new GenericMessage[String](m) }
//
//    assertTrue(messageFlow.sendAndReceive("hello").isInstanceOf[Message[_]])
//  }
//  
//  @Test
//  def sendAndReceive_ProcessPayloadReplyWithMessageAndReceivePayload = {
//    val messageFlow = 
//      handle.using { m: String => new GenericMessage[String](m) }
//
//    assertTrue(messageFlow.sendAndReceive[String]("hello").isInstanceOf[String])
//  }
//  
//  @Test
//  def sendAndReceive_ProcessPayloadReplyWithPayloadAndReceiveMessage = {
//    val messageFlow = 
//      handle.using { m: String => m }
//
//    assertTrue(messageFlow.sendAndReceive[Message[_]]("hello").isInstanceOf[Message[_]])
//  }
//  
//  @Test
//  def sendAndReceive_ProcessPayloadReplyWithPayloadAndReceivePayload = {
//    val messageFlow = 
//      handle.using { m: String => m }
//
//    assertTrue(messageFlow.sendAndReceive[String]("hello").isInstanceOf[String])
//  }
//  
//  @Test
//  def sendAndProcessMessageWithReplyPayload= {
//    val messageFlow = 
//      handle.using { m: Message[_] => m.getPayload() }
//
//    messageFlow.sendAndReceive("hello")
//    println("done")
//  }
//
//  @Test
//  def validateFlowCompositionFromSubflows = {
//
//    val messageFlowA =   
//      handle.using("messageFlowA-1") --> 
//      Channel("messageFlowA-2") -->
//      transform.using{s:String => s}.where(name="transformerA")
//  
//    val messageFlowB =
//      filter.using{s:Boolean => s}.where(name="filterB") -->
//        PubSubChannel("messageFlowB-2") -->
//        transform.using{s:String => s}.where(name="transformerB")
//        
//    assertNull(DslUtils.getStartingComposition(messageFlowB).parentComposition)
//   
//    val messageFlowBParentBeforeMerge = messageFlowB.parentComposition
//
//    val composedFlow = messageFlowA --> messageFlowB
//    
//    // assert that flow composition itself is not altered
//    assertNull(DslUtils.getStartingComposition(messageFlowB).parentComposition)
//
//    val messageFlowBParentAfterMerge = messageFlowB.parentComposition
//
//    assertEquals(messageFlowBParentBeforeMerge, messageFlowBParentAfterMerge)
//
//    val targetList = DslUtils.toProductSeq(composedFlow);
//
//    assertEquals(6, targetList.size)
//
//    assertEquals(new WrappedString("messageFlowA-1"), targetList(0).asInstanceOf[ServiceActivator].target)
//    assertEquals("messageFlowA-2", targetList(1).asInstanceOf[Channel].name)
//    assertEquals("transformerA", targetList(2).asInstanceOf[Transformer].name)
//    assertEquals("filterB", targetList(3).asInstanceOf[MessageFilter].name)
//    assertEquals("messageFlowB-2", targetList(4).asInstanceOf[PubSubChannel].name)
//    assertEquals("transformerB", targetList(5).asInstanceOf[Transformer].name)   
//  }
//  
//  @Test
//  def validateFlowWithMultipleSubscribers = {
//
//    val messageFlow =   
//      handle.using("service") --> 
//      PubSubChannel("pubSubChannel") --> (
//          {handle.using("A1") where(name = "A1")} --> 
//          {handle.using("A2") where(name = "A2")}
//          ,
//          {handle.using("B1") where(name = "B1")} --> 
//          {handle.using("B2") where(name = "B2")}
//      ) 
//      val targetList = DslUtils.toProductSeq(messageFlow);
//      
//      assertTrue(targetList.size == 3)
//      assertEquals(new WrappedString("service"), targetList(0).asInstanceOf[ServiceActivator].target)
//      assertEquals("pubSubChannel", targetList(1).asInstanceOf[PubSubChannel].name)
//      val subscribers = targetList(2).asInstanceOf[Seq[_]]
//      
//      assertTrue(subscribers.size == 2)
//      assertTrue(subscribers(0).asInstanceOf[Seq[_]].size == 2)
//      assertEquals("A1", subscribers(0).asInstanceOf[Seq[_]](0).asInstanceOf[ServiceActivator].name)
//      assertEquals("A2", subscribers(0).asInstanceOf[Seq[_]](1).asInstanceOf[ServiceActivator].name)
//      assertTrue(subscribers(1).asInstanceOf[Seq[_]].size == 2)
//      assertEquals("B1", subscribers(1).asInstanceOf[Seq[_]](0).asInstanceOf[ServiceActivator].name)
//      assertEquals("B2", subscribers(1).asInstanceOf[Seq[_]](1).asInstanceOf[ServiceActivator].name)
//  }
//  
//   @Test
//  def validateFlowWithSingleMultipleSubscriberVsMultipleSubscribers = {	
//    val messageFlowLegal = 
//      PubSubChannel("direct") --> (
//        transform.using { m: Message[String] => m }
//      ) --> transform.using { m: Message[String] => m.getPayload().toUpperCase() }
//      
//    assertEquals("HELLO", messageFlowLegal.sendAndReceive[String]("hello"))
//    
//    // code below should not compile since PubSubChannel has multiple subscribers
//    // and therefore the Sequence of the subscribers can not apply a continuity operator.
////    val messageFlowIllegal = 
////      PubSubChannel("direct") --> (
////        transform.using { m: Message[String] => m },
////        transform.using { m: Message[String] => m }
////      ) --> transform.using { m: Message[String] => m.getPayload().toUpperCase() }
//   }
//}
