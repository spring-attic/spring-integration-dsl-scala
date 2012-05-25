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
import java.util.concurrent.Executors

import scala.collection.JavaConversions._

import org.junit.Assert
import org.junit.Test
import org.springframework.integration.dsl.utils.IntegrationDomTreeBuilder
import org.springframework.integration.endpoint.EventDrivenConsumer
import org.springframework.integration.endpoint.PollingConsumer
import org.springframework.integration.Message
import org.springframework.util.xml.DomUtils
import org.w3c.dom.Document
import org.w3c.dom.Element
/**
 * @author Oleg Zhurakousky
 */
class IntegrationDomTreeBuilderTests {

  @Test
  def generateDirectChannel = {
    val messageFlow = Channel("foo")

    val document = IntegrationDomTreeBuilder.toDocument(messageFlow)
    val elements = document.getElementsByTagName("int:channel")
    Assert.assertEquals(1, elements.getLength());
    Assert.assertEquals(1, elements.item(0).getAttributes().getLength());
    Assert.assertEquals("foo", elements.item(0).getAttributes().getNamedItem("id").getNodeValue())

    val applicationContext = IntegrationDomTreeBuilder.buildApplicationContext(messageFlow)
    Assert.assertTrue(applicationContext.containsBean("foo"))
  }

  @Test
  def generatePubSubChannel = {

    val messageFlow = PubSubChannel("foo")
    val document = IntegrationDomTreeBuilder.toDocument(messageFlow)
    val elements = document.getElementsByTagName("int:publish-subscribe-channel")
    Assert.assertEquals(1, elements.getLength());
    Assert.assertEquals(1, elements.item(0).getAttributes().getLength());
    Assert.assertEquals("foo", elements.item(0).getAttributes().getNamedItem("id").getNodeValue())

    val applicationContext = IntegrationDomTreeBuilder.buildApplicationContext(messageFlow)
    Assert.assertTrue(applicationContext.containsBean("foo"))
  }

  @Test
  def generateQueueChannel = {

    val messageFlow = Channel("foo").withQueue

    val document = IntegrationDomTreeBuilder.toDocument(messageFlow)
    val elements = document.getElementsByTagName("int:channel")
    Assert.assertEquals(1, elements.getLength());
    val channelElement = elements.item(0).asInstanceOf[Element]
    Assert.assertEquals(1, channelElement.getAttributes().getLength());
    Assert.assertEquals("foo", channelElement.getAttribute("id"))

    val childElements = channelElement.getElementsByTagName("int:queue")
    Assert.assertEquals(1, childElements.getLength());

    val applicationContext = IntegrationDomTreeBuilder.buildApplicationContext(messageFlow)
    Assert.assertTrue(applicationContext.containsBean("foo"))
  }

  @Test
  def generateExecutorChannel = {

    val messageFlow = Channel("foo").withDispatcher(taskExecutor = Executors.newCachedThreadPool())

    val document = IntegrationDomTreeBuilder.toDocument(messageFlow)
    val elements = document.getElementsByTagName("int:channel")
    Assert.assertEquals(1, elements.getLength());
    val channelElement = elements.item(0).asInstanceOf[Element]
    Assert.assertEquals(1, channelElement.getAttributes().getLength());
    Assert.assertEquals("foo", channelElement.getAttribute("id"))

    val dispatcherElements = channelElement.getElementsByTagName("int:dispatcher")
    Assert.assertEquals(1, dispatcherElements.getLength());
    val dispatcher = dispatcherElements.item(0).asInstanceOf[Element]
    Assert.assertTrue(dispatcher.hasAttribute("task-executor"))

    val applicationContext = IntegrationDomTreeBuilder.buildApplicationContext(messageFlow)
    Assert.assertTrue(applicationContext.containsBean("foo"))
  }

  @Test
  def generateChannelBridgeWithDirectChannels = {

    val messageFlow =
      Channel("foo") -->
      Channel("bar")

    val document = IntegrationDomTreeBuilder.toDocument(messageFlow)
    val channelElements = document.getElementsByTagName("int:channel")
    Assert.assertEquals(2, channelElements.getLength());
    Assert.assertEquals(1, channelElements.item(0).getAttributes().getLength());
    Assert.assertEquals(1, channelElements.item(1).getAttributes().getLength());

    val bridgeElements = document.getElementsByTagName("int:bridge")
    Assert.assertEquals(1, bridgeElements.getLength());
    val bridgeElement: Element = bridgeElements.item(0).asInstanceOf[Element]
    Assert.assertEquals(3, bridgeElement.getAttributes().getLength());
    Assert.assertEquals("foo", bridgeElement.getAttribute("input-channel"))
    Assert.assertEquals("bar", bridgeElement.getAttribute("output-channel"))

    val applicationContext = IntegrationDomTreeBuilder.buildApplicationContext(messageFlow)
    val names =  applicationContext.getBeanDefinitionNames()

    Assert.assertTrue(applicationContext.containsBean("foo"))
    Assert.assertTrue(applicationContext.containsBean("bar"))
    val bridgeElementsAc = document.getElementsByTagName("int:bridge")
    Assert.assertEquals(1, bridgeElementsAc.getLength());
    val bridgeElementAc: Element = bridgeElements.item(0).asInstanceOf[Element]
    Assert.assertTrue(applicationContext.containsBean(bridgeElementAc.getAttribute("id")))
    Assert.assertTrue(applicationContext.getBean(bridgeElementAc.getAttribute("id")).isInstanceOf[EventDrivenConsumer])
  }

  @Test
  def generateChannelBridgeWithOnePollableChannelAndPoller = {

    val messageFlow =
      Channel("foo").withQueue(4) --> poll.withFixedDelay(10000).withMaxMessagesPerPoll(4) -->
        Channel("bar")

    val document = IntegrationDomTreeBuilder.toDocument(messageFlow)

    val bridgeElements = document.getElementsByTagName("int:bridge")
    Assert.assertEquals(1, bridgeElements.getLength());
    val bridgeElement = bridgeElements.item(0).asInstanceOf[Element]
    val pollerElements = bridgeElement.getElementsByTagName("int:poller")
    Assert.assertEquals(1, pollerElements.getLength())
    val pollerElement = pollerElements.item(0).asInstanceOf[Element]
    Assert.assertTrue(pollerElement.hasAttribute("fixed-delay"))
    Assert.assertTrue(pollerElement.hasAttribute("max-messages-per-poll"))

    val applicationContext = IntegrationDomTreeBuilder.buildApplicationContext(messageFlow)
    val names =  applicationContext.getBeanDefinitionNames()

    Assert.assertTrue(applicationContext.containsBean("foo"))
    Assert.assertTrue(applicationContext.containsBean("bar"))
    val bridgeElementsAc = document.getElementsByTagName("int:bridge")
    Assert.assertEquals(1, bridgeElementsAc.getLength());
    val bridgeElementAc: Element = bridgeElements.item(0).asInstanceOf[Element]
    Assert.assertTrue(applicationContext.containsBean(bridgeElementAc.getAttribute("id")))
    Assert.assertTrue(applicationContext.getBean(bridgeElementAc.getAttribute("id")).isInstanceOf[PollingConsumer])
  }

  @Test
  def generateServiceActivatorWithImpliedChannel = {

    val messageFlow = handle { m: Message[_] => println(m) }

    val document = IntegrationDomTreeBuilder.toDocument(messageFlow)
    val channelElements = document.getElementsByTagName("int:channel")
    val channelElement: Element = channelElements.item(0).asInstanceOf[Element]
    Assert.assertEquals(1, channelElements.getLength());
    Assert.assertEquals(1, channelElements.item(0).getAttributes().getLength())

    val saElements = document.getElementsByTagName("int:service-activator")
    Assert.assertEquals(1, saElements.getLength());
    Assert.assertEquals(4, saElements.item(0).getAttributes().getLength())
    val saElement: Element = saElements.item(0).asInstanceOf[Element]
    Assert.assertEquals(channelElement.getAttribute("id"), saElement.getAttribute("input-channel"))

    val applicationContext = IntegrationDomTreeBuilder.buildApplicationContext(messageFlow)
    Assert.assertTrue(applicationContext.containsBean(saElement.getAttribute("id")))
    Assert.assertTrue(applicationContext.containsBean(saElement.getAttribute("ref")))
  }

  @Test
  def generateServiceActivatorWithDifferentFuunctionSignatures = {
    val messageFlowA = handle { s: String => println(s) }
    val documentA = IntegrationDomTreeBuilder.toDocument(messageFlowA)
    val saElementA: Element = documentA.getElementsByTagName("int:service-activator").item(0).asInstanceOf[Element]
    Assert.assertEquals("sendPayload", saElementA.getAttribute("method"))

    val applicationContextA = IntegrationDomTreeBuilder.buildApplicationContext(messageFlowA)
    Assert.assertTrue(applicationContextA.containsBean(saElementA.getAttribute("id")))
    Assert.assertTrue(applicationContextA.containsBean(saElementA.getAttribute("ref")))

    val messageFlowD = handle { s: Message[_] => println(s) }
    val documentD = IntegrationDomTreeBuilder.toDocument(messageFlowD)
    val saElementD: Element = documentD.getElementsByTagName("int:service-activator").item(0).asInstanceOf[Element]
    Assert.assertEquals("sendMessage", saElementD.getAttribute("method"))

    val applicationContextD = IntegrationDomTreeBuilder.buildApplicationContext(messageFlowD)
    Assert.assertTrue(applicationContextD.containsBean(saElementD.getAttribute("id")))
    Assert.assertTrue(applicationContextD.containsBean(saElementD.getAttribute("ref")))

    val messageFlowB = handle { (s: String, m: Map[String, _]) => println(s) }
    val documentB = IntegrationDomTreeBuilder.toDocument(messageFlowB)
    val saElementB: Element = documentB.getElementsByTagName("int:service-activator").item(0).asInstanceOf[Element]
    Assert.assertEquals("sendPayloadAndHeaders", saElementB.getAttribute("method"))

    val applicationContextB = IntegrationDomTreeBuilder.buildApplicationContext(messageFlowB)
    Assert.assertTrue(applicationContextB.containsBean(saElementB.getAttribute("id")))
    Assert.assertTrue(applicationContextB.containsBean(saElementB.getAttribute("ref")))

    val messageFlowC = handle { (s: String, m: Map[String, _]) => s }
    val documentC = IntegrationDomTreeBuilder.toDocument(messageFlowC)
    val saElementC: Element = documentC.getElementsByTagName("int:service-activator").item(0).asInstanceOf[Element]
    Assert.assertEquals("sendPayloadAndHeadersAndReceive", saElementC.getAttribute("method"))

    val applicationContextC = IntegrationDomTreeBuilder.buildApplicationContext(messageFlowC)
    Assert.assertTrue(applicationContextC.containsBean(saElementC.getAttribute("id")))
    Assert.assertTrue(applicationContextC.containsBean(saElementC.getAttribute("ref")))
  }

  @Test
  def generateServiceActivatorsWithImpliedChannel = {

    val messageFlow =
      handle { s: String => s }.additionalAttributes(name = "start") -->
        handle { s: String => println }.additionalAttributes(name = "end")

    val document = IntegrationDomTreeBuilder.toDocument(messageFlow)
    val serviceActivators = document.getElementsByTagName("int:service-activator")
    Assert.assertEquals(2, serviceActivators.getLength())
    val saStart = serviceActivators.item(1).asInstanceOf[Element]
    val saEnd = serviceActivators.item(0).asInstanceOf[Element]
    Assert.assertEquals(saStart.getAttribute("output-channel"), saEnd.getAttribute("input-channel"))
    Assert.assertTrue(saStart.hasAttribute("input-channel"))
    Assert.assertFalse(saEnd.hasAttribute("output-channel"))
    val channels = document.getElementsByTagName("int:channel")
    Assert.assertEquals(2, channels.getLength())

    val applicationContext = IntegrationDomTreeBuilder.buildApplicationContext(messageFlow)
    Assert.assertTrue(applicationContext.containsBean(saStart.getAttribute("id")))
    Assert.assertTrue(applicationContext.containsBean(saStart.getAttribute("ref")))
    Assert.assertTrue(applicationContext.containsBean(saEnd.getAttribute("id")))
    Assert.assertTrue(applicationContext.containsBean(saEnd.getAttribute("ref")))
  }

  @Test
  def generateServiceActivatorsWithExplicitChannelMultipleSubscribers = {

    val messageFlow =
      handle { s: String => s }.additionalAttributes(name = "start") -->
        PubSubChannel("pubsub") --> (
          handle { s: String => println }.additionalAttributes(name = "endA"),
          handle { s: String => println }.additionalAttributes(name = "endB"))

    val document = IntegrationDomTreeBuilder.toDocument(messageFlow)
    val serviceActivators = document.getElementsByTagName("int:service-activator")
    Assert.assertEquals(3, serviceActivators.getLength())

    val saStartElements = this.getChildElementsByTagNameAndAttribute(document, "int:service-activator", "id=start")
    Assert.assertEquals(1, saStartElements.length)
    val saStartElement = saStartElements(0)
    Assert.assertEquals("pubsub", saStartElement.getAttribute("output-channel"))

    val endAElements = this.getChildElementsByTagNameAndAttribute(document, "int:service-activator", "id=endA")
    Assert.assertEquals(1, endAElements.length)
    val endAElement = endAElements(0)
    Assert.assertEquals("pubsub", endAElement.getAttribute("input-channel"))
    Assert.assertFalse(endAElement.hasAttribute("output-channel"))

    val endBElements = this.getChildElementsByTagNameAndAttribute(document, "int:service-activator", "id=endB")
    Assert.assertEquals(1, endAElements.length)
    val endBElement = endBElements(0)
    Assert.assertEquals("pubsub", endBElement.getAttribute("input-channel"))
    Assert.assertFalse(endBElement.hasAttribute("output-channel"))

    val applicationContext = IntegrationDomTreeBuilder.buildApplicationContext(messageFlow)
    Assert.assertTrue(applicationContext.containsBean("start"))
    Assert.assertTrue(applicationContext.containsBean(saStartElement.getAttribute("ref")))
    Assert.assertTrue(applicationContext.containsBean("endA"))
    Assert.assertTrue(applicationContext.containsBean("endB"))
  }

  @Test
  def generateServiceActivatorsWithExplicitChannel = {

    val messageFlow =
      Channel("startChannel") -->
        handle { s: String => s }.additionalAttributes(name = "start") -->
        Channel("endChannel") -->
        handle { s: String => println }.additionalAttributes(name = "end")

    val document = IntegrationDomTreeBuilder.toDocument(messageFlow)
    val serviceActivators = document.getElementsByTagName("int:service-activator")
    Assert.assertEquals(2, serviceActivators.getLength())
    val saStart = serviceActivators.item(1).asInstanceOf[Element]
    val saEnd = serviceActivators.item(0).asInstanceOf[Element]
    Assert.assertEquals(saStart.getAttribute("output-channel"), saEnd.getAttribute("input-channel"))
    Assert.assertTrue(saStart.hasAttribute("input-channel"))
    Assert.assertFalse(saEnd.hasAttribute("output-channel"))
    val channels = document.getElementsByTagName("int:channel")
    Assert.assertEquals(2, channels.getLength())
  }

  @Test
  def generateServiceActivatorsWithPoller = {

    val messageFlow =
      Channel("startChannel").withQueue(4) --> poll.withFixedDelay(3).withExecutor(Executors.newCachedThreadPool()) -->
        handle { s: String => s }.additionalAttributes(name = "start") -->
        handle { s: String => println }.additionalAttributes(name = "end")

    val document = IntegrationDomTreeBuilder.toDocument(messageFlow)
    val serviceActivators = document.getElementsByTagName("int:service-activator")
    Assert.assertEquals(2, serviceActivators.getLength());
    val serviceActivatorElement = serviceActivators.item(1).asInstanceOf[Element]
    val pollerElements = serviceActivatorElement.getElementsByTagName("int:poller")
    Assert.assertEquals(1, pollerElements.getLength());
    val pollerElement = pollerElements.item(0).asInstanceOf[Element]
    Assert.assertTrue(pollerElement.hasAttribute("task-executor"))
    Assert.assertTrue(pollerElement.hasAttribute("fixed-delay"))
  }

  @Test
  def generateTransformer = {

    val messageFlow =
      transform { s: String => s }.additionalAttributes(name = "xfmr")

    val document = IntegrationDomTreeBuilder.toDocument(messageFlow)
    val transformers = document.getElementsByTagName("int:transformer")
    Assert.assertEquals(1, transformers.getLength());
    val transformerElement = transformers.item(0).asInstanceOf[Element]
    Assert.assertEquals("xfmr", transformerElement.getAttribute("id"));
    Assert.assertEquals("sendPayloadAndReceive", transformerElement.getAttribute("method"));

    val channels =
      this.getChildElementsByTagNameAndAttribute(document, "int:channel", "id=" + transformerElement.getAttribute("input-channel"))

    Assert.assertEquals(1, channels.length)
  }

  @Test
  def generateTransformerWithSubflow = {

    val messageSubFlow =
      handle { _: Any => }

    val messageFlow =
      transform { s: String => messageSubFlow.sendAndReceive[String](s) }.additionalAttributes(name = "xfmr")

    val document = IntegrationDomTreeBuilder.toDocument(messageFlow)
    val transformers = document.getElementsByTagName("int:transformer")
    Assert.assertEquals(1, transformers.getLength());
    val transformerElement = transformers.item(0).asInstanceOf[Element]
    Assert.assertEquals("xfmr", transformerElement.getAttribute("id"));
    Assert.assertEquals("sendPayloadAndReceive", transformerElement.getAttribute("method"));

    val channels =
      this.getChildElementsByTagNameAndAttribute(document, "int:channel", "id=" + transformerElement.getAttribute("input-channel"))

    Assert.assertEquals(1, channels.length)
  }

  @Test
  def generateSplitter = {

    val messageFlow =
      split { s: String => s.toTraversable }.additionalAttributes(name = "splitter")

    val document = IntegrationDomTreeBuilder.toDocument(messageFlow)
    val transformers = document.getElementsByTagName("int:splitter")
    Assert.assertEquals(1, transformers.getLength());
    val transformerElement = transformers.item(0).asInstanceOf[Element]
    Assert.assertEquals("splitter", transformerElement.getAttribute("id"));
    Assert.assertEquals("sendPayloadAndReceive", transformerElement.getAttribute("method"));

    val channels =
      this.getChildElementsByTagNameAndAttribute(document, "int:channel", "id=" + transformerElement.getAttribute("input-channel"))

    Assert.assertEquals(1, channels.length)
  }

  @Test
  def generateFilter = {

    val messageFlow =
      filter { s: String => s.equals("foo") }.additionalAttributes(name = "filter", exceptionOnRejection = true)

    val document = IntegrationDomTreeBuilder.toDocument(messageFlow)
    val transformers = document.getElementsByTagName("int:filter")
    Assert.assertEquals(1, transformers.getLength());
    val transformerElement = transformers.item(0).asInstanceOf[Element]
    Assert.assertEquals("filter", transformerElement.getAttribute("id"));
    Assert.assertEquals("sendPayloadAndReceive", transformerElement.getAttribute("method"));

    val channels =
      this.getChildElementsByTagNameAndAttribute(document, "int:channel", "id=" + transformerElement.getAttribute("input-channel"))

    Assert.assertEquals(1, channels.length)
  }

  @Test
  def generateHeaderEnricherWithTuple = {
    val messageFlow = enrich.header("hello" -> "bye")
    val document = IntegrationDomTreeBuilder.toDocument(messageFlow)
  }

  @Test
  def generateHeaderEnricherWithFunctionAsValue = {
    val messageFlow = enrich.header("hello" -> Some({ m: Message[String] => m.getPayload().toUpperCase() }))
    val document = IntegrationDomTreeBuilder.toDocument(messageFlow)
  }

  @Test
  def generateHeaderEnricherWithMessageFunctionAsProcessor = {
    val messageFlow = enrich.header("hello" -> { m: Message[String] => m.getPayload().toUpperCase() })
    val document = IntegrationDomTreeBuilder.toDocument(messageFlow)
  }

  @Test
  def generateHeaderEnricherWithMultiTuple = {

    val messageFlow =
      enrich.headers("foo" -> "foo",
        "bar" -> { m: Message[String] => m.getPayload().toUpperCase() },
        "phrase" -> Some({ m: Message[String] => m.getPayload().toUpperCase() }))

    val document = IntegrationDomTreeBuilder.toDocument(messageFlow)
  }

  @Test
  def generateContentEnricher = {
    case class Person(var name: String = null, var age: Int = 0)
    case class Employee(val firstName: String, val lastName: String, val age: Int)

    val employee = new Employee("John", "Doe", 23)

    val messageFlow =
      enrich { p: Person => p.name = employee.firstName + " " + employee.lastName; p.age = employee.age; p }

    val document = IntegrationDomTreeBuilder.toDocument(messageFlow)
  }

  @Test
  def generateContentEnricherWithSubFlow = {

    case class Person(var name: String = null, var age: Int = 0)
    case class Employee(val firstName: String, val lastName: String, val age: Int)

    val employeeBuldingFlow =
      transform { attributes: List[String] => new Employee(attributes(0), attributes(1), Integer.parseInt(attributes(2))) }

    val messageFlow =
      enrich { p: Person =>
        val employee = employeeBuldingFlow.sendAndReceive[Employee](List[String]("John", "Doe", "25"))
        p.name = employee.firstName + " " + employee.lastName
        p.age = employee.age
        p
      }

    val document = IntegrationDomTreeBuilder.toDocument(messageFlow)
  }

  @Test
  def generateHeaderValueRouter = {

    val messageFlow =
      route.onValueOfHeader("someHeaderName")(
        when("foo") then
          handle { m: Message[_] => println("Header is 'foo': " + m) },
        when("bar") then
          handle { m: Message[_] => println("Header is 'bar': " + m) })

    val document = IntegrationDomTreeBuilder.toDocument(messageFlow)
  }

  @Test
  def generatePayloadTypeRouter = {

    val messageFlow =
      route.onPayloadType(
        when(classOf[String]) then
          handle { m: Message[_] => println("Payload is String: " + m) },
        when(classOf[Int]) then
          handle { m: Message[_] => println("Payload is Int: " + m) })

    val document = IntegrationDomTreeBuilder.toDocument(messageFlow)
  }

  @Test
  def generateCustomRouter = {

    val messageFlow =
      route { m: Message[String] => m.getPayload }(

        when("Hello") then
          handle { m: Message[_] => println("Payload is Hello: " + m) },
        when("Bye") then
          handle { m: Message[_] => println("Payload is Bye: " + m) })

    val document = IntegrationDomTreeBuilder.toDocument(messageFlow)
  }

  @Test
  def generateAggregator = {

    val messageFlow =
      aggregate()

    val document = IntegrationDomTreeBuilder.toDocument(messageFlow)
  }

  private def getChildElementsByTagNameAndAttribute(document: Document, tagName: String, attribute: String): Array[Element] = {
    val splittedAttribute = attribute.split("=")
    val childElements = DomUtils.getChildElementsByTagName(document.getDocumentElement(), tagName).toArray(Array[Element]())

    val elementsToReturn =
      for {
        childElement <- childElements
        if (childElement.hasAttribute(splittedAttribute(0)) && childElement.getAttribute(splittedAttribute(0)).equals(splittedAttribute(1)))
      } yield childElement
    elementsToReturn
  }

}