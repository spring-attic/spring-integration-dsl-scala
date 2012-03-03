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

import org.springframework.integration.store.{SimpleMessageStore, MessageStore}
import java.util.UUID
import org.springframework.util.StringUtils


/**
 * @author Oleg Zhurakousky
 */

/**
 * SERVICE ACTIVATOR
 */
object handle {
  
  def using(function:Function1[_,_]) = new SendingEndpointComposition(null, new ServiceActivator(target = function)) {
    def where(name:String)= {
      require(StringUtils.hasText(name), "'name' must not be empty")
      new SendingEndpointComposition(null, new ServiceActivator(name = name, target = function))
    }
  }
  
}



/**
 * TRANSFORMER
 */
object transform {

  def using(function:Function1[_,AnyRef]) = new SendingEndpointComposition(null, new Transformer(target = function)) {
    def where(name:String) = { 
      require(StringUtils.hasText(name), "'name' must not be empty")
      new SendingEndpointComposition(null, new Transformer(name = name, target = function))
    }
  }
}

/**
 * FILTER
 */
object filter {

  def using(function:Function1[_,Boolean]) = new SendingEndpointComposition(null, new MessageFilter(target = function)) {
    def where(name:String  = "$flt_" + UUID.randomUUID().toString.substring(0, 8), exceptionOnRejection:Boolean = false) = {
      new SendingEndpointComposition(null, new MessageFilter(name = name, target = function, exceptionOnRejection = exceptionOnRejection))
    }
  }
}


/**
 * ENRICHER (payload, header)
 */
object enrich {
  
  def apply(function:Function1[_,AnyRef]) = new SendingEndpointComposition(null, new Enricher(target = function)) {
    def where(name:String) = {
      require(StringUtils.hasText(name), "'name' must not be empty")
      new SendingEndpointComposition(null, new Enricher(name = name, target = function))
    }
  }
  
  def headers(headersMap:(Tuple2[String, AnyRef])*) = new SendingEndpointComposition(null, new Enricher(target = headersMap)) {
    def where(name:String) = {
      require(StringUtils.hasText(name), "'name' must not be empty")
      new SendingEndpointComposition(null, new Enricher(name = name, target = headersMap))
    }
  }
 
  def header(headerMap:Tuple2[String, AnyRef]) = new SendingEndpointComposition(null, new Enricher(target = headerMap)) {
    def where(name:String) = {
      require(StringUtils.hasText(name), "'name' must not be empty")
      new SendingEndpointComposition(null, new Enricher(name = name, target = headerMap))
    }
  }
}

/**
 * SPLITTER
 */
object split {

  def using(function:Function1[_,Iterable[Any]]) = new SendingEndpointComposition(null, new MessageSplitter(target=function)) {
    def where(name:String = "$split_" + UUID.randomUUID().toString.substring(0, 8), applySequence:Boolean = true) = 
      new SendingEndpointComposition(null, new MessageSplitter(name = name, target = function, applySequence = applySequence))
  }
}

private[dsl] class ServiceActivator(name:String = "$sa_" + UUID.randomUUID().toString.substring(0, 8), target:Any)
            extends SimpleEndpoint(name, target)

private[dsl] class MessagingBridge(name:String = "$br_" + UUID.randomUUID().toString.substring(0, 8))
            extends SimpleEndpoint(name, null)

private[dsl] class Enricher(name:String = "$enr_" + UUID.randomUUID().toString.substring(0, 8), target:Any)
            extends SimpleEndpoint(name, target)

private[dsl] class Transformer(name:String = "$xfmr_" + UUID.randomUUID().toString.substring(0, 8), target:Any)
            extends SimpleEndpoint(name, target)

private[dsl] class MessageFilter(name:String = "$flt_" + UUID.randomUUID().toString.substring(0, 8), target:Any, val exceptionOnRejection:Boolean = false)
            extends SimpleEndpoint(name, target)

private[dsl] class MessageSplitter(name:String = "$splt_" + UUID.randomUUID().toString.substring(0, 8), target:Any, val applySequence:Boolean = false)
            extends SimpleEndpoint(name, target)

private[dsl] abstract class SimpleEndpoint(name:String, val target:Any = null) extends IntegrationComponent(name) {
  override def toString = name
}

