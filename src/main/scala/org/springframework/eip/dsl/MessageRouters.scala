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

  def onPayloadType(conditionCompositions:PayloadTypeConditionComposition*) = new Router(null, null) with Where {
    def where(name:String): Router  = {
      this
    }
  }

  def onValueOfHeader(headerName:String)(conditionCompositions:HeaderValueConditionComposition*) = new Router(null, null) with Where {
    def where(name:String): Router  = {
      this
    }
  }

  def using(spelExpression:String) = new Router(null, null) with Where {
    def where(name:String): Router  = {
      this
    }
  }

  def using(function:Function1[_,AnyRef]) = new Router(null, null) with Where {
    def where(name:String): Router  = {
      this
    }
  }

  private[route] trait Where {
    def where(name:String): Router
  }
}

object when {

  def apply(payloadType:Class[_]) = new PayloadTypeConditionComposition(null, new PayloadTypeCondition(payloadType))

  def apply(headerValue:AnyRef) = new HeaderValueConditionComposition(null, new HeaderValueConDition(headerValue))

}

private[dsl] case class Router(val name:String, val targetProcesor:Any)

private[dsl] class PayloadTypeCondition(val payloadType:Class[_])

private[dsl] class HeaderValueConDition(val headerValue:AnyRef)