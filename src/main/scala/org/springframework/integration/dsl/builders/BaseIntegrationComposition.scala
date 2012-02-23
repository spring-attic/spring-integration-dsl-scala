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
package org.springframework.integration.dsl.builders
import java.util.UUID

import scala.collection.mutable.ListBuffer

import org.apache.commons.logging.LogFactory
import org.springframework.integration.dsl.utils.DslUtils

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
        this.generateComposition(parentComposition, ic)
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
        !startingComposition.target.isInstanceOf[InboundMessageSource]){
      DslUtils.injectParentComposition(startingComposition, Channel("$ch_" + UUID.randomUUID().toString.substring(0, 8)))
    }
    new BaseIntegrationComposition(newComposition.parentComposition, newComposition.target)
  }

  /**
   *
   */
  private[dsl] def generateComposition[T <: BaseIntegrationComposition](parent: T, child: IntegrationComponent): IntegrationComposition = {
    val composition = child match {
      case ch: Channel => new ChannelIntegrationComposition(parent, child)
      case psch: PubSubChannel => new ChannelIntegrationComposition(parent, child)
      case _ => new IntegrationComposition(parent, child)
    }
    composition
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
class SendingIntegrationComposition(parentComposition: BaseIntegrationComposition, target: IntegrationComponent)
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
  def sendAndReceive[T <: Any](message: Any, timeout: Long = 0, headers: Map[String, Any] = null, errorFlow: IntegrationComposition = null)(implicit m: scala.reflect.Manifest[T]): T = {
    val context = this.getContext()
    context.sendAndReceive[T](message, timeout, headers, errorFlow)
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
  
  def -->[T <: IntegrationComposition](a: T)(implicit g: ComposableIntegrationComponent[T]) = {
    if (this.logger.isDebugEnabled()) this.logger.debug("Adding " + a.target + " to " + this.target)
    g.compose(this, a)
  }
    
}

/**
 *
 */
class IntegrationComposition(parentComposition: BaseIntegrationComposition, target: IntegrationComponent)
  extends SendingIntegrationComposition(parentComposition, target) {
  /**
   *
   */
  def -->[T <: IntegrationComposition](a: T)(implicit g: ComposableIntegrationComponent[T]) = {
    if (this.logger.isDebugEnabled()) this.logger.debug("Adding " + a.target + " to " + this.target)
    g.compose(this, a)
  }
}

/**
 *
 */
class ChannelIntegrationComposition(parentComposition: BaseIntegrationComposition, target: IntegrationComponent)
  extends IntegrationComposition(parentComposition, target) {

  /**
   *
   */
  def --<[T <: BaseIntegrationComposition](a: T*)(implicit g: ComposableIntegrationComponent[T]): SendingIntegrationComposition = {
    if (this.logger.isDebugEnabled()) {
      for (element <- a) this.logger.debug("Adding " + DslUtils.getStartingComposition(element).target + " to " + this.target)
    }
    g.compose(this, a: _*)
  }

}

/**
 *
 */
class PollableChannelIntegrationComposition(parentComposition: IntegrationComposition, target: IntegrationComponent)
  extends ChannelIntegrationComposition(parentComposition, target) {
  /**
   *
   */
  def -->(p: Poller) = new IntegrationComposition(this, p)

}

/**
 *
 */
private[dsl] abstract class ComposableIntegrationComponent[T] {
  def compose(c: IntegrationComposition, e: T): T
  
  def compose(c: ListeningIntegrationComposition, e: BaseIntegrationComposition): ListeningIntegrationComposition = {
    new ListeningIntegrationComposition(c, e.target)
  }

  def compose(c: IntegrationComposition, e: BaseIntegrationComposition*): SendingIntegrationComposition = {
    val buffer = new ListBuffer[BaseIntegrationComposition]()
    for (element <- e) {
      val copiedComposition = element.copy()
      val startingComposition = DslUtils.getStartingComposition(copiedComposition)
      DslUtils.injectParentComposition(startingComposition, c)
      buffer += copiedComposition
    }
    new SendingIntegrationComposition(c, new ListOfCompositions(buffer.toList))
  }
}
/**
 *
 */
private[dsl] class ListOfCompositions[T](val compositions: List[BaseIntegrationComposition]) extends IntegrationComponent(null)
/**
 *
 */
private[dsl] abstract class IntegrationComponent(val name: String = null)
