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

import org.junit.{Test, Assert}
import org.springframework.core.task.SyncTaskExecutor

/**
 * @author Soby Chacko
 */
class InboundChannelAdapterTests {

	@Test
	def pollInboundChannelAdapterWithExpressionAtFixedRateWithMaxMessagesAndTaskExecutor() {
		val executor = new SyncTaskExecutor
		val fl = inbound.poll("T(java.lang.System).currentTimeMillis()")
										.atFixedRate(100)
										.withMaxMessagesPerPoll(2)
										.withTaskExecutor(executor)

		Assert.assertTrue(fl.isInstanceOf[ListeningIntegrationComposition])

		Assert.assertTrue(fl.target.isInstanceOf[InboundChannelAdapterConfig])

		val ibcac = fl.target.asInstanceOf[InboundChannelAdapterConfig]
		Assert.assertEquals(ibcac.poller.fixedRate, 100)
		Assert.assertEquals(ibcac.poller.maxMessagesPerPoll, 2)
		Assert.assertEquals(ibcac.poller.taskExecutor, executor)
		Assert.assertEquals(ibcac.target, "T(java.lang.System).currentTimeMillis()")
	}

	@Test
	def pollInboundChannelAdapterWithExpressionAtFixedRateAndTaskExecutor() {
		val executor = new SyncTaskExecutor
		val fl = inbound.poll("T(java.lang.System).currentTimeMillis()")
										.atFixedRate(100)
										.withTaskExecutor(executor)

		Assert.assertTrue(fl.isInstanceOf[ListeningIntegrationComposition])

		Assert.assertTrue(fl.target.isInstanceOf[InboundChannelAdapterConfig])

		val ibcac = fl.target.asInstanceOf[InboundChannelAdapterConfig]
		Assert.assertEquals(ibcac.poller.fixedRate, 100)
		Assert.assertEquals(ibcac.poller.taskExecutor, executor)
		Assert.assertEquals(ibcac.target, "T(java.lang.System).currentTimeMillis()")
	}

	@Test
	def pollInboundChannelAdapterWithFunctionDefinitionAtFixedRateWithMaxMessagesAndTaskExecutor() {
		val executor = new SyncTaskExecutor
		val fl = inbound.poll{ () => java.lang.System.currentTimeMillis }
										.atFixedRate(100)
										.withMaxMessagesPerPoll(2)
										.withTaskExecutor(executor)

		Assert.assertTrue(fl.isInstanceOf[ListeningIntegrationComposition])

		Assert.assertTrue(fl.target.isInstanceOf[InboundChannelAdapterConfig])

		val ibcac = fl.target.asInstanceOf[InboundChannelAdapterConfig]
		Assert.assertEquals(ibcac.poller.fixedRate, 100)
		Assert.assertEquals(ibcac.poller.maxMessagesPerPoll, 2)
		Assert.assertEquals(ibcac.poller.taskExecutor, executor)
		Assert.assertTrue(ibcac.target.isInstanceOf[Function0[_]])
	}

	@Test
	def pollInboundChannelAdapterWithFunctionDefinitionAtFixedRateAndTaskExecutor() {
		val executor = new SyncTaskExecutor
		val fl = inbound.poll{ () => java.lang.System.currentTimeMillis }
										.atFixedRate(100)
										.withTaskExecutor(executor)

		Assert.assertTrue(fl.isInstanceOf[ListeningIntegrationComposition])

		Assert.assertTrue(fl.target.isInstanceOf[InboundChannelAdapterConfig])

		val ibcac = fl.target.asInstanceOf[InboundChannelAdapterConfig]
		Assert.assertEquals(ibcac.poller.fixedRate, 100)
		Assert.assertEquals(ibcac.poller.taskExecutor, executor)
		Assert.assertTrue(ibcac.target.isInstanceOf[Function0[_]])
	}

	@Test
	def pollInboundChannelAdapterWithExpressionAtFixedDelayWithMaxMessagesAndTaskExecutor() {
		val executor = new SyncTaskExecutor
		val fl = inbound.poll("T(java.lang.System).currentTimeMillis()")
										.withFixedDelay(1000)
										.withMaxMessagesPerPoll(2)
										.withTaskExecutor(executor)

		Assert.assertTrue(fl.isInstanceOf[ListeningIntegrationComposition])

		Assert.assertTrue(fl.target.isInstanceOf[InboundChannelAdapterConfig])

		val ibcac = fl.target.asInstanceOf[InboundChannelAdapterConfig]
		Assert.assertEquals(ibcac.poller.fixedDelay, 1000)
		Assert.assertEquals(ibcac.poller.maxMessagesPerPoll, 2)
		Assert.assertEquals(ibcac.poller.taskExecutor, executor)
		Assert.assertEquals(ibcac.target, "T(java.lang.System).currentTimeMillis()")
	}

		@Test
	def pollInboundChannelAdapterWithExpressionAtFixedDelayAndTaskExecutor() {
		val executor = new SyncTaskExecutor
		val fl = inbound.poll("T(java.lang.System).currentTimeMillis()")
										.withFixedDelay(10000)
										.withTaskExecutor(executor)

		Assert.assertTrue(fl.isInstanceOf[ListeningIntegrationComposition])

		Assert.assertTrue(fl.target.isInstanceOf[InboundChannelAdapterConfig])

		val ibcac = fl.target.asInstanceOf[InboundChannelAdapterConfig]
		Assert.assertEquals(ibcac.poller.fixedDelay, 10000)
		Assert.assertEquals(ibcac.poller.taskExecutor, executor)
		Assert.assertEquals(ibcac.target, "T(java.lang.System).currentTimeMillis()")
	}

	@Test
	def pollInboundChannelAdapterWithFunctionDefinitionAtFixedDelayWithMaxMessagesAndTaskExecutor() {
		val executor = new SyncTaskExecutor
		val fl = inbound.poll{ () => java.lang.System.currentTimeMillis }
										.withFixedDelay(300)
										.withMaxMessagesPerPoll(2)
										.withTaskExecutor(executor)

		Assert.assertTrue(fl.isInstanceOf[ListeningIntegrationComposition])

		Assert.assertTrue(fl.target.isInstanceOf[InboundChannelAdapterConfig])

		val ibcac = fl.target.asInstanceOf[InboundChannelAdapterConfig]
		Assert.assertEquals(ibcac.poller.fixedDelay, 300)
		Assert.assertEquals(ibcac.poller.maxMessagesPerPoll, 2)
		Assert.assertEquals(ibcac.poller.taskExecutor, executor)
		Assert.assertTrue(ibcac.target.isInstanceOf[Function0[_]])
	}

	@Test
	def pollInboundChannelAdapterWithFunctionDefinitionAtFixedDelayAndTaskExecutor() {
		val executor = new SyncTaskExecutor
		val fl = inbound.poll{ () => java.lang.System.currentTimeMillis }
										.withFixedDelay(1050)
										.withTaskExecutor(executor)

		Assert.assertTrue(fl.isInstanceOf[ListeningIntegrationComposition])

		Assert.assertTrue(fl.target.isInstanceOf[InboundChannelAdapterConfig])

		val ibcac = fl.target.asInstanceOf[InboundChannelAdapterConfig]
		Assert.assertEquals(ibcac.poller.fixedDelay, 1050)
		Assert.assertEquals(ibcac.poller.taskExecutor, executor)
		Assert.assertTrue(ibcac.target.isInstanceOf[Function0[_]])
	}
}
