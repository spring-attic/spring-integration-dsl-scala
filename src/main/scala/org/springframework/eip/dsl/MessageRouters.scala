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

//  def onPayloadType(conditionCompositions:PayloadTypeConditionComposition*) =
//              new SimpleComposition(null, new Router(null, null, null, conditionCompositions)) with Where {
//
//    def where(name:String) = new SimpleComposition(null, new Router(name, null, null, conditionCompositions))
//  }
//
//  def onValueOfHeader(headerName:String)(conditionCompositions:ValueConditionComposition*) =
//              new SimpleComposition(null, new Router(null, null, headerName, conditionCompositions)) with Where {
//
//    def where(name:String) = new SimpleComposition(null, new Router(name, null, headerName, conditionCompositions))
//  }
//
//  def using(target:String)(conditionCompositions:ValueConditionComposition*) =
//                    new SimpleComposition(null, new Router(null, target, null, conditionCompositions)) with Where {
//    def where(name:String) = new SimpleComposition(null, new Router(name, target, null, conditionCompositions))
//  }
//
//  def using(target:Function1[_,Any])(conditionCompositions:ValueConditionComposition*) =
//    new SimpleComposition(null, new Router(null, target, null, conditionCompositions)) with Where {
//      def where(name:String) = new SimpleComposition(null, new Router(name, target, null, conditionCompositions))
//    }
//
//  private[route] trait Where {
//    def where(name:String): SimpleComposition
//  }
}
/**
 * 
 */
object when {
//  def apply(payloadType:Class[_]) = new PayloadTypeConditionComposition(null, payloadType)
//  
//  def apply(headerValue:Any) = new ValueConditionComposition(null, headerValue)
}

//trait ThenChannel {
//  def then(channelComposition:ChannelComposition){
//    
//  }
//}
//object when {
//
//  def apply(payloadType:Class[_])(c:EIPConfigurationComposition) =
//    new PayloadTypeConditionComposition(null, new PayloadTypeCondition(payloadType, c))
//
//  def apply(headerValue:Any)(c:EIPConfigurationComposition) =
//    new ValueConditionComposition(null, new ValueCondition(headerValue, c))
//
//}

//private[dsl] case class Router(override val name:String, override val target:Any, val headerName:String, val compositions:Seq[ConditionComposition])
//            extends SimpleEndpoint(name, target)

private[dsl] class PayloadTypeCondition(val payloadType:Class[_], val channelComposition:SimpleComposition with ChannelComposition)

private[dsl] class ValueCondition(val value:Any, val channelComposition:SimpleComposition with ChannelComposition)