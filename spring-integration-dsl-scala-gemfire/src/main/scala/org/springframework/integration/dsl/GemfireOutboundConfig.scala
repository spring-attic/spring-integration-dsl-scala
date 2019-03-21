/*
 * Copyright 2002-2013 the original author or authors.
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
import com.gemstone.gemfire.cache.Region
import org.springframework.util.StringUtils
import org.w3c.dom.Element
import org.w3c.dom.Document
import org.springframework.data.gemfire.CacheFactoryBean
import com.gemstone.gemfire.cache.Cache
import scala.collection.JavaConversions
import org.springframework.integration.channel.DirectChannel
import scala.collection.JavaConversions

/**
 * @author Oleg Zhurakousky
 * @author Soby Chacko
 */
private[dsl] class GemfireOutboundConfig(name: String = "$gfe_out" + UUID.randomUUID().toString.substring(0, 8),
  target: Any,
  private var cache: Cache,
  region: Region[_, _])
  extends SimpleEndpoint(name, target) {

  override def build(document: Document = null,
    targetDefinitionFunction: Function1[Any, Tuple2[String, String]],
    compositionInitFunction: Function2[BaseIntegrationComposition, AbstractChannel, Unit] = null,
    inputChannel:AbstractChannel,
    outputChannel:AbstractChannel): Element = {

    require(inputChannel != null, "'inputChannel' must be provided")

    val beansElement = document.getElementsByTagName("beans").item(0).asInstanceOf[Element]
    if (!beansElement.hasAttribute("xmlns:int-gfe")){
       beansElement.setAttribute("xmlns:int-gfe", "http://www.springframework.org/schema/integration/gemfire")
       val schemaLocation = beansElement.getAttribute("xsi:schemaLocation")
       beansElement.setAttribute("xsi:schemaLocation", schemaLocation + GemfireDsl.gemfireSchema);
    }

    def transformerFunction = {
      payload:Any =>
        payload match {
          case scalaMapPayload:Map[_,_] => JavaConversions.mapAsJavaMap(scalaMapPayload)
          case _ => payload
        }
    }
    val targetRefMethodPair = targetDefinitionFunction.apply(transformerFunction)

     val chainElement = document.createElement("int:chain")
     chainElement.setAttribute("input-channel", inputChannel.name)

    val transformerElement = document.createElement("int:transformer")
    transformerElement.setAttribute("ref", targetRefMethodPair._1)
    transformerElement.setAttribute("method", targetRefMethodPair._2)

    chainElement.appendChild(transformerElement)

    val adapterElement = document.createElement("int-gfe:outbound-channel-adapter")
    adapterElement.setAttribute("region", targetDefinitionFunction.apply(Some(this.region))._1)
    adapterElement.setAttribute("id", this.name) // raise JIRA since it must not be required

    chainElement.appendChild(adapterElement)

    chainElement
  }
}