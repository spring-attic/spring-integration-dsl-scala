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
import org.springframework.eip.dsl._
import org.springframework.eip.dsl.DSL._
import org.springframework.integration.Message
import java.util.Random
import org.junit._
import java.util.concurrent.Executors

/**
 * @author Oleg Zhurakousky
 *
 */
class OrderProcessing {
  
  @Test
  def runDemo() = {

    val validOrder = PurchaseOrder(List(
      PurchaseOrderItem("books", "Spring Integration in Action"),
      PurchaseOrderItem("books", "DSLs in Action"),
      PurchaseOrderItem("bikes", "Canyon Torque FRX")))

    val invalidOrder = PurchaseOrder(List())
    
    val bookChannel = Channel("bookChannel")
    val bikesChannel = Channel("bikesChannel")
    val eFlow = handle.using{m:Message[_] => println("Received ERROR: " + m); "ERROR processing order"}
    val aggregationChannel = Channel("aggregationChannel")
    
    
//    val bikeFlow = 
//      handle.using{m:Message[_] => println("Processing bikes order: " + m); m} --> 
//      aggregationFlow
//      
//    val bookFlow = 
//      handle.using{m:Message[_] => println("Processing bikes order: " + m); m} --> 
//      aggregationFlow
//      
//    val aggregationFlow = 
//      aggregationChannel -->
//      aggregate()
   
//    val orderProcessingFlow =
//      filter.using{p:PurchaseOrder => !p.items.isEmpty}.where(exceptionOnRejection = true) -->
//      split.using{p:PurchaseOrder => p.items} -->
//      Channel.withDispatcher(taskExecutor = Executors.newCachedThreadPool) -->    
//      route.using{pi:PurchaseOrderItem => pi.itemType}(
//        when("books") then bikeFlow, 
//        when("bikes") then 
//        	handle.using{m:Message[_] => println("Processing bikes order: " + m); m} --> 
//        	aggregationFlow  
//      ) 
//
//    val result = orderProcessingFlow.sendAndReceive[Any](validOrder, errorFlow = eFlow)
////    val result = orderProcessingFlow.sendAndReceive[Any](invalidOrder, errorFlow = eFlow)
//
//    println("Result: " + result)
//    
//    // orderProcessingFlow.aggregationChannel.sendAndRecieve???

  }

  case class PurchaseOrder(val items: List[PurchaseOrderItem])

  case class PurchaseOrderItem(val itemType: String, val title: String)

}
