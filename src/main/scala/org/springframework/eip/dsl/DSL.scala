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
  
  implicit def anyComponent[T <: BaseIntegrationComposition]  = new ComposableIntegrationComponent[T] { 
    def compose(i: IntegrationComposition, s:T) = {
      val composition = s match {
        case ch:ChannelIntegrationComposition => {
          new ChannelIntegrationComposition(i, s.target)
        }
        case int:IntegrationComposition => {
          new IntegrationComposition(i, s.target)
        }
        case _ => {
          new IntegrationComposition(i, s.target)
        }
      }
      composition.asInstanceOf[T]
    }
  }
  
  /**
   * 
   */
  implicit def channelComponent  = new ComposableIntegrationComponent[ChannelIntegrationComposition] { 
    def compose(i: IntegrationComposition, c:ChannelIntegrationComposition) = {
      new ChannelIntegrationComposition(i, c.target)
    }
  }
  
  /**
   * 
   */
  implicit def pollableChannelComponent  = new ComposableIntegrationComponent[PollableChannelIntegrationComposition] { 
    def compose(i: IntegrationComposition, c:PollableChannelIntegrationComposition) = {
      new PollableChannelIntegrationComposition(i, c.target)
    }
  }
  
}

/**
 * 
 */
abstract class ComposableIntegrationComponent[T] {
  def compose(c: IntegrationComposition, e:T) :T 
  
  def compose(c: IntegrationComposition, e:T*) :BaseIntegrationComposition = {
    new BaseIntegrationComposition(c, e)
  }
}

/**
 * 
 */
case class BaseIntegrationComposition(private[dsl] val parentComposition:IntegrationComposition, private[dsl] val target:Any) {
  private[dsl] val logger = Logger.getLogger(this.getClass)
}

/**
 * 
 */
case class IntegrationComposition(override private[dsl] val parentComposition:IntegrationComposition, override private[dsl] val target:Any) 
															extends BaseIntegrationComposition(parentComposition, target) { 
  def -->[T <: IntegrationComposition](a: T)(implicit g :ComposableIntegrationComponent[T]) = {
    if (this.logger.isDebugEnabled()){
      this.logger.debug("Adding " + a.target + " to " + this.target)
    }
    g.compose(this, a)
  }
 
}

/**
 * 
 */
case class ChannelIntegrationComposition(override private[dsl] val parentComposition:IntegrationComposition, override private[dsl] val target:Any) 
															extends IntegrationComposition(parentComposition, target) {
  
  def --<[T <: BaseIntegrationComposition](a :T*)(implicit g :ComposableIntegrationComponent[T]):BaseIntegrationComposition = {
    if (this.logger.isDebugEnabled()){
      for (element <- a){
        this.logger.debug("Adding " + DslUtils.getStartingComposition(element).target + " to " + this.target) 
      }
      
    }
    g.compose(this, a: _*)
  }
  
}

case class PollableChannelIntegrationComposition(override private[dsl] val parentComposition:IntegrationComposition, override private[dsl] val target:Any) 
															extends ChannelIntegrationComposition(parentComposition, target) {
  
  def -->(p :Poller) = new IntegrationComposition(this, p)
  
}
