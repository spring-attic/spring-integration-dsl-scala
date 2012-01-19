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

/**
 * @author Oleg Zhurakousky
 */

/**
 * SERVICE ACTIVATOR
 */
object handle {

  def using(function:Function1[_,_]) = new SimpleComposition(null, new ServiceActivator(null, function)) with Where {
    def where(name:String)= new SimpleComposition(null, new ServiceActivator(name, function))
  }

  def using(spelExpression:String) = new SimpleComposition(null, new ServiceActivator(null, spelExpression)) with Where {
    def where(name:String)= new SimpleComposition(null, new ServiceActivator(name, spelExpression))
  }

  private[handle] trait Where {
    def where(name:String): SimpleComposition
  }
}

/**
 * TRANSFORMER
 */
object transform {

  def using(function:Function1[_,AnyRef]) = new SimpleComposition(null, new Transformer(null, function)) with Where {
    def where(name:String)= new SimpleComposition(null, new Transformer(name, function))
  }

  def using(spelExpression:String) = new SimpleComposition(null, new Transformer(null, spelExpression)) with Where {
    def where(name:String)= new SimpleComposition(null, new Transformer(name, spelExpression))
  }

  private[transform] trait Where {
    def where(name:String): SimpleComposition
  }
}

private[dsl] case class ServiceActivator(val name:String, val target:Any)

private[dsl] case class Transformer(val name:String, val target:Any)