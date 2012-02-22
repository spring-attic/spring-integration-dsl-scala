/**
 *
 */
package org.springframework.integration.dsl.builders

import org.springframework.beans.factory.support.BeanDefinitionBuilder
import org.springframework.integration.dsl._
import org.springframework.integration.transformer.HeaderEnricher.StaticHeaderValueMessageProcessor
import org.springframework.integration.transformer.HeaderEnricher.HeaderValueMessageProcessor
import org.springframework.integration.transformer.MessageTransformingHandler
import org.springframework.integration.transformer.HeaderEnricher
import scala.collection.JavaConversions
import org.springframework.expression.Expression
import scala.collection.mutable.WrappedArray

/**
 * @author ozhurakousky
 *
 */
private[dsl] object HeaderEnricherBuilder {

  def buildHandler(enricher: Enricher): BeanDefinitionBuilder = {
    val headerValueMessageProcessorMap: Map[String, HeaderValueMessageProcessor[_]] =
      enricher.target match {
        case tp: Tuple2[String, AnyRef] => {
          val headerValueMessageProcessor: HeaderValueMessageProcessor[_] =
            tp._2 match {
              case fn: Function[_, _] => this.doWithFunction(fn, enricher)

              case expression: Expression => this.doWithExpression(expression, enricher)

              case _ => this.doWithAny(tp._2, enricher)
            }
          Map[String, HeaderValueMessageProcessor[_]](tp._1 -> headerValueMessageProcessor)
        }
        case wa: WrappedArray[Tuple2[String, AnyRef]] => {
          var map = Map[String, HeaderValueMessageProcessor[_]]()
          for (element <- wa) {
            val headerValueMessageProcessor: HeaderValueMessageProcessor[_] =
              element._2 match {
                case fn: Function[_, _] => this.doWithFunction(fn, enricher)

                case expression: Expression => this.doWithExpression(expression, enricher)

                case _ => this.doWithAny(element._2, enricher)
              }
            map += (element._1 -> headerValueMessageProcessor)
          }
          map
        }
      }

    val handlerBuilder = BeanDefinitionBuilder.rootBeanDefinition(classOf[MessageTransformingHandler])
    val transformerBuilder = BeanDefinitionBuilder.rootBeanDefinition(classOf[HeaderEnricher])
    transformerBuilder.addConstructorArg(JavaConversions.asJavaMap(headerValueMessageProcessorMap))
    handlerBuilder.addConstructorArg(transformerBuilder.getBeanDefinition())
    handlerBuilder
  }

  private def doWithFunction(fn: Function1[_, _], enricher: Enricher): HeaderValueMessageProcessor[_] = {
    val clazz = Class.forName("org.springframework.integration.transformer.HeaderEnricher$MessageProcessingHeaderValueMessageProcessor")
    val functionInvoker = new ApplicationContextBuilder.FunctionInvoker(fn, enricher)
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