/*
 * Copyright 2002-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package scala.c24.demo

import org.junit.Test
import org.springframework.integration.Message
import org.springframework.integration.dsl._
import org.springframework.data.gemfire.CacheFactoryBean
import com.gemstone.gemfire.cache.Cache
import demo.IntegrationDemoUtils
import org.springframework.core.io.ClassPathResource
import javax.xml.xpath.XPathFactory
import javax.xml.xpath.XPathConstants
import javax.xml.parsers.DocumentBuilderFactory
import javax.jms.Session
import org.springframework.jms.core.JmsTemplate
import org.springframework.jms.core.MessageCreator
import scala.c24.demo.java.C24SwiftUnmarshallingTransformer
import scala.c24.demo.java.C24DemoUtils
import scala.c24.demo.java.C24FixUnmarshallingTransformer
import scala.c24.demo.java.C24Validator
import scala.c24.demo.java.C24Iso20022UnmarshallingTransformer
import scala.c24.demo.java.C24Object

/**
 * @author Oleg Zhurakousky
 * @author Soby Chacko
 */
class C24Demo {

  val region = this.createDefaultGemfireRegion

  val connectionFactory = IntegrationDemoUtils.localConnectionFactory

  val c24Validator = new C24Validator

  val c24SwiftUnmarshallingTransformer = new C24SwiftUnmarshallingTransformer

  val c24FixUnmarshallingTransformer = new C24FixUnmarshallingTransformer

  val c24Iso20022UnmarshallingTransformer = new C24Iso20022UnmarshallingTransformer


  val commonFlow =
      enrich.header{"VALID" -> {payload:C24Object => if (c24Validator.isValid(payload)) true else false}} --> // inject some header value based on valid or not
      route.onValueOfHeader("VALID")(
        when(true) andThen
          transform { m: Message[C24Object] => println("Storing valid C24 Message in Gemfire"); Map("c24_" + m.getHeaders.getId -> m.getPayload) } -->
          region.store,
        when(false) andThen
          handle { m: Message[C24Object] => println("Invalid C24 object: " + m) }
      )


  val processingFlow =
      route{payload:String => xpathParsingFunction(payload, "//transaction/@type")}(
        when("swift") andThen
          handle { payload: String => println("Routing as SWIFT transaction"); c24SwiftUnmarshallingTransformer parse payload } -->
          commonFlow,
        when("fix") andThen
          handle { payload: String => println("Routing as FIX transaction"); c24FixUnmarshallingTransformer parse payload } -->
          commonFlow,
        when("iso20022") andThen
          handle { payload: String => println("Routing as ISO20022 transaction"); c24Iso20022UnmarshallingTransformer parse payload} -->
          commonFlow
      )


  @Test
  def c24Demo = {

    val inboundJmsFlow =
      jms.listen(requestDestinationName = "c24Queue", connectionFactory = connectionFactory) -->
      processingFlow

    inboundJmsFlow.start

    val validSwiftDoc = C24DemoUtils.getDocumentAsString(new ClassPathResource("valid-swift.xml"))

    val jmsTemplate = new JmsTemplate(connectionFactory);
    val request = new org.apache.activemq.command.ActiveMQQueue("c24Queue")
    jmsTemplate.send(request, new MessageCreator {
      def createMessage(session: Session) = {
        val message = session.createTextMessage();
        message.setText(validSwiftDoc);
        message;
      }
    });

    Thread.sleep(5000)

    inboundJmsFlow.stop
    println("done")
  }

  private def xpathParsingFunction(xml:String, xpathStringExpression:String):String = {
    val document = C24DemoUtils.getDocumentFromString(xml)

    val xpath = XPathFactory.newInstance().newXPath();
    val xpathExpression = xpath.compile(xpathStringExpression);
    xpathExpression.evaluate(document);
  }

  private def createDefaultGemfireRegion: GemfireRegion = {
    val cacheFactoryBean = new CacheFactoryBean
    cacheFactoryBean.afterPropertiesSet
    val cache = cacheFactoryBean.getObject.asInstanceOf[Cache]
    val region = if (cache.getRegion("$") == null)
      cache.createRegionFactory[String, String]().create("$") else cache.getRegion("$")
    new GemfireRegion(region, cache)
  }
}