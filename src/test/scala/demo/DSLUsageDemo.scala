package org.springframework.eip.dsl

import org.junit.{Assert, Test}
import org.springframework.eip.dsl.DSL._
import org.springframework.integration.Message
import org.springframework.integration.message.GenericMessage

class DSLUsageDemo {

  @Test
  def simpleCompositionTestSend = {
    val messageFlow = 
      transform.using{m:Message[String] => m.getPayload().toUpperCase()} -->
      handle.using{m:Message[_] => println(m)}
      
    messageFlow.send("hello")
    println("done")
  }
  
  @Test
  def simpleCompositionTestSendAndReceive = {
    val messageFlow = 
      transform.using{m:Message[String] => m.getPayload().toUpperCase()} -->
      handle.using{m:Message[_] => println(m);m}
      
    val reply = messageFlow.sendAndReceive[String]("hello")
    println(reply)
  }
  
  @Test
  def simpleCompositionTestSendWithPubSubChannel = {
    val messageFlow = 
      transform.using{m:Message[String] => m.getPayload().toUpperCase()} -->
      PubSubChannel("pubSub") --< (
         transform.using{m:Message[_] => m.getPayload() + " - subscriber-1"} -->
         handle.using{m:Message[_] => println(m)}
         ,
         transform.using{m:Message[_] => m.getPayload() + " - subscriber-2"} -->
         handle.using{m:Message[_] => println(m)}
      )
      
    messageFlow.send("hello")
    println("done")
  }
  
  
  @Test
  def complexCompositionTestSend = {
    val messageFlow = SI(
      transform.using{m:Message[String] => m.getPayload().toUpperCase()} -->
      handle.using{m:Message[_] => println(m)}
    )
      
    messageFlow.send("hello")
    println("done")
  }
  
  @Test
  def complexCompositionTestSendAndReceive = {
    val messageFlow = SI(
      transform.using{m:Message[String] => m.getPayload().toUpperCase()} -->
      handle.using{m:Message[_] => println(m);m}
    )
      
    val reply = messageFlow.sendAndReceive[Message[_]]("hello")
    println(reply)
  }
  
   @Test
  def complexCompositionTestSendWithPubSubChannel = {
    val messageFlow = SI(
      transform.using{m:Message[String] => m.getPayload().toUpperCase()} -->
      PubSubChannel("pubSub") --< (
         transform.using{m:Message[_] => m.getPayload() + " - subscriber-1"} -->
         handle.using{m:Message[_] => println(m)}
         ,
         transform.using{m:Message[_] => m.getPayload() + " - subscriber-2"} -->
         handle.using{m:Message[_] => println(m)}
      )
    )
      
    messageFlow.send("hello")
    println("done")
  }
   
  @Test
  def complexCompositionTestSendWithSimpleRouter = {
    val helloChannel = Channel("helloChannel")
    val byeChannel = Channel("byeChannel")
    
    val messageFlow = SI(
      transform.using{m:Message[String] => m.getPayload().toUpperCase()} -->
      route.using{m:Message[String] => m.getPayload()}(
          when("HELLO") then helloChannel,
          when("BYE") then byeChannel
      )
      ,
      helloChannel --> 
      handle.using{m:Message[_] => println("From Hello channel - " + m)}
      ,
      byeChannel --> 
      handle.using{m:Message[_] => println("From Bye channel - " + m)}
    )
      
    messageFlow.send("hello")
    messageFlow.send("bye")
    println("done")
  }
  
  @Test
  def complexCompositionTestSendWithHeaderValueRouter = {
    val helloChannel = Channel("helloChannel")
    val byeChannel = Channel("byeChannel")
    
    val messageFlow = SI(
      transform.using{m:Message[String] => m.getPayload().toUpperCase()} -->
      route.onValueOfHeader("myHeader")(
          when("hello") then helloChannel,
          when("bye") then byeChannel
      )
      ,
      helloChannel --> 
      handle.using{m:Message[_] => println("From Hello channel - " + m)}
      ,
      byeChannel --> 
      handle.using{m:Message[_] => println("From Bye channel - " + m)}
    )
      
    messageFlow.send("hello", headers=Map("myHeader" -> "hello"))
    messageFlow.send("bye", headers=Map("myHeader" -> "bye"))
    println("done")
  }
  
  @Test
  def complexCompositionTestSendWithPayloadTypeRouterAndDefaultChannel = {
    val helloChannel = Channel("helloChannel")
    val byeChannel = Channel("byeChannel")
    
    val messageFlow = SI(
      transform.using{m:Message[_] => m} -->
      route.onPayloadType(
          when(classOf[String]) then helloChannel,
          when(classOf[Int]) then byeChannel
      ) --> 
      handle.using{m:Message[_] => println("From default channel - " + m)}
      ,
      helloChannel --> 
      handle.using{m:Message[_] => println("From Hello channel - " + m)}
      ,
      byeChannel --> 
      handle.using{m:Message[_] => println("From Bye channel - " + m)}
    )
      
    messageFlow.send("hello")
    messageFlow.send(123)
    messageFlow.send(true)
    println("done")
  }
  
  @Test
  def simpleCompositionTestSendWithBridge = {
    val messageFlow = 
      Channel("A") -->
      Channel("B") -->
      handle.using{m:Message[_] => println("From Hello channel - " + m)}
      
    messageFlow.send("hello")
    
    println("done")
  }
  
//  @Test
//  def simpleCompositionTestSendWithPolingBridge = {
//    val messageFlow = 
//      Channel("A") -->
//      Channel("B").withQueue --> poll.usingFixedRate(1) -->
//      handle.using{m:Message[_] => println("From Hello channel - " + m)}
//      
//    messageFlow.send("hello")
//    
//    println("done")
//  }
  
  def simpleCompositionWithEnricher = {
    val enrichFlow = 
      handle.using("someSpel") -->
      transform.using("someSpel")
      
    val bazEnrichFlow = 
      handle.using("someSpel") -->
      transform.using("someSpel")
      
//    val messageFlow = 
//      enrich.header.using("hello" -> "bye", "foo" -> "@myBean.foo()", "baz" -> {m:Message[_] => bazEnrichFlow.sendAndReceive(m)}) -->
//      enrich.payload.withData{m:Message[_] => m} -->
//      handle.using{m:Message[_] => println(m);m} -->
//      enrich.payload.using{payload:String => enrichFlow.sendAndReceive(payload)} -->
//      handle.using{m:Message[_] => println(m);m}
  }
}