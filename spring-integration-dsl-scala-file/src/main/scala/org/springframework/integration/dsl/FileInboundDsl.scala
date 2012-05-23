/*
 * Copyright 2002-2012 the original author or authors.
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
import java.io.File
import java.util.UUID
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.apache.commons.logging.LogFactory

/**
 * @author Oleg Zhurakousky
 */
private[dsl] class FileInboundAdapterConfig(name: String = "$file_in_" + UUID.randomUUID().toString.substring(0, 8),
  target: String, poller: Poller) extends InboundMessageSource(name, target) {

  private val logger = LogFactory.getLog(this.getClass());

  def build(document: Document = null,
            targetDefinitionFunction: Function1[Any, Tuple2[String, String]],
            pollerDefinitionFunction: Function3[IntegrationComponent, Poller, Element, Unit],
            requestChannelName: String): Element = {

    require(requestChannelName != null, "File Inbound Channel Adapter requires continuation but message flow ends here")

    val beansElement = document.getElementsByTagName("beans").item(0).asInstanceOf[Element]
    if (!beansElement.hasAttribute("xmlns:int-file")){
       beansElement.setAttribute("xmlns:int-file", "http://www.springframework.org/schema/integration/file")
       val schemaLocation = beansElement.getAttribute("xsi:schemaLocation")
       beansElement.setAttribute("xsi:schemaLocation", schemaLocation + FileDsl.fileSchema);
    }

    val element = document.createElement("int-file:inbound-channel-adapter")
    element.setAttribute("id", this.name)

    element.setAttribute("channel", requestChannelName)

    element.setAttribute("directory", new File(this.target).getAbsolutePath())

    element.setAttribute("auto-startup", "false")
    pollerDefinitionFunction(this, poller, element)
    element
  }

}
