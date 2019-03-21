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
package org.springframework.integration.dsl
import java.io.File
import java.util.concurrent.Executor

/**
 * @author Oleg Zhurakousky
 */
private[dsl] object FileDsl {
  val fileSchema = " http://www.springframework.org/schema/integration/file " +
    "https://www.springframework.org/schema/integration/file/spring-integration-file.xsd"

}

object file {
  def poll(directory: String) = new {

    def atFixedRate(rate: Int): ListeningIntegrationComposition = {
      val poller = new Poller(fixedRate = rate)
      new ListeningIntegrationComposition(null, new FileInboundAdapterConfig(target = directory, poller = poller)) {

        def withMaxMessagesPerPoll(maxMessagesPerPoll: Int): ListeningIntegrationComposition = {
          val poller = new Poller(fixedRate = rate, maxMessagesPerPoll = maxMessagesPerPoll)
          new ListeningIntegrationComposition(null, new FileInboundAdapterConfig(target = directory, poller = poller)) {

            def withTaskExecutor(taskExecutor: Executor): ListeningIntegrationComposition = {
              val poller = new Poller(fixedRate = rate, maxMessagesPerPoll = maxMessagesPerPoll, taskExecutor = taskExecutor)
              new ListeningIntegrationComposition(null, new FileInboundAdapterConfig(target = directory, poller = poller))
            }
          }
        }

        def withTaskExecutor(taskExecutor: Executor): ListeningIntegrationComposition = {
          val poller = new Poller(fixedRate = rate, taskExecutor = taskExecutor)
          new ListeningIntegrationComposition(null, new FileInboundAdapterConfig(target = directory, poller = poller))
        }

      }

    }

    def withFixedDelay(delay: Int) = {
      val poller = new Poller(fixedDelay = delay)
      new ListeningIntegrationComposition(null, new FileInboundAdapterConfig(target = directory, poller = poller)) {

        def withMaxMessagesPerPoll(maxMessagesPerPoll: Int): ListeningIntegrationComposition = {
          val poller = new Poller(fixedDelay = delay, maxMessagesPerPoll = maxMessagesPerPoll)
          new ListeningIntegrationComposition(null, new FileInboundAdapterConfig(target = directory, poller = poller)) {

            def withTaskExecutor(taskExecutor: Executor): ListeningIntegrationComposition = {
              val poller = new Poller(fixedDelay = delay, maxMessagesPerPoll = maxMessagesPerPoll, taskExecutor = taskExecutor)
              new ListeningIntegrationComposition(null, new FileInboundAdapterConfig(target = directory, poller = poller))
            }
          }
        }

        def withTaskExecutor(taskExecutor: Executor): ListeningIntegrationComposition = {
          val poller = new Poller(fixedDelay = delay, taskExecutor = taskExecutor)
          new ListeningIntegrationComposition(null, new FileInboundAdapterConfig(target = directory, poller = poller))
        }

      }

    }
  }

  def write(directory: String) =
    new SendingEndpointComposition(null, new FileOutboundGatewayConfig(target = directory, oneway = true, fileNameGeneratioinFunction = null)) {

      def asFileName(fileNameGeneratioinFunction: _ => String) =
        new SendingEndpointComposition(null, new FileOutboundGatewayConfig(target = directory, oneway = true, fileNameGeneratioinFunction = fileNameGeneratioinFunction))
    }

  def write = new SendingEndpointComposition(null, new FileOutboundGatewayConfig(target = "", oneway = true, fileNameGeneratioinFunction = null)) {

      def asFileName(fileNameGeneratioinFunction: _ => String) =
        new SendingEndpointComposition(null, new FileOutboundGatewayConfig(target = "", oneway = true, fileNameGeneratioinFunction = fileNameGeneratioinFunction))
    }

}
