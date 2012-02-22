package org.springframework.integration.dsl

import org.junit.{ Assert, Test }
import org.springframework.integration.dsl.DSL._
import org.springframework.integration.dsl.builders.IntegrationComposition
import org.springframework.integration.dsl.builders.filter
import org.springframework.integration.dsl.builders.PubSubChannel
import org.springframework.integration.dsl.utils.DslUtils
import org.springframework.integration.dsl.builders.handle
import org.springframework.integration.dsl.builders.ServiceActivator
import org.springframework.integration.dsl.builders.MessageFilter
import org.springframework.integration.dsl.builders.Channel
import org.springframework.integration.dsl.builders.Transformer
import org.springframework.integration.dsl.builders.transform
import org.apache.commons.logging.LogFactory

class DSLTest {
  
  private val logger = LogFactory.getLog(this.getClass());

  @Test
  def validateCompositionTypes() {

//    // this should simply compile
//
//    val a: IntegrationComposition = new IntegrationComposition(null, "First") --> new IntegrationComposition(null, "Second")
//
//    val b: IntegrationComposition = new IntegrationComposition(null, "First") --> new ChannelIntegrationComposition(null, "Second") --> new IntegrationComposition(null, "Third")
//
//    val c: ChannelIntegrationComposition =
//      new IntegrationComposition(null, "First") -->
//        new ChannelIntegrationComposition(null, "Second") -->
//        new IntegrationComposition(null, "Third") -->
//        new ChannelIntegrationComposition(null, "Fourth")
//
//    val d: BaseIntegrationComposition =
//      new IntegrationComposition(null, "First") -->
//        new ChannelIntegrationComposition(null, "Second") -->
//        new ChannelIntegrationComposition(null, "Third") --< (
//          new IntegrationComposition(null, "FirstA") -->
//          new ChannelIntegrationComposition(null, "SecondA"),
//          new IntegrationComposition(null, "FirstB"),
//          new ChannelIntegrationComposition(null, "FirstC"))
//
//    val e = new IntegrationComposition(null, "First") -->
//      new PollableChannelIntegrationComposition(null, "Second") -->
//      poll.usingFixedDelay(1) -->
//      new ChannelIntegrationComposition(null, "Fourth") -->
//      new IntegrationComposition(null, "Fifth")
//
//    val f = Channel("channelA") -->
//      new IntegrationComposition(null, "First") -->
//      Channel("channelB").withQueue() --> poll.usingFixedDelay(1) -->
//      new IntegrationComposition(null, "Second")

  }
  
  @Test
  def validateLogger = {
    
    this.logger.debug("foo")
    
  }

  @Test
  def validateMessagingBridge = {
     val messageBridge = Channel("A") --> Channel("B")
  }

  @Test
  def validateCompositionTypesWithDsl = {

    val messageFlowA: IntegrationComposition =
      handle.using("messageFlowA-1") -->
        Channel("messageFlowA-2") -->
        transform.using("messageFlowA-3")

    val messageFlowB: IntegrationComposition =
      filter.using("messageFlowB-1") -->
        PubSubChannel("messageFlowB-2") -->
        transform.using("messageFlowB-3")

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
