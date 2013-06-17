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

import java.util.UUID
import org.w3c.dom.{Element, Document}

/**
 * @author Soby Chacko
 */
private[dsl] class InboundChannelAdapterConfig(name: String = "$inbound_ch_" + UUID.randomUUID().toString.substring(0, 8),
																							 target: Any, val poller: Poller) extends InboundMessageSource(name, target) {

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
