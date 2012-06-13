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

package demo;


import org.junit.Test
import org.junit.Assert._
import org.springframework.integration.Message
import org.junit.Before
import org.junit.After
import org.apache.commons.logging.LogFactory
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.datasource.embedded.{EmbeddedDatabaseType, EmbeddedDatabaseBuilder, EmbeddedDatabase}
import org.springframework.integration.dsl._
import java.util.{ArrayList, Map}

/**
 * @author Ewan Benfield
 */
class JdbcPollingChannelAdapterTests {

  val logger = LogFactory.getLog(this.getClass())

  var embeddedDatabase: EmbeddedDatabase = null

  var jdbcTemplate: JdbcTemplate = null

  var message: Message[_] = null

  @Before
  def setUp = {
    val builder = new EmbeddedDatabaseBuilder
    builder
      .setType(EmbeddedDatabaseType.DERBY)
      .addScript(
      "classpath:org/springframework/integration/jdbc/pollingChannelAdapterIntegrationTest.sql")
    embeddedDatabase = builder.build()
    jdbcTemplate = new JdbcTemplate(embeddedDatabase)
  }

  @After
  def tearDown = {
    embeddedDatabase.shutdown()
  }

  def get(mm: Message[_], key: String): AnyRef = {
    val message: Message[ArrayList[Map[String, String]]] = mm.asInstanceOf[Message[ArrayList[Map[String, String]]]]
    val payload: ArrayList[Map[String, String]] = message.getPayload
    val map: Map[String, String] = payload.get(0)
    return map.get(key)
  }

  @Test
  def validateInboundGateway = {

    val inboundFlow =
      jdbc.poll(jdbcTemplate getDataSource).withFixedDelay("select * from item", 10) -->
        Channel("foo") -->
        handle {
          m: Message[_] => this.message = m
        }

    inboundFlow start

    jdbcTemplate.update("insert into item (id, status) values(1,2)")

    Thread.sleep(200)

    inboundFlow stop

    assertNotNull(message)
    assertEquals(1, get(message, "ID"))
    assertEquals(2, get(message, "STATUS"))
  }

  @Test
  def validateOutboundGatewayWithReply = {

    val query = "insert into item (id, status) values (3, 4)"

    val inboundFlow = jdbc.poll(jdbcTemplate getDataSource).withFixedDelay("select * from item", 10) -->
      handle {
        m: Message[_] => this.message = m
      }

    val outboundFlow = transform{p:String => p.toUpperCase()} --> jdbc.store(query, jdbcTemplate getDataSource)

    inboundFlow start

    outboundFlow.send("")

    Thread.sleep(200)

    inboundFlow stop

    assertNotNull(message)
    assertEquals(3, get(message, "ID"))
    assertEquals(4, get(message, "STATUS"))
  }
}