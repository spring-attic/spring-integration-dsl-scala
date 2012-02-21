package org.springframework.eip.dsl

import org.junit.{Assert, Test}
import org.springframework.eip.dsl.DSL._
import org.springframework.integration.Message
import org.springframework.integration.message.GenericMessage

class DSLUsageTests {

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
}