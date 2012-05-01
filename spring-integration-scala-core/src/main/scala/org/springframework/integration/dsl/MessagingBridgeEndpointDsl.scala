package org.springframework.integration.dsl
import org.springframework.beans.factory.support.BeanDefinitionBuilder
import org.springframework.integration.handler.BridgeHandler
import java.util.UUID
import org.w3c.dom.Element
import org.w3c.dom.Document

private[dsl] class MessagingBridge(name: String = "$br_" + UUID.randomUUID().toString.substring(0, 8))
  extends SimpleEndpoint(name, null) {

  override def build(document: Document,
    targetDefinitionFunction: Function1[Any, Tuple2[String, String]],
    compositionInitFunction: Function2[BaseIntegrationComposition, AbstractChannel, Unit]): Element = {
    val element = document.createElement("int:bridge")
    element.setAttribute("id", this.name)
    element
  }
}