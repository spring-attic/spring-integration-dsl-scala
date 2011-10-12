/**
 *
 */
package eip
import scala.collection.mutable.ListBuffer
/**
 * @author Oleg Zhurakousky
 *
 */
class CompositionMetadataAssembler extends Function1[ListBuffer[Any], Unit]{  
   def apply(buffer: ListBuffer[Any]) = {
	  
	   val component = buffer.last.asInstanceOf[ComposableEipComponent]
	   val configBuffer = new ListBuffer[Any]
       for(cmp <- buffer){
         cmp match {
           case composableChannel: ComposableChannel => {
             configBuffer += composableChannel.configMap
           }
           case composableEndpoint: ComposableEipComponent => {
              configBuffer += composableEndpoint.configMap
           }
         }
       }
	   buffer.clear()
	   buffer.append(configBuffer)
	   println(buffer)
   }
}  