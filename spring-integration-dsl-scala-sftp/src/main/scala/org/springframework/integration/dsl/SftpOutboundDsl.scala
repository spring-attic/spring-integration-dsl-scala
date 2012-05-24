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
import java.util.UUID
import org.w3c.dom.Element
import org.w3c.dom.Document
import java.io.File
import org.springframework.integration.dsl.utils.DslUtils
import org.springframework.integration.sftp.session.DefaultSftpSessionFactory
/**
 * @author Oleg Zhurakousky
 */
private[dsl] class SftpOutboundGatewayConfig(name: String = "$ftp_out_" + UUID.randomUUID().toString.substring(0, 8),
  override val target: Any,
  val oneway: Boolean,
  val fileNameGeneratioinFunction: _ => String,
  val sessionFactory:DefaultSftpSessionFactory,
  additionalAttributes:Map[String,_] = null) extends SimpleEndpoint(name, target) with OutboundAdapterEndpoint {

  override def build(document: Document = null,
    targetDefinitionFunction: Function1[Any, Tuple2[String, String]],
    compositionInitFunction: Function2[BaseIntegrationComposition, AbstractChannel, Unit] = null,
    inputChannel: AbstractChannel,
    outputChannel: AbstractChannel): Element = {

    require(inputChannel != null, "'inputChannel' must be provided")

    val beansElement = document.getElementsByTagName("beans").item(0).asInstanceOf[Element]
    if (!beansElement.hasAttribute("xmlns:int-sftp")) {
      beansElement.setAttribute("xmlns:int-sftp", "http://www.springframework.org/schema/integration/sftp")
      val schemaLocation = beansElement.getAttribute("xsi:schemaLocation")
      beansElement.setAttribute("xsi:schemaLocation", schemaLocation + SftpDsl.sftpSchema);
    }

    val element: Element =
      if (oneway) {
        val outboundAdapterElement = document.createElement("int-sftp:outbound-channel-adapter")

        this.target match {
          case str: String => outboundAdapterElement.setAttribute("remote-directory", new File(str).getAbsolutePath)
          case _ => {
            val directoryNameGenerator = targetDefinitionFunction(this.target)
            val expressionParam =
              if (directoryNameGenerator._2.startsWith("sendMessage")) "#this"
              else if (directoryNameGenerator._2.startsWith("sendPayloadAndHeaders")) "payload, headers"
              else if (directoryNameGenerator._2.startsWith("sendPayload")) "payload"
            	  outboundAdapterElement.setAttribute("remote-directory-expression", "@" + directoryNameGenerator._1 + "." +
            	  directoryNameGenerator._2 + "(" + expressionParam + ")")
          }
        }

        outboundAdapterElement.setAttribute("channel", inputChannel.name)

        if (fileNameGeneratioinFunction != null) {
          val fileNameGenerator = targetDefinitionFunction(fileNameGeneratioinFunction)
          val expressionParam =
            if (fileNameGenerator._2.startsWith("sendMessage")) "#this"
            else if (fileNameGenerator._2.startsWith("sendPayloadAndHeaders")) "payload, headers"
            else if (fileNameGenerator._2.startsWith("sendPayload")) "payload"
          outboundAdapterElement.setAttribute("remote-filename-generator-expression", "@" + fileNameGenerator._1 + "." +
            fileNameGenerator._2 + "(" + expressionParam + ")")
        }

        outboundAdapterElement
      } else {

        throw new UnsupportedOperationException("int-sftp:outbound-gateway is not currently supported")
      }

    element.setAttribute("id", this.name)

    val sessionFactoryName = targetDefinitionFunction(Some(sessionFactory))._1

    element.setAttribute("session-factory", sessionFactoryName)

    if (additionalAttributes != null){
      DslUtils.setAdditionalAttributes(element, additionalAttributes)
    }
    element
  }
}