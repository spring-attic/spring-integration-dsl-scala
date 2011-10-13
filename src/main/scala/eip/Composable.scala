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
import scala.collection.mutable.ListBuffer
import org.apache.log4j._
import scalaz._
import Scalaz._
/**
 * @author Oleg Zhurakousky
 *
 */
private[eip] object Composable {

  implicit def toComposable(component: EipComponent): Composable = new Composable(component){}
    
  implicit def fromComposable(composableComponent: Composable): EipComponent = {
    composableComponent.self
  }
}

/**
 *
 */
private[eip] class Composable(val self: EipComponent) extends ComposableEipComponent {}

/**
 *
 */
private[eip] class ComposableChannel(override val self: Channel) extends Composable(self) {}

/**
 *
 */
final class Composition(val k: Kleisli[Responder, ListBuffer[Any], ListBuffer[Any]]) extends ComposableEipComponent {
  def self = k
}

/**
 *
 */
private[eip] trait ComposableEipComponent extends Proxy {

  private[eip] def compose(composable: ComposableEipComponent): Kleisli[Responder, ListBuffer[Any], ListBuffer[Any]] = {
    kleisli((assembledComponents: ListBuffer[Any]) => new MessageFlowComposer(assembledComponents, composable).map(r => r))
  }
  /**
   * Defines >=> as flow composition operator to add an AssembledComponent
   * while delegating to the real Kleisli >=> operator
   */
  def >=>(e: ComposableEipComponent): Composition = {
    this match {
      case k: Composition => {
        println("Composition")
        new Composition(k.self >=> compose(e))
      }
      case a: ComposableEipComponent => {
        println("ComposableEipComponent")
        new Composition(compose(this) >=> compose(e))
      }
      case _ => {
        throw new IllegalArgumentException("Unrecognized component " + e)
      }
    }
  }
  //
//  /**
//   * Defines >=> as flow composition operator to add a collection of assembled KleisliComponent
//   * while delegating to the real Kleisli >=> operator
//   */
//  def >=>(components: Composition*): Composition = {
//    val thisK = compose(this)
//    val kliesliBuffer = new ListBuffer[Any]
//    for (kl <- components) {
//      val listBuffer = new ListBuffer[Any]
//      val b = kl.self.apply(listBuffer).respond(r => r)
//      kliesliBuffer += listBuffer
//    }
//    new Composition(thisK >=> kleisli((s1: ListBuffer[Any]) => new MessageFlowComposer(s1, kliesliBuffer).map(r => r)))
//  }

}

/**
 * Assembles Message flow continuation while also identifying
 * Channels (e.g., input/output) for Integration components
 */
private class MessageFlowComposer(fromComponents: ListBuffer[Any], toComponents: Any) extends Responder[ListBuffer[Any]] {
  private val logger = Logger.getLogger(this.getClass)
  /**
   *
   */
  def respond(function: (ListBuffer[Any]) => Unit) = {
    toComponents match {
      case componentBuffer: ListBuffer[Any] => {
        for (subFlow <- componentBuffer) {
          subFlow match {
            case buffer: ListBuffer[Composable] => {
              val firstComponent: Composable = buffer.first
              this.wireComponents(fromComponents.last.asInstanceOf[Composable], firstComponent)
            }
            case _ => {
              throw new IllegalArgumentException("Unrecognized configuration: " + subFlow)
            }
          }
        }
      }
      case ic: Composable => {
        if (!fromComponents.isEmpty) {
          val fromComponent: Composable = fromComponents.last.asInstanceOf[Composable]
          this.wireComponents(fromComponent, ic)
        }
      }
    }

    val s = fromComponents += toComponents
    function(s)
  }
  /*
   * 
   */
  private def wireComponents(from: Composable, to: Composable) {
    from match {
      case ch: ComposableChannel => {
        this.addChannel(to, ch, true)
      }
      case ic: Composable => {
        var ch: ComposableChannel = null
        var inputRequired = false
        to match {
          case c: ComposableChannel => {
            ch = c
          }
          case _ => {
            inputRequired = true
            ch = new ComposableChannel(new P2PChannel(null))
          }
        }
        this.addChannel(ic, ch, false)
        if (inputRequired) {
          this.addChannel(to, ch, true)
        }
      }
    }
  }
  /*
     * 
     */
  private def addChannel(ic: Composable, ch: EipComponent, input: Boolean) {
    ic match {
      //      case gw: ComposableGateway => {
      //        if (!input) {
      //          logger.debug(">=> Adding default-request-channel '" + ch + "' to the " + gw)
      //          gw.defaultRequestChannel = ch
      //        } else {
      //          logger.debug(">=> Adding default-reply-channel '" + ch + "' to the " + gw)
      //          gw.defaultReplyChannel = ch
      //        }
      //      }
      case endpoint: ComposableEipComponent => {
        if (!input) {
          logger.debug(">=> Adding output-channel '" + ch + "' to the " + endpoint)
          endpoint.configMap  += (EipComponent.outputChannel -> ch)
        } else {
          logger.debug(">=> Adding input-channel '" + ch + "' to the " + endpoint)
          endpoint.configMap  += (EipComponent.inputChannel -> ch)
        }
      }
      case _ => {
        throw new IllegalArgumentException("OOOOPS: " + ic)
      }
    }
  }
}
