package org.springframework.integration.dsl

import java.util.UUID
import org.apache.commons.logging.LogFactory
import org.w3c.dom.{Element, Document}

/**
 * @author Soby Chacko
 */
private[dsl] class InboundChannelAdapterConfig(name: String = "$inbound_ch_" + UUID.randomUUID().toString.substring(0, 8),
                                               target: Any, poller: Poller, expression: String = null) extends InboundMessageSource(name, target) {

  private val logger = LogFactory.getLog(this.getClass());

  def build(document: Document = null,
            targetDefinitionFunction: Function1[Any, Tuple2[String, String]],
            pollerDefinitionFunction: Function3[IntegrationComponent, Poller, Element, Unit],
            requestChannelName: String): Element = {

    require(requestChannelName != null, "Inbound Channel Adapter requires continuation but message flow ends here")

    val element = document.createElement("int:inbound-channel-adapter")
    element.setAttribute("id", this.name)
    element.setAttribute("channel", requestChannelName)

    this.target match {
      case fn: Function0[_] => {
        val targetDefinnition = targetDefinitionFunction.apply(this.target)
        element.setAttribute("ref", targetDefinnition._1);
        element.setAttribute("method", targetDefinnition._2);
      }
      case expression: String => {
        element.setAttribute("expression", expression)
      }
    }

    element.setAttribute("auto-startup", "false")
    pollerDefinitionFunction(this, poller, element)
    element
  }
}
