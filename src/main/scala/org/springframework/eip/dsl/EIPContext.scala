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

  def apply(compositions:CompletableEIPConfigurationComposition*) = new EIPContext

  def apply(parentApplicationContext:ApplicationContext)(compositions:CompletableEIPConfigurationComposition*) = new EIPContext
}

class EIPContext {
  
  def channel(name:String):MessageChannel = {
    new DirectChannel()
  }

  def channel(name:ChannelComposition):MessageChannel = {
    new DirectChannel()
  }

  def channel(name:PollableComposition):PollableChannel = {
    new QueueChannel()
  }

}