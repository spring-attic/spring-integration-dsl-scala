package org.springframework.integration.dsl
import org.springframework.beans.factory.support.BeanDefinitionBuilder
import org.springframework.integration.handler.BridgeHandler
import java.util.UUID

private[dsl] class MessagingBridge(name:String = "$br_" + UUID.randomUUID().toString.substring(0, 8))
            extends SimpleEndpoint(name, null) {
  
  override def build(targetDefFunction: Function2[SimpleEndpoint, BeanDefinitionBuilder, Unit],
                     compositionInitFunction: Function2[BaseIntegrationComposition, AbstractChannel, Unit]): BeanDefinitionBuilder = {
    BeanDefinitionBuilder.rootBeanDefinition(classOf[BridgeHandler])
  }
}