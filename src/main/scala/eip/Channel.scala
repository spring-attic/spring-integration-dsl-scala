/*
 * Copyright 2002-2011 the original author or authors.
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
package eip
import EipType._
/**
 * @author Oleg Zhurakousky
 *
 */
private[eip] class Channel(name: String, eipType:EipType) extends EipComponent(name, eipType) {
  
}

private[eip] class P2PChannel(name: String) extends Channel(name, EipType.P2P_CHANNEL) {

}

object p2p {
  
  def apply():ComposableChannel = new ComposableChannel(new P2PChannel(null))
  
}

//class PubSubChannel(name:String) extends Channel(name, EipType.PUB_SUB_CHANNEL){
//
//}
//
//object pubSub {
//
//  def apply():PubSubChannel = new PubSubChannel(null)
//  
//  def apply(name:String):PubSubChannel = new PubSubChannel(name)
//  
//}