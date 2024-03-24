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
package org.springframework.integration.dsl

import java.util.concurrent.Executor

/**
 * @author Soby Chacko
 *
 */
object inbound {

	def poll(tgt: Any) = new {

		def atFixedRate(rate: Int) = {
			val poller = new Poller(fixedRate = rate)
			new ListeningIntegrationComposition(null, new InboundChannelAdapterConfig(target= tgt, poller = poller)) {

				def withMaxMessagesPerPoll(maxMessagesPerPoll: Int) = {
					val poller = new Poller(fixedRate = rate, maxMessagesPerPoll = maxMessagesPerPoll)
					new ListeningIntegrationComposition(null, new InboundChannelAdapterConfig(target= tgt, poller = poller)) {

						def withTaskExecutor(taskExecutor: Executor) = {
							val poller = new Poller(fixedRate = rate, maxMessagesPerPoll = maxMessagesPerPoll, taskExecutor = taskExecutor)
							new ListeningIntegrationComposition(null, new InboundChannelAdapterConfig(target= tgt, poller = poller))
						}
					}
				}

				def withTaskExecutor(taskExecutor: Executor): ListeningIntegrationComposition = {
					val poller = new Poller(fixedRate = rate, taskExecutor = taskExecutor)
					new ListeningIntegrationComposition(null, new InboundChannelAdapterConfig(target= tgt, poller = poller))
				}
			}
		}

		def withFixedDelay(delay: Int) = {
			val poller = new Poller(fixedDelay = delay)
			new ListeningIntegrationComposition(null, new InboundChannelAdapterConfig(target= tgt, poller = poller)) {

				def withMaxMessagesPerPoll(maxMessagesPerPoll: Int) = {
					val poller = new Poller(fixedDelay = delay, maxMessagesPerPoll = maxMessagesPerPoll)
					new ListeningIntegrationComposition(null, new InboundChannelAdapterConfig(target= tgt, poller = poller)) {

						def withTaskExecutor(taskExecutor: Executor): ListeningIntegrationComposition = {
							val poller = new Poller(fixedDelay = delay, maxMessagesPerPoll = maxMessagesPerPoll, taskExecutor = taskExecutor)
							new ListeningIntegrationComposition(null, new InboundChannelAdapterConfig(target= tgt, poller = poller))
						}
					}
				}

				def withTaskExecutor(taskExecutor: Executor): ListeningIntegrationComposition = {
					val poller = new Poller(fixedDelay = delay, taskExecutor = taskExecutor)
					new ListeningIntegrationComposition(null, new InboundChannelAdapterConfig(target= tgt, poller = poller))
				}
			}
		}
  }
}
