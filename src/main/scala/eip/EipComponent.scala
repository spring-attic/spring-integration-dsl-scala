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
 */
private[eip] object EipComponent {
  val using = "using"
  val eipType = "eipType"
  val name = "name"
  val order = "order"
  val inputChannel = "inputChannel"
  val outputChannel = "outputChannel"
}
/**
 *
 */
private[eip] abstract class EipComponent(val name: String, val eipType: EipType) {

  private[eip] val configMap = scala.collection.mutable.Map[Any, Any]()

  this.configMap += (EipComponent.name -> (if (name == null) eipType + "_" + this.hashCode() else name))
  
  this.configMap += (EipComponent.eipType -> eipType)

  /**
   * 
   */
  override def toString() = this.configMap(EipComponent.name).toString()

}