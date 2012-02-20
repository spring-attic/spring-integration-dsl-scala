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


/**
 * @author Oleg Zhurakousky
 * Date: 1/18/12
 */

object route {

  def onPayloadType(conditionCompositions:PayloadTypeCondition*) = new IntegrationComposition(null,  new Router(null, null, null, conditionCompositions: _*)) { 

    def where(name:String) = new IntegrationComposition(null, new Router(name, null, null, conditionCompositions: _*))
  }

  def onValueOfHeader(headerName: String)(conditionCompositions: ValueCondition*) =
    new IntegrationComposition(null, new Router(null, null, headerName, conditionCompositions: _*)) {

      def where(name: String) = new IntegrationComposition(null, new Router(name, null, headerName, conditionCompositions: _*))
    }

  def using(target: String)(conditions: ValueCondition* ) =
    new IntegrationComposition(null, new Router(null, target, null, conditions: _*))  {
      def where(name: String) = new IntegrationComposition(null, new Router(name, target, null, conditions: _*))
    }

  def using(target: Function1[_, Any])(conditions: ValueCondition*) =
    new IntegrationComposition(null, new Router(null, target, null, conditions: _*)) {
      def where(name: String) = new IntegrationComposition(null, new Router(name, target, null, conditions: _*))
    }
  
//  def using(target: String) =
//    new IntegrationComposition(null, new Router(null, target, null, null))  {
//      def where(name: String) = new IntegrationComposition(null, new Router(name, target, null, null))
//    }
//
//  def using(target: Function1[_, Any]) =
//    new IntegrationComposition(null, new Router(null, target, null, null)) {
//      def where(name: String) = new IntegrationComposition(null, new Router(name, target, null, null))
//    }
}
/**
 * 
 */
object when {
  def apply(payloadType:Class[_]) = new {
    def then(channel:ChannelIntegrationComposition) = new PayloadTypeCondition(payloadType, channel)
  }
  
  def apply(headerValue:Any) = new  {
    def then(channel:ChannelIntegrationComposition) = new ValueCondition(headerValue, channel)
  }
}

private[dsl] case class Router(override val name:String, override val target:Any, val headerName:String, val compositions:Condition*)
            extends SimpleEndpoint(name, target)

private[dsl] abstract class Condition

private[dsl] class PayloadTypeCondition(val payloadType:Class[_], val channelComposition:ChannelIntegrationComposition) extends Condition

private[dsl] class ValueCondition(val value:Any, val channelComposition:ChannelIntegrationComposition) extends Condition