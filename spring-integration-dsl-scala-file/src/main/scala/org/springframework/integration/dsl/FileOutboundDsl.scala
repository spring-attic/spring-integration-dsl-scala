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
import java.util.UUID
import org.w3c.dom.Element
import org.w3c.dom.Document
import java.io.File
/**
 * @author Oleg Zhurakousky
 */
private[dsl] class FileOutboundGatewayConfig(name: String = "$file_out_" + UUID.randomUUID().toString.substring(0, 8),
  target: String,
  oneway: Boolean,
  fileNameGeneratioinFunction:_ => String) extends SimpleEndpoint(name, target) with OutboundAdapterEndpoint {

  override def build(document: Document = null,
    targetDefinitionFunction: Function1[Any, Tuple2[String, String]],
    compositionInitFunction: Function2[BaseIntegrationComposition, AbstractChannel, Unit] = null,
    inputChannel: AbstractChannel,
    outputChannel: AbstractChannel): Element = {

    require(inputChannel != null, "'inputChannel' must be provided")

    val beansElement = document.getElementsByTagName("beans").item(0).asInstanceOf[Element]
    if (!beansElement.hasAttribute("xmlns:int-file")){
       beansElement.setAttribute("xmlns:int-file", "http://www.springframework.org/schema/integration/file")
       val schemaLocation = beansElement.getAttribute("xsi:schemaLocation")
       beansElement.setAttribute("xsi:schemaLocation", schemaLocation + FileDsl.fileSchema);
    }

    val element: Element =
      if (oneway) {
        val outboundAdapterElement = document.createElement("int-file:outbound-channel-adapter")

        outboundAdapterElement.setAttribute("directory", new File(this.target).getAbsolutePath)
        outboundAdapterElement.setAttribute("channel", inputChannel.name)

        if (fileNameGeneratioinFunction != null){
          val fileNameGenerator = targetDefinitionFunction(fileNameGeneratioinFunction)
          val expressionParam =
          	if (fileNameGenerator._2.startsWith("sendMessage")) "#this"
          	else if (fileNameGenerator._2.startsWith("sendPayloadAndHeaders")) "payload, headers"
          	else if (fileNameGenerator._2.startsWith("sendPayload")) "payload"
          outboundAdapterElement.setAttribute("filename-generator-expression", "@" + fileNameGenerator._1 + "." +
          fileNameGenerator._2 + "(" + expressionParam + ")")
        }

        outboundAdapterElement
      } else {

        throw new UnsupportedOperationException("int-file:outbound-gateway is not currently supported")
      }
    element.setAttribute("id", this.name)
    element
  }
}