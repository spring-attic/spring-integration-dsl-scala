/*
 * Copyright 2002-2013 the original author or authors.
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
package org.springframework.integration.dsl

import LogLevel._
import java.util.UUID
import org.w3c.dom.{Element, Document}

/**
 * @author Soby Chacko
 *
 */
object log {

	def apply(loggerName: String = null) =  new SendingEndpointComposition(null, new Logger(loggerName = loggerName)){
		def withFullMessage(logFullMessage: Boolean) = getSendingEndpointWithLogFullMessageAndLogLevel(loggerName, logFullMessage)
		def withLogLevel(logLevel: Value) =  getSendingEndpointWithLogLevelAndLogFullLogOrExpression(loggerName, logLevel)
		def withExpression(expression: String) = getSendingEndpointWithExpressionAndLogLevel(loggerName, expression)
	}

	private def getSendingEndpointWithLogFullMessageAndLogLevel(loggerName: String = null, logFullMessage: Boolean) = {
		new SendingEndpointComposition(null, new Logger(loggerName = loggerName, logFullMessage = logFullMessage)) {
			def withLogLevel(logLevel: Value): SendingEndpointComposition =  {
				new SendingEndpointComposition(null, new Logger(loggerName = loggerName, logFullMessage = logFullMessage, logLevel = logLevel))
			}
		}
	}

	private def getSendingEndpointWithLogLevelAndLogFullLogOrExpression(loggerName: String = null, logLevel: Value) = {
		new SendingEndpointComposition(null, new Logger(loggerName = loggerName, logLevel = logLevel)) {
			def withFullMessage(logFullMessage: Boolean): SendingEndpointComposition = {
				new SendingEndpointComposition(null, new Logger(loggerName = loggerName, logFullMessage = logFullMessage, logLevel = logLevel))
			}

			def withExpression(expression: String): SendingEndpointComposition = {
				new SendingEndpointComposition(null, new Logger(loggerName = loggerName, logLevel = logLevel, expression = expression))
			}
		}
	}

	private def getSendingEndpointWithExpressionAndLogLevel(loggerName: String = null, expression: String) = {
		new SendingEndpointComposition(null, new Logger(loggerName = loggerName, expression = expression)) {
			def withLogLevel(logLevel: Value): SendingEndpointComposition = {
				new SendingEndpointComposition(null, new Logger(loggerName = loggerName, logLevel = logLevel, expression = expression))
			}
		}
	}

	def withFullMessage(logFullMessage: Boolean) = getSendingEndpointWithLogFullMessageAndLogLevel(logFullMessage = logFullMessage)
	def withLogLevel(logLevel: Value) =  getSendingEndpointWithLogLevelAndLogFullLogOrExpression(logLevel = logLevel)
	def withExpression(expression: String) = getSendingEndpointWithExpressionAndLogLevel(expression = expression)
}

private[dsl] class Logger(name: String = "$logging_ch_" + UUID.randomUUID().toString.substring(0, 8),
													val loggerName: String = null,
													val logFullMessage: Boolean = false,
													val logLevel: Value = INFO,
													val expression: String = null) extends SimpleEndpoint(name, null) with OutboundAdapterEndpoint {

	override def build(document: Document = null,
										targetDefinitionFunction: Function1[Any, Tuple2[String, String]],
										compositionInitFunction: Function2[BaseIntegrationComposition, AbstractChannel, Unit] = null,
										inputChannel: AbstractChannel,
										outputChannel: AbstractChannel): Element = {

		val element = document.createElement("int:logging-channel-adapter")
		element.setAttribute("id", this.name)
		element.setAttribute("logger-name", loggerName)
		element.setAttribute("channel", inputChannel.name)
		if (expression == null) {
			element.setAttribute("log-full-message", logFullMessage.toString)
		}
		element.setAttribute("level", logLevel.toString)
		element.setAttribute("expression", expression)
		element
	}

}
