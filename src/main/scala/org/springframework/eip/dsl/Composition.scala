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
private[dsl] abstract class EIPConfigurationComposition(val parentComposition:EIPConfigurationComposition, val target:Any)

/**
 *
 */
private[dsl] trait CompletableEIPConfigurationComposition
/**
 *
 */
private[dsl] case class SimpleComposition(override val parentComposition:EIPConfigurationComposition, override val target:Any)
  extends EIPConfigurationComposition(parentComposition, target){

  def -->(composition: SimpleComposition) = {
    composition.copy(this, composition.target)
  }

  def -->(composition: PollableComposition) = {
    composition.copy(this, composition.target)
  }
}

/**
 *
 */
private[dsl] case class SimpleCompletableComposition(override val parentComposition:EIPConfigurationComposition, override val target:Any)
  extends SimpleComposition(parentComposition, target){

  override def -->(composition: SimpleComposition) = {
    new SimpleCompletableComposition(this, composition.target) with CompletableEIPConfigurationComposition
  }
}

/**
 *
 */
private[dsl] abstract class ConditionComposition(override val parentComposition:EIPConfigurationComposition, override val target:Any)
  extends EIPConfigurationComposition(parentComposition, target){

  def -->(composition: SimpleComposition): ConditionComposition

  def -->(composition: PollableComposition): ConditionComposition

}

private[dsl] case class PayloadTypeConditionComposition(override val parentComposition:EIPConfigurationComposition, override val target:Any)
      extends ConditionComposition(parentComposition, target)   {

  def -->(composition: SimpleComposition) = new PayloadTypeConditionComposition(this, composition.target)

  def -->(composition: PollableComposition) = new PayloadTypeConditionComposition(this, composition.target)
}

private[dsl] case class HeaderValueConditionComposition(override val parentComposition:EIPConfigurationComposition, override val target:Any)
      extends ConditionComposition(parentComposition, target) {

  def -->(composition: SimpleComposition) = new HeaderValueConditionComposition(this, composition.target)

  def -->(composition: PollableComposition) = new HeaderValueConditionComposition(this, composition.target)
}

/**
 *
 */
private[dsl] case class PollableComposition(override val parentComposition:EIPConfigurationComposition, override val target:Channel)
  extends EIPConfigurationComposition(parentComposition, target){

  def -->(poller: Poller) = new SimpleComposition(this, poller)  {
    override def -->(composition: SimpleComposition) = new SimpleCompletableComposition(this, composition.target) with CompletableEIPConfigurationComposition
  }


}