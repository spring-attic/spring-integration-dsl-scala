package org.springframework.integration.dsl

import org.junit.Assert
import org.junit.Test
import org.springframework.integration.dsl.utils.DslUtils

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
      transform.using("messageFlowA-3")
      
    println(DslUtils.toProductList(messageFlowA))

    val messageFlowB =
      filter.using("messageFlowB-1") -->
        PubSubChannel("messageFlowB-2") -->
        transform.using("messageFlowB-3")
        
    println(DslUtils.toProductList(messageFlowB))

    val messageFlowBParentBeforeMerge = messageFlowB.parentComposition

    val composedFlow = messageFlowA --> messageFlowB

    val messageFlowBParentAfterMerge = messageFlowB.parentComposition

    Assert.assertEquals(messageFlowBParentBeforeMerge, messageFlowBParentAfterMerge)

    val targetList = DslUtils.toProductList(composedFlow);

    Assert.assertEquals(6, targetList.size)

    Assert.assertEquals("messageFlowA-1", targetList(0).asInstanceOf[ServiceActivator].target)
    Assert.assertEquals("messageFlowA-2", targetList(1).asInstanceOf[Channel].name)
    Assert.assertEquals("messageFlowA-3", targetList(2).asInstanceOf[Transformer].target)
    Assert.assertEquals("messageFlowB-1", targetList(3).asInstanceOf[MessageFilter].target)
    Assert.assertEquals("messageFlowB-2", targetList(4).asInstanceOf[PubSubChannel].name)
    Assert.assertEquals("messageFlowB-3", targetList(5).asInstanceOf[Transformer].target)
  }
}
