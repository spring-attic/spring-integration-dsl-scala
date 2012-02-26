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
import org.junit._
import org.springframework.integration.dsl.utils.DslUtils
import org.springframework.integration.dsl._
import org.springframework.integration.dsl.implicites._
import org.springframework.integration.dsl.builders.PubSubChannel
import org.springframework.integration.dsl.builders.handle
import org.springframework.integration.dsl.builders.Channel
import org.springframework.integration.dsl.builders.transform
import org.springframework.integration.dsl.builders.poll
import org.springframework.integration.dsl.builders.filter

/**
 * @author Oleg Zhurakousky
 */
class DslDemo {
 @Test
  def validateCompositionTypesViaDSL(){
     
      // this should simply compile
     
     val a = 
	  handle.using("1") -->
	  Channel("2") -->
	  transform.using("3") -->
	  Channel.withQueue --> poll.usingFixedDelay(1) -->
	  filter.using("5") -->
	  Channel("6") --< 
	  (
	      handle.using("InnerA-1") -->
	      Channel("InnerA-2") 
	      ,
	      handle.using("InnerB-1") -->
	      Channel("InnerB-2") 
	  )
	  
	 
	 val b = 
	  handle.using("1") -->
	  Channel("2").withDispatcher(failover=false) -->
	  transform.using("3") -->
	  Channel.withQueue(34) --> poll.usingFixedDelay(1) -->
	  filter.using("5") -->
	  Channel("6") --< (
	      handle.using("InnerA-1") -->
	      Channel("InnerA-2") 
      ,
	      handle.using("InnerB-1") -->
	      Channel("InnerB-2") 
	  )

	  val c = 
	   PubSubChannel.applyingSequence --> 
	   handle.using("1") -->
	   PubSubChannel("2") --< (
	      handle.using("InnerA-1") -->
	      Channel("InnerA-2").withDispatcher(failover=true)
	      ,
	      handle.using("InnerB-1") -->
	      Channel("InnerB-2") -->
	      handle.using("InnerB-3") -->
	      PubSubChannel("InnerB-4").applyingSequence --< (
	          handle.using("InnerB-4A-1") -->
	          Channel("InnerB-4A-2") 
	          ,
	          handle.using("InnerB-4B-1") -->
	          Channel("InnerB-4B-2")
	      )
	   )
	   
	   println(DslUtils.toProductList(c))
   }
}
