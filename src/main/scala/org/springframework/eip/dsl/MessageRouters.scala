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
package org.springframework.eip.dsl

import org.springframework.integration.MessageChannel

/**
 * @author Oleg Zhurakousky
 * Date: 1/18/12
 */

object route {

  def onPayloadType(conditionCompositions:PayloadTypeConditionComposition*) =
              new SimpleComposition(null, new Router(null, null, null, conditionCompositions)) with Where {

    def where(name:String) = new SimpleComposition(null, new Router(name, null, null, conditionCompositions))
  }

  def onValueOfHeader(headerName:String)(conditionCompositions:HeaderValueConditionComposition*) =
              new SimpleComposition(null, new Router(null, null, headerName, conditionCompositions)) with Where {

    def where(name:String) = new SimpleComposition(null, new Router(name, null, headerName, conditionCompositions))
  }

  def using(spelExpression:String) = new SimpleComposition(null, new Router(null, spelExpression, null, null)) with Where {
    def where(name:String) = new SimpleComposition(null, new Router(name, spelExpression, null, null))
  }

  def using(function:Function1[_,AnyRef]) = new SimpleComposition(null, new Router(null, function, null, null)) with Where {
    def where(name:String) = new SimpleComposition(null, new Router(name, function, null, null))
  }

  private[route] trait Where {
    def where(name:String): SimpleComposition
  }
}

object when {

  def apply(payloadType:Class[_])(c:EIPConfigurationComposition) =
    new PayloadTypeConditionComposition(null, new PayloadTypeCondition(payloadType, c))

  def apply(headerValue:AnyRef)(c:EIPConfigurationComposition) =
    new HeaderValueConditionComposition(null, new HeaderValueCondition(headerValue, c))

}

private[dsl] case class Router(override val name:String, override val target:Any, val headerName:String, val compositions:Seq[ConditionComposition])
            extends SimpleEndpoint(name, target)

private[dsl] class PayloadTypeCondition(val payloadType:Class[_], val composition:EIPConfigurationComposition)

private[dsl] class HeaderValueCondition(val headerValue:AnyRef, val composition:EIPConfigurationComposition)