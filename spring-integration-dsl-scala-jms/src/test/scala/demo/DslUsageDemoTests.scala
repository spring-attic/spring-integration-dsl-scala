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
import org.junit.Test
import org.junit.Assert._
import org.springframework.integration.dsl.utils.JmsDslTestUtils
import org.springframework.integration.dsl.handle
import org.springframework.integration.dsl.jms
import org.springframework.integration.dsl.transform
import org.springframework.integration.Message
import org.springframework.jms.core.JmsTemplate
import org.springframework.jms.core.MessageCreator
import javax.jms.Session
import javax.jms.TextMessage
import java.io.File
import org.junit.Before
import javax.jms.ConnectionFactory
import org.springframework.jms.connection.CachingConnectionFactory
import org.junit.After
/**
 * @author Oleg Zhurakousky
 */
class DslUsageDemoTests {

  var connectionFactory: CachingConnectionFactory = _

  @Test
  def jmsInboundGateway = {

    val flow =
      jms.listen(requestDestinationName = "myQueue", connectionFactory = connectionFactory) -->
        handle { m: Message[_] => println("logging existing message and passing through " + m); m } -->
        transform { value: String => value.toUpperCase() }

    flow.start()

    val jmsTemplate = new JmsTemplate(connectionFactory);
    val request = new org.apache.activemq.command.ActiveMQQueue("myQueue")
    val reply = new org.apache.activemq.command.ActiveMQQueue("myReply")
    jmsTemplate.send(request, new MessageCreator {
      def createMessage(session: Session) = {
        val message = session.createTextMessage();
        message.setText("Hello from JMS");
        message.setJMSReplyTo(reply);
        message;
      }
    });

    val replyMessage = jmsTemplate.receive(reply);
    assertNotNull(replyMessage)
    println("Reply Message: " + replyMessage.asInstanceOf[TextMessage].getText())

    flow.stop()
    println("done")
  }

  @Test
  def jmsOutboundGatewayWithReply = {

    val sendingMessageFlow =
      transform { p: String => p.toUpperCase() } -->
        jms.sendAndReceive(requestDestinationName = "myQueue", connectionFactory = connectionFactory)

    val receivingMessageFlow =
      jms.listen(requestDestinationName = "myQueue", connectionFactory = connectionFactory) -->
        handle { p: String => println("received " + p); "REPLY: " + p }

    receivingMessageFlow.start()
    val reply = sendingMessageFlow.sendAndReceive[String]("Hello JMS!")
    assertNotNull(reply)
    receivingMessageFlow.stop()
    println("Received reply: " + reply)
  }

  @Test
  def jmsOutboundGatewayWithoutReply = {

    val sendingMessageFlow =
      transform { p: String => p.toUpperCase() } -->
      	jms.send(requestDestinationName = "myQueue", connectionFactory = connectionFactory)

    val receivingMessageFlow =
      jms.listen(requestDestinationName = "myQueue", connectionFactory = connectionFactory) -->
      	handle { p: String => println("received " + p) }

    receivingMessageFlow.start()
    sendingMessageFlow.send("Hello JMS!")
    Thread.sleep(2000)
    receivingMessageFlow.stop()
    println("Done")
  }

  @Before
  def before = {
    val activeMqTempDir = new File("activemq-data")
    deleteDir(activeMqTempDir)

    def deleteDir(directory: File): Unit = {
      if (directory.exists) {
        val children = directory.list();

        if (children != null) {
          for (child <- children) deleteDir(new File(directory, child))
        }
      }
      directory.delete();
    }
    connectionFactory = JmsDslTestUtils.localConnectionFactory
  }
  @After
  def after = {
    connectionFactory.destroy()
    Thread.sleep(1000)
  }
}