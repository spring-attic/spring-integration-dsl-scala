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
package org.springframework.integration.dsl.builders
import java.util.UUID

import org.springframework.http.HttpMethod
/**
 * @author Oleg Zhurakousky
 */
object to {

  def http = new {
    def GET[T](httpUrl: String)(implicit m: scala.reflect.Manifest[T]) =
      new IntegrationComposition(null, new HttpOutboundGateway(target = httpUrl,
        														httpMethod = HttpMethod.GET,
																expectedResponseType = m.erasure)) {
        def where(name: String, requestTimeout: Int) =
          new IntegrationComposition(null, new HttpOutboundGateway(name = name,
            														target = httpUrl,
            														requestTimeout = requestTimeout,
            														httpMethod = HttpMethod.GET,
            														expectedResponseType = m.erasure))
      }

    def GET[T](function: Function[_, String])(implicit m: scala.reflect.Manifest[T]) =
      new IntegrationComposition(null, new HttpOutboundGateway(target = function,
        														httpMethod = HttpMethod.GET,
        														expectedResponseType = m.erasure)) {
        def where(name: String, requestTimeout: Int) =
          new IntegrationComposition(null, new HttpOutboundGateway(name = name,
            														target = function,
            														requestTimeout = requestTimeout,
            														httpMethod = HttpMethod.GET,
            														expectedResponseType = m.erasure))
      }
  }
}

private[dsl] class HttpOutboundGateway(name: String = "$http_out_" + UUID.randomUUID().toString.substring(0, 8),
  										target: Any,
										val requestTimeout: Int = 0,
										val httpMethod: HttpMethod = HttpMethod.POST,
										val expectedResponseType: Class[_]) extends SimpleEndpoint(name, target)