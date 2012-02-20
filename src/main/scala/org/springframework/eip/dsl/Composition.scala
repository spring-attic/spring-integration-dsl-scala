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
package org.springframework.eip.dsl

import java.util.UUID
import java.lang.{IllegalStateException, ThreadLocal}
import org.apache.log4j.Logger
import org.springframework.integration.Message
import collection.JavaConversions._
import scala.reflect.Manifest


/**
 * @author Oleg Zhurakousky
 * Date: 1/12/12
 */
abstract class EIPConfigurationComposition(val parentComposition:EIPConfigurationComposition, val target:Any) {

  private val logger = Logger.getLogger(this.getClass)

  

  private[dsl] trait WithErrorFlow {
    def withErrorFlow(errorComposition:EIPConfigurationComposition):Unit
  }
  
//  private[dsl] def add[T](composition: EIPConfigurationComposition):T = {
//    null.asInstanceOf[T]
//  }
  
  def copy(): EIPConfigurationComposition = {
    this
  }
    
  def start():Unit = {

  }

  def stop():Unit = {

  }
 
  def send(message:Any, timeout:Long = 0, headers:Map[String,  Any] = null):Boolean = {
//    //val context = this.getContext()
//    context.send(message, timeout, headers)
    false
  }

  def sendAndReceive[T](message:Any, timeout:Long = 0, headers:Map[String,  Any] = null, errorFlow:EIPConfigurationComposition = null)
                       (implicit m: scala.reflect.Manifest[T]): T  = {
//    val context = this.getContext()
//
//    val reply = try {
//       val replyMessage = context.sendAndReceive(message)
//       if (m.erasure.isAssignableFrom(classOf[Message[_]])){
//         replyMessage.asInstanceOf[T]
//       }
//       else {
//         val reply = replyMessage.getPayload
//         val convertedReply = reply match {
//           case list:java.util.List[_]  => {
//             list.toList
//           }
//           case list:java.util.Set[_] => {
//             list.toSet
//           }
//           case list:java.util.Map[_, _] => {
//             list.toMap
//           }
//           case _ => {
//             reply
//           }
//         }
//         convertedReply.asInstanceOf[T]
//       }
//    } catch {
//      case ex:Exception => {
//        if (errorFlow != null){
//          errorFlow.sendAndReceive[T](ex)
//        }
//        else {
//          throw new RuntimeException("boo")
//        }
//      }
//    }
//
//    reply.asInstanceOf[T]
	  null.asInstanceOf[T]
  }

//  def copy(): T// = {
//    if (this.parentComposition != null){
//      new EIPConfigurationComposition(this.parentComposition.copy(), this.target)
//    }
//    else {
//      new EIPConfigurationComposition(null, this.target)
//    }
//  }

//  private[dsl] def normalizeComposition(): EIPConfigurationComposition = {
//    this match {
//      case cmp:CompletableEIPConfigurationComposition =>   {
//        cmp
//      }
//      case _ => {
//        if (logger.isDebugEnabled){
//          logger.debug("Normalizing message flow composition: " + this)
//        }
//        val newComposition = this.copy()
//
//        val startingComposition = newComposition.getStartingComposition()
//        val field = classOf[EIPConfigurationComposition].getDeclaredField("parentComposition")
//        field.setAccessible(true)
//        field.set(startingComposition, Channel("$ch_" + UUID.randomUUID().toString.substring(0,8)))
//
//        new EIPConfigurationComposition(newComposition.parentComposition, newComposition.target)
//      }
//    }
//  }

  /**
   *
   */
//  private def getContext():EIPContext = {
//
//    threadLocal.get() match {
//      case eipContext:EIPContext => {
//        if (logger.isDebugEnabled){
//          logger.debug("Retrieving existing EIP context")
//        }
//        eipContext
//      }
//      case _ => {
//        if (logger.isDebugEnabled){
//          logger.debug("Creating new EIP context")
//        }
////        val normalizedComposition = normalizeComposition()
////        normalizedComposition.target match {
////          case poller:Poller => {
////            throw new IllegalStateException("The resulting message flow configuration ends with Poller which " +
////              "has no consumer Consumer: " + poller)
////          }
////          case ch:Channel => {
////            if (ch.capacity == Integer.MIN_VALUE){
////              throw new IllegalStateException("The resulting message flow configuration ends with " +
////                "Direct or PubSub Channel but no subscribers are configured: " + ch)
////            }
////          }
////          case _ =>
////        }
//        
////        val eipContext = EIPContext(
////          new SimpleCompletableComposition(normalizedComposition.parentComposition, normalizedComposition.target)
////              with CompletableEIPConfigurationComposition
////        )
////        threadLocal.set(eipContext)
////        eipContext
//        null
//      }
//    }
//  }

  private[dsl] def getStartingComposition():EIPConfigurationComposition = {
    if (this.parentComposition != null){
      this.parentComposition.getStartingComposition
    }
    else {
      this
    }
  }

  
  private[dsl] def merge(fromComposition:EIPConfigurationComposition, toComposition:EIPConfigurationComposition) = {
    val startingComposition = toComposition.getStartingComposition
    val field = classOf[EIPConfigurationComposition].getDeclaredField("parentComposition")
    field.setAccessible(true)
    field.set(startingComposition, this)
  }
  
  private[dsl] def toListOfTargets():List[Any] = {

    def toSet(comp:EIPConfigurationComposition): Set[Any] = {
      if (comp.parentComposition != null){
        var l = toSet(comp.parentComposition)
        l += comp.target
        l
      }
      else {
        Set(comp.target)
      }
    }

    toSet(this).toList
  }
}

///**
// *
// */
//private[dsl] trait CompletableEIPConfigurationComposition

private[dsl] case class ListComposition(override val parentComposition:EIPConfigurationComposition, override val target:Any)
  extends EIPConfigurationComposition(parentComposition, target) {

}
/**
 *
 */
private[dsl] case class SimpleComposition(override val parentComposition:EIPConfigurationComposition, override val target:Any)
  extends EIPConfigurationComposition(parentComposition, target) {
  
//  def -->(composition: EIPConfigurationComposition[_]): T forSome {type T <: composition.type} = {
//  def -->(composition: EIPConfigurationComposition):T forSome {type T <: composition.type} = {
  def -->(composition: EIPConfigurationComposition):EIPConfigurationComposition with Composable = {
    //composition.add(this)
    null
  }
//    T = composition.type
    
//    def using[T <: {def close(): Unit}](obj: T) = {
//      
//    }
//    composition.add[T](this)
//    null
//  }

  def add(composition: EIPConfigurationComposition):SimpleComposition = {
//    println("adding SimpleComposition: " + this);
//	if (this.parentComposition != null){
//		val copyComposition = this.copy()
//        composition.merge(composition, copyComposition)
//		val v = new SimpleComposition(copyComposition.parentComposition, copyComposition.target) 
//		println(v);
//		v
//	}
//    else {
//    	val v = new SimpleComposition(composition, this.target)
//    	println(v);
//		v
//    }
    null
  }

  override def copy(): SimpleComposition = {
    if (this.parentComposition != null){
      new SimpleComposition(this.parentComposition.copy(), this.target)
    }
    else {
      new SimpleComposition(null, this.target)
    }
  }
}

private[dsl] case class ChannelComposition(override val parentComposition:EIPConfigurationComposition, override val target:Any)
  extends SimpleComposition(parentComposition, target) {
  
  def --<(compositions: (EIPConfigurationComposition)*) = new ListComposition(this, compositions) 
  
  override def add(composition: EIPConfigurationComposition):ChannelComposition = {
    println("adding ChannelComposition: " + this);
	if (this.parentComposition != null){
		val copyComposition = this.copy()
        composition.merge(composition, copyComposition)
		val v = new ChannelComposition(copyComposition.parentComposition, copyComposition.target)
		println(v);
		v
	}
    else {
    	val v = new ChannelComposition(composition, this.target)
    	println(v);
		v
    }
  }

 override  def copy(): ChannelComposition = {
    if (this.parentComposition != null){
      new ChannelComposition(this.parentComposition.copy(), this.target)
    }
    else {
      new ChannelComposition(null, this.target)
    }
  }
}

/**
 *
 */
//private[dsl] case class SimpleCompletableComposition(override val parentComposition:EIPConfigurationComposition, override val target:Any)
//  extends SimpleComposition(parentComposition, target){
//
//
//}

///**
// *
// */
//private[dsl] abstract class ConditionComposition(override val parentComposition:EIPConfigurationComposition, override val target:Any)
//  extends EIPConfigurationComposition(parentComposition, target){
//
//  def then(channelComposition:SimpleComposition with ChannelComposition): ConditionComposition
//
//}
/**
 *
 */
//private[dsl] case class PayloadTypeConditionComposition(override val parentComposition:EIPConfigurationComposition, override val target:Any)
//      extends ConditionComposition(parentComposition, target)   {
//  
//  override def add(composition: EIPConfigurationComposition):ConditionComposition with Composable = {
//    println("adding ConditionComposition payload");
//    null
//  }
//
//  def then(composition: SimpleComposition with ChannelComposition) = new PayloadTypeConditionComposition(this, new PayloadTypeCondition(target.asInstanceOf[Class[_]], composition))
//  
//  override def copy(): PayloadTypeConditionComposition = {
//    if (this.parentComposition != null){
//      new PayloadTypeConditionComposition(this.parentComposition.copy(), this.target)
//    }
//    else {
//      new PayloadTypeConditionComposition(null, this.target)
//    }
//  }
//}
/**
 *
 */
//private[dsl] case class ValueConditionComposition(override val parentComposition:EIPConfigurationComposition, override val target:Any)
//      extends ConditionComposition(parentComposition, target) {
//  
//  override def add(composition: EIPConfigurationComposition):ConditionComposition with Composable = {
//    println("adding ConditionComposition value");
//    null
//  }
// 
//  def then(composition: SimpleComposition with ChannelComposition) = new ValueConditionComposition(this, new ValueCondition(target, composition))
//  
//  override def copy(): ValueConditionComposition = {
//    if (this.parentComposition != null){
//      new ValueConditionComposition(this.parentComposition.copy(), this.target)
//    }
//    else {
//      new ValueConditionComposition(null, this.target)
//    }
//  }
//
//}

/**
 *
 */
private[dsl] case class PollableComposition(override val parentComposition:EIPConfigurationComposition, override val target:Any)
  extends ChannelComposition(parentComposition, target){

  def -->(poller: Poller) = new PollableComposition(this, poller)//  {
    //override def -->(composition: EIPConfigurationComposition[_]) = new SimpleCompletableComposition(this, composition.target) //with CompletableEIPConfigurationComposition
  //}
}

trait Composable extends EIPConfigurationComposition {
  def -->(composition: EIPConfigurationComposition):EIPConfigurationComposition with Composable = {
//    composition.add(this)
    null
  }  
}

trait ChannelComposable extends Composable {
  def --<(compositions: EIPConfigurationComposition*):EIPConfigurationComposition = {
    val listComposition = new ListComposition(this, compositions)
    listComposition
  }
}