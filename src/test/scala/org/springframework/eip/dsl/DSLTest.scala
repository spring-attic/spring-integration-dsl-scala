package org.springframework.eip.dsl

import org.junit.{Assert, Test}
import org.springframework.eip.dsl.DSL._

class DSLTest {
  
  @Test
  def validateCompositionTypes(){
    
    // this should simply compile
   
	val a:IntegrationComposition = new IntegrationComposition(null, "First") --> new IntegrationComposition(null, "Second")
	
	val b:IntegrationComposition = new IntegrationComposition(null, "First") --> new ChannelIntegrationComposition(null, "Second") --> new IntegrationComposition(null, "Third")
	
	val c:ChannelIntegrationComposition = 
	  					new IntegrationComposition(null, "First") --> 
						new ChannelIntegrationComposition(null, "Second") --> 
						new IntegrationComposition(null, "Third") --> 
						new ChannelIntegrationComposition(null, "Fourth")
						
	val d:BaseIntegrationComposition = 
	  					new IntegrationComposition(null, "First") --> 
						new ChannelIntegrationComposition(null, "Second") --> 
						new ChannelIntegrationComposition(null, "Third") --< (
					       new IntegrationComposition(null, "FirstA") --> 
					       new ChannelIntegrationComposition(null, "SecondA")
					       , 
					       new IntegrationComposition(null, "FirstB")
					       , 
					       new ChannelIntegrationComposition(null, "FirstC")
					    ) 
	
	val e = new IntegrationComposition(null, "First") --> 
			new PollableChannelIntegrationComposition(null, "Second") --> 
			poll.usingFixedDelay(1) -->
			new ChannelIntegrationComposition(null, "Fourth") -->
			new IntegrationComposition(null, "Fifth")
	
    val f = Channel("channelA") -->
			new IntegrationComposition(null, "First") -->
			Channel("channelB").withQueue() --> poll.usingFixedDelay(1) -->
			new IntegrationComposition(null, "Second")
			
  }	
}
