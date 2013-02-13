/*
 * Copyright 2002-2013 the original author or authors.
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
package org.springframework.integration.dsl.utils
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.StringWriter
import java.util.UUID
import org.apache.commons.logging.LogFactory
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader
import org.springframework.context.support.GenericApplicationContext
import org.springframework.context.ApplicationContext
import org.springframework.core.io.InputStreamResource
import org.springframework.integration.dsl.BaseIntegrationComposition
import org.springframework.integration.dsl.Channel
import org.springframework.integration.dsl.FunctionInvoker
import org.springframework.integration.dsl.InboundMessageSource
import org.springframework.integration.dsl.IntegrationComponent
import org.springframework.integration.dsl.ListOfCompositions
import org.springframework.integration.dsl.MessagingBridge
import org.springframework.integration.dsl.PollableChannel
import org.springframework.integration.dsl.Poller
import org.springframework.integration.dsl.PubSubChannel
import org.springframework.integration.dsl.SimpleEndpoint
import org.w3c.dom.Document
import org.w3c.dom.Element
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.beans.factory.BeanFactoryAware
import org.springframework.integration.dsl.AbstractChannel

/**
 * @author Oleg Zhurakousky
 * @author Soby Chacko
 */
object IntegrationDomTreeBuilder {
  def toDocument[T <: BaseIntegrationComposition](integrationComposition: T): Document = {
    new IntegrationDomTreeBuilder().toDocument(integrationComposition)
  }

  def buildApplicationContext[T <: BaseIntegrationComposition](integrationComposition: T) =
    new IntegrationDomTreeBuilder().buildApplicationContext(null, integrationComposition)

  def buildApplicationContext[T <: BaseIntegrationComposition](applicationContext: ApplicationContext = null, integrationComposition: T) =
    new IntegrationDomTreeBuilder().buildApplicationContext(applicationContext, integrationComposition)
}
/**
 *
 */
class IntegrationDomTreeBuilder {

  private val logger = LogFactory.getLog(this.getClass());

  val integrationComponents = scala.collection.mutable.Map[String, Any]()
  val supportingBeans = scala.collection.mutable.Map[String, Any]()

  val docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
  val document = docBuilder.newDocument()
  val root = document.createElement("beans");
  root.setAttribute("xmlns", "http://www.springframework.org/schema/beans")
  root.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance")
  root.setAttribute("xmlns:int", "http://www.springframework.org/schema/integration")
  root.setAttribute("xsi:schemaLocation", "http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd " +
    "http://www.springframework.org/schema/integration http://www.springframework.org/schema/integration/spring-integration.xsd")

  root.appendChild(document.createComment("Generated file. Don't modify"))
  document.appendChild(root);

  val transformerFactory = TransformerFactory.newInstance()
  transformerFactory.setAttribute("indent-number", "2")
  val transformer = transformerFactory.newTransformer()
  transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
  transformer.setOutputProperty(OutputKeys.INDENT, "yes");

  /**
   *
   */
  def buildApplicationContext[T <: BaseIntegrationComposition](parentContext: ApplicationContext, integrationComposition: T): ConfigurableApplicationContext = {
    this.toDocument(integrationComposition)

    val applicationContext = new GenericApplicationContext(parentContext)

    this.supportingBeans.foreach { element: Tuple2[String, Any] =>
      applicationContext.getBeanFactory().registerSingleton(element._1, element._2)
      element._2 match {
        case e:BeanFactoryAware => {
          e.setBeanFactory(applicationContext.getBeanFactory())
        }
        case _ =>
      }
    }

    val outputStream = new ByteArrayOutputStream()
    val xmlSource = new DOMSource(document)
    val outputTarget = new StreamResult(outputStream)
    TransformerFactory.newInstance().newTransformer().transform(xmlSource, outputTarget)

    val stream = new ByteArrayInputStream(outputStream.toByteArray())

    val reader = new XmlBeanDefinitionReader(applicationContext)
    reader.setValidationMode(XmlBeanDefinitionReader.VALIDATION_XSD)
    reader.loadBeanDefinitions(new InputStreamResource(stream))
    applicationContext.refresh()
    applicationContext
  }

  /**
   *
   */
  def toDocument[T <: BaseIntegrationComposition](integrationComposition: T): Document = {

    this.init(integrationComposition)

    if (this.logger.isDebugEnabled()){
      this.printDocument
    }

    document
  }

  private def init(composition: BaseIntegrationComposition, outputChannel: AbstractChannel = null): Unit = {
    if (logger.isDebugEnabled()) logger.debug("Initializing " + composition.target)
    require(composition.target.name != null, "Each component must be named " + composition.target)

    val inputChannel: AbstractChannel =
      if (composition.target.isInstanceOf[InboundMessageSource]) null else this.determineInputChannel(composition)

    if (inputChannel != null) this.buildChannelElement(inputChannel)

    val nextOutputChannel: AbstractChannel =
      if (composition.target.isInstanceOf[InboundMessageSource]) null else this.determineNextOutputChannel(composition, inputChannel)

    if (nextOutputChannel != null) this.buildChannelElement(nextOutputChannel)

    composition.target match {
      case channel: AbstractChannel =>
        this.wireMessagingBridge(composition, inputChannel, nextOutputChannel)

      case ims: InboundMessageSource =>
        this.processInboundMessageSource(ims, outputChannel)

      case listComp: ListOfCompositions[_] =>
        this.processListOfCompositions(listComp.asInstanceOf[ListOfCompositions[BaseIntegrationComposition]],
          composition.parentComposition, outputChannel)

      case endpoint: SimpleEndpoint =>
        this.processEndpoint(endpoint, inputChannel, outputChannel, this.getPollerIfAvailable(composition))

      case _: Poller => //ignore since its going to be configured as part of the endpoint's parent

      case _ => throw new IllegalArgumentException("Unrecognized BaseIntegrationComposition: " + composition.target)
    }

    if (composition.parentComposition != null)
      this.init(composition.parentComposition, nextOutputChannel)
  }

  /**
   *
   */
  private def determineInputChannel(composition: BaseIntegrationComposition): AbstractChannel = {
    if (composition.parentComposition != null) {
      composition.parentComposition.target match {
        case ch: AbstractChannel =>
          ch

        case poller: Poller =>
          if (composition.parentComposition.parentComposition != null){
            composition.parentComposition.parentComposition.target.asInstanceOf[AbstractChannel]
          }
          else {
            null
          }
        case endpoint: IntegrationComponent =>
          val channel: AbstractChannel =
            if (!this.integrationComponents.contains(composition.target.name))
              new Channel("$ch_" + UUID.randomUUID().toString.substring(0, 8))
            else
              null

          channel

        case _ => throw new IllegalStateException("Unrecognized component " + composition)
      }
    } else if (composition.target.isInstanceOf[SimpleEndpoint]) {
      if (!this.integrationComponents.contains(composition.target.name))
        new Channel("$ch_" + UUID.randomUUID().toString.substring(0, 8))
      else null
    } else null

  }

  private def buildChannelElement(channelDefinition: AbstractChannel): Unit = {
    if (!this.integrationComponents.contains(channelDefinition.name)) {
      channelDefinition match {
        case ch: Channel =>
          val channel = document.createElement("int:channel");
          channel.setAttribute("id", ch.name);
          root.appendChild(channel)

          if (ch.taskExecutor != null) {
            val dispatcher = document.createElement("int:dispatcher")
            val executorId = "exctr_" + ch.taskExecutor.hashCode()
            dispatcher.setAttribute("task-executor", executorId)
            this.supportingBeans += (executorId -> ch.taskExecutor)
            channel.appendChild(dispatcher)
          }
        case queue: PollableChannel => {
          val channel = document.createElement("int:channel");
          channel.setAttribute("id", queue.name);
          val queueElement = document.createElement("int:queue");
          queueElement.setAttribute("capacity", queue.capacity.toString())
          channel.appendChild(queueElement)
          root.appendChild(channel)

        }
        case pubsub: PubSubChannel =>
          val channel = document.createElement("int:publish-subscribe-channel");
          channel.setAttribute("id", pubsub.name);
          root.appendChild(channel)

        case _ =>
          throw new IllegalArgumentException("Unsupported Channel type: " + channelDefinition)

      }
      this.integrationComponents += (channelDefinition.name -> channelDefinition)
    }
  }

  /**
   *
   */
  private def determineNextOutputChannel(composition: BaseIntegrationComposition, previousInputChannel: AbstractChannel): AbstractChannel = {
    composition.target match {
      case ch: AbstractChannel =>
        ch

      case _ =>
        previousInputChannel
    }
  }

  /**
   * Since channel itself will be processed at the time of endpoint wiring, this method will
   * only be called when two channels are bridged together (e.g., channel --> channel), so essentially
   * this method defines a MessagingBridge
   */
  private def wireMessagingBridge(composition: BaseIntegrationComposition, inputChannel: AbstractChannel, outputChannel: AbstractChannel) {
    require(outputChannel != null)
    if (composition.parentComposition != null) {
      composition.parentComposition.target match {
        case parentChannel: Channel => {
          if (logger.isDebugEnabled)
            logger.debug("[" + inputChannel.name + " --> bridge --> " + composition.target.asInstanceOf[AbstractChannel].name + "]")

          this.wireEndpoint(new MessagingBridge("br_" + inputChannel.name + "_to_" + outputChannel.name), inputChannel, outputChannel)
        }
        case parentChannel: PollableChannel => {
          if (logger.isDebugEnabled)
            logger.debug("[" + inputChannel.name + " --> bridge --> " + composition.target.asInstanceOf[Channel].name + "]")

          this.wireEndpoint(new MessagingBridge("br_" + inputChannel.name + "_to_" + outputChannel.name), inputChannel, outputChannel, new Poller)
        }
        case poller: Poller => {
          if (logger.isDebugEnabled)
            logger.debug("[" + inputChannel.name + " --> bridge --> " + composition.target.asInstanceOf[Channel].name + "]")

          this.wireEndpoint(new MessagingBridge("br_" + inputChannel.name + "_to_" + outputChannel.name), inputChannel, outputChannel, poller)
        }
        case _ =>
      }
    }
  }

  /**
   *
   */
  private def processInboundMessageSource(ims: InboundMessageSource, outputChannel: AbstractChannel) {
    val element = ims.build(document, this.defineAndRegisterTarget, this.configurePoller,  outputChannel.name)
    this.root.appendChild(element)
  }

  /**
   *
   */
  private def processListOfCompositions(listComp: ListOfCompositions[BaseIntegrationComposition], parentComposition: BaseIntegrationComposition, outputChannel: AbstractChannel) {
    for (comp <- listComp.compositions) {
      this.init(new BaseIntegrationComposition(parentComposition, comp.target), outputChannel)
    }
  }

  /**
   *
   */
  private def processEndpoint(endpoint: SimpleEndpoint, inputChannel: AbstractChannel, outputChannel: AbstractChannel, poller: Poller) {
    if (!this.integrationComponents.contains(endpoint.name)) {
      this.wireEndpoint(endpoint, inputChannel, (if (outputChannel != null) outputChannel else null), poller)
    }
  }

  /**
   *
   */
  private def wireEndpoint(endpoint: SimpleEndpoint, inputChannel: AbstractChannel, outputChannel: AbstractChannel, poller: Poller = null) {

    if (logger.isDebugEnabled) logger.debug("Creating " + endpoint)

    val element = endpoint.build(document, this.defineAndRegisterTarget, this.init, inputChannel, outputChannel)
    root.appendChild(element)

    if (poller != null)
      this.configurePoller(endpoint, poller, element)

    this.integrationComponents += (endpoint.name -> endpoint)
  }

  /**
   *
   */
  private def configurePoller(endpoint: IntegrationComponent, pollerConfig: Poller, consumerElement: Element):Unit = {
    if (logger.isDebugEnabled) logger debug "Creating Polling consumer using " + pollerConfig

    val pollerElement = document.createElement("int:poller")

    if (pollerConfig.fixedDelay > 0)
      pollerElement.setAttribute("fixed-delay", pollerConfig.fixedDelay.toString)
    else if (pollerConfig.fixedRate > 0)
      pollerElement.setAttribute("fixed-rate", pollerConfig.fixedRate.toString)

    if (pollerConfig.taskExecutor != null) {
      val taskExecutorName = "exec_" + pollerConfig.taskExecutor.hashCode
      this.supportingBeans += (taskExecutorName -> pollerConfig.taskExecutor)
      pollerElement.setAttribute("task-executor", taskExecutorName)
    }

    if (pollerConfig.maxMessagesPerPoll > 0)
      pollerElement.setAttribute("max-messages-per-poll", pollerConfig.maxMessagesPerPoll.toString)

    consumerElement.appendChild(pollerElement)
  }

  private def getPollerIfAvailable(composition: BaseIntegrationComposition): Poller = {
    if (composition.parentComposition != null) {
      composition.parentComposition.target match {
        case poller: Poller => poller
        case _ => null
      }
    } else
      null
  }

  /**
   *
   */
  private def defineAndRegisterTarget(target: Any): Tuple2[String, String] = {
    val targetBeanName = "bean_" + target.hashCode

    val targetDefinition = target match {
      case Some(rootTarget) => {
        val s = rootTarget
        this.supportingBeans += (targetBeanName -> rootTarget)
        (targetBeanName, null)
      }
      case _ => {
        val functionInvoker = new FunctionInvoker(target)
        this.supportingBeans += (targetBeanName -> functionInvoker)
        (targetBeanName, functionInvoker.methodName)
      }
    }
    targetDefinition
  }

  private def printDocument = {
    val sw = new StringWriter();
    val result = new StreamResult(sw);
    val source = new DOMSource(document);
    transformer.transform(source, result);

    println(sw.toString())
  }
}