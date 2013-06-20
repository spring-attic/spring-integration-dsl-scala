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
package org.springframework.integration.dsl

import LogLevel._
import org.junit.{Test, Assert}

/**
 * @author Soby Chacko
 */
class LoggingChannelAdapterEndpointTests {

	@Test
	def basicLoggingChannelAdpater() {

		val lc = log("logger")

		Assert.assertTrue(lc.isInstanceOf[SendingEndpointComposition])
		Assert.assertTrue(lc.target.isInstanceOf[Logger])

		val lcae = lc.target.asInstanceOf[Logger]
		Assert.assertEquals(lcae.loggerName, "logger")
		Assert.assertFalse(lcae.logFullMessage)
		Assert.assertEquals(lcae.logLevel, INFO)
	}

	@Test
	def basicLoggingChannelAdpaterWithoutExplictLoggerName() {

		val lc = log.withFullMessage(true).withLogLevel(TRACE)

		Assert.assertTrue(lc.isInstanceOf[SendingEndpointComposition])
		Assert.assertTrue(lc.target.isInstanceOf[Logger])

		val lcae = lc.target.asInstanceOf[Logger]
		Assert.assertNull(lcae.loggerName)
		Assert.assertTrue(lcae.logFullMessage)
		Assert.assertEquals(lcae.logLevel, TRACE)
	}

	@Test
	def loggingChannelAdpaterWithFullLogMessage() {

		val lc = log("logger").withFullMessage(true)

		Assert.assertTrue(lc.isInstanceOf[SendingEndpointComposition])
		Assert.assertTrue(lc.target.isInstanceOf[Logger])

		val lcae = lc.target.asInstanceOf[Logger]
		Assert.assertEquals(lcae.loggerName, "logger")
		Assert.assertTrue(lcae.logFullMessage)
	}

	@Test
	def loggingChannelAdpaterWithFullLogMessageWithLogLevel() {

		val lc = log("logger").withFullMessage(true).withLogLevel(DEBUG)

		Assert.assertTrue(lc.isInstanceOf[SendingEndpointComposition])
		Assert.assertTrue(lc.target.isInstanceOf[Logger])

		val lcae = lc.target.asInstanceOf[Logger]
		Assert.assertEquals(lcae.loggerName, "logger")
		Assert.assertTrue(lcae.logFullMessage)
		Assert.assertEquals(lcae.logLevel, DEBUG)
	}

	@Test
	def loggingChannelAdpaterWithLogLevel() {

		val lc = log("logger").withLogLevel(DEBUG)

		Assert.assertTrue(lc.isInstanceOf[SendingEndpointComposition])
		Assert.assertTrue(lc.target.isInstanceOf[Logger])

		val lcae = lc.target.asInstanceOf[Logger]
		Assert.assertEquals(lcae.loggerName, "logger")
		Assert.assertEquals(lcae.logLevel, DEBUG)
	}

	@Test
	def loggingChannelAdpaterWithLogLevelWithFullLogMessage() {

		val lc = log("logger").withLogLevel(DEBUG).withFullMessage(true)

		Assert.assertTrue(lc.isInstanceOf[SendingEndpointComposition])
		Assert.assertTrue(lc.target.isInstanceOf[Logger])

		val lcae = lc.target.asInstanceOf[Logger]
		Assert.assertEquals(lcae.loggerName, "logger")
		Assert.assertTrue(lcae.logFullMessage)
		Assert.assertEquals(lcae.logLevel, DEBUG)
	}

	@Test
	def loggingChannelAdpaterWithExpressionAndLogLevel() {

		val lc = log("logger").withExpression("hello").withLogLevel(DEBUG)

		Assert.assertTrue(lc.isInstanceOf[SendingEndpointComposition])
		Assert.assertTrue(lc.target.isInstanceOf[Logger])

		val lcae = lc.target.asInstanceOf[Logger]
		Assert.assertEquals(lcae.loggerName, "logger")
		Assert.assertEquals(lcae.expression, "hello")
		Assert.assertEquals(lcae.logLevel, DEBUG)
	}
}
