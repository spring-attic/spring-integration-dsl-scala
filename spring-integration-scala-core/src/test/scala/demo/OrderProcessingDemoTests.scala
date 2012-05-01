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
package demo
import org.springframework.integration.dsl._

import org.springframework.integration.Message
import java.util.Random
import org.junit._
import java.util.concurrent.Executors


/**  
 * @author Oleg Zhurakousky
 *
 */
class OrderProcessingDemoTests {
  
  @Test
  def runDemo() = {

    val validOrder = PurchaseOrder(List(
      PurchaseOrderItem("books", "Spring Integration in Action"),
      PurchaseOrderItem("books", "DSLs in Action"),
      PurchaseOrderItem("bikes", "Canyon Torque FRX")))

    val invalidOrder = PurchaseOrder(List())
    
    val errorFlow = handle{m:Message[_] => println("Received ERROR: " + m); "ERROR processing order"}
    
    val aggregationFlow = aggregate()

    val bikeFlow = 
      handle{m:Message[_] => println("Processing bikes order: " + m); m} --> 
      aggregationFlow
   
    val orderProcessingFlow =
      filter{p:PurchaseOrder => !p.items.isEmpty}.where(exceptionOnRejection = true) -->
      split{p:PurchaseOrder => p.items} -->
      Channel.withDispatcher(taskExecutor = Executors.newCachedThreadPool) -->    
      route{pi:PurchaseOrderItem => pi.itemType}(
        when("books") then 
            handle{m:Message[_] => println("Processing books order: " + m); m} --> 
        	aggregationFlow, 
        when("bikes") then 
            bikeFlow    	 
      ) 

    val resultValid = orderProcessingFlow.sendAndReceive[Any](validOrder, errorFlow = errorFlow)
    println("Result: " + resultValid)
    
    val resultInvalid = orderProcessingFlow.sendAndReceive[Any](invalidOrder, errorFlow = errorFlow)
    println("Result: " + resultInvalid)
    
  }

  case class PurchaseOrder(val items: List[PurchaseOrderItem])

  case class PurchaseOrderItem(val itemType: String, val title: String)

}
