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
package demo

import org.junit.Test
import org.springframework.integration.dsl._
import org.springframework.integration.Message
import org.junit.Ignore
import java.io.File
import utils.DslUtils
import org.mockito.Mockito
import java.util.concurrent.Executor
import org.springframework.integration.sftp.session.DefaultSftpSessionFactory

/**
 * @author Oleg Zhurakousky
 */
class DSLUsageDemoTests {

  @Test
  def sftpInboundAdapterCompilationTest = {

    val sessionFactory = Mockito.mock(classOf[DefaultSftpSessionFactory])
    val executor = Mockito.mock(classOf[Executor])

    sftp(sessionFactory).poll("/").into("~/").atFixedRate(3)

    sftp(sessionFactory).poll("/").into("~/").atFixedRate(3).additionalAttributes(name = "foo", deleteRemoteFiles = false)

    sftp(sessionFactory).poll("/").into("~/").atFixedRate(3).withMaxMessagesPerPoll(4)

    sftp(sessionFactory).poll("/").into("~/").atFixedRate(3).withMaxMessagesPerPoll(4).additionalAttributes(name = "foo")

    sftp(sessionFactory).poll("/").into("~/").atFixedRate(3).withMaxMessagesPerPoll(4).withTaskExecutor(executor)

    sftp(sessionFactory).poll("/").into("~/").atFixedRate(3).withMaxMessagesPerPoll(4).withTaskExecutor(executor).additionalAttributes(name = "fpp")

    sftp(sessionFactory).poll("/").into("~/").atFixedRate(3).withTaskExecutor(executor)

    sftp(sessionFactory).poll("/").into("~/").atFixedRate(3).withTaskExecutor(executor).additionalAttributes(name = "fpp")

    sftp(sessionFactory).poll("/").into("~/").withFixedDelay(6)

    sftp(sessionFactory).poll("/").into("~/").withFixedDelay(6).additionalAttributes(name = "fpp")

    sftp(sessionFactory).poll("/").into("~/").withFixedDelay(3).withMaxMessagesPerPoll(4)

    sftp(sessionFactory).poll("/").into("~/").withFixedDelay(3).withMaxMessagesPerPoll(4).additionalAttributes(name = "fpp")

    sftp(sessionFactory).poll("/").into("~/").withFixedDelay(3).withMaxMessagesPerPoll(4).withTaskExecutor(executor)

    sftp(sessionFactory).poll("/").into("~/").withFixedDelay(3).withMaxMessagesPerPoll(4).withTaskExecutor(executor).additionalAttributes(name = "fpp")

    sftp(sessionFactory).poll("/").into("~/").withFixedDelay(3).withTaskExecutor(executor)

    sftp(sessionFactory).poll("/").into("~/").withFixedDelay(3).withTaskExecutor(executor).additionalAttributes(name = "fpp")

    println("done")
  }

  @Test
  def sftpInboundAdapterTest = {

    val sessionFactory = Mockito.mock(classOf[DefaultSftpSessionFactory])

    val messageFlow =
      sftp(sessionFactory).poll("/").into("build").atFixedRate(3).additionalAttributes(name = "myAdapter", deleteRemoteFiles = false) -->
        handle { f: File => f.getAbsolutePath() }

    messageFlow.start()
    messageFlow.stop

    println("done")
  }

  @Test
  def sftpInboundAdapterWithExplicitChannelTest = {

    val sessionFactory = Mockito.mock(classOf[DefaultSftpSessionFactory])

    val messageFlow =
      sftp(sessionFactory).poll("/").into("build").atFixedRate(3) -->
        Channel("foo") -->
        handle { p: File => println("File: " + p.getAbsolutePath()) }

    messageFlow.start()
    messageFlow.stop

    println("done")
  }

  @Test
  def sftpOutboundAdapterCompilationTest = {

    val sessionFactory = Mockito.mock(classOf[DefaultSftpSessionFactory])
    val executor = Mockito.mock(classOf[Executor])

    sftp(sessionFactory).send("remote/directory")

    sftp(sessionFactory).send { s: String => s }

    sftp(sessionFactory).send("remote/directory").additionalAttributes(name = "foo")

    sftp(sessionFactory).send { s: String => s }.additionalAttributes(name = "foo")

    sftp(sessionFactory).send("remote/directory").asFileName { s: String => s }

    sftp(sessionFactory).send("remote/directory").asFileName { s: String => s }.additionalAttributes(name = "foo")

    sftp(sessionFactory).send { s: String => s }.asFileName { s: String => s }

    sftp(sessionFactory).send { s: String => s }.asFileName { s: String => s }.additionalAttributes(name = "foo")

    println("done")
  }

  @Test
  def sftpOutboundAdapterTest = {

    val sessionFactory = new TestSessionFactory

    val messageFlow =
      transform { p: String => p.toUpperCase() } -->
        sftp(sessionFactory).send("/").asFileName { s: String => "foo.txt" }.additionalAttributes(cacheSessions = false)

    messageFlow.send("Hello File")

    println("done")
  }

}