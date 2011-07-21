/**
 *
 */
package org.springframework.integration.scala.dsl
import scala.collection.mutable.ListBuffer
import org.apache.log4j._
import scalaz._
import Scalaz._
/**
 * @author Oleg Zhurakousky
 *
 */
private[dsl] object ComposableEndpoint {

  def apply(component: AbstractEndpoint) = new ComposableEndpoint(component)
  implicit def toComposable(component: AbstractEndpoint): Composable = new ComposableEndpoint(component)
  implicit def fromComposable(composableComponent: ComposableEndpoint): AbstractEndpoint = {
    composableComponent.self
  }
}
/**
 * 
 */
private[dsl] class ComposableEndpoint(val self: AbstractEndpoint) extends Composable {

}
private[dsl] object ComposableChannel {

  def apply(component: AbstractChannel) = new ComposableChannel(component)
  implicit def toComposable(component: AbstractChannel): Composable = new ComposableChannel(component)
  implicit def fromComposable(composableComponent: ComposableChannel): AbstractChannel = {
    composableComponent.self
  }
}
private[dsl] class ComposableChannel(val self: AbstractChannel) extends Composable {

}

private[dsl] object ComposableGateway {

  def apply(component: IntegrationComponent with gateway) = new ComposableGateway(component)
  implicit def toComposable(component: IntegrationComponent with gateway): Composable = new ComposableGateway(component)
  implicit def fromComposable(composableComponent: ComposableGateway): IntegrationComponent with gateway = {
    composableComponent.self
  }
}
private[dsl] class ComposableGateway(val self: IntegrationComponent with gateway) extends Composable {

}
/**
 * 
 */
private[dsl] object Composable {
  implicit def componentToKleisli(assembledComponent: Composable): KleisliComponent = {
    new KleisliComponent(kleisli((s1: ListBuffer[Any]) => new MessageFlowComposer(s1, assembledComponent).map(r => r)))
  }
}
/**
 * 
 */
private[dsl] trait Composable extends Proxy {
  
  private[dsl] def produce(assembledComponent: Composable): Kleisli[Responder, ListBuffer[Any], ListBuffer[Any]] = {
    kleisli((assembledComponents: ListBuffer[Any]) => new MessageFlowComposer(assembledComponents, assembledComponent).map(r => r))
  }
  /**
   * Defines >=> as flow composition operator to add an AssembledComponent
   * while delegating to the real Kleisli >=> operator
   */
  def >=>(e: Composable): KleisliComponent = {
    this match {
      case k: KleisliComponent => {
        new KleisliComponent(k.kleisliComponent >=> produce(e))
      }
      case a: Composable => {
        new KleisliComponent(produce(this) >=> produce(e))
      }
      case _ => {
        throw new IllegalArgumentException("Unrecognized component " + e)
      }
    }
  }
//
  /**
   * Defines >=> as flow composition operator to add a collection of assembled KleisliComponent
   * while delegating to the real Kleisli >=> operator
   */
  def >=>(components: KleisliComponent*): KleisliComponent = {
    val thisK = produce(this)
    val kliesliBuffer = new ListBuffer[Any]
    for (kl <- components) {
      val listBuffer = new ListBuffer[Any]
      val b = kl.kleisliComponent.apply(listBuffer).respond(r => r)
      kliesliBuffer += listBuffer
    }
    new KleisliComponent(thisK >=> kleisli((s1: ListBuffer[Any]) => new MessageFlowComposer(s1, kliesliBuffer).map(r => r)))
  }

}

/**
 * Assembles Message flow continuation while also identifying
 * Channels (e.g., input/output) for Integration components
 */
private[dsl] class MessageFlowComposer(fromComponents: ListBuffer[Any], toComponents: Any) extends Responder[ListBuffer[Any]] {
  private val logger = Logger.getLogger(this.getClass)
  /**
   *
   */
  def respond(function: (ListBuffer[Any]) => Unit) = {
    toComponents match {
      case componentBuffer: ListBuffer[Any] => {
        for (lbRoute <- componentBuffer) {
          lbRoute match {
            case buffer: ListBuffer[Composable] => {
              val firstComponent: Composable = buffer.first
              this.wireComponents(fromComponents.last.asInstanceOf[Composable], firstComponent)
            }
            case _ => {
              throw new IllegalArgumentException("Unrecognized component: " + lbRoute)
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
            ch = new ComposableChannel(channel())
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
  private def addChannel(ic: Composable, ch: ComposableChannel, input: Boolean) {
    ic match {
      case gw: ComposableGateway => {
        if (!input) {
          logger.debug(">=> Adding default-request-channel '" + ch + "' to the " + gw)
          gw.defaultRequestChannel = ch
        } else {
          logger.debug(">=> Adding default-reply-channel '" + ch + "' to the " + gw)
          gw.defaultReplyChannel = ch
        }
      }
      case endpoint: ComposableEndpoint => {
        if (!input) {
          logger.debug(">=> Adding output-channel '" + ch + "' to the " + endpoint)
          endpoint.outputChannel = ch
        } else {
          logger.debug(">=> Adding input-channel '" + ch + "' to the " + endpoint)
          endpoint.inputChannel = ch
        }
      }
      case _ => {
        throw new IllegalArgumentException("OOOOPS: " + ic)
      }
    }
  }
}
