/*
 * Copyright 2002-2012 the original author or authors.
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
package demo

import org.junit.Test
import org.springframework.integration.Message
import org.junit.Before
import org.junit.After
import org.apache.commons.logging.LogFactory
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.datasource.embedded.{EmbeddedDatabaseType, EmbeddedDatabaseBuilder, EmbeddedDatabase}
import org.springframework.integration.dsl._
import org.springframework.context.support.ClassPathXmlApplicationContext
import javax.sql.DataSource

/**
 * @author Ewan Benfield
 */
class DslUsageDemoTests {

  private final val MAX_PROCESSING_TIME = 10000

  val logger = LogFactory.getLog(this.getClass())
 
  var message: Message[_] = null
  
  var dataSource:DataSource = null; 
  
  var jdbcTemplate:JdbcTemplate = null
  
  var context:ClassPathXmlApplicationContext = null

  
  @Test(timeout = MAX_PROCESSING_TIME)
  def jdbcInboundAdapterTest = {
    
    var jdbc = new Jdbc(dataSource)

    val inboundFlow =
      jdbc.poll("select * from item").withFixedDelay(10) -->
        handle {
          m: Message[_] => message = m
        }

    inboundFlow start

    jdbcTemplate.update("insert into item (id, status) values(1,2)")

    while(message == null)
      Thread.sleep(50)

    println("Received payload: " + message.getPayload)

    inboundFlow stop
  }

  @Test(timeout = MAX_PROCESSING_TIME)
  def jdbcInboundAdapterWithExplicitChannelTest = {
    
    var jdbc = new Jdbc(dataSource)

    val inboundFlow =
      jdbc.poll("select * from item").withFixedDelay(10) -->
        Channel("foo") -->
        handle {
          m: Message[_] => this.message = m
        }

    inboundFlow start

    jdbcTemplate.update("insert into item (id, status) values(1,2)")

    while(message == null)
      Thread.sleep(50)

    println("Received payload: " + message.getPayload)

    inboundFlow stop

  }

  @Test(timeout = MAX_PROCESSING_TIME)
  def jdbcInboundAdapterWithFixedRateTest = {

    var jdbc = new Jdbc(dataSource)
    
    val inboundFlow =
      jdbc.poll("select * from item").atFixedRate(10) -->
        Channel("foo") -->
        handle {
          m: Message[_] => this.message = m
        }

    inboundFlow start

    jdbcTemplate.update("insert into item (id, status) values(1,2)")

    while(message == null)
      Thread.sleep(50)

    println("Received payload: " + message.getPayload)

    inboundFlow stop
  }

  //TODO Actually use Message
//  @Test(timeout = MAX_PROCESSING_TIME)
//  def jdbcOutboundAdapterWithReply = {
//    val jdbc = new Jdbc(dataSource)
//    
//    val query = "insert into item (id, status) values (3, 4)"
//      
//    val inboundFlow = jdbc.poll("select * from item").withFixedDelay(10) -->
//      handle {
//        m: Message[_] => this.message = m
//      }
//
//    val outboundFlow = transform{p:String => p.toUpperCase()} --> jdbc.store(query)
//
//    inboundFlow start
//
//    outboundFlow.send("")
//
//    while(message == null)
//      Thread.sleep(50)
//
//    println("Received payload: " + message.getPayload)
//
//    inboundFlow stop
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
}