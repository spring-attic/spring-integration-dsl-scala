package org.springframework.integration.dsl

import java.util.UUID
import org.w3c.dom.{Element, Document}

/**
 * @author Soby Chacko
 *
 */
object loggingChannel {

	def apply(loggerName: String) =
		new SendingEndpointComposition(null, new LoggingChannelAdapterEndpoint(loggerName = loggerName))
}

private[dsl] class LoggingChannelAdapterEndpoint(name: String = "$logging_ch_" + UUID.randomUUID().toString.substring(0, 8),
																							 val loggerName: String) extends SimpleEndpoint(name, null) with OutboundAdapterEndpoint {

	override def build(document: Document = null,
										 targetDefinitionFunction: Function1[Any, Tuple2[String, String]],
										 compositionInitFunction: Function2[BaseIntegrationComposition, AbstractChannel, Unit] = null,
										 inputChannel: AbstractChannel,
										 outputChannel: AbstractChannel): Element = {

		val element = document.createElement("int:logging-channel-adapter")
		element.setAttribute("id", this.name)
		element.setAttribute("logger-name", loggerName)
		element.setAttribute("channel", inputChannel.name)
		element
	}

}
