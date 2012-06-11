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
package org.springframework.integration.dsl
import java.util.concurrent.Executor
import javax.sql.DataSource

/**
 * @author Ewan Benfield
 */


private[dsl] object JdbcDsl {
  val jdbcSchema = " http://www.springframework.org/schema/integration/jdbc " +
    "http://www.springframework.org/schema/integration/jdbc/spring-integration-jdbc.xsd"

}

//TODO Decide on appropriate API here
object jdbc {
  def poll(dataSource: DataSource) = new {

    def atFixedRate(query: String, rate: Int): ListeningIntegrationComposition = {
      val poller = new Poller(fixedRate = rate)
      new ListeningIntegrationComposition(null, new JdbcInboundAdapterConfig(target = query, poller = poller, dataSource = dataSource)) {

        def withMaxMessagesPerPoll(maxMessagesPerPoll: Int): ListeningIntegrationComposition = {
          val poller = new Poller(fixedRate = rate, maxMessagesPerPoll = maxMessagesPerPoll)
          new ListeningIntegrationComposition(null, new JdbcInboundAdapterConfig(target = query, poller = poller, dataSource = dataSource)) {

            def withTaskExecutor(taskExecutor: Executor): ListeningIntegrationComposition = {
              val poller = new Poller(fixedRate = rate, maxMessagesPerPoll = maxMessagesPerPoll, taskExecutor = taskExecutor)
              new ListeningIntegrationComposition(null, new JdbcInboundAdapterConfig(target = query, poller = poller, dataSource = dataSource))
            }
          }
        }

        def withTaskExecutor(taskExecutor: Executor): ListeningIntegrationComposition = {
          val poller = new Poller(fixedRate = rate, taskExecutor = taskExecutor)
          new ListeningIntegrationComposition(null, new JdbcInboundAdapterConfig(target = query, poller = poller, dataSource = dataSource))
        }

      }

    }

    def withFixedDelay(query: String, delay: Int) = {
      val poller = new Poller(fixedDelay = delay)
      new ListeningIntegrationComposition(null, new JdbcInboundAdapterConfig(target= query, poller = poller, dataSource = dataSource)) {

        def withMaxMessagesPerPoll(maxMessagesPerPoll: Int): ListeningIntegrationComposition = {
          val poller = new Poller(fixedDelay = delay, maxMessagesPerPoll = maxMessagesPerPoll)
          new ListeningIntegrationComposition(null, new JdbcInboundAdapterConfig(target= query, poller = poller, dataSource = dataSource)) {

            def withTaskExecutor(taskExecutor: Executor): ListeningIntegrationComposition = {
              val poller = new Poller(fixedDelay = delay, maxMessagesPerPoll = maxMessagesPerPoll, taskExecutor = taskExecutor)
              new ListeningIntegrationComposition(null, new JdbcInboundAdapterConfig(target= query, poller = poller, dataSource = dataSource))
            }
          }
        }

        def withTaskExecutor(taskExecutor: Executor): ListeningIntegrationComposition = {
          val poller = new Poller(fixedDelay = delay, taskExecutor = taskExecutor)
          new ListeningIntegrationComposition(null, new JdbcInboundAdapterConfig(target= query, poller = poller, dataSource = dataSource))
        }

      }

    }
  }

  def store(query:String, dataSource: DataSource) =
    new SendingEndpointComposition(null, new JdbcOutboundAdapterConfig(target = query, oneway = true, dataSource = dataSource)) {

      /*def asFileName(fileNameGenerationFunction: _ => String) =
      new SendingEndpointComposition(null, new JdbcOutboundGatewayConfig(target = query, dataSource = dataSource))*/
    }
}
