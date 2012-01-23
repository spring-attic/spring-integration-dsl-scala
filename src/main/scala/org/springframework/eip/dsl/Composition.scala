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

import org.springframework.integration.Message
import org.springframework.integration.message.GenericMessage
import org.springframework.beans.factory.BeanFactory
import org.springframework.beans.factory.support.{BeanDefinitionBuilder, DefaultListableBeanFactory}
import org.springframework.integration.config.TransformerFactoryBean

/**
 * @author Oleg Zhurakousky
 * Date: 1/12/12
 */
private[dsl] abstract class Composition(val parentComposition:Composition, val target:Any) {
  private var started = false

  def exposeChannel(channel:MessageChannelComposition with PollableComposition): Receivable =  {
    if (!started){
      this.start()
    }
    new Receivable {
      def receive(): Message[_]  = {
        println("stubbing out receive")
        new GenericMessage[String]("hello")
      }

      def receive(timeout:Int): Message[_] = {
        println("stubbing out receive with timeout")
        new GenericMessage[String]("hello")
      }

      def send(message:Message[_]) = {
        println("stubbing out send")
      }
    }
  }

  def exposeChannel(channel:MessageChannelComposition with Composition): Sendable =  {
    if (!started){
      this.start()
    }
    new Sendable {
      def send(message:Message[_]) = {
        println("stubbing out send")
      }
    }
  }

  def start(): Unit = {
    this.doStart(new DefaultListableBeanFactory)
    started = true
  }
  
  private def doStart(beanFactory:BeanFactory): Unit = {
    println(this.target)
    this.target match {
      case xfmr:Transformer => {
        //BeanDefinitionBuilder transformerBuilder = BeanDefinitionBuilder.genericBeanDefinition(classOf[TransformerFactoryBean]);
      }
    }

    if (this.parentComposition != null){
      this.parentComposition.doStart(beanFactory)
    }
  }
}

/**
 *
 */
private[dsl] case class SimpleComposition(override val parentComposition:Composition, override val target:Any)
  extends Composition(parentComposition, target){

  def -->(composition: SimpleComposition) = {
    composition.copy(this, composition.target)
  }

  def -->(composition: PollableComposition) = {
    composition.copy(this, composition.target)
  }
}

/**
 *
 */
private[dsl] abstract class ConditionComposition(override val parentComposition:Composition, override val target:Any) extends Composition(parentComposition, target){

  def -->(composition: SimpleComposition): ConditionComposition

  def -->(composition: PollableComposition): ConditionComposition

}

private[dsl] case class PayloadTypeConditionComposition(override val parentComposition:Composition, override val target:Any)
      extends ConditionComposition(parentComposition, target)   {

  def -->(composition: SimpleComposition) = new PayloadTypeConditionComposition(this, composition.target)

  def -->(composition: PollableComposition) = new PayloadTypeConditionComposition(this, composition.target)
}

private[dsl] case class HeaderValueConditionComposition(override val parentComposition:Composition, override val target:Any)
      extends ConditionComposition(parentComposition, target) {

  def -->(composition: SimpleComposition) = new HeaderValueConditionComposition(this, composition.target)

  def -->(composition: PollableComposition) = new HeaderValueConditionComposition(this, composition.target)
}

/**
 *
 */
private[dsl] case class PollableComposition(override val parentComposition:Composition, override val target:Channel) extends Composition(parentComposition, target){

  def -->(poller: Poller):SimpleComposition = {
    new SimpleComposition(this, poller)
  }

}