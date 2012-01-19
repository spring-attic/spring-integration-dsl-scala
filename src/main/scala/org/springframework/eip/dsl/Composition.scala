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
 * Date: 1/12/12
 */
private[dsl] abstract class Composition(val parentComposition:Composition, val target:Any)

/**
 *
 */
private[dsl] case class SimpleComposition(override val parentComposition:Composition, override val target:Any)
  extends Composition(parentComposition, target){

  def -->(composition: SimpleComposition) = {
    println("1-receiving: " + composition)
    composition.copy(this, composition.target)
  }

  def -->(composition: PollableComposition) = {
    println("2-receiving: " + composition)
    composition.copy(this, composition.target)
  }
}

/**
 *
 */
private[dsl] abstract class ConditionComposition(override val parentComposition:Composition, override val target:Any) extends Composition(parentComposition, target){

  def -->(composition: SimpleComposition): ConditionComposition

  def -->(composition: PollableComposition): ConditionComposition

}

private[dsl] class PayloadTypeConditionComposition(override val parentComposition:Composition, override val target:Any)
      extends ConditionComposition(parentComposition, target)   {

  def -->(composition: SimpleComposition) = new PayloadTypeConditionComposition(this, composition.target)

  def -->(composition: PollableComposition) = new PayloadTypeConditionComposition(this, composition.target)
}

private[dsl] class HeaderValueConditionComposition(override val parentComposition:Composition, override val target:Any)
      extends ConditionComposition(parentComposition, target) {

  def -->(composition: SimpleComposition) = new HeaderValueConditionComposition(this, composition.target)

  def -->(composition: PollableComposition) = new HeaderValueConditionComposition(this, composition.target)
}

/**
 *
 */
private[dsl] case class PollableComposition(override val parentComposition:Composition, override val target:Any) extends Composition(parentComposition, target){

  def -->(poller: Poller):SimpleComposition = {
    println("receiving poler: " + poller)
    new SimpleComposition(this, poller)
  }

}