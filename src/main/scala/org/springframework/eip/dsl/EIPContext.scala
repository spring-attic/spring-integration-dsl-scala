package org.springframework.eip.dsl

import org.springframework.context.ApplicationContext
import org.springframework.integration.MessageChannel
import org.springframework.integration.core.PollableChannel
import org.springframework.integration.channel.{QueueChannel, DirectChannel}

/**
 * Created by IntelliJ IDEA.
 * User: ozhurakousky
 * Date: 1/24/12
 * Time: 12:18 PM
 * To change this template use File | Settings | File Templates.
 */

object EIPContext {

  def apply(compositions:CompletableEIPConfigurationComposition*) = new EIPContext(compositions: _*)

  def apply(parentApplicationContext:ApplicationContext)(compositions:CompletableEIPConfigurationComposition*) =
            new EIPContext(compositions: _*)
}

class EIPContext(val compositions:CompletableEIPConfigurationComposition*) {
  
  for (composition <- compositions){
    println(composition)
  }
  
  def channel(name:String) = new DirectChannel() with SimpeSendable

  def channel(name:ChannelComposition) = new DirectChannel() with SimpeSendable

  def channel(name:PollableComposition) = new QueueChannel() with SimpeSendable

  private[EIPContext] trait SimpeSendable {
    def send(payload:AnyRef):Boolean = {
       true
    }

    def send(payload:String, timeout:Long):Boolean = {
       true
    }
  }

}