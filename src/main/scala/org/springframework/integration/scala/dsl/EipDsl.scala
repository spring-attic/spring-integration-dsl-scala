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
  val applySequence = "applySequence"

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
 *
 */
trait andName extends IntegrationComponent {
  def andName(componentName: String): IntegrationComponent = {
    this.configMap.put(IntegrationComponent.name, componentName)
    this
  }
}
///**
// * Wrapper over Kleisli composition
// */
private[dsl] class KleisliComponent(val k: Kleisli[Responder, ListBuffer[Any], ListBuffer[Any]]) extends Composable {
  val kleisliComponent = k
  
  def self = k
}