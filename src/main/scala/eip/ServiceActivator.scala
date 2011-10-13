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
package eip
import org.springframework.util.StringUtils

/**
 * @author Oleg Zhurakousky
 *
 */

class ServiceActivator(name: String = null, order: Int = 0) extends EipComponent(name, EipType.SERVICE_ACTIVATOR) with Using {
  
  if (order != 0) {
	  this.configMap += (EipComponent.order -> order)
  }
}

object process {

  val eipType = EipType.SERVICE_ACTIVATOR

  def apply() = doApply(null)

  def apply(name: String = null, order: Int = 0) = new ServiceActivator(name, order)

  def apply(name: String) = doApply(name)
  /**
   *
   */
  def name(name: String) = doApply(name)
  
  def using(target: AnyRef) = new EipComponent(null,eipType){ 
    this.configMap += EipComponent.eipType -> eipType
    def where(): EipComponent = {
      this.configMap += "s" -> "d"
      this
    }
  }

//  /**
//   *
//   */
//  def requireReply(requireReply: Boolean = true) = new Endpoint(null, eipType) with Using {
//    // setRequireReply
//    def order(order: Int) = new Endpoint(name, eipType) with Using {
//      setOrder(order)
//      def name(name: String) = new Endpoint(name, eipType) with Using {
//        setName(name)
//      }
//    }
//    def name(name: String) = new Endpoint(name, eipType) with Using {
//      setName(name)
//      def order(order: Int) = new Endpoint(name, eipType) with Using {
//        setOrder(order)
//      }
//    }
//  }
//  /**
//   *
//   */
//  def order(order: Int) = new Endpoint(null, eipType) with Using {
//    //setOrder(order, this)
//    def name(name: String) = new Endpoint(name, eipType) with Using {
//      this.setName(name)
//      def requireReply(requireReply: Boolean = true) = new Endpoint(name, eipType) with Using {
//        // setRequireReply
//      }
//    }
//    def requireReply(requireReply: Boolean = true) = new Endpoint(name, eipType) with Using {
//      // setRequireReply
//      def name(name: String) = new Endpoint(name, eipType) with Using {
//        setName(name)
//      }
//    }
//  }
//
  //============== PRIVATE ==============
  private def doApply(name: String) = new Endpoint(name, eipType) with Using {
    def order(order: Int) = new Endpoint(name, eipType) with Using {
      setOrder(order)
      def requireReply(requireReply: Boolean = true) = new Endpoint(name, eipType) with Using {
        // setRequireReply
        def foo() = new Endpoint(name, eipType) with Using // remove
      }
    }
    def requireReply(requireReply: Boolean = true) = new Endpoint(name, eipType) with Using {
      // setRequireReply
      def order(order: Int) = new Endpoint(name, eipType) with Using {
        setOrder(order)
      }
    }
  }
}




