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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.integration.Message;
import org.springframework.integration.jdbc.JdbcPollingChannelAdapter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map
import org.springframework.integration.dsl.handle._
import org.springframework.integration.dsl.{handle, Channel, jdbc}
;

/**
 * @author Ewan Benfield
 */
class JdbcPollingChannelAdapterTests {

  val logger = LogFactory.getLog(this.getClass())

  var embeddedDatabase:EmbeddedDatabase = null

  var jdbcTemplate:JdbcTemplate = null

	@Before
	def setUp =  {
		val builder = new EmbeddedDatabaseBuilder
		builder
				.setType(EmbeddedDatabaseType.H2)
				.addScript(
						"classpath:org/springframework/integration/jdbc/pollingChannelAdapterIntegrationTest.sql")
		embeddedDatabase = builder.build()
		jdbcTemplate = new JdbcTemplate(embeddedDatabase)
	}

	@After
	def tearDown = {
		embeddedDatabase.shutdown()
	}

	@Test
	def testSimplePollForListOfMapsNoUpdate = {
    def processMessage (message: Message[_]) {

      println("Processing message: " + message.getPayload + " on thread: " + Thread.currentThread().getName + " to sleep")
    }

    val messageFlow =
      jdbc.poll(jdbcTemplate getDataSource).withFixedDelay("select * from item", 10) -->
        Channel("bar") -->
        handle {m:Message[_] => processMessage(m)}

    messageFlow.start()
    jdbcTemplate.update("insert into item values(1,2)")
    println("Sending thread " + Thread.currentThread().getName + " to sleep")
    Thread.sleep(5000)
    println("Waking thread " + Thread.currentThread().getName)
    println("done")

		/*Message<Object> message = adapter.receive();
		Object payload = message.getPayload();
		assertTrue("Wrong payload type", payload instanceof List<?>);
		List<?> rows = (List<?>) payload;
		assertEquals("Wrong number of elements", 1, rows.size());
		assertTrue("Returned row not a map", rows.get(0) instanceof Map<?, ?>);
		Map<?, ?> row = (Map<?, ?>) rows.get(0);
		assertEquals("Wrong id", 1, row.get("id"));
		assertEquals("Wrong status", 2, row.get("status")); */
	}




}