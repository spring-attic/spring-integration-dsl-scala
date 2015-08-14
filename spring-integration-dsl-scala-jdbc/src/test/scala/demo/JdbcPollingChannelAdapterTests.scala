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

package demo;


import org.junit.Test
import org.junit.Assert._
import org.springframework.messaging.Message
import org.junit.Before
import org.junit.After
import org.apache.commons.logging.LogFactory
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.datasource.embedded.{EmbeddedDatabaseType, EmbeddedDatabaseBuilder, EmbeddedDatabase}
import org.springframework.integration.dsl._
import java.util.{ArrayList, Map}
import org.springframework.context.support.ClassPathXmlApplicationContext
import javax.sql.DataSource

/**
 * @author Ewan Benfield
 */
class JdbcPollingChannelAdapterTests {

  val logger = LogFactory.getLog(this.getClass())

  var message: Message[_] = null

  var context:ClassPathXmlApplicationContext = null;
  
  var dataSource:DataSource = null; 
  
  var jdbcTemplate:JdbcTemplate = null

  @Test
  def validateInboundGateway = {
	  
    val jdbc = new Jdbc(context.getBean(classOf[DataSource]))
    
    val inboundFlow =
      jdbc.poll("select * from item").withFixedDelay(10) -->
      handle {m: Message[_] => this.message = m}

    inboundFlow start
    
    Thread.sleep(200);// let the poller run and select nothing
    assertNull(message)
    
    // add something to the database (outside of SI) and ensure the message was polled
    val jdbcTemplate = new JdbcTemplate(context.getBean(classOf[DataSource]));
    jdbcTemplate.update("insert into item (id, status) values(1,2)")

    Thread.sleep(200);// let the poller run again

    inboundFlow stop

    assertNotNull(message)
    assertEquals(1, get(message, "ID"))
    assertEquals(2, get(message, "STATUS"))
  }

  // need more thinking
//  @Test
//  def validateOutboundGatewayWithReply = {
//    val jdbc = new Jdbc(context.getBean(classOf[DataSource]))
//    
//    val query = "insert into item (id, status) values (3, 4)"
//
//    val inboundFlow = 
//      jdbc.poll("select * from item").withFixedDelay(10) -->
//      handle {m: Message[_] => this.message = m}
//
//    val outboundFlow = transform{p:String => p.toUpperCase()} --> jdbc.store(query)
//
//    inboundFlow start
//
//    outboundFlow.send("")
//
//    Thread.sleep(200)
//
//    inboundFlow stop
//
//    assertNotNull(message)
//    assertEquals(3, get(message, "ID"))
//    assertEquals(4, get(message, "STATUS"))
//  }
  
  @Before
  def setUp = {
    context = new ClassPathXmlApplicationContext("derby-config.xml")
    dataSource = context.getBean(classOf[DataSource])
    jdbcTemplate = new JdbcTemplate(dataSource);   
  }
  
  @After
  def teardown = {
   context.destroy()
  }

  def get(mm: Message[_], key: String): AnyRef = {
    val message: Message[ArrayList[Map[String, String]]] = mm.asInstanceOf[Message[ArrayList[Map[String, String]]]]
    val payload: ArrayList[Map[String, String]] = message.getPayload
    val map: Map[String, String] = payload.get(0)
    return map.get(key)
  }
}