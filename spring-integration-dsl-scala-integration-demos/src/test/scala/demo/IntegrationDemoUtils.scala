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
package demo
import org.springframework.jms.connection.CachingConnectionFactory
import java.io.File
/**
 * @author Oleg Zhurakousky
 */
object IntegrationDemoUtils {

  def localConnectionFactory = {
    val taretConnectionFactory = new org.apache.activemq.ActiveMQConnectionFactory
    taretConnectionFactory.setBrokerURL("vm://localhost")
    val connectionFactory = new CachingConnectionFactory
    connectionFactory.setTargetConnectionFactory(taretConnectionFactory)
    connectionFactory.setSessionCacheSize(10)
    connectionFactory.setCacheProducers(false)
    connectionFactory.setCacheConsumers(false)
    connectionFactory
  }

  def before = {
    val activeMqTempDir = new File("activemq-data")
    deleteDir(activeMqTempDir)

    def deleteDir(directory: File): Unit = {
      if (directory.exists) {
        val children = directory.list();

        if (children != null) {
          for (child <- children) deleteDir(new File(directory, child))
        }
      }
      directory.delete();
    }
  }

  def after = {
    Thread.sleep(1000)
  }
}