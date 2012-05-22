package org.springframework.integration.dsl
import java.io.File

object FileDsl {
   val fileSchema = "http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd " +
    "http://www.springframework.org/schema/integration http://www.springframework.org/schema/integration/spring-integration.xsd " +
    "http://www.springframework.org/schema/integration/file http://www.springframework.org/schema/integration/file/spring-integration-file.xsd"


}

object file {
  def apply(directory:String)(pollerComposition:PollerComposition) =
    new ListeningIntegrationComposition(null, new FileInboundAdapterConfig(target = directory, poller = pollerComposition.target))

}
