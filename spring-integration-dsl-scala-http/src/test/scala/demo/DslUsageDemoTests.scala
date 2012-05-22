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

/**
 * @author Oleg Zhurakousky
 */
class DSLUsageDemoTests {

  @Test
  @Ignore
  def httpOutboundWithFunctionUrl = {

    val tickerService =
      transform { s: String =>
        s.toLowerCase() match {
          case "vmw" => "VMWare"
          case "orcl" => "Oracle"
          case _ => "vmw"
        }
      }

    val httpFlow =
      enrich.header("company" -> { name: String => tickerService.sendAndReceive[String](name) }) -->
      http.GET[String] { ticker: String => "http://www.google.com/finance/info?q=" + ticker } -->
      handle { (payload: String, headers: Map[String, _]) =>
          println("QUOTES for " + headers.get("company") + " : " + payload)
      }

    httpFlow.send("vmw")

    println("done")
  }

  @Test
  def httpOutboundWithStringUrl = {

    val tickerService =
      transform { s: String =>
        s.toLowerCase() match {
          case "vmware" => "vmw"
          case "oracle" => "orcl"
          case _ => ""
        }
      }

    val httpFlow =
      http.GET[String]("http://www.google.com/finance/info?q=" + tickerService.sendAndReceive[String]("Oracle")) -->
        handle { quotes: Message[_] => println("QUOTES for " + quotes.getHeaders().get("company") + " : " + quotes) }

    httpFlow.send("don't care")

    println("done")
  }

  @Test
  def httpOutboundWithPOSTthenGET = {

    val httpFlow =
        http.POST[String]("http://posttestserver.com/post.php") -->
        transform { response: String =>
          println(response) // poor man transformer to extract URL from which the POST results are visible
          response.substring(response.indexOf("View") + 11, response.indexOf("Post") - 1)
        } -->
        http.GET[String] { url: String => url } -->
        handle { response: String => println(response) }

    httpFlow.send("Spring Integration")

    println("done")
  }
}