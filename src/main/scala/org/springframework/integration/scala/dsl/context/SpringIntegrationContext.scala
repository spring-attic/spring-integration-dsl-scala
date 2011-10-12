/**
 * 
 */
package org.springframework.integration.scala.dsl.context

import eip._
import org.springframework.context._
import scala.collection.mutable.ListBuffer
/**
 * @author ozhurakousky
 *
 */
object SpringIntegrationContext {
  def apply(composition: Composition): SpringIntegrationContext = new SpringIntegrationContext(null, composition)
//  def apply(parentContext: ApplicationContext, compositions: Composition*): SpringIntegrationContext =
//    new SpringIntegrationContext(parentContext, compositions: _*)
}

class SpringIntegrationContext(parentContext: ApplicationContext, composition: Composition) {
  val buffer = new ListBuffer[Any]
  composition.self.apply(buffer).respond(new  CompositionMetadataAssembler)
  
  for (compositionElement <- buffer) {
    println(compositionElement)
  }
}