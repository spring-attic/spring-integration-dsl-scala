package demo
import org.springframework.jms.core.JmsTemplate
import javax.jms.TextMessage
import org.springframework.integration.dsl.jms
import org.junit.Test
import org.springframework.jms.core.MessageCreator
import org.springframework.integration.dsl.handle
import javax.jms.Session
import org.springframework.integration.Message
import org.springframework.integration.dsl.transform
import org.springframework.integration.dsl.utils.JmsDslTestUtils

class DslUsageDemoTests {

  @Test
  def jmsInboundGateway = {
    val connectionFactory = JmsDslTestUtils.localConnectionFactory

    val flow =
      jms.listen(requestDestinationName = "myQueue", connectionFactory = connectionFactory) -->
        handle.using { m: Message[_] => println("logging existing message and passing through " + m); m } -->
        transform.using { value: String => value.toUpperCase() }

    flow.start

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
    println("Reply Message: " + replyMessage.asInstanceOf[TextMessage].getText())

    flow.stop
    println("done")
  }
}