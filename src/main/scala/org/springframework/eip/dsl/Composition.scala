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

import java.lang.{IllegalStateException, ThreadLocal}
import java.util.UUID


/**
 * @author Oleg Zhurakousky
 * Date: 1/12/12
 */
class EIPConfigurationComposition(val parentComposition:EIPConfigurationComposition, val target:Any) {

  val threadLocal:ThreadLocal[EIPContext] = new ThreadLocal[EIPContext]

  def start():Unit = {

  }

  def stop():Unit = {

  }

  def send(message:Any):Boolean = {
    this.getContext()
    true
  }

  def send(message:Any, timeout:Long, channelName:String="default"):Boolean = {
    true
  }

  def send(message:Any, timeout:Long):Boolean = {
    true
  }

  def sendAndReceive[T](payload:Any): T = {
    null.asInstanceOf[T]
  }
  
  private def getContext():EIPContext = {

    def normalizeComposition(): CompletableEIPConfigurationComposition = {
      this match {
        case cmp:CompletableEIPConfigurationComposition =>   {
          cmp
        }
        case _ => {
          println("normaliziing composition ")

          val startingComosition = this.getStartingComposition(this)
          val field = classOf[EIPConfigurationComposition].getDeclaredField("parentComposition")
          field.setAccessible(true)
          field.set(startingComosition, Channel("$ch_" + UUID.randomUUID().toString.substring(0,8)))

          new SimpleCompletableComposition(this.parentComposition, this.target) with CompletableEIPConfigurationComposition
        }
      }
    }

    threadLocal.get() match {
      case eipContext:EIPContext => {
        println("retrieved existing context")
        eipContext
      }
      case _ => {
        println("creating context")
        val eipContext = EIPContext(normalizeComposition())
        threadLocal.set(eipContext)
        eipContext
      }
    }
  }

  def getStartingComposition(composition:EIPConfigurationComposition):EIPConfigurationComposition = {
    if (composition.parentComposition != null){
      getStartingComposition(composition.parentComposition)
    }
    else {
      composition
    }
  }

  
  def merge(fromComposition:EIPConfigurationComposition, toComposition:EIPConfigurationComposition) = {
    val startingComposition = getStartingComposition(toComposition)
    //val startingCompositionC = new EIPConfigurationComposition(null, startingComposition.target)
    val field = classOf[EIPConfigurationComposition].getDeclaredField("parentComposition")
    field.setAccessible(true)
    field.set(startingComposition, this)
  }
}

/**
 *
 */
private[dsl] trait CompletableEIPConfigurationComposition
/**
 *
 */
private[dsl] case class SimpleComposition(override val parentComposition:EIPConfigurationComposition, override val target:Any)
  extends EIPConfigurationComposition(parentComposition, target) {

  def -->(composition: SimpleComposition):SimpleComposition = {

    if (composition.parentComposition != null){
      val copyComposition = composition.copy()
      this.merge(this, copyComposition)
      new SimpleComposition(copyComposition.parentComposition, copyComposition.target)
    }
    else {
      new SimpleComposition(this, composition.target)
    }
  }

  def -->(composition: PollableComposition) = {
    if (composition.parentComposition != null){
      val copyComposition = composition.copy()
      this.merge(this, copyComposition)
      new PollableComposition(copyComposition.parentComposition, copyComposition.target)
    }
    else {
      new PollableComposition(this, composition.target)
    }
  }

  def copy(): SimpleComposition = {
    println("copying SimpleComposition")
    if (this.parentComposition != null){
      new SimpleComposition(this.parentComposition.asInstanceOf[SimpleComposition].copy(), this.target)
    }
    else {
      new SimpleComposition(null, this.target)
    }
  }


}

/**
 *
 */
private[dsl] case class SimpleCompletableComposition(override val parentComposition:EIPConfigurationComposition, override val target:Any)
  extends SimpleComposition(parentComposition, target){

  override def -->(composition: SimpleComposition):SimpleCompletableComposition with CompletableEIPConfigurationComposition = {
    if (composition.parentComposition != null){

      val copyComposition = composition.copy()
      println("1-parent: " + composition.parentComposition)
      println("1-copy: " + copyComposition.parentComposition)
      this.merge(this, copyComposition)
      println("2-parent: " + composition.parentComposition)
      println("2-copy: " + copyComposition.parentComposition)
      new SimpleCompletableComposition(copyComposition.parentComposition, copyComposition.target) with CompletableEIPConfigurationComposition
    }
    else {
      new SimpleCompletableComposition(this, composition.target) with CompletableEIPConfigurationComposition
    }
  }

  override def copy(): SimpleCompletableComposition = {
    println("copying SimpleCompletableComposition")
    if (this.parentComposition != null){
      new SimpleCompletableComposition(this.parentComposition.asInstanceOf[SimpleComposition].copy(), this.target)
    }
    else {
      new SimpleCompletableComposition(null, this.target)
    }
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

  def copy(): PollableComposition = {
    println("copying")
    if (this.parentComposition != null){
      new PollableComposition(this.parentComposition.asInstanceOf[SimpleComposition].copy(), this.target)
    }
    else {
      new PollableComposition(null, this.target)
    }

  }
}