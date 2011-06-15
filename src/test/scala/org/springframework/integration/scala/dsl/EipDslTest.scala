package org.springframework.integration.scala.dsl
import java.util.concurrent._
import org.springframework.integration._
import org.springframework.integration.support._
import org.springframework.integration.message._

object EipDslTest {

  def main(args: Array[String]): Unit = {

    def processMessage(m:Message[Any]):Message[Any] = {
      println("Message: " + m)
      return m
    }
    	
    // argument is pre-defined Scala function
    val t1 = transform(){m:Message[Any] => processMessage(m)	}
    println(t1.targetObject.isInstanceOf[Function[Any, Any]])
    
    // argument is inline Scala function hat returns Message 
    transform(){m:Message[Any] => {MessageBuilder.withPayload(String.valueOf(m.getPayload).toUpperCase).build}	}
    
    // argument is inline Scala function hat returns Object (e.g., String) 
    transform(){m:Message[Any] => {
	    				var s = "hey"; 
	    				println(m);
	    				s
    				}	
    }
    
    // argument is String (which should be treated as script (e.g., SpEL)
    transform(){"hello"}
    	
  }
}
