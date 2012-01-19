/*
 * Copyright 2002-2011 the original author or authors.
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
//import org.springframework.integration.scala.dsl._
//import org.springframework.integration.Message
//import scala.collection.JavaConversions
//import java.util.Random
//import java.util.concurrent._
//import org.junit._
/**
 * @author Oleg Zhurakousky
 *
 */
class OrderProcessing {
  
//  @Test
//  def runDemo() = {
//    val validOrder = PurchaseOrder(List(
//      PurchaseOrderItem("books", "Spring Integration in Action"),
//      PurchaseOrderItem("books", "DSLs in Action"),
//      PurchaseOrderItem("bikes", "Canyon Torque FRX")))  
//      
//    val invalidOrder = PurchaseOrder(List())      
//   // gateway(classOf[OrderProcessingGateway]).withErrorChannel("errorFlowChannel");
//    val orderGateway = gateway.withErrorChannel("errorFlowChannel").using(classOf[OrderProcessingGateway]) // invert this as well wheere class ges first and then errir channel and stuff
//    val aggregationChannel = channel.withName("aggregationChannel")
//    val booksChannel = channel("booksChannel")
//    val bikesChannel = channel("bikesChannel")
//    
//    val integrationContext = IntegrationContext(   
//        {
//          orderGateway >=>    
//// consider asking for required first and then show all the optional things
//            // and may be use named parameters with overloaded constructurs
//            //filter("myname").using("")
//          filter.withName("orderValidator").andErrorOnRejection(true).using{p:PurchaseOrder => !p.items.isEmpty} >=>
//          split.using{p:PurchaseOrder => JavaConversions.asList(p.items)} >=>  
//          channel.withExecutor >=>
//          route.using(Map("books" -> {
//        	  booksChannel >=>
//        	  service.using{m:Message[_] => println("Processing books order: " + m); Thread.sleep(new Random().nextInt(2000)); m} >=>
//          }, "bikes" ->  {
//        	  bikesChannel >=>
//        	  service.using{m:Message[_] => println("Processing bikes order: " + m); m} >=>
//          })).using{pi:PurchaseOrderItem => pi.itemType} // invert this and move it on top
//          , "foo"
//        } >=>  aggregate(),
//        {
//          channel("errorFlowChannel") >=>
//	      service.using{m:Message[_] => println("Received ERROR: " + m); "ERROR processing order"}
//        },
//
//    )
//    
//    val reply = orderGateway.processOrder(validOrder)
//    println("Reply: " + reply)
////    orderGateway.processOrder(invalidOrder)
//  }
//  
//  trait OrderProcessingGateway  {
//    def processOrder(order:PurchaseOrder): Object
//  }
//
//  case class PurchaseOrder(val items: List[PurchaseOrderItem]) {
//  }
//
//  case class PurchaseOrderItem(val itemType: String, val title: String) {
//  }
  
  //    filter {
//      withName 'orderValidator'
//      errorOnRejection true
//      using classOf[OrderProcessingGateway]  
//    } >=>
//    splitter {
//      withName 'orderValidator'
//      errorOnRejection true
//      using classOf[OrderProcessingGateway]  
//    }
}
