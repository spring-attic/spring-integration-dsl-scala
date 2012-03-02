package org.springframework.integration.dsl

import org.junit.Assert
import org.junit.Test
import org.springframework.integration.dsl.utils.DslUtils
import scala.collection.immutable.WrappedString

class DSLTest {

  @Test
  def validateMessagingBridge = {
     val messageBridge = Channel("A") --> Channel("B")
  }

  @Test
  def validateCompositionTypesWithDsl = {

    val messageFlowA =   
      handle.using("messageFlowA-1") --> 
      Channel("messageFlowA-2") -->
      transform.using{s:String => s}.where(name="transformerA")
      
    println(DslUtils.toProductList(messageFlowA))

    val messageFlowB =
      filter.using{s:Boolean => s}.where(name="filterB") -->
        PubSubChannel("messageFlowB-2") -->
        transform.using{s:String => s}.where(name="transformerB")
        
    println(DslUtils.toProductList(messageFlowB))

    val messageFlowBParentBeforeMerge = messageFlowB.parentComposition

    val composedFlow = messageFlowA --> messageFlowB

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
