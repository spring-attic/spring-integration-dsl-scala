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
package org.springframework.integration.scala.dsl
import org.junit._

import java.util.concurrent._
import org.springframework.integration.gateway._
import org.springframework.integration._
import org.springframework.integration.channel._
import org.springframework.integration.message._
import org.springframework.aop.framework._
import org.springframework.aop.target._
import org.springframework.context.support._
/**
 * @author Oleg Zhurakousky
 *
 */
class DslUsageTests{
  
  @Test
  def testGatewayRequestObjectOnly() {
     val orderGateway = gateway.using(classOf[RequestObjectOnly])
       
     val integrationContext = SpringIntegrationContext(
        orderGateway ->
        service.using{order:String => order.toUpperCase()} ->
        service.using{m:Message[_] => println(m)}
     )
     
     orderGateway.processOrder("Spring Integration in Action")
  }
  
  @Test
  def testGatewayRequestObjectReplyObject() {
     val orderGateway = gateway.using(classOf[RequestObjectReplyObject])
       
     val integrationContext = SpringIntegrationContext(
        orderGateway ->
        service.using{order:String => order.toUpperCase()} 
     )
     
     var reply = orderGateway.processOrder("Spring Integration in Action").asInstanceOf[String]
     assert(reply.equals("SPRING INTEGRATION IN ACTION"))
  }
  
  @Test
  def testGatewayRequestObjectReplyMessage() {
     val orderGateway = gateway.using(classOf[RequestObjectReplyMessage])
       
     val integrationContext = SpringIntegrationContext(
        orderGateway ->
        service.using{order:String => order.toUpperCase()} 
     )
     
     var reply = orderGateway.processOrder("Spring Integration in Action")
     assert(reply.isInstanceOf[Message[_]])
     assert(reply.asInstanceOf[Message[_]].getPayload.equals("SPRING INTEGRATION IN ACTION"))
  }
  
  @Test
  def testGatewayRequestMessageReplyMessage() {
     val orderGateway = gateway.using(classOf[RequestMessageReplyMessage])
       
     val integrationContext = SpringIntegrationContext(
        orderGateway ->
        service.using{order:String => order.toUpperCase()} 
     )
     
     var reply = orderGateway.processOrder(new GenericMessage[String]("Spring Integration in Action"))
     assert(reply.isInstanceOf[Message[_]])
     assert(reply.asInstanceOf[Message[_]].getPayload.equals("SPRING INTEGRATION IN ACTION"))
  }
  
  @Test
  def testGatewayRequestObjectOnlyWithErrorChannel() {
     
     val orderGateway = gateway.withErrorChannel("errChannel").using(classOf[RequestObjectOnly])
       
     val integrationContext = SpringIntegrationContext(
         {
           orderGateway ->
           service.using{order:String => order.toUpperCase()} ->
           service.using{m:Message[_] => throw new IllegalArgumentException("intentional")}
         },
         {
           channel("errChannel") ->
           service.using{errorMessage:Message[_] => println(errorMessage)}
         }
     )
     
     orderGateway.processOrder("Spring Integration in Action")
  }
  
  @Test
  def testGatewayRequestObjectReplyObjectWithErrorChannel() {
     val orderGateway = gateway.withErrorChannel("errChannel").using(classOf[RequestObjectReplyObject])
       
     val integrationContext = SpringIntegrationContext(
         {
           orderGateway ->
           service.using{order:String => throw new IllegalArgumentException("intentional")} 
         },
         {
           channel("errChannel") ->
           service.using{errorMessage:Message[_] => "You got ERROR: " + errorMessage.getPayload}
         }   
     )
     
     var reply = orderGateway.processOrder("Spring Integration in Action").asInstanceOf[String]
     assert(reply.startsWith("You got ERROR"))
  }
  
  @Test
  def testWithParentContext() {
     

     val parentContext = new ClassPathXmlApplicationContext("parent-config.xml", this.getClass);
     
     val inputChannel = channel("inputChannel")
     
     val integrationContext = SpringIntegrationContext(parentContext,
        inputChannel ->
        service.using("@simpleService.printMessage(#this)")
     )
     
     inputChannel.send(new GenericMessage("Hello from Scala"))
  }
   
  trait RequestObjectOnly  {
    def processOrder(order:String): Unit
  }
  trait RequestObjectReplyObject  {
    def processOrder(order:String): Object
  }
  trait RequestObjectReplyMessage  {
    def processOrder(order:String): Message[_]
  }
  trait RequestMessageReplyMessage  {
    def processOrder(order:Message[_]): Message[_]
  }

}