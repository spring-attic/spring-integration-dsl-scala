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
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.apache.commons.logging.LogFactory
import javax.sql.DataSource

/**
 * @author Ewan Benfield
 */
private[dsl] class JdbcInboundAdapterConfig(name: String = "$jdbc_in_" + UUID.randomUUID().toString.substring(0, 8),
  target: String, poller: Poller, dataSource: DataSource) extends InboundMessageSource(name) {

  private val logger = LogFactory.getLog(this.getClass());

  def build(document: Document,
            targetDefinitionFunction: Function1[Any, Tuple2[String, String]],
            pollerDefinitionFunction: Function3[IntegrationComponent, Poller, Element, Unit],
            requestChannelName: String): Element = {

    require(requestChannelName != null, "JDBC Inbound Channel Adapter requires query")

    val beansElement = document.getElementsByTagName("beans").item(0).asInstanceOf[Element]
    if (!beansElement.hasAttribute("xmlns:int-jdbc")){
       beansElement.setAttribute("xmlns:int-jdbc", "http://www.springframework.org/schema/integration/jdbc")
       val schemaLocation = beansElement.getAttribute("xsi:schemaLocation")
       beansElement.setAttribute("xsi:schemaLocation", schemaLocation + JdbcDsl.jdbcSchema);
    }

    val element = document.createElement("int-jdbc:inbound-channel-adapter")
    element.setAttribute("id", this.name)
    element.setAttribute("channel", requestChannelName)
    element.setAttribute("query", target)

    val dataSourceName = targetDefinitionFunction(Some(this.dataSource))._1
    element.setAttribute("data-source", dataSourceName)
    element.setAttribute("auto-startup", "true")
    pollerDefinitionFunction(this, poller, element)
    element
  }

}
