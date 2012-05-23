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
package org.springframework.integration.dsl.utils
import java.lang.Boolean
import java.lang.Double
import java.lang.Integer
import java.lang.Long
import java.lang.Short
import scala.collection.mutable.ArrayBuffer
import org.springframework.integration.dsl.ListOfCompositions
import org.springframework.integration.dsl.BaseIntegrationComposition
import org.springframework.integration.dsl.IntegrationComponent
import org.springframework.integration.dsl.IntegrationComponent
import org.springframework.integration.dsl.IntegrationComponent
import org.springframework.integration.dsl.IntegrationComponent
import org.w3c.dom.Element
import org.springframework.util.StringUtils

/**
 * @author Oleg Zhurakousky
 */
object DslUtils {

  /**
   *
   */
  def toProductTraversble[T <: BaseIntegrationComposition](integrationComposition: T): Traversable[Any] = {

    val products =
      for (product <- integrationComposition.productIterator if product != null) yield {
        product match {
          case composition: BaseIntegrationComposition =>
            this.toProductTraversble(composition)
          case lc: ListOfCompositions[BaseIntegrationComposition] =>
            List(for (element <- lc.compositions) yield this.toProductTraversble(element))
          case _ =>
            List(product.asInstanceOf[IntegrationComponent].toMapOfProperties)
        }
      }

    products.toList.flatten
  }

  /**
   * Will return the starting BaseIntegrationComposition of this BaseIntegrationComposition
   */
  def getStartingComposition(integrationComposition: BaseIntegrationComposition): BaseIntegrationComposition = {
    if (integrationComposition.parentComposition != null)
      this.getStartingComposition(integrationComposition.parentComposition)
    else
      integrationComposition
  }

  private[dsl] def injectParentComposition(rootComposition: BaseIntegrationComposition, parentComposition: BaseIntegrationComposition) = {
    val field = classOf[BaseIntegrationComposition].getDeclaredField("parentComposition")
    field.setAccessible(true)
    field.set(rootComposition, parentComposition)
  }

  private[dsl] def setAdditionalAttributes(element: Element, attributeMap: Map[String, Any]): Unit = {
    attributeMap.keys.foreach { key: String =>
      val propertyValue: Any = attributeMap.get(key).elements.next()
      val attributeName = Conventions.propertyNameToAttributeName(key)
      val propertyValueToSet =
        if (propertyValue != null) {
          propertyValue match {
            case str: String => if (StringUtils.hasText(str)) str else null
            case _ => propertyValue.toString()
          }
        } else {
          null
        }
      if (propertyValueToSet != null) {
        element.setAttribute(attributeName, propertyValueToSet)
      }
    }
  }

  //TODO - there must be something in Scala already to do that
  private[dsl] def toJavaType(t: Class[_]): Class[_] = {
    if (t.isAssignableFrom(classOf[scala.Int]))
      classOf[java.lang.Integer]
    else if (t.isAssignableFrom(classOf[scala.Long]))
      classOf[java.lang.Long]
    else if (t.isAssignableFrom(classOf[scala.Double]))
      classOf[java.lang.Double]
    else if (t.isAssignableFrom(classOf[scala.Short]))
      classOf[java.lang.Short]
    else if (t.isAssignableFrom(classOf[scala.Boolean]))
      classOf[java.lang.Boolean]
    else
      t
  }
}