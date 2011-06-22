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
import org.springframework.util._
import org.springframework.aop.framework._
import org.springframework.aop.target._
import java.lang.reflect._
import org.aopalliance.intercept._
import org.springframework.context._
import org.springframework.integration.gateway._
/**
 * @author Oleg Zhurakousky
 *
 */
private[dsl] class AbstractEndpoint extends IntegrationComponent {

  private[dsl] var inputChannel: channel = null;

  private[dsl] var outputChannel: channel = null;
}

/**
 * Transformer
 */
class transform extends AbstractEndpoint {
  override def toString = {
    var name = this.configMap.get(IntegrationComponent.name).asInstanceOf[String]
    if (StringUtils.hasText(name)) name else "transforml_" + this.hashCode
  }
}
object transform {
  def withName(componentName: String) = new transform() with using with andPoller {
    this.configMap.put(IntegrationComponent.name, componentName)
  }

  def using(usingCode: AnyRef) = new transform() with InitializedComponent{
    this.configMap.put(IntegrationComponent.using, usingCode)
  }

  def withPoller(fixedRate: Int, maxMessagesPerPoll: Int) = new transform() with using with andName {
    this.configMap.put(IntegrationComponent.poller, Map(IntegrationComponent.maxMessagesPerPoll -> maxMessagesPerPoll, IntegrationComponent.fixedRate -> fixedRate))
  }
  def withPoller(fixedRate: Int) = new transform() with using with andName {
    this.configMap.put(IntegrationComponent.poller, Map(IntegrationComponent.fixedRate -> fixedRate))
  }
}

/**
 * Service Activator
 */
class service extends AbstractEndpoint {
  override def toString = {
    var name = this.configMap.get(IntegrationComponent.name).asInstanceOf[String]
    if (StringUtils.hasText(name)) name else "activate_" + this.hashCode
  }
}


object service {
  def withName(componentName: String) = new service() with using with andPoller {
    require(StringUtils.hasText(componentName))
    this.configMap.put(IntegrationComponent.name, componentName)
  }

  def using(usingCode: AnyRef) = new service() with InitializedComponent{
    this.configMap.put(IntegrationComponent.using, usingCode)
  }

  def withPoller(maxMessagesPerPoll: Int, fixedRate: Int) = new service() with using with andName {
    this.configMap.put(IntegrationComponent.poller, Map(IntegrationComponent.maxMessagesPerPoll -> maxMessagesPerPoll, IntegrationComponent.fixedRate -> fixedRate))
  }
  def withPoller(maxMessagesPerPoll: Int, cron: String) = new service() with using with andName {
    this.configMap.put(IntegrationComponent.poller, Map(IntegrationComponent.maxMessagesPerPoll -> maxMessagesPerPoll, IntegrationComponent.cron -> cron))
  }
  def withPoller(cron: String) = new service() with using with andName {
    this.configMap.put(IntegrationComponent.poller, Map(IntegrationComponent.cron -> cron))
  }
  def withPoller(fixedRate: Int) = new service() with using with andName {
    this.configMap.put(IntegrationComponent.poller, Map(IntegrationComponent.fixedRate -> fixedRate))
  }
}

/**
 * Filter
 */
class filter extends AbstractEndpoint {
  override def toString = {
    var name = this.configMap.get(IntegrationComponent.name).asInstanceOf[String]
    if (StringUtils.hasText(name)) name else "filter_" + this.hashCode
  }
}


object filter {
  def withName(componentName: String) = new filter() with using with andPoller {
    require(StringUtils.hasText(componentName))
    this.configMap.put(IntegrationComponent.name, componentName)
  }

  def using(usingCode: AnyRef) = new filter() with InitializedComponent{
    this.configMap.put(IntegrationComponent.using, usingCode)
  }

  def withPoller(maxMessagesPerPoll: Int, fixedRate: Int) = new filter() with using with andName {
    this.configMap.put(IntegrationComponent.poller, Map(IntegrationComponent.maxMessagesPerPoll -> maxMessagesPerPoll, IntegrationComponent.fixedRate -> fixedRate))
  }
  def withPoller(maxMessagesPerPoll: Int, cron: String) = new filter() with using with andName {
    this.configMap.put(IntegrationComponent.poller, Map(IntegrationComponent.maxMessagesPerPoll -> maxMessagesPerPoll, IntegrationComponent.cron -> cron))
  }
  def withPoller(cron: String) = new filter() with using with andName {
    this.configMap.put(IntegrationComponent.poller, Map(IntegrationComponent.cron -> cron))
  }
  def withPoller(fixedRate: Int) = new filter() with using with andName {
    this.configMap.put(IntegrationComponent.poller, Map(IntegrationComponent.fixedRate -> fixedRate))
  }
}

/**
 * Splitter
 */
class split extends AbstractEndpoint {
  override def toString = {
    var name = this.configMap.get(IntegrationComponent.name).asInstanceOf[String]
    if (StringUtils.hasText(name)) name else "splitter_" + this.hashCode
  }
}


object split {
  def withName(componentName: String) = new split() with using with andPoller {
    require(StringUtils.hasText(componentName))
    this.configMap.put(IntegrationComponent.name, componentName)
  }

  def using(usingCode: AnyRef) = new split() with InitializedComponent{
    this.configMap.put(IntegrationComponent.using, usingCode)
  }

  def withPoller(maxMessagesPerPoll: Int, fixedRate: Int) = new split() with using with andName {
    this.configMap.put(IntegrationComponent.poller, Map(IntegrationComponent.maxMessagesPerPoll -> maxMessagesPerPoll, IntegrationComponent.fixedRate -> fixedRate))
  }
  def withPoller(maxMessagesPerPoll: Int, cron: String) = new split() with using with andName {
    this.configMap.put(IntegrationComponent.poller, Map(IntegrationComponent.maxMessagesPerPoll -> maxMessagesPerPoll, IntegrationComponent.cron -> cron))
  }
  def withPoller(cron: String) = new split() with using with andName {
    this.configMap.put(IntegrationComponent.poller, Map(IntegrationComponent.cron -> cron))
  }
  def withPoller(fixedRate: Int) = new split() with using with andName {
    this.configMap.put(IntegrationComponent.poller, Map(IntegrationComponent.fixedRate -> fixedRate))
  }
}
/**
 * ROUTER (work in progress)
 */
class route extends AbstractEndpoint {
  override def toString = {
    var name = this.configMap.get("name").asInstanceOf[String]
    if (StringUtils.hasText(name)) name else "route_" + this.hashCode
  }
}
object route {

  def withName(componentName: String) = new route() with using with andPoller {
    require(StringUtils.hasText(componentName))
    this.configMap.put(IntegrationComponent.name, componentName)
  }
  
  def withChannelMappings(channelMappings: collection.immutable.Map[String, Any]) = new route() with using with andPoller {
    require(channelMappings != null)
    //this.configMap.put(IntegrationComponent.name, componentName)
  }

  def using(usingCode: AnyRef) = new route() with InitializedComponent{
    this.configMap.put(IntegrationComponent.using, usingCode)
  }

  def withPoller(maxMessagesPerPoll: Int, fixedRate: Int) = new route() with using with andName {
    this.configMap.put(IntegrationComponent.poller, Map(IntegrationComponent.maxMessagesPerPoll -> maxMessagesPerPoll, IntegrationComponent.fixedRate -> fixedRate))
  }
  def withPoller(maxMessagesPerPoll: Int, cron: String) = new route() with using with andName {
    this.configMap.put(IntegrationComponent.poller, Map(IntegrationComponent.maxMessagesPerPoll -> maxMessagesPerPoll, IntegrationComponent.cron -> cron))
  }
  def withPoller(cron: String) = new route() with using with andName {
    this.configMap.put(IntegrationComponent.poller, Map(IntegrationComponent.cron -> cron))
  }
  def withPoller(fixedRate: Int) = new route() with using with andName {
    this.configMap.put(IntegrationComponent.poller, Map(IntegrationComponent.fixedRate -> fixedRate))
  }
  
  trait andMappings extends route with using {
	def andMappings(r: AnyRef): route with using = {
	  this.configMap.put("andMappings", "andMappings")
	  this
	}
  }
}

/**
 * Messaging Gateway
 */
object gateway{
  
  def withErrorChannel(errorChannelName: String) = new IntegrationComponent() with gateway {
    this.configMap.put(IntegrationComponent.errorChannelName, errorChannelName)
   
    def andName(name: String) = new IntegrationComponent() with gateway {
	    require(StringUtils.hasText(name))
	    this.configMap.put(IntegrationComponent.name, name)
	    println(this.configMap) 
	}
  }
  
  def withName(componentName: String) = new IntegrationComponent() with gateway{
    require(StringUtils.hasText(componentName))
    this.configMap.put(IntegrationComponent.name, componentName)
    
    def andErrorChannel(errorChannelName: String): IntegrationComponent with gateway = {
	    require(StringUtils.hasText(errorChannelName))
	    this.configMap.put(IntegrationComponent.errorChannelName, errorChannelName)
	    this
	}
  }
 
  def using[T](serviceTrait:Class[T]): T with InitializedComponent = {
    require(serviceTrait != null)
    val proxy = generateProxy(serviceTrait)
    val gw = new IntegrationComponent with gateway
    return proxy
  }
  
  private def generateProxy[T](serviceTrait:Class[T]): T  with InitializedComponent = {
    
    val gw = new IntegrationComponent with InitializedComponent with gateway
    gw.configMap.put(IntegrationComponent.serviceInterface, serviceTrait)
    
    var factory = new ProxyFactory()
    factory.addInterface(classOf[InitializedComponent])
    factory.addInterface(serviceTrait)
    factory.addAdvice(new MethodInterceptor {
      def invoke(invocation:MethodInvocation): Object = {
        val methodName = invocation.getMethod().getName
        println("Invoking method: " + methodName)
        methodName match {
          case "$minus$greater" => {
            val to = invocation.getArguments()(0).asInstanceOf[collection.mutable.WrappedArray[_]](0).asInstanceOf[InitializedComponent]
            return gw -> to
          }
          case _ => {
            try {
              if (gw.underlyingContext != null){
                var gatewayProxy = gw.underlyingContext.getBean(serviceTrait)
                var method = invocation.getMethod
                ReflectionUtils.makeAccessible(method);
                var argument = invocation.getArguments()(0)
			    return method.invoke(gatewayProxy, argument);
              }
              return invocation.proceed
            }
            catch {
              case ex:Exception => throw new IllegalArgumentException("Invocation of method '" + 
                  methodName + "' happened too early. Proxy has not been initialized", ex)
            }          
          }
        }    
      }
    })
    var proxy = factory.getProxy
    return proxy.asInstanceOf[T with InitializedComponent]
  } 
}
trait gateway { 
	  private[dsl] var defaultReplyChannel: channel = null;

	  private[dsl] var defaultRequestChannel: channel = null;
  
	  private[dsl] var underlyingContext: ApplicationContext = null;
  
	  def using[T](serviceTrait:Class[T]): T with InitializedComponent = {
		require(serviceTrait != null)
		gateway.generateProxy(serviceTrait)
	  }
}


/**
 * Common Traits
 */
trait using extends IntegrationComponent {
  def using(usingCode: AnyRef): InitializedComponent = { 
    this match {
      case service:service => {
        val activator = new service() with InitializedComponent
        activator.configMap.putAll(this.configMap)
        activator.configMap.put(IntegrationComponent.using, usingCode)
        activator
      }
      case transformer:transform => {
        val transformer = new transform() with InitializedComponent
        transformer.configMap.putAll(this.configMap)
        transformer.configMap.put(IntegrationComponent.using, usingCode)
        transformer
      }
      case fltr:filter => {
        val filter = new filter() with InitializedComponent
        filter.configMap.putAll(this.configMap)
        filter.configMap.put(IntegrationComponent.using, usingCode)
        filter
      }
      case _ => {
         throw new IllegalArgumentException("'using' trait is unsupported for this pattern: " + this) 
      }
    }
  }
}
trait andPoller extends AbstractEndpoint with using with andName{
  def andPoller(maxMessagesPerPoll: Int, fixedRate: Int): AbstractEndpoint with using with andName = {
    this.configMap.put(IntegrationComponent.poller, Map(IntegrationComponent.maxMessagesPerPoll -> maxMessagesPerPoll, IntegrationComponent.fixedRate -> fixedRate))
    this
  }
  def andPoller(maxMessagesPerPoll: Int, cron: String):AbstractEndpoint with using with andName = {
    this.configMap.put(IntegrationComponent.poller, Map(IntegrationComponent.maxMessagesPerPoll -> maxMessagesPerPoll, IntegrationComponent.cron -> cron))
    this
  }
  def andPoller(cron: String): AbstractEndpoint with using with andName =  {
    this.configMap.put(IntegrationComponent.poller, Map(IntegrationComponent.cron -> cron))
    this
  }
  def andPoller(fixedRate: Int):AbstractEndpoint with using with andName = {
    this.configMap.put(IntegrationComponent.poller, Map(IntegrationComponent.fixedRate -> fixedRate))
    this
  }
}