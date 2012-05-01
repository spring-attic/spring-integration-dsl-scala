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
import org.junit.{ Assert, Test }
import java.util.HashMap
/**
 * @author Oleg Zhurakousky
 */
class EnricherTests {
  
  case class Person(var name: String = null, var age: Int = 0)

  case class Employee(val firstName: String, val lastName: String, val age: Int)

  @Test
  def validateEnricherWithFunctionAsTargetInvokingJavaObject {
    
    val employee = new Employee("John", "Doe", 23)
    val service = new SimpleTransformer
    
    val enricher =
      enrich { p: Person => p.name = service.transform(employee.firstName) + " " + service.transform(employee.lastName); p.age = employee.age; p } 

    val person = enricher.sendAndReceive[Person](new Person)
    
    Assert.assertEquals(person.name, "JOHN DOE")
  }
  
  @Test
  def validateEnricherWithPayloadAndHeaders {
    
    val employee = new Employee("John", "Doe", 23)
    val service = new SimpleTransformer
    
    val enricher =
      enrich { (p: Person, h:Map[String, _]) => p.name = service.transform(employee.firstName) + " " + 
      service.transform(h.apply("replacedName").asInstanceOf[String]); p.age = employee.age; p } 

    val person = enricher.sendAndReceive[Person](new Person, headers=Map("replacedName" -> "dude"))
    
    Assert.assertEquals(person.name, "JOHN DUDE")
  }
}