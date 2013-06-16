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
import java.io.File
import java.util.concurrent.Executor
import org.springframework.integration.sftp.session.DefaultSftpSessionFactory

/**
 * @author Oleg Zhurakousky
 */
private[dsl] object SftpDsl {
  val sftpSchema = " http://www.springframework.org/schema/integration/sftp " +
    "http://www.springframework.org/schema/integration/sftp/spring-integration-sftp.xsd"

}

object sftp {
  def apply(sessionFactory: DefaultSftpSessionFactory) = new {
    def poll(remoteDirectory: String) = new {

      def into(localDirectory: String) = new {
        def atFixedRate(rate: Int) =
          new ListeningIntegrationComposition(null, new SftpInboundAdapterConfig(target = remoteDirectory, localDirectory = localDirectory, poller = new Poller(fixedRate = rate), sessionFactory = sessionFactory)) with WithInboundAttributes {

            def withMaxMessagesPerPoll(maxMessagesPerPoll: Int) =
              new ListeningIntegrationComposition(null, new SftpInboundAdapterConfig(target = remoteDirectory, localDirectory = localDirectory, poller = new Poller(fixedRate = rate, maxMessagesPerPoll = maxMessagesPerPoll), sessionFactory = sessionFactory)) with WithInboundAttributes {

                def withTaskExecutor(taskExecutor: Executor) = {
                  val poller = new Poller(fixedRate = rate, maxMessagesPerPoll = maxMessagesPerPoll, taskExecutor = taskExecutor)
                  new ListeningIntegrationComposition(null, new SftpInboundAdapterConfig(target = remoteDirectory, localDirectory = localDirectory, poller = poller, sessionFactory = sessionFactory)) with WithInboundAttributes
                }

              }

            def withTaskExecutor(taskExecutor: Executor) = {
              val poller = new Poller(fixedRate = rate, taskExecutor = taskExecutor)
              new ListeningIntegrationComposition(null, new SftpInboundAdapterConfig(target = remoteDirectory, localDirectory = localDirectory, poller = poller, sessionFactory = sessionFactory)) with WithInboundAttributes
            }

          }

        def withFixedDelay(delay: Int) =
          new ListeningIntegrationComposition(null, new SftpInboundAdapterConfig(target = remoteDirectory, localDirectory = localDirectory, poller = new Poller(fixedDelay = delay), sessionFactory = sessionFactory)) with WithInboundAttributes {

            def withMaxMessagesPerPoll(maxMessagesPerPoll: Int) =
              new ListeningIntegrationComposition(null, new SftpInboundAdapterConfig(target = remoteDirectory,localDirectory = localDirectory,  poller = new Poller(fixedDelay = delay, maxMessagesPerPoll = maxMessagesPerPoll), sessionFactory = sessionFactory)) with WithInboundAttributes {

                def withTaskExecutor(taskExecutor: Executor) = {
                  val poller = new Poller(fixedDelay = delay, maxMessagesPerPoll = maxMessagesPerPoll, taskExecutor = taskExecutor)
                  new ListeningIntegrationComposition(null, new SftpInboundAdapterConfig(target = remoteDirectory, localDirectory = localDirectory, poller = poller, sessionFactory = sessionFactory)) with WithInboundAttributes
                }

              }

            def withTaskExecutor(taskExecutor: Executor) = {
              val poller = new Poller(fixedDelay = delay, taskExecutor = taskExecutor)
              new ListeningIntegrationComposition(null, new SftpInboundAdapterConfig(target = remoteDirectory, localDirectory = localDirectory, poller = poller, sessionFactory = sessionFactory)) with WithInboundAttributes
            }

          }

      }

    }

    def send(directory: String) =
      new SendingEndpointComposition(null, new SftpOutboundGatewayConfig(target = directory, oneway = true, fileNameGeneratioinFunction = null, sessionFactory = sessionFactory)) with WithSftpOutboundAttributes {

        def asFileName(fileNameGeneratioinFunction: _ => String) =
          new SendingEndpointComposition(null, new SftpOutboundGatewayConfig(target = directory, oneway = true, fileNameGeneratioinFunction = fileNameGeneratioinFunction, sessionFactory = sessionFactory)) with WithSftpOutboundAttributes
      }

    def send(directoryNameGeneratioinFunction: _ => String) = new SendingEndpointComposition(null, new SftpOutboundGatewayConfig(target = directoryNameGeneratioinFunction, oneway = true, fileNameGeneratioinFunction = null, sessionFactory = sessionFactory))  with WithSftpOutboundAttributes {

      def asFileName(fileNameGeneratioinFunction: _ => String) =
        new SendingEndpointComposition(null, new SftpOutboundGatewayConfig(target = directoryNameGeneratioinFunction, oneway = true, fileNameGeneratioinFunction = fileNameGeneratioinFunction, sessionFactory = sessionFactory)) with WithSftpOutboundAttributes
    }
  }

}

private[dsl] trait WithInboundAttributes {

  def additionalAttributes(name: String = null,
    localDirectory: String = null,
    deleteRemoteFiles: java.lang.Boolean = null,
    autoCreateLocalDirectory: java.lang.Boolean = null,
    filenamePattern: String = null) = {

    val composition: ListeningIntegrationComposition = this.asInstanceOf[ListeningIntegrationComposition]
    val thisTarget = composition.target.asInstanceOf[SftpInboundAdapterConfig]

    val additionalAttributes = Map("id" -> name, "localDirectory" -> localDirectory, "deleteRemoteFiles" -> deleteRemoteFiles,
      "autoCreateLocalDirectory" -> autoCreateLocalDirectory, "filenamePattern" -> filenamePattern)

    new ListeningIntegrationComposition(null, new SftpInboundAdapterConfig(target = thisTarget.target, localDirectory = thisTarget.localDirectory, poller = thisTarget.poller, sessionFactory = thisTarget.sessionFactory, additionalAttributes = additionalAttributes))
  }
}

private[dsl] trait WithSftpOutboundAttributes {

  def additionalAttributes(name: String = null,
    autoCreateDirectory: java.lang.Boolean = null,
    charset: String = null,
    order:java.lang.Integer = null,
    remoteFileSeparator: String = null,
    temporaryFileSuffix: String = null,
    cacheSessions: java.lang.Boolean = null) = {

    val composition: SendingEndpointComposition = this.asInstanceOf[SendingEndpointComposition]
    val thisTarget = composition.target.asInstanceOf[SftpOutboundGatewayConfig]

    val additionalAttributes = Map("id" -> name, "autoCreateDirectory" -> autoCreateDirectory, "charset" -> charset,
      "order" -> order, "remoteFileSeparator" -> remoteFileSeparator, "temporaryFileSuffix" -> temporaryFileSuffix, "cacheSessions" -> cacheSessions)

    new SendingEndpointComposition(null, new SftpOutboundGatewayConfig(target = thisTarget.target, oneway = thisTarget.oneway, fileNameGeneratioinFunction = thisTarget.fileNameGeneratioinFunction, sessionFactory = thisTarget.sessionFactory, additionalAttributes = additionalAttributes))
  }
}

