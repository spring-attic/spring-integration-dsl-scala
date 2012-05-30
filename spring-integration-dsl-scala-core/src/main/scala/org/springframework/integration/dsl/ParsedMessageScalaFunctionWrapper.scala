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
import org.springframework.integration.Message
import scala.collection.JavaConversions._
/**
 *
 * @author Oleg Zhurakousky
 */
private[dsl] class ParsedMessageScalaFunctionWrapper[I,F](val f: Function2[I, Map[String, _], F])(implicit manifestI:Manifest[I]) extends Function2[I, Map[String, _], F] {
  val iErasure = manifestI.erasure

  def apply(payload:I, headers:Map[String, _]):F = {
    //val payload = message.getPayload
    //val headers:Map[String, _] = message.getHeaders.toMap

    val reply =
      if (classOf[Iterable[I]].isAssignableFrom(iErasure)) {
        payload match {
          case javaCollection: java.util.Collection[I] => {
            f(javaCollection.toIterable.asInstanceOf[I], headers)
          }
          case _ => f(payload, headers)
        }
      } else {
        f(payload, headers)
      }
    reply
  }
}