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

import org.junit.{Assert, Test}
import org.springframework.integration.Message
import org.springframework.core.task.SimpleAsyncTaskExecutor

/**
 * @author Oleg Zhurakousky
 */

class MessageAggregatorTests {

  @Test
  def validateDefaultAggregatorConfiguration(){

    val aggregator = aggregate()
    Assert.assertNotNull(aggregator.target.name)
    
    val namedAggregator = aggregate().where(name = "myAggregator")
    Assert.assertEquals("myAggregator", namedAggregator.target.name)
    val aggr = namedAggregator.target.asInstanceOf[Aggregator]
    Assert.assertFalse(aggr.keepReleasedMessages)
    Assert.assertFalse(aggr.expireGroupsUponCompletion)
    Assert.assertTrue(aggr.sendPartialResultsOnExpiry)
    Assert.assertNotNull(aggr.messageStore)
  }
  
  // commented syntax is dues to be implemented in 1.0.0.M2
  @Test
  def validateAggregatorConfiguration(){

    aggregate()
    aggregate.where(name = "myAggregator")
    
//    aggregate.on{s:Any => new Object}
//    aggregate.on{s:Any => new Object}.andExpire
//    aggregate.on{s:Any => new Object}.where(name="aggr")
//    aggregate.on{s:Any => new Object}.andExpire.where(name="aggr")
//    aggregate.on{s:Any => new Object}.until{s:Any => 2<1}.where(name="aggr")
//    aggregate.on{s:Any => new Object}.until{s:Any => 2<1}.andExpire.where(name="aggr")
//    
//    aggregate.until{s:Any => 2<1}
//    aggregate.until{s:Any => 2<1}.andExpire
//    aggregate.until{s:Any => 2<1}.where(name="aggr")
//    aggregate.until{s:Any => 2<1}.andExpire.where(name="aggr")   
//    
//    aggregate.using{s:Any => new Object}
//    aggregate.using{s:Any => new Object}.where(name="aggr")
//    
//    aggregate.using{s:Any => new Object}.on{s:Any => new Object}.where(name="aggr")
//    aggregate.using{s:Any => new Object}.on{s:Any => new Object}.andExpire.where(name="aggr")
//    aggregate.using{s:Any => new Object}.on{s:Any => new Object}.until{s:Any => 2<1}.andExpire.where(name="aggr")
//   
//    aggregate.using{s:Any => new Object}.until{s:Any => 2<1}
//    aggregate.using{s:Any => new Object}.until{s:Any => 2<1}.andExpire
//    aggregate.using{s:Any => new Object}.until{s:Any => 2<1}.where(name="aggr")
//    aggregate.using{s:Any => new Object}.until{s:Any => 2<1}.andExpire.where(name="aggr")
    
    
    
//    aggregate.andExpire.where{name=""}
//    aggregate.andExpire.andSendPartialResults.where{name=""}
//    aggregate.andKeepReleasedMessages.where(name = "")
    
//    aggregate.on{""}
//    aggregate.on{""}.where(name = "")
//    aggregate.on{""}.andExpire
//    aggregate.on{""}.andExpire.where(name = "")
//    aggregate.on{""}.andExpire.andSendPartialResults
//    aggregate.on{""}.andExpire.andSendPartialResults.where(name = "")
//    aggregate.on{""}.andKeepReleasedMessages
//    aggregate.on{""}.andKeepReleasedMessages.where(name = "")
    
//    aggregate.on{""}.until{""}
//    aggregate.on{""}.until{""}.where(name = "")
//    aggregate.on{""}.until{""}.andExpire
//    aggregate.on{""}.until{""}.andExpire.where(name = "")
//    aggregate.on{""}.until{""}.andExpire.andSendPartialResults
//    aggregate.on{""}.until{""}.andExpire.andSendPartialResults.where(name = "")
//    aggregate.on{""}.until{""}.andKeepReleasedMessages
//    aggregate.on{""}.until{""}.andKeepReleasedMessages.where(name = "")
//    
//    aggregate.until{s:String => s != "foo"}
//    aggregate.until{s:String => s != "foo"}.where(name = "myAggregator")
//    aggregate.until{""}.andExpire
//    aggregate.until{""}.andExpire.where(name = "")
//    aggregate.until{""}.andExpire.andSendPartialResults
//    aggregate.until{""}.andExpire.andSendPartialResults.where(name = "")
//    aggregate.until{""}.andKeepReleasedMessages
//    aggregate.until{""}.andKeepReleasedMessages.where(name = "")
    
  }

}