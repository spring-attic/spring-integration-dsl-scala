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
import scala.collection.mutable.ListBuffer
import scala.collection.mutable.WrappedArray
import java.lang.Long
import org.springframework.integration.dsl.BaseIntegrationComposition
import org.springframework.integration.dsl.ListOfCompositions
/**
 * @author Oleg Zhurakousky
 */
object DslUtils {

  /**
   * 
   */
  def toProductList[T <: BaseIntegrationComposition](integrationComposition: T): List[Any] = {

    val listBuffer = new ListBuffer[Any]()
    this.doToList(integrationComposition, listBuffer);
    listBuffer.toList
  }
 /**
  * Will return the starting BaseIntegrationComposition of this BaseIntegrationComposition
  */
  def getStartingComposition(integrationComposition: BaseIntegrationComposition): BaseIntegrationComposition = {
    if (integrationComposition.parentComposition != null) {
      getStartingComposition(integrationComposition.parentComposition)
    } else {
      integrationComposition
    }
  }
  
  private[dsl] def injectParentComposition(rootComposition:BaseIntegrationComposition, parentComposition:BaseIntegrationComposition) = {
    val field = classOf[BaseIntegrationComposition].getDeclaredField("parentComposition")
    field.setAccessible(true)
    field.set(rootComposition, parentComposition)
  }
  
  private[dsl] def toJavaType(t:Class[_]):Class[_] = {
    if (t.isAssignableFrom(classOf[scala.Int])) classOf[java.lang.Integer]
    else if (t.isAssignableFrom(classOf[scala.Long])) classOf[java.lang.Long]
    else if (t.isAssignableFrom(classOf[scala.Double])) classOf[java.lang.Double]
    else if (t.isAssignableFrom(classOf[scala.Short])) classOf[java.lang.Short]
    else if (t.isAssignableFrom(classOf[scala.Boolean])) classOf[java.lang.Boolean]
    else 
    t
  }

  private def doToList(integrationComposition: BaseIntegrationComposition, lb: ListBuffer[Any]): Unit = {
    for (p <- integrationComposition.productIterator) {
      if (p != null) {
        p match {
          case c: BaseIntegrationComposition => this.doToList(c, lb)

          case lc: ListOfCompositions[BaseIntegrationComposition] =>
            if (lc.compositions.size == 1) 
              lb += lc.compositions(0).target
            else 
              for (element <- lc.compositions) lb += this.toProductList(element)
              
          case _ => lb += p
        }
      }

    }
  }

}