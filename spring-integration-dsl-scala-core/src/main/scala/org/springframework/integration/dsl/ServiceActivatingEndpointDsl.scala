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
package org.springframework.integration.dsl
import org.springframework.beans.factory.support.BeanDefinitionBuilder
import org.springframework.integration.config.ServiceActivatorFactoryBean
import org.springframework.util.StringUtils
import java.util.UUID
import scala.collection.immutable.WrappedString
import org.w3c.dom.Element
import org.w3c.dom.Document

/**
 * This class provides DSL and related components to support "Service Activator" pattern
 *
 * @author Oleg Zhurakousky
 */

object handle {

  private trait In[I, O] {
    def apply(function: _ => I): O
    def apply(function: (_, Map[String, _]) => I): O
  }

  private trait InOut {
    implicit def anySendingEndpointComposition[I] = new In[I, SendingEndpointComposition with WithAttributesContinued] {
      def apply(function: _ => I) = new SendingEndpointComposition(null, new ServiceActivator(name = "$sa_" + function.hashCode ,target = function)) with WithAttributesContinued{
        def additionalAttributes(name: String) = doWithAttributesWithContinuity(name, function)
      }
      def apply(function: (_, Map[String, _]) => I) = new SendingEndpointComposition(null, new ServiceActivator(name = "$sa_" + function.hashCode, target = function)) with WithAttributesContinued{
        def additionalAttributes(name: String) = doWithAttributesWithContinuity(name, function)
      }
    }
  }

  private object In extends InOut {
    implicit object UnitUnit extends In[Unit, SendingIntegrationComposition with WithAttributes] {
      def apply(function: _ => Unit) = new SendingEndpointComposition(null, new ServiceActivator(name = "$sa_" + function.hashCode, target = function)) with WithAttributes{
        def additionalAttributes(name: String) = doWithAttributesWithoutContinuity(name, function)
      }
      def apply(function: (_, Map[String, _]) => Unit) = new SendingEndpointComposition(null, new ServiceActivator(name = "$sa_" + function.hashCode, target = function)) with WithAttributes{
        def additionalAttributes(name: String) = doWithAttributesWithoutContinuity(name, function)
      }
    }
  }

  private def doWithAttributesWithContinuity(name: String, target:  => Any) = {
    require(StringUtils.hasText(name), "'name' must not be empty")
    new SendingEndpointComposition(null, new ServiceActivator(name = name, target = target))
  }
  private def doWithAttributesWithoutContinuity(name: String, target:  => Any) = {
    require(StringUtils.hasText(name), "'name' must not be empty")
    new SendingIntegrationComposition(null, new ServiceActivator(name = name, target = target))
  }

  trait WithAttributes {
    def additionalAttributes(name: String):SendingIntegrationComposition
  }
  trait WithAttributesContinued {
    def additionalAttributes(name: String):SendingEndpointComposition
  }

  def apply[F, R](function: _ => F)(implicit ab: In[F, R]): R = ab.apply(function)

  def apply[F, R](function: (_, Map[String, _]) => F)(implicit ab: In[F, R]): R = ab.apply(function)

}

private[dsl] class ServiceActivator(name: String, target: Any)
  						extends SimpleEndpoint(name, target) {

  override def build(document: Document = null,
    targetDefinitionFunction: Function1[Any, Tuple2[String, String]],
    compositionInitFunction: Function2[BaseIntegrationComposition, AbstractChannel, Unit] = null,
    inputChannel:AbstractChannel,
    outputChannel:AbstractChannel): Element = {

    require(inputChannel != null, "'inputChannel' must be provided")

    val element = document.createElement("int:service-activator")
    element.setAttribute("id", this.name)
    val targetDefinnition = targetDefinitionFunction.apply(this.target)
    element.setAttribute("ref", targetDefinnition._1);
    element.setAttribute("method", targetDefinnition._2);
    element.setAttribute("input-channel", inputChannel.name);
    if (outputChannel != null){
      element.setAttribute("output-channel", outputChannel.name);
    }
    element
  }
}