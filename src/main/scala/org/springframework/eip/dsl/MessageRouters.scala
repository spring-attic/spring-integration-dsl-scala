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
import java.util.UUID


/**
 * @author Oleg Zhurakousky
 * Date: 1/18/12
 */

object route {

  def onPayloadType(conditionCompositions:PayloadTypeCondition*) = new IntegrationComposition(null,  new Router()(conditionCompositions: _*)) { 

    def where(name:String) = new IntegrationComposition(null, new Router(name, null, null)(conditionCompositions: _*))
  }

  def onValueOfHeader(headerName: String)(conditionCompositions: ValueCondition*) =
    new IntegrationComposition(null, new Router(null, null, headerName)(conditionCompositions: _*)) {

      def where(name: String) = new IntegrationComposition(null, new Router(name, null, headerName)(conditionCompositions: _*))
    }

  def using(target: String)(conditions: ValueCondition* ) =
    new IntegrationComposition(null, new Router(target = target)(conditions: _*))  {
      def where(name: String) = new IntegrationComposition(null, new Router(name, target, null)(conditions: _*))
    }

  def using(target: Function1[_, Any])(conditions: ValueCondition*) =
    new IntegrationComposition(null, new Router(target = target)(conditions: _*)) {
      def where(name: String) = new IntegrationComposition(null, new Router(name = name, target = target)(conditions: _*))
    }
}
/**
 * 
 */
object when {
  def apply(payloadType:Class[_]) = new {
    def then(composition:IntegrationComposition) = new PayloadTypeCondition(payloadType, composition)
    def -->(composition:IntegrationComposition) = new PayloadTypeCondition(payloadType, composition)
  }
 
  def apply(value:Any) = new  {
    def then(composition:IntegrationComposition) = new ValueCondition(value, composition)
    def -->(composition:IntegrationComposition) = new ValueCondition(value, composition)
  } 
}


private[dsl] class Router(name:String = "$rtr_" + UUID.randomUUID().toString.substring(0, 8), target:Any = null, val headerName:String = null)( val conditions:Condition*)
            extends SimpleEndpoint(name, target)

private[dsl] abstract class Condition(val value:Any, val integrationComposition:BaseIntegrationComposition)

private[dsl] class PayloadTypeCondition(val payloadType:Class[_], override val integrationComposition:BaseIntegrationComposition) 
	extends Condition(DslUtils.toJavaType(payloadType).getName(), integrationComposition)

private[dsl] class ValueCondition(override val value:Any, override val integrationComposition:BaseIntegrationComposition) 
	extends Condition(value, integrationComposition)