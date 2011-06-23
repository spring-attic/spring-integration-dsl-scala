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
import org.aopalliance.intercept._
import org.springframework.context._
import org.apache.log4j._
 
/**
 * @author Oleg Zhurakousky
 *
 */
object gateway{
  private val logger = Logger.getLogger(this.getClass)
  /**
   * 
   */
  def withErrorChannel(errorChannelName: String) = new IntegrationComponent() with gateway {
    this.configMap.put(IntegrationComponent.errorChannelName, errorChannelName)
   
    def andName(name: String) = new IntegrationComponent() with gateway {
	    require(StringUtils.hasText(name))
	    this.configMap.put(IntegrationComponent.name, name)
	}
  }
  /**
   * 
   */
  def withName(componentName: String) = new IntegrationComponent() with gateway{
    require(StringUtils.hasText(componentName))
    this.configMap.put(IntegrationComponent.name, componentName)
    
    def andErrorChannel(errorChannelName: String): IntegrationComponent with gateway = {
	    require(StringUtils.hasText(errorChannelName))
	    this.configMap.put(IntegrationComponent.errorChannelName, errorChannelName)
	    this
	}
  }
  /**
   * 
   */
  def using[T](serviceTrait:Class[T]): T with InitializedComponent = {
    require(serviceTrait != null)
    val proxy = generateProxy(serviceTrait, null)
    val gw = new IntegrationComponent with gateway
    return proxy
  }
  /*
   * 
   */
  private def generateProxy[T](serviceTrait:Class[T], g:gateway): T  with InitializedComponent = {
    val gw = new IntegrationComponent with InitializedComponent with gateway
    
   
    if (g != null){
      gw.configMap.putAll(g.asInstanceOf[IntegrationComponent].configMap)
    }
    
    gw.configMap.put(IntegrationComponent.serviceInterface, serviceTrait)
    
    var factory = new ProxyFactory()
    factory.addInterface(classOf[InitializedComponent])
    factory.addInterface(serviceTrait)
    factory.addAdvice(new MethodInterceptor {
      def invoke(invocation:MethodInvocation): Object = {
        val methodName = invocation.getMethod().getName
        if (logger.isDebugEnabled){
          logger.debug("Invoking method: " + methodName)
        }
        
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
/**
 * 
 */
trait gateway { 
	  private[dsl] var defaultReplyChannel: channel = null;

	  private[dsl] var defaultRequestChannel: channel = null;
  
	  private[dsl] var underlyingContext: ApplicationContext = null;
  
	  def using[T](serviceTrait:Class[T]): T with InitializedComponent = {
		require(serviceTrait != null)
		gateway.generateProxy(serviceTrait, this)
	  }
}