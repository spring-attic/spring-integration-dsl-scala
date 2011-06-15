/*
 * Copyright 2002-2011 the original author or authors.
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
package org.springframework.integration.scala.dsl

/**
 * @author Oleg Zhurakousky
 *
 */
import java.util.concurrent._

abstract class ConfigurationParameter {

}
/**
 * 
 */
class queue(c:Int) extends ConfigurationParameter{
  val queueSize = c
}
object queue {
  def apply(): queue = new queue(0);
  def apply(c:Int): queue = new queue(c);
}

class poller(rate:Int, maxPerPoll:Int) extends ConfigurationParameter{
  val fixedRate = rate
  val maxMessagesPerPoll = maxPerPoll
}
object poller {
  def apply(fixedRate:Int, maxPerPoll:Int): poller = new poller(fixedRate, maxPerPoll);
}
/**
 * 
 */
class executor(ex:Executor) extends ConfigurationParameter{
  val threadExecutor = ex
}
object executor {
  def apply(): executor = new executor(Executors.newCachedThreadPool);
  def apply(ex:Executor): executor = new executor(ex);
}