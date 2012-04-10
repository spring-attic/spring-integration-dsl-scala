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
/**
 * @author Oleg Zhurakousky
 */
class ParentApplicationContextTests {
    
  @Test
  def validateInitializationOfParentAc = {
    val context = new ClassPathXmlApplicationContext("parent-config.xml")
    
    val messageFlow = handle.using(context.getBean("simpleService"))
   
    Assert.assertEquals(context.getBean("simpleService"), messageFlow.getContext(context).applicationContext.getBean("simpleService"))
  }

}