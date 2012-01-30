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
import java.lang.{IllegalStateException, ThreadLocal}


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

  def send(message:Any, timeout:Long = 0, headers:Map[String,  Any] = null, channelName:String=null):Boolean = {
    val context = this.getContext()
    context.send(message, timeout, headers, channelName)
  }

  def sendAndReceive[T](payload:Any): T = {
    null.asInstanceOf[T]
  }

  def copy(): EIPConfigurationComposition = {
    if (this.parentComposition != null){
      new EIPConfigurationComposition(this.parentComposition.copy(), this.target)
    }
    else {
      new EIPConfigurationComposition(null, this.target)
    }
  }

  private[dsl] def normalizeComposition(): EIPConfigurationComposition = {
    this match {
      case cmp:CompletableEIPConfigurationComposition =>   {
        cmp
      }
      case _ => {
        println("normaliziing composition ")
        val newComposition = this.copy()

        val startingComposition = newComposition.getStartingComposition()
        val field = classOf[EIPConfigurationComposition].getDeclaredField("parentComposition")
        field.setAccessible(true)
        field.set(startingComposition, Channel("$ch_" + UUID.randomUUID().toString.substring(0,8)))

        new EIPConfigurationComposition(newComposition.parentComposition, newComposition.target)
      }
    }
  }
  
  private def getContext():EIPContext = {

    threadLocal.get() match {
      case eipContext:EIPContext => {
        println("retrieved existing context")
        eipContext
      }
      case _ => {
        println("creating context")
        val normalizedComposition = normalizeComposition()
        normalizedComposition.target match {
          case poller:Poller => {
            throw new IllegalStateException("The resulting message flow configuration ends with Poller with no Consumer: " + poller)
          }
          case ch:Channel => {
            if (ch.capacity == Integer.MIN_VALUE){
              throw new IllegalStateException("The resulting message flow configuration ends with " +
                "Direct or PubSub Channel but no subscribers were configured: " + ch)
            }
          }
          case _ =>
        }
        
        val eipContext = EIPContext(
          new SimpleCompletableComposition(normalizedComposition.parentComposition, normalizedComposition.target)
              with CompletableEIPConfigurationComposition
        )
        threadLocal.set(eipContext)
        eipContext
      }
    }
  }

  private[dsl] def getStartingComposition():EIPConfigurationComposition = {
    if (this.parentComposition != null){
      this.parentComposition.getStartingComposition
    }
    else {
      this
    }
  }

  
  private[dsl] def merge(fromComposition:EIPConfigurationComposition, toComposition:EIPConfigurationComposition) = {
    val startingComposition = toComposition.getStartingComposition
    val field = classOf[EIPConfigurationComposition].getDeclaredField("parentComposition")
    field.setAccessible(true)
    field.set(startingComposition, this)
  }
  
  private[dsl] def toListOfTargets():List[Any] = {

    def toSet(comp:EIPConfigurationComposition): Set[Any] = {
      if (comp.parentComposition != null){
        var l = toSet(comp.parentComposition)
        l += comp.target
        l
      }
      else {
        Set(comp.target)
      }
    }

    toSet(this).toList
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

  override def copy(): SimpleComposition = {
    if (this.parentComposition != null){
      new SimpleComposition(this.parentComposition.copy(), this.target)
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
      this.merge(this, copyComposition)
      new SimpleCompletableComposition(copyComposition.parentComposition, copyComposition.target) with CompletableEIPConfigurationComposition
    }
    else {
      new SimpleCompletableComposition(this, composition.target) with CompletableEIPConfigurationComposition
    }
  }

  override def copy(): SimpleCompletableComposition = {
    if (this.parentComposition != null){
      new SimpleCompletableComposition(this.parentComposition.copy(), this.target)
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

  override def copy(): PollableComposition = {
    if (this.parentComposition != null){
      new PollableComposition(this.parentComposition.copy(), this.target)
    }
    else {
      new PollableComposition(null, this.target)
    }

  }
}