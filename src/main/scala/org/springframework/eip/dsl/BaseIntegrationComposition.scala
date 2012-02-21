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
import org.apache.log4j.Logger
import org.springframework.integration.Message

/**
 * @author Oleg Zhurakousky
 */
case class BaseIntegrationComposition(private[dsl] val parentComposition: BaseIntegrationComposition, private[dsl] val target: IntegrationComponent) {

  private[dsl] val logger = Logger.getLogger(this.getClass)

  private val threadLocal: ThreadLocal[SI] = new ThreadLocal[SI]
  
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
  def sendAndReceive[T <: Any](message: Any, timeout: Long = 0, headers: Map[String, Any] = null, errorFlow: BaseIntegrationComposition = null)(implicit m: scala.reflect.Manifest[T]): T = {
    val context = this.getContext()

    val reply = try {
      context.sendAndReceive[T](message)
    } 
    catch {
      case ex: Exception => {
        if (errorFlow != null) {
          errorFlow.sendAndReceive[T](ex)
        } else throw new RuntimeException("Failed to process Message through Error flow")
      }
    }

    reply
  }

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
    val field = classOf[BaseIntegrationComposition].getDeclaredField("parentComposition")
    field.setAccessible(true)
    field.set(startingComposition, this)
  }

  /**
   * Will add an input-channel to this composition (as a DirectChannel) if it does not begin with one
   */
  private[dsl] def normalizeComposition(): BaseIntegrationComposition = {

    val newComposition = this.copy()

    val startingComposition = DslUtils.getStartingComposition(newComposition)
    val field = classOf[BaseIntegrationComposition].getDeclaredField("parentComposition")
    field.setAccessible(true)
    field.set(startingComposition, Channel("$ch_" + UUID.randomUUID().toString.substring(0, 8)))

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
  
  private def getContext(): SI = {

    threadLocal.get() match {
      case eipContext: SI => {
        if (logger.isDebugEnabled) logger.debug("Retrieving existing EIP context")
        eipContext
      }
      case _ => {
        val eipContext = SI(this)
        threadLocal.set(eipContext)
        eipContext
      }
    }
  }
}

/**
 *
 */
case class IntegrationComposition(override private[dsl] val parentComposition: BaseIntegrationComposition, override private[dsl] val target: IntegrationComponent)
  extends BaseIntegrationComposition(parentComposition, target) {
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
case class ChannelIntegrationComposition(override private[dsl] val parentComposition: BaseIntegrationComposition, override private[dsl] val target: IntegrationComponent)
  extends IntegrationComposition(parentComposition, target) {

  /**
   * 
   */
  def --<[T <: BaseIntegrationComposition](a: T*)(implicit g: ComposableIntegrationComponent[T]): BaseIntegrationComposition = {
    if (this.logger.isDebugEnabled()) {
      for (element <- a) this.logger.debug("Adding " + DslUtils.getStartingComposition(element).target + " to " + this.target)
    }
    g.compose(this, a: _*)
  }

}

/**
 * 
 */
case class PollableChannelIntegrationComposition(override private[dsl] val parentComposition: IntegrationComposition, override private[dsl] val target: IntegrationComponent)
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

  def compose(c: IntegrationComposition, e: T*): BaseIntegrationComposition = {
    new BaseIntegrationComposition(c, new ListOfCompositions(e: _*))
  }
}
/**
 * 
 */
private[dsl] class ListOfCompositions[T](val compositions: T*) extends IntegrationComponent(null)
/**
 * 
 */
private[dsl] abstract class IntegrationComponent(val name: String = null)
