package demo

import org.junit.{ Assert, Test }
import org.springframework.integration.dsl._
import org.springframework.integration.dsl.DSL._
import org.springframework.integration.Message
import org.springframework.integration.message.GenericMessage
import org.springframework.integration.dsl.builders.transform
import org.springframework.integration.dsl.builders.enrich
import org.springframework.integration.dsl.builders.Channel
import org.springframework.integration.dsl.builders.PubSubChannel
import org.springframework.integration.dsl.builders.poll
import org.springframework.integration.dsl.builders.handle
import org.springframework.expression.spel.standard.SpelExpressionParser
import org.springframework.expression.spel.SpelParserConfiguration

class DSLUsageDemo {

  @Test
  def demoSend = {
    val messageFlow =
      transform.using { m: Message[String] => m.getPayload().toUpperCase() } -->
        handle.using { m: Message[_] => println(m) }

    messageFlow.send("hello")
    println("done")
  }

  @Test
  def sdemoSendAndReceive = {
    val messageFlow =
      transform.using { m: Message[String] => m.getPayload().toUpperCase() } -->
        handle.using { m: Message[_] => println(m); m }

    val reply = messageFlow.sendAndReceive[String]("hello")
    println(reply)
  }

  @Test
  def demoSendWithPubSubChannel = {
    val messageFlow =
      transform.using { m: Message[String] => m.getPayload().toUpperCase() } -->
        PubSubChannel("pubSub") --< (
          transform.using { m: Message[_] => m.getPayload() + " - subscriber-1" } -->
          handle.using { m: Message[_] => println(m) },
          transform.using { m: Message[_] => m.getPayload() + " - subscriber-2" } -->
          handle.using { m: Message[_] => println(m) })

    messageFlow.send("hello")
    println("done")
  }

  @Test
  def demoSendWithBridge = {
    val messageFlow =
      Channel("A") -->
        Channel("B") -->
        handle.using { m: Message[_] => println("From Hello channel - " + m) }

    messageFlow.send("hello")

    println("done")
  }

  @Test
  def demoSendWithPolingBridge = {
    val messageFlow =
      Channel("A") -->
        Channel("B").withQueue --> poll.usingFixedRate(1) -->
        handle.using { m: Message[_] => println("From Hello channel - " + m) }

    messageFlow.send("hello")
    Thread.sleep(1000)
    println("done")
  }

  @Test
  def headerEnricherWithTuple = {
    val enricherA = enrich.header("hello" -> "bye") --> handle.using { m: Message[_] => println(m) }
    enricherA.send("Hello")
    println("done")
  }

  @Test
  def headerEnricherWithFunctionAsValue = {
    val enricherB = enrich.header("hello" -> Some({ m: Message[String] => m.getPayload().toUpperCase() })) --> handle.using { m: Message[_] => println(m) }
    enricherB.send("Hello")
    println("done")
  }

  @Test
  def headerEnricherWithMessageFunctionAsProcessor = {
    val enricherB = enrich.header("hello" -> { m: Message[String] => m.getPayload().toUpperCase() }) --> handle.using { m: Message[_] => println(m) }
    enricherB.send("Hello")
    println("done")
  }

  @Test
  def headerEnricherWithStringA = {
    val enricherB = enrich.header("hello" -> { "boo" + "bar" }) --> handle.using { m: Message[_] => println(m) }
    enricherB.send("Hello")
    println("done")
  }

  @Test
  def headerEnricherWithStringB = {
    val enricherB = enrich.header("hello" -> "foo") --> handle.using { m: Message[_] => println(m) }
    enricherB.send("Hello")
    println("done")
  }

  @Test
  def headerEnricherWithExpression = {
    val expression = new SpelExpressionParser(new SpelParserConfiguration(true, true)).parseExpression("(2 * 6) + ' days of Christmas'");
    val enricherB = enrich.header("phrase" -> expression) --> handle.using { m: Message[_] => println(m) }
    enricherB.send("Hello")
    println("done")
  }

  @Test
  def headerEnricherWithMultiTuple = {
    val expression = new SpelExpressionParser(new SpelParserConfiguration(true, true)).parseExpression("(2 * 6) + ' days of Christmas'");
    val enricherB =
      enrich.headers("foo" -> "foo",
        "bar" -> { m: Message[String] => m.getPayload().toUpperCase() },
        "phrase" -> expression) -->
      handle.using { m: Message[_] => println(m) }
      
    enricherB.send("Hello")
    println("done")
  }

  @Test
  def simpleCompositionWithEnricher = {
    //    val enrichFlow = 
    //      handle.using("someSpel") -->
    //      transform.using("someSpel")
    //      
    //    val bazEnrichFlow = 
    //      handle.using("someSpel") -->
    //      transform.using("someSpel")
    //      
    //    val someServiceAsAFlow =   
    //      handle.using("someSpel") -->
    //      transform.using("someSpel")
    //    
    //    val enricherA = enrich.headers("hello" -> "bye", "foo" -> "@myBean.foo()", "baz" -> {m:Message[_] => bazEnrichFlow.sendAndReceive(m)}) 
    //    val enricherA = enrich.header("hello" -> "bye") --> handle.using{m:Message[_] => println(m)}
    //    enricherA.send("Hello")
    //    
    //    val enricherB = enrich.header("hello" -> Some({m:Message[String] => m.getPayload().toUpperCase()})) --> handle.using{m:Message[_] => println(m)}
    //    enricherB.send("Hello")
    //    
    //    val enricherC = enrich.header("hello" -> {m:Message[String] => m.getPayload().toUpperCase()}) --> handle.using{m:Message[_] => println(m)}
    //    enricherC.send("Hello")
    //    
    //    val enricherD = enrich.header("hello" -> {m:Message[String] => m.getPayload().toUpperCase()}) --> handle.using{m:Message[_] => println(m)}
    //    enricherD.send("Hello")
    //    
    //    val enricherE = enrich.header("hello" -> {"Hello"}) --> handle.using{m:Message[_] => println(m)}
    //    enricherE.send("Hello")
    ()
    //    val enricherE = enrich{p:Person => 
    //      val employee = someServiceAsAFlow.sendAndReceive[Employee]("123-45-2453")
    //      p.name = employee.firstName + employee.lastName
    //      p.age = employee.age
    //      p
    //    }
    //val enricherF = enrich{""}
  }

  class Person(var name: String, var age: Int)

  class Employee(val firstName: String, val lastName: String, val age: Int)
}