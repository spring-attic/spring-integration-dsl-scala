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

/**
 * @author Oleg Zhurakousky
 */
class DSLUsageDemoTests {

  @Test
  def fileInboundAdapterTest = {

    val messageFlow =
      file(""){poll.atFixedRate(1000)} -->
      handle { p: File => println("File: " + p.getAbsolutePath()) }

    messageFlow.start()

    println("done")
  }

  @Test
  def fileInboundAdapterWithExplicitChannelTest = {

    val messageFlow =
      file(""){poll.atFixedRate(1000)} -->
      Channel("foo") -->
      handle { p: File => println("File: " + p.getAbsolutePath()) }

    messageFlow.start()

    println("done")
  }

  @Test
  def fileOutboundAdapter = {

    val messageFlow =
      transform{p:String => p.toUpperCase()} -->
      file.write("")

    messageFlow.send("Hello File")

    println("done")
  }

  @Test
  def fileOutboundAdapterWithFileName = {

    val messageFlow =
      transform{p:String => p.toUpperCase()} -->
      file.write("").asFile{s:String => s.substring(0, 3) + "-file.txt"}

    messageFlow.send("Hello File")

    println("done")
  }

}