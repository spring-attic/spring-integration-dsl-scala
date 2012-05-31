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
import org.springframework.integration.ftp.session.DefaultFtpSessionFactory
import java.util.concurrent.Executor

/**
 * @author Oleg Zhurakousky
 */
class DSLUsageDemoTests {

  @Test
  def ftpInboundAdapterCompilationTest = {

    val sessionFactory = Mockito.mock(classOf[DefaultFtpSessionFactory])
    val executor = Mockito.mock(classOf[Executor])

    ftp(sessionFactory).poll("/").into("~/").atFixedRate(3)

    ftp(sessionFactory).poll("/").into("~/").atFixedRate(3).additionalAttributes(name = "foo", deleteRemoteFiles = false)

    ftp(sessionFactory).poll("/").into("~/").atFixedRate(3).withMaxMessagesPerPoll(4)

    ftp(sessionFactory).poll("/").into("~/").atFixedRate(3).withMaxMessagesPerPoll(4).additionalAttributes(name = "foo")

    ftp(sessionFactory).poll("/").into("~/").atFixedRate(3).withMaxMessagesPerPoll(4).withTaskExecutor(executor)

    ftp(sessionFactory).poll("/").into("~/").atFixedRate(3).withMaxMessagesPerPoll(4).withTaskExecutor(executor).additionalAttributes(name = "fpp")

    ftp(sessionFactory).poll("/").into("~/").atFixedRate(3).withTaskExecutor(executor)

    ftp(sessionFactory).poll("/").into("~/").atFixedRate(3).withTaskExecutor(executor).additionalAttributes(name = "fpp")

    ftp(sessionFactory).poll("/").into("~/").withFixedDelay(6)

    ftp(sessionFactory).poll("/").into("~/").withFixedDelay(6).additionalAttributes(name = "fpp")

    ftp(sessionFactory).poll("/").into("~/").withFixedDelay(3).withMaxMessagesPerPoll(4)

    ftp(sessionFactory).poll("/").into("~/").withFixedDelay(3).withMaxMessagesPerPoll(4).additionalAttributes(name = "fpp")

    ftp(sessionFactory).poll("/").into("~/").withFixedDelay(3).withMaxMessagesPerPoll(4).withTaskExecutor(executor)

    ftp(sessionFactory).poll("/").into("~/").withFixedDelay(3).withMaxMessagesPerPoll(4).withTaskExecutor(executor).additionalAttributes(name = "fpp")

    ftp(sessionFactory).poll("/").into("~/").withFixedDelay(3).withTaskExecutor(executor)

    ftp(sessionFactory).poll("/").into("~/").withFixedDelay(3).withTaskExecutor(executor).additionalAttributes(name = "fpp")

    println("done")
  }

  @Test
  def ftpInboundAdapterTest = {

    val sessionFactory = Mockito.mock(classOf[DefaultFtpSessionFactory])

    val messageFlow =
      ftp(sessionFactory).poll("/").into("build").atFixedRate(3).additionalAttributes(name = "myAdapter", deleteRemoteFiles = false) -->
        handle { f: File => f.getAbsolutePath() }

    messageFlow.start()
    messageFlow.stop

    println("done")
  }

  @Test
  def ftpInboundAdapterWithExplicitChannelTest = {

    val sessionFactory = Mockito.mock(classOf[DefaultFtpSessionFactory])

    val messageFlow =
      ftp(sessionFactory).poll("/").into("build").atFixedRate(3) -->
        Channel("foo") -->
        handle { p: File => println("File: " + p.getAbsolutePath()) }

    messageFlow.start()
    messageFlow.stop

    println("done")
  }

  @Test
  def ftpOutboundAdapterCompilationTest = {

    val sessionFactory = Mockito.mock(classOf[DefaultFtpSessionFactory])
    val executor = Mockito.mock(classOf[Executor])

    ftp(sessionFactory).send("remote/directory")

    ftp(sessionFactory).send { s: String => s }

    ftp(sessionFactory).send("remote/directory").additionalAttributes(name = "foo")

    ftp(sessionFactory).send { s: String => s }.additionalAttributes(name = "foo")

    ftp(sessionFactory).send("remote/directory").asFileName { s: String => s }

    ftp(sessionFactory).send("remote/directory").asFileName { s: String => s }.additionalAttributes(name = "foo")

    ftp(sessionFactory).send { s: String => s }.asFileName { s: String => s }

    ftp(sessionFactory).send { s: String => s }.asFileName { s: String => s }.additionalAttributes(name = "foo")

    println("done")
  }

  @Test
  def ftpOutboundAdapterTest = {

    val sessionFactory = new TestSessionFactory

    val messageFlow =
      transform { p: String => p.toUpperCase() } -->
        ftp(sessionFactory).send("/").asFileName { s: String => "foo.txt" }.additionalAttributes(cacheSessions = false)

    messageFlow.send("Hello File")

    println("done")
  }

}