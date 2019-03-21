/*
 * Copyright 2002-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.integration.dsl
import org.springframework.util.StringUtils
import java.util.UUID
import org.springframework.beans.factory.support.BeanDefinitionBuilder
import org.springframework.integration.transformer.HeaderEnricher.HeaderValueMessageProcessor
import org.springframework.integration.config.TransformerFactoryBean
import org.springframework.integration.transformer.MessageTransformingHandler
import org.springframework.integration.transformer.HeaderEnricher
import org.springframework.expression.Expression
import scala.collection.mutable.WrappedArray
import scala.collection.JavaConversions
import org.w3c.dom.Element
import org.w3c.dom.Document

/**
 * This class provides DSL and related components to support "Content Enricher"
 * pattern (both 'payload' and 'headers')
 *
 * @author Oleg Zhurakousky
 */
object enrich {

  trait RestrictiveFunction[A, B]

  type NotUnitType[T] = RestrictiveFunction[T, Unit]

  implicit def nsub[A, B]: RestrictiveFunction[A, B] = null
  implicit def nsubAmbig1[A, B >: A]: RestrictiveFunction[A, B] = null
  implicit def nsubAmbig2[A, B >: A]: RestrictiveFunction[A, B] = null

  def apply[R: NotUnitType](function: Function1[_, R]) = new SendingEndpointComposition(null, new Enricher(target = function)) {
    def additionalAttributes(name: String) = {
      require(StringUtils.hasText(name), "'name' must not be empty")
      new SendingEndpointComposition(null, new Enricher(name = name, target = function))
    }
  }

  def apply[T, R: NotUnitType](function: (_, Map[String, _]) => R) = new SendingEndpointComposition(null, new Enricher(target = function)) {
    def additionalAttributes(name: String) = {

      require(StringUtils.hasText(name), "'name' must not be empty")
      new SendingEndpointComposition(null, new Enricher(name = name, target = function))
    }
  }

  def headers(headersMap: (Tuple2[String, AnyRef])*) = new SendingEndpointComposition(null, new Enricher(target = headersMap)) {
    def additionalAttributes(name: String) = {
      require(StringUtils.hasText(name), "'name' must not be empty")
      new SendingEndpointComposition(null, new Enricher(name = name, target = headersMap))
    }
  }

  def header(headerMap: Tuple2[String, Any]) = new SendingEndpointComposition(null, new Enricher(target = headerMap)) {
    def additionalAttributes(name: String) = {
      require(StringUtils.hasText(name), "'name' must not be empty")
      new SendingEndpointComposition(null, new Enricher(name = name, target = headerMap))
    }
  }
}

private[dsl] class Enricher(name: String = "$enr_" + UUID.randomUUID().toString.substring(0, 8), target: Any)
  extends SimpleEndpoint(name, target) {

  override def build(document: Document,
    targetDefinitionFunction: Function1[Any, Tuple2[String, String]],
    compositionInitFunction: Function2[BaseIntegrationComposition, AbstractChannel, Unit],
    inputChannel:AbstractChannel,
    outputChannel:AbstractChannel): Element = {

    require(inputChannel != null, "'inputChannel' must be provided")

    val headerValueTargetDefinitions: Map[String, _] =
      this.target match {
        case tp @ (t1: String, _)  => {
          val targetDefinition =
            tp._2 match {
              case fn: Function[_, _] => targetDefinitionFunction.apply(fn)

              case s @ Some(_: Function[_, _]) => "@" + targetDefinitionFunction.apply(s)._1

              case _ => tp._2
            }
          Map(t1 -> targetDefinition)
        }
        case wa: WrappedArray[Tuple2[String, _]] => {
          val result =
            for (element <- wa) yield {
              val targetDefinition =
                element._2 match {
                  case fn: Function[_, _] => targetDefinitionFunction.apply(fn)

                  case s @ Some(_: Function[_, _]) => "@" + targetDefinitionFunction.apply(s)._1

                  case _ => element._2
                }
              (element._1 -> targetDefinition)
            }
          Map(result: _*)
        }
        case _ => null
      }

    val element = headerValueTargetDefinitions match {
      case null => {
        val transformerElement = document.createElement("int:transformer")
        transformerElement.setAttribute("id", this.name)
        val targetDefinnition = targetDefinitionFunction.apply(this.target)
        transformerElement.setAttribute("ref", targetDefinnition._1);
        transformerElement.setAttribute("method", targetDefinnition._2);
        transformerElement
      }
      case _ => {
        val headerEnricherElement = document.createElement("int:header-enricher")
        headerEnricherElement.setAttribute("id", this.name)

        headerValueTargetDefinitions.foreach { s: Tuple2[String, Any] =>
          val headerElement = document.createElement("int:header")
          headerElement.setAttribute("name", s._1)
          s._2 match {
            case fn @  (t1: String, t2: String) => {
              headerElement.setAttribute("ref", t1)
              headerElement.setAttribute("method", t2)
            }
            case _ =>
              headerElement.setAttribute("value", s._2.toString())

          }
          headerEnricherElement.appendChild(headerElement)
        }
        headerEnricherElement
      }
    }
    element.setAttribute("input-channel", inputChannel.name);
    if (outputChannel != null){
      element.setAttribute("output-channel", outputChannel.name);
    }
    element
  }
}