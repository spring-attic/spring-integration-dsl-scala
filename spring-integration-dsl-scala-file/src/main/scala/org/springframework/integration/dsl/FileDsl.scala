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

/**
 * @author Oleg Zhurakousky
 */
object FileDsl {
   val fileSchema = "http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd " +
    "http://www.springframework.org/schema/integration http://www.springframework.org/schema/integration/spring-integration.xsd " +
    "http://www.springframework.org/schema/integration/file http://www.springframework.org/schema/integration/file/spring-integration-file.xsd"

}

object file {
  def apply(directory:String)(pollerComposition:PollerComposition) =
    new ListeningIntegrationComposition(null, new FileInboundAdapterConfig(target = directory, poller = pollerComposition.target))

  def write(directory:String) =
     new SendingEndpointComposition(null, new FileOutboundGatewayConfig(target = directory, oneway = true, fileNameGeneratioinFunction = null)) {

    def asFile(fileNameGeneratioinFunction: _ => String) =
      new SendingEndpointComposition(null, new FileOutboundGatewayConfig(target = directory, oneway = true, fileNameGeneratioinFunction = fileNameGeneratioinFunction))
  }

}
