/*
 * Copyright 2002-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.integration.dsl
import org.apache.commons.logging.LogFactory
import org.w3c.dom.Document
import org.w3c.dom.Element
import utils.DslUtils
import java.util.UUID
import com.gemstone.gemfire.cache.EntryEvent
import com.gemstone.gemfire.cache.Region
import org.springframework.util.CollectionUtils
import org.springframework.util.StringUtils
import scala.collection.JavaConversions._

/**
 * @author Oleg Zhurakousky
 */
private[dsl] class GemfireInboundConfig[V](name: String = "$gfe_in_" + UUID.randomUUID().toString.substring(0, 8),
  override val target: Function1[EntryEvent[Any, V], Any] = null,
  region: Region[_, _],
  val additionalAttributes: Map[String, _] = null)(val events: GemfireEvents.EventType*) extends InboundMessageSource(name, target) {

  private val logger = LogFactory.getLog(this.getClass());

  def build(document: Document = null,
    targetDefinitionFunction: Function1[Any, Tuple2[String, String]],
    pollerDefinitionFunction: Function3[IntegrationComponent, Poller, Element, Unit],
    requestChannelName: String): Element = {

    require(requestChannelName != null, "Gemfire Inbound Channel Adapter requires continuation but message flow ends here")

    val beansElement = document.getElementsByTagName("beans").item(0).asInstanceOf[Element]
    if (!beansElement.hasAttribute("xmlns:int-gfe")) {
      beansElement.setAttribute("xmlns:int-gfe", "http://www.springframework.org/schema/integration/gemfire")
      val schemaLocation = beansElement.getAttribute("xsi:schemaLocation")
      beansElement.setAttribute("xsi:schemaLocation", schemaLocation + GemfireDsl.gemfireSchema);
    }

    val adapterElement = document.createElement("int-gfe:inbound-channel-adapter")
    adapterElement.setAttribute("id", this.name)

    adapterElement.setAttribute("channel", requestChannelName)

    if (target != null) {
      val expressionGenerator = targetDefinitionFunction(target)
      val expressionParam =
        if (expressionGenerator._2.startsWith("sendMessage")) "#this"
        else if (expressionGenerator._2.startsWith("sendPayloadAndHeaders")) "payload, headers"
        else if (expressionGenerator._2.startsWith("sendPayload")) "payload"
      adapterElement.setAttribute("expression", "@" + expressionGenerator._1 + "." +
        expressionGenerator._2 + "(" + expressionParam + ")")
    }

    adapterElement.setAttribute("region", targetDefinitionFunction.apply(Some(this.region))._1)

    val eventsToInject = for (event <- events) yield event

    if (events.size > 0) {
      adapterElement.setAttribute("cache-events", StringUtils.arrayToCommaDelimitedString(eventsToInject.toArray))
    }
    adapterElement
  }

}
