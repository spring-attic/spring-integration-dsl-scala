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
import scala.collection.mutable.WrappedArray
import org.apache.log4j.Logger

/**
 * @author Oleg Zhurakousky
 */
object DSL {

  implicit def anyComponent[T <: BaseIntegrationComposition] = new ComposableIntegrationComponent[T] {
    def compose(i: IntegrationComposition, s: T):T  = {
      println("anyComponent")
      val mergedComposition = if (s.parentComposition != null) {
        val copyComposition = s.copy()
        i.merge(copyComposition)
        i.generateComposition(copyComposition.parentComposition, copyComposition.target) 
      } else {
        i.generateComposition(i, s.target) 
      }
      
      mergedComposition.asInstanceOf[T]
    }
  }

  /**
   *
   */
  implicit def channelComponent = new ComposableIntegrationComponent[ChannelIntegrationComposition] {
    def compose(i: IntegrationComposition, c: ChannelIntegrationComposition) = {
      new ChannelIntegrationComposition(i, c.target)
    }
  }

  /**
   *
   */
  implicit def pollableChannelComponent = new ComposableIntegrationComponent[PollableChannelIntegrationComposition] {
    def compose(i: IntegrationComposition, c: PollableChannelIntegrationComposition) = {
      new PollableChannelIntegrationComposition(i, c.target)
    }
  }

}

/**
 *
 */
abstract class ComposableIntegrationComponent[T] {
  def compose(c: IntegrationComposition, e: T): T

  def compose(c: IntegrationComposition, e: T*): BaseIntegrationComposition = {
    new BaseIntegrationComposition(c, e)
  }
}

/**
 *
 */
case class BaseIntegrationComposition(private[dsl] val parentComposition: BaseIntegrationComposition, private[dsl] val target: Any) {

  private[dsl] val logger = Logger.getLogger(this.getClass)

  private val threadLocal: ThreadLocal[$] = new ThreadLocal[$]
  
  /**
   * 
   */
  private[dsl] def copy(): BaseIntegrationComposition = {
    
    val parentComposition = if (this.parentComposition != null) this.parentComposition.copy() else null
    this.generateComposition(parentComposition, this.target)
    
  }
  
  /**
   * 
   */
  private[dsl] def merge(toComposition:BaseIntegrationComposition) = {
    val startingComposition = DslUtils.getStartingComposition(toComposition)
    val field = classOf[BaseIntegrationComposition].getDeclaredField("parentComposition")
    field.setAccessible(true)
    field.set(startingComposition, this)
  }

  def send(message: Any, timeout: Long = 0, headers: Map[String, Any] = null): Boolean = {
    //    //val context = this.getContext()
    //    context.send(message, timeout, headers)
    false
  }

  def sendAndReceive[T](message: Any, timeout: Long = 0, headers: Map[String, Any] = null, errorFlow: EIPConfigurationComposition = null)(implicit m: scala.reflect.Manifest[T]): T = {
    //    val context = this.getContext()
    //
    //    val reply = try {
    //       val replyMessage = context.sendAndReceive(message)
    //       if (m.erasure.isAssignableFrom(classOf[Message[_]])){
    //         replyMessage.asInstanceOf[T]
    //       }
    //       else {
    //         val reply = replyMessage.getPayload
    //         val convertedReply = reply match {
    //           case list:java.util.List[_]  => {
    //             list.toList
    //           }
    //           case list:java.util.Set[_] => {
    //             list.toSet
    //           }
    //           case list:java.util.Map[_, _] => {
    //             list.toMap
    //           }
    //           case _ => {
    //             reply
    //           }
    //         }
    //         convertedReply.asInstanceOf[T]
    //       }
    //    } catch {
    //      case ex:Exception => {
    //        if (errorFlow != null){
    //          errorFlow.sendAndReceive[T](ex)
    //        }
    //        else {
    //          throw new RuntimeException("boo")
    //        }
    //      }
    //    }
    //
    //    reply.asInstanceOf[T]
    null.asInstanceOf[T]
  }
  
  private[dsl] def generateComposition[T <: BaseIntegrationComposition](parent:T, child:Any) :T = {
    val composition = child match  {
      case ch:Channel => {
        new ChannelIntegrationComposition(parent, child)
      }
      case pch:PubSubChannel => {
        new ChannelIntegrationComposition(parent, child)
      }
      case _ => {
        new IntegrationComposition(parent, child)
      }
    }
    composition.asInstanceOf[T]
  } 
}

/**
 *
 */
case class IntegrationComposition(override private[dsl] val parentComposition: BaseIntegrationComposition, override private[dsl] val target: Any)
  extends BaseIntegrationComposition(parentComposition, target) {
  def -->[T <: IntegrationComposition](a: T)(implicit g: ComposableIntegrationComponent[T]) = {
    if (this.logger.isDebugEnabled()) {
      this.logger.debug("Adding " + a.target + " to " + this.target)
    }
    g.compose(this, a)
  }

}

/**
 *
 */
case class ChannelIntegrationComposition(override private[dsl] val parentComposition: BaseIntegrationComposition, override private[dsl] val target: Any)
  extends IntegrationComposition(parentComposition, target) {

  def --<[T <: BaseIntegrationComposition](a: T*)(implicit g: ComposableIntegrationComponent[T]): BaseIntegrationComposition = {
    if (this.logger.isDebugEnabled()) {
      for (element <- a) {
        this.logger.debug("Adding " + DslUtils.getStartingComposition(element).target + " to " + this.target)
      }

    }
    g.compose(this, a: _*)
  }

}

case class PollableChannelIntegrationComposition(override private[dsl] val parentComposition: IntegrationComposition, override private[dsl] val target: Any)
  extends ChannelIntegrationComposition(parentComposition, target) {

  def -->(p: Poller) = new IntegrationComposition(this, p)

}
