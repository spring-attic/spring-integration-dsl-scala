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
import org.springframework.eip.dsl._
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

    val orderProcessingFlow = 
      filter.using{p:PurchaseOrder => !p.items.isEmpty}.where(exceptionOnRejection = true) -->
      split.using{p:PurchaseOrder => p.items} -->
      Channel.withDispatcher(taskExecutor = Executors.newCachedThreadPool) -->
      route.using{pi:PurchaseOrderItem => pi.itemType}(
        when("books") {
          handle.using{m:Message[_] => println("Processing bikes order: " + m); m} // prints Message and returns it
        },
        when("bikes") {
          handle.using{
            m:Message[_] => println("Processing books order: " + m); Thread.sleep(new Random().nextInt(2000)); m
          } // prints Message, delays it randomly and returns it
        }
      ) -->
      aggregate()

    val errorFlow = handle.using{m:Message[_] => println("Received ERROR: " + m); "ERROR processing order"}

    val result = orderProcessingFlow.sendAndReceive[Any](validOrder, errorFlow = errorFlow)
    //val result = orderProcessingFlow.sendAndReceive[Any](invalidOrder, errorFlow = errorFlow)

    println("Result: " + result)

  }

  case class PurchaseOrder(val items: List[PurchaseOrderItem])

  case class PurchaseOrderItem(val itemType: String, val title: String)

}
