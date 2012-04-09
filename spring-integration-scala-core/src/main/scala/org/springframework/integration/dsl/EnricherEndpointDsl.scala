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

/**
 * This class provides DSL and related components to support "Content Enricher" 
 * pattern (both 'payload' and 'headers') 
 * 
 * @author Oleg Zhurakousky
 */
object enrich {

  def apply(function: Function1[_, AnyRef]) = new SendingEndpointComposition(null, new Enricher(target = function)) {
    def where(name: String) = {
      require(StringUtils.hasText(name), "'name' must not be empty")
      new SendingEndpointComposition(null, new Enricher(name = name, target = function))
    }
  }

  def headers(headersMap: (Tuple2[String, AnyRef])*) = new SendingEndpointComposition(null, new Enricher(target = headersMap)) {
    def where(name: String) = {
      require(StringUtils.hasText(name), "'name' must not be empty")
      new SendingEndpointComposition(null, new Enricher(name = name, target = headersMap))
    }
  }

  def header(headerMap: Tuple2[String, AnyRef]) = new SendingEndpointComposition(null, new Enricher(target = headerMap)) {
    def where(name: String) = {
      require(StringUtils.hasText(name), "'name' must not be empty")
      new SendingEndpointComposition(null, new Enricher(name = name, target = headerMap))
    }
  }
}

private[dsl] class Enricher(name: String = "$enr_" + UUID.randomUUID().toString.substring(0, 8), target: Any)
  extends SimpleEndpoint(name, target) {
  
  override def build(targetDefFunction: Function2[SimpleEndpoint, BeanDefinitionBuilder, Unit],
                     compositionInitFunction: Function2[BaseIntegrationComposition, AbstractChannel, Unit]): BeanDefinitionBuilder = {
    val headerValueMessageProcessorMap: Map[String, HeaderValueMessageProcessor[_]] =
      this.target match {
        case tp: Tuple2[String, AnyRef] => {
          val headerValueMessageProcessor: HeaderValueMessageProcessor[_] =
            tp._2 match {
              case fn: Function[_, _] => this.doWithFunction(fn, this)

              case expression: Expression => this.doWithExpression(expression, this)

              case _ => this.doWithAny(tp._2, this)
            }
          Map[String, HeaderValueMessageProcessor[_]](tp._1 -> headerValueMessageProcessor)
        }
        case wa: WrappedArray[Tuple2[String, AnyRef]] => {
          var map = Map[String, HeaderValueMessageProcessor[_]]()
          for (element <- wa) {
            val headerValueMessageProcessor: HeaderValueMessageProcessor[_] =
              element._2 match {
                case fn: Function[_, _] => this.doWithFunction(fn, this)

                case expression: Expression => this.doWithExpression(expression, this)

                case _ => this.doWithAny(element._2, this)
              }
            map += (element._1 -> headerValueMessageProcessor)
          }
          map
        }
        case fn: Function1[_, AnyRef] => null
      }

    val handlerBuilder =
      if (headerValueMessageProcessorMap == null) {
        val handlerBuilder = BeanDefinitionBuilder.rootBeanDefinition(classOf[TransformerFactoryBean])
        val functionInvoker = new FunctionInvoker(this.target.asInstanceOf[Function[_, _]], this)
        handlerBuilder.addPropertyValue("targetObject", functionInvoker);
        handlerBuilder.addPropertyValue("targetMethodName", functionInvoker.methodName);
        handlerBuilder
      } else {
        val handlerBuilder = BeanDefinitionBuilder.rootBeanDefinition(classOf[MessageTransformingHandler])
        val transformerBuilder = BeanDefinitionBuilder.rootBeanDefinition(classOf[HeaderEnricher])
        transformerBuilder.addConstructorArgValue(JavaConversions.mapAsJavaMap(headerValueMessageProcessorMap))
        handlerBuilder.addConstructorArgValue(transformerBuilder.getBeanDefinition())
        handlerBuilder
      }
    handlerBuilder
  }

  private def doWithFunction(fn: Function1[_, _], enricher: Enricher): HeaderValueMessageProcessor[_] = {

    // The following try/catch is necessary to maintain backward compatibility with 2.0 and 2.1.0. See INT-2399 for more details
    val clazz =
      try {
        Class.forName("org.springframework.integration.transformer.HeaderEnricher$MessageProcessingHeaderValueMessageProcessor")
      } catch {
        case e: ClassNotFoundException =>
          Class.forName("org.springframework.integration.transformer.HeaderEnricher$MethodInvokingHeaderValueMessageProcessor")
      }

    val functionInvoker = new FunctionInvoker(fn, enricher)
    val const = clazz.getDeclaredConstructor(classOf[Any], classOf[String])
    const.setAccessible(true)
    val p = const.newInstance(functionInvoker, functionInvoker.methodName)
    p.asInstanceOf[HeaderValueMessageProcessor[_]]
  }

  private def doWithExpression(expression: Expression, enricher: Enricher): HeaderValueMessageProcessor[_] = {
    val clazz = Class.forName("org.springframework.integration.transformer.HeaderEnricher$ExpressionEvaluatingHeaderValueMessageProcessor")
    val const = clazz.getDeclaredConstructor(classOf[Expression], classOf[Class[_]])
    const.setAccessible(true)
    val p = const.newInstance(expression, null)
    p.asInstanceOf[HeaderValueMessageProcessor[_]]
  }

  private def doWithAny(value: Object, enricher: Enricher): HeaderValueMessageProcessor[_] = {
    val clazz = Class.forName("org.springframework.integration.transformer.HeaderEnricher$StaticHeaderValueMessageProcessor")
    val const = clazz.getDeclaredConstructor(classOf[Any])
    const.setAccessible(true)
    val p = const.newInstance(value)
    p.asInstanceOf[HeaderValueMessageProcessor[_]]
  }
}