package demo
import org.springframework.integration.dsl._
import org.springframework.messaging.Message
import java.util.concurrent.Executors

/**
 * @author fbalicchia
 */
object OrderProcessingApp {

  def main(args: Array[String]): Unit = {

    val validOrder = PurchaseOrder(List(
      PurchaseOrderItem("books", "Spring Integration in Action"),
      PurchaseOrderItem("books", "DSLs in Action"),
      PurchaseOrderItem("bikes", "Canyon Torque FRX")))

    val invalidOrder = PurchaseOrder(List())

    val errorFlow = handle { m: Message[_] => println("Received ERROR: " + m); "ERROR processing order" }

    val aggregateOrder = aggregate()

    val processBikeOrder =
      handle { m: Message[_] => println("Processing bikes order: " + m); m } -->
        aggregateOrder

    val orderProcessingFlow =
        filter { p: PurchaseOrder => !p.items.isEmpty } -->
        split { p: PurchaseOrder => p.items } -->
        Channel.withDispatcher(taskExecutor = Executors.newCachedThreadPool) -->
        route { pi: PurchaseOrderItem => pi.itemType }(
          when("books") andThen
            handle { m: Message[_] => println("Processing books order: " + m); m } -->
            aggregateOrder,
          when("bikes") andThen
            processBikeOrder)

    val validOrderResult = orderProcessingFlow.sendAndReceive[Any](validOrder, errorFlow = errorFlow)
    println("Result: " + validOrderResult)
  }
}

case class PurchaseOrder(val items: List[PurchaseOrderItem])

case class PurchaseOrderItem(val itemType: String, val title: String)