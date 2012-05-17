/**
 *
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
 *
 */
class GemfireRegion(val region: Region[_, _], private var cache: Cache) {

  require(region != null, "'region' must be provided")
  require(cache != null, "'cache' must be provided")

  def read(key: Any): Any = {
    val result = region.get(key)
    result
  }

  def store = new SendingIntegrationComposition(null, new GemfireOutboundAdapter(target = null, region = region, cache = cache))

  def store(function: Function1[_, Map[_, _]]) = new SendingIntegrationComposition(null, new GemfireOutboundAdapter(target = function, region = region, cache = cache))

  def store(function: (_, Map[String, _]) => Map[_, _]) = new SendingIntegrationComposition(null, new GemfireOutboundAdapter(target = function, region = region, cache = cache))

  private def convertToJavaMap(function: Function1[_, Map[_, _]]) {

  }
}

private[dsl] class GemfireOutboundAdapter(name: String = "$gfe_out" + UUID.randomUUID().toString.substring(0, 8),
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
    beansElement.setAttribute("xmlns:int-gfe", "http://www.springframework.org/schema/integration/gemfire")
    val schemaLocation = beansElement.getAttribute("xsi:schemaLocation")

    val mergedLocation = schemaLocation +
      " http://www.springframework.org/schema/integration/gemfire http://www.springframework.org/schema/integration/gemfire/spring-integration-gemfire.xsd"

    beansElement.setAttribute("xsi:schemaLocation", mergedLocation)
    
    def transformerFunction = {
      payload:Any =>
        payload match {
          case scalaMapPayload:Map[_,_] => JavaConversions.asJavaMap(scalaMapPayload)
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