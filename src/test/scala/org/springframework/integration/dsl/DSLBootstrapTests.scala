package org.springframework.integration.dsl

import org.junit.Assert
import org.junit.Test
import org.springframework.integration.dsl.utils.DslUtils
import scala.collection.immutable.WrappedString
import utils.DslUtils
import org.springframework.integration.Message
import org.springframework.integration.message.GenericMessage

class DSLBootstrapTests {
  
  @Test
  def sendAndProcessMessageNoReply = {

    val messageFlow =
      handle.using { m: Message[_] => println(m) }
    
    Assert.assertTrue(messageFlow.send("hello"))
  }
  
  @Test
  def sendAndProcessPayloadNoReplyA= {

    val messageFlow =
      handle.using { m: Any => println(m) }

    Assert.assertTrue(messageFlow.send("hello"))
  }
  
  @Test
  def sendAndProcessPayloadNoReplyB= {

    val messageFlow =
      handle.using { m: String => println(m) }

    Assert.assertTrue(messageFlow.send("hello"))
  }
  
  @Test
  def sendAndReceive_ProcessMessageReplyWithMessageAndReceiveMessage = {
    val messageFlow = 
      handle.using { m: Message[_] => m }

    Assert.assertTrue(messageFlow.sendAndReceive("hello").isInstanceOf[Message[_]])
  }
  
  @Test
  def sendAndReceive_ProcessMessageReplyWithMessageAndReceivePayload = {
    val messageFlow = 
      handle.using { m: Message[_] => m }

    Assert.assertTrue(messageFlow.sendAndReceive[String]("hello").isInstanceOf[String])
  }
  
  @Test
  def sendAndReceive_ProcessMessageReplyWithPayloadAndReceiveMessage = {
    val messageFlow = 
      handle.using { m: Message[_] => m.getPayload }

    Assert.assertTrue(messageFlow.sendAndReceive("hello").isInstanceOf[Message[_]])
  }
  
  @Test
  def sendAndReceive_ProcessMessageReplyWithPayloadAndReceivePayload = {
    val messageFlow = 
      handle.using { m: Message[_] => m.getPayload }

    Assert.assertTrue(messageFlow.sendAndReceive[String]("hello").isInstanceOf[String])
  }
  
  @Test
  def sendAndReceive_ProcessPayloadReplyWithMessageAndReceiveMessage= {
    val messageFlow = 
      handle.using { m: String => new GenericMessage[String](m) }

    Assert.assertTrue(messageFlow.sendAndReceive("hello").isInstanceOf[Message[_]])
  }
  
  @Test
  def sendAndReceive_ProcessPayloadReplyWithMessageAndReceivePayload = {
    val messageFlow = 
      handle.using { m: String => new GenericMessage[String](m) }

    Assert.assertTrue(messageFlow.sendAndReceive[String]("hello").isInstanceOf[String])
  }
  
  @Test
  def sendAndReceive_ProcessPayloadReplyWithPayloadAndReceiveMessage = {
    val messageFlow = 
      handle.using { m: String => m }

    Assert.assertTrue(messageFlow.sendAndReceive[Message[_]]("hello").isInstanceOf[Message[_]])
  }
  
  @Test
  def sendAndReceive_ProcessPayloadReplyWithPayloadAndReceivePayload = {
    val messageFlow = 
      handle.using { m: String => m }

    Assert.assertTrue(messageFlow.sendAndReceive[String]("hello").isInstanceOf[String])
  }
  
  @Test
  def sendAndProcessMessageWithReplyPayload= {
    val messageFlow = 
      handle.using { m: Message[_] => m.getPayload() }

    messageFlow.sendAndReceive("hello")
    println("done")
  }

  @Test
  def validateFlowCompositionFromSubflows = {

    val messageFlowA =   
      handle.using("messageFlowA-1") --> 
      Channel("messageFlowA-2") -->
      transform.using{s:String => s}.where(name="transformerA")
  
    val messageFlowB =
      filter.using{s:Boolean => s}.where(name="filterB") -->
        PubSubChannel("messageFlowB-2") -->
        transform.using{s:String => s}.where(name="transformerB")
        
    Assert.assertNull(DslUtils.getStartingComposition(messageFlowB).parentComposition)
   
    val messageFlowBParentBeforeMerge = messageFlowB.parentComposition

    val composedFlow = messageFlowA --> messageFlowB
    
    // assert that flow composition itself is not altered
    Assert.assertNull(DslUtils.getStartingComposition(messageFlowB).parentComposition)

    val messageFlowBParentAfterMerge = messageFlowB.parentComposition

    Assert.assertEquals(messageFlowBParentBeforeMerge, messageFlowBParentAfterMerge)

    val targetList = DslUtils.toProductList(composedFlow);

    Assert.assertEquals(6, targetList.size)

    Assert.assertEquals(new WrappedString("messageFlowA-1"), targetList(0).asInstanceOf[ServiceActivator].target)
    Assert.assertEquals("messageFlowA-2", targetList(1).asInstanceOf[Channel].name)
    Assert.assertEquals("transformerA", targetList(2).asInstanceOf[Transformer].name)
    Assert.assertEquals("filterB", targetList(3).asInstanceOf[MessageFilter].name)
    Assert.assertEquals("messageFlowB-2", targetList(4).asInstanceOf[PubSubChannel].name)
    Assert.assertEquals("transformerB", targetList(5).asInstanceOf[Transformer].name)   
  }
}
