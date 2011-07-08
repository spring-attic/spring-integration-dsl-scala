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
package org.springframework.integration.scala.dsl
import scala.collection.mutable.ListBuffer
import java.lang.reflect._
import org.apache.log4j._
import java.util._
import java.util.concurrent._
import org.springframework.integration._
import org.springframework.integration.channel._
import org.springframework.context._
import scalaz._
import Scalaz._

/**
 * @author Oleg Zhurakousky
 *
 */
object IntegrationComponent {
  private[dsl] class ConcatResponder(fromComponents: ListBuffer[Any], toComponents: Any) extends Responder[ListBuffer[Any]] {
    private val logger = Logger.getLogger(this.getClass)
    /**
     * 
     */
    def respond(function: (ListBuffer[Any]) => Unit) = {
      toComponents match {
        case componentBuffer:ListBuffer[Any] => {
          for(lbRoute <- componentBuffer){
            lbRoute match {
              case buffer:ListBuffer[IntegrationComponent] => {
                val firstComponent:IntegrationComponent = buffer.first
                this.wireComponents(fromComponents.last.asInstanceOf[IntegrationComponent], firstComponent)
              }
              case _ => {
                throw new IllegalArgumentException("Unrecognized component: " + lbRoute)
              }
            }
          }
        }
        case ic:IntegrationComponent => {
          if (!fromComponents.isEmpty){
            val fromComponent:IntegrationComponent = fromComponents.last.asInstanceOf[IntegrationComponent]
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
    private def wireComponents(from:IntegrationComponent, to:IntegrationComponent) {
      from match {
        case ch:AbstractChannel => {
          this.addChannel(to, ch, true)
        }
        case ic:IntegrationComponent => {
          var ch:AbstractChannel = null
          var inputRequired = false
          to match {
            case c:AbstractChannel => {
              ch = c
            }
            case _ => {
              inputRequired = true
              ch = channel()
            }
          }
          this.addChannel(ic, ch, false)
          if (inputRequired){
            this.addChannel(to, ch, true)
          }
        }
      }
    }
    /*
     * 
     */
    private def addChannel(ic:IntegrationComponent, ch:AbstractChannel, input:Boolean) {
      ic match {
        case gw:gateway => {
          if (!input){
            logger.debug(">=> Adding default-request-channel '" + ch + "' to the " + gw)
            gw.defaultRequestChannel = ch
          }
          else {
            logger.debug(">=> Adding default-reply-channel '" + ch + "' to the " + gw)
            gw.defaultReplyChannel = ch
          } 
        }
        case endpoint:AbstractEndpoint => {
          if (!input){
            logger.debug(">=> Adding output-channel '" + ch + "' to the " + endpoint)
            endpoint.outputChannel = ch
          }
          else {
            logger.debug(">=> Adding input-channel '" + ch + "' to the " + endpoint)
            endpoint.inputChannel = ch
          } 
        }
        case _ =>{
          throw new IllegalArgumentException("OOOOPS")
        }
      }
    }
  }
  
  // END 
  implicit def componentToKleisli(x: InitializedComponent): Kleisli[Responder, ListBuffer[Any], ListBuffer[Any]] = {
    kleisli((s1: ListBuffer[Any]) => new ConcatResponder(s1, x).map(r => r))
  }
  private[dsl] def compose(e: InitializedComponent): Kleisli[Responder, ListBuffer[Any], ListBuffer[Any]] = {
    kleisli((s1: ListBuffer[Any]) => new ConcatResponder(s1, e).map(r => r))
  }

  val name = "componentName"
  val outputChannel = "outputChannel"
  val inputChannelName = "inputChannelName"
  val queueCapacity = "queueCapacity"
  val executor = "executor"
  val poller = "poller"
  val using = "using"
  val handler = "handler"
  val errorChannelName = "errorChannelName"
  val targetObject = "targetObject"
  val targetMethodName = "targetMethodName"
  val expressionString = "expressionString"

  // POLLER Constants
  val maxMessagesPerPoll = "maxMessagesPerPoll"
  val fixedRate = "fixedRate"
  val cron = "cron"
  val trigger = "trigger"
  val pollerMetadata = "pollerMetadata"
}
/**
 *
 */
abstract class IntegrationComponent {
  private[dsl] val logger = Logger.getLogger(this.getClass)

  private[dsl] val configMap = new HashMap[Any, Any]

}
/**
 * Trait which defines '->' method which composes the Message Flow
 */
trait InitializedComponent extends IntegrationComponent {

  import IntegrationComponent._

  def >=>(e: InitializedComponent): Kleisli[Responder, ListBuffer[Any], ListBuffer[Any]] = {
    compose(this) >=> compose(e)
  }

  def >=>(e: Kleisli[Responder, ListBuffer[Any], ListBuffer[Any]]*): Kleisli[Responder, ListBuffer[Any], ListBuffer[Any]] = {
    val thisK = compose(this)
      val kliesliBuffer = new ListBuffer[Any]
      for (kl <- e) {
        val listBuffer = new ListBuffer[Any]
        val b = kl.apply(listBuffer).respond(r => r)
        kliesliBuffer += listBuffer
        println()
      }
      thisK >=> kleisli((s1: ListBuffer[Any]) => new ConcatResponder(s1, kliesliBuffer).map(r => r))
  }
}
/**
 *
 */
trait andName extends IntegrationComponent with using {
  def andName(componentName: String): IntegrationComponent with using = {
    this.configMap.put(IntegrationComponent.name, componentName)
    this
  }
}