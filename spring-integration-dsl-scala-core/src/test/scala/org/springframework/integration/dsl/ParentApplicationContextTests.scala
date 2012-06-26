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
import org.junit.Test
import org.springframework.context.support.ClassPathXmlApplicationContext
import org.junit.Assert
import org.springframework.beans.factory.BeanFactoryAware

/**
 * @author Oleg Zhurakousky / Ewan Benfield
 */
class ParentApplicationContextTests {
    
  @Test
  def validateInitializationOfParentAc = {
    val context = new ClassPathXmlApplicationContext("parent-config.xml")

    val messageFlow:BaseIntegrationComposition = context.getBean("messageFlow")  match {
      case elt:BaseIntegrationComposition => elt
      case _ => throw new ClassCastException
    }
    Assert.assertEquals(context.getBean("messageFlow"), messageFlow.getContext(context).applicationContext.getBean("messageFlow"))
  }
  object scalaDslFlowFactory {

    def createFlow(): BaseIntegrationComposition = {

      val messageFlow =
        filter {payload: String => payload == "World"} -->
          transform { payload: String => "Hello " + payload} -->
          handle { payload: String => println(payload) }

      messageFlow
    }
  }
}