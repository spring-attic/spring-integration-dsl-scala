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
package org.springframework.integration.dsl
import java.util.UUID
import scala.collection.mutable.ListBuffer
import org.apache.commons.logging.LogFactory
import org.springframework.integration.dsl.utils.DslUtils
import scala.collection.mutable.WrappedArray

/**
 * @author Oleg Zhurakousky
 */
private[dsl] case class BaseIntegrationComposition(private[integration] val parentComposition: BaseIntegrationComposition, private[integration] val target: IntegrationComponent) {

  val logger = LogFactory.getLog(this.getClass());

  private val threadLocal: ThreadLocal[SI] = new ThreadLocal[SI]

  /**
   * Will produce a copy of this composition
   */
  private[dsl] def copy(): BaseIntegrationComposition = {

    this.target match {
      case ic: IntegrationComponent => {
        val parentComposition = if (this.parentComposition != null) this.parentComposition.copy() else null
        this.generateComposition(parentComposition, this)
      }
      case _ => this
    }
  }

  /**
   * Will merge to compositions by assigning 'this' composition as a 'parentComposition' of 'toComposition'
   */
  private[dsl] def merge(toComposition: BaseIntegrationComposition) = {
    val startingComposition = DslUtils.getStartingComposition(toComposition)
    DslUtils.injectParentComposition(startingComposition, this)
  }

  /**
   * Will add an input-channel to this composition (as a DirectChannel) if it does not begin with one
   */
  private[dsl] def normalizeComposition(): BaseIntegrationComposition = {

    val newComposition = this.copy()
    val startingComposition = DslUtils.getStartingComposition(newComposition)
    if (!startingComposition.isInstanceOf[ChannelIntegrationComposition] &&
      !startingComposition.target.isInstanceOf[InboundMessageSource]) {
      DslUtils.injectParentComposition(startingComposition, Channel("$ch_" + UUID.randomUUID().toString.substring(0, 8)))
    }
    new BaseIntegrationComposition(newComposition.parentComposition, newComposition.target)
  }

  /**
   *
   */
  private[dsl] def generateComposition[T <: BaseIntegrationComposition](parent: T, composition: T): BaseIntegrationComposition = {
    composition.target match {
      case ch: Channel => new ChannelIntegrationComposition(parent, composition.target)
      case queue: PollableChannel => new PollableChannelIntegrationComposition(parent, composition.target)
      case pubsub: PubSubChannel => new ChannelIntegrationComposition(parent, composition.target)
      case _ => new BaseIntegrationComposition(parent, composition.target)
    }
  }

  private[dsl] def getContext(): SI = {

    threadLocal.get() match {
      case eipContext: SI => {
        if (logger.isDebugEnabled) logger.debug("Retrieving existing EIP context")
        eipContext
      }
      case _ => {
        val eipContext = new SI(null, this)
        threadLocal.set(eipContext)
        eipContext
      }
    }
  }
}

/**
 *
 */
private[dsl] class SendingIntegrationComposition(parentComposition: BaseIntegrationComposition, target: IntegrationComponent)
  extends BaseIntegrationComposition(parentComposition, target) {
  /**
   *
   */
  def send(message: Any, timeout: Long = 0, headers: Map[String, Any] = null): Boolean = {
    val context = this.getContext()
    context.send(message, timeout, headers)
  }

  /**
   *
   */
  def sendAndReceive[T <: Any](message: Any, timeout: Long = 0, headers: Map[String, Any] = null, errorFlow: SendingEndpointComposition = null)(implicit m: scala.reflect.Manifest[T]): T = {
    val context = this.getContext()
    context.sendAndReceive[T](message, timeout, headers, errorFlow)
  }
}
/**
 *
 */
private[dsl] class SendingEndpointComposition(parentComposition: BaseIntegrationComposition, target: IntegrationComponent)
  extends SendingIntegrationComposition(parentComposition, target) {

  def -->[T <: BaseIntegrationComposition](a: T) = { //(implicit g: ComposableIntegrationComponent[T]) = {
    val g = new ComposableIntegrationComponent[T]
    if (this.logger.isDebugEnabled()) this.logger.debug("Adding " + a.target + " to " + this.target)
    val composed = g.compose(this, a)
    g.composeFinal(this, a, composed)
  }
}

class SendingChannelComposition(parentComposition: BaseIntegrationComposition, target: IntegrationComponent)
  extends SendingIntegrationComposition(parentComposition, target) {

  def -->[T <: BaseIntegrationComposition](a: T*) = { //(implicit g: ComposableIntegrationComponent[T]) = {
    val g = new ComposableIntegrationComponent[T]
    if (this.logger.isDebugEnabled())
      for (element <- a) this.logger.debug("Adding " + DslUtils.getStartingComposition(element).target + " to " + this.target)

    if (a.size == 1) {
      val composed = g.compose(this, a(0))
      g.composeFinal(this, a(0), composed)
    } else {
      val buffer = new ListBuffer[BaseIntegrationComposition]()
      for (element <- a) buffer += element
      val merged = new BaseIntegrationComposition(this, new ListOfCompositions(buffer.toList))
      g.composeFinal(this, merged, merged)
    }
  }
}

/**
 *
 */
class ListeningIntegrationComposition(parentComposition: BaseIntegrationComposition, target: IntegrationComponent)
  extends BaseIntegrationComposition(parentComposition, target) {
  /**
   *
   */
  def start() = this.getContext.start

  def stop() = this.getContext.stop

  def -->[T <: BaseIntegrationComposition](a: T) = { //(implicit g: ComposableIntegrationComponent[T]) = {
    val g = new ComposableIntegrationComponent[T]
    if (this.logger.isDebugEnabled()) this.logger.debug("Adding " + a.target + " to " + this.target)
    val composed = g.compose(this, a)
    g.composeFinal(this, composed)
  }

}

class PollerComposition(parentComposition: BaseIntegrationComposition, target: Poller)
  extends SendingChannelComposition(parentComposition, target)

/**
 *
 */
class ChannelIntegrationComposition(parentComposition: BaseIntegrationComposition, target: IntegrationComponent)
  extends SendingChannelComposition(parentComposition, target)

/**
 *
 */
class PollableChannelIntegrationComposition(parentComposition: BaseIntegrationComposition, target: IntegrationComponent)
  extends ChannelIntegrationComposition(parentComposition, target) {
  /**
   *
   */
  def -->(p: Poller)(implicit g: ComposableIntegrationComponent[Poller]) = {
    val merged = new BaseIntegrationComposition(this, p)
    g.composeFinal(this.asInstanceOf[SendingChannelComposition], merged, merged)
  }
    
}

/**
 *
 */
private[dsl] class ComposableIntegrationComponent[T] {

  def compose[T <: BaseIntegrationComposition](i: BaseIntegrationComposition, s: T): T = {
    val mergedComposition =
      if (s.parentComposition != null) {
        val copyComposition = s.copy()
        i.merge(copyComposition)
        i.generateComposition(copyComposition.parentComposition, copyComposition)
      } 
      else
        i.generateComposition(i, s)

    mergedComposition.asInstanceOf[T]
  }

  def composeFinal(c: ListeningIntegrationComposition, e: BaseIntegrationComposition) =
    new ListeningIntegrationComposition(c, e.target)

  def composeFinal[T <: BaseIntegrationComposition](parent: SendingEndpointComposition, child: BaseIntegrationComposition, merged: T) = {
    val returnValue = child match {
      case pch: PollableChannelIntegrationComposition =>
        new PollableChannelIntegrationComposition(merged.parentComposition, merged.target)
      case ch: ChannelIntegrationComposition =>
        new ChannelIntegrationComposition(merged.parentComposition, merged.target)
      case sch: SendingChannelComposition =>
        new SendingChannelComposition(merged.parentComposition, merged.target)
      case _ =>
        new SendingEndpointComposition(merged.parentComposition, merged.target)
    }
    returnValue.asInstanceOf[T]
  }

  def composeFinal(parent: SendingChannelComposition, child: BaseIntegrationComposition, merged: BaseIntegrationComposition) = {
    new SendingChannelComposition(merged.parentComposition, merged.target)
  }

}
/**
 *
 */
private[dsl] class ListOfCompositions[T](val compositions: List[BaseIntegrationComposition])
  extends IntegrationComponent("ListOfCompositions")
/**
 *
 */
private[dsl] abstract class IntegrationComponent(val name: String = null)
