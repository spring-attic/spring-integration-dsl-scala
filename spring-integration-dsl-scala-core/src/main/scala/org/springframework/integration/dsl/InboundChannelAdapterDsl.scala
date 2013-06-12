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
