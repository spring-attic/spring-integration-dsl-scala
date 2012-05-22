package org.springframework.integration.dsl
import org.junit.Test
import utils.DslUtils

class DslUtilsTest {
  
  @Test
  def testDslUtils = {
    
    val subFlow = 
      handle{s:String => s} -->
      transform{s:String => s}
    
    val messageFlow = 
      handle{s:String => s} -->
      transform{s:String => s} -->
      PubSubChannel() --> (
          filter{s:Boolean => s} -->
          transform{s:String => s},
          handle{s:String => s} -->
          subFlow -->
          aggregate()
      )
      
      
    println(DslUtils.toProductTraversble(messageFlow))
  }

}