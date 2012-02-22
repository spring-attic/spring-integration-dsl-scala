package org.springframework.integration.dsl.utils
import org.springframework.integration.handler.ServiceActivatingHandler
import org.springframework.integration.Message
import org.springframework.integration.handler.AbstractReplyProducingMessageHandler
import org.springframework.integration.http.outbound.HttpRequestExecutingMessageHandler
import org.springframework.integration.handler.MethodInvokingMessageProcessor
import org.springframework.http.HttpMethod
import org.springframework.integration.support.MessageBuilder

private[dsl] class HttpRequestExecutingMessageHandlerWrapper(val targetObject:Any, 
                                                             val targetMethod:String, 
                                                             val httpMethod:HttpMethod = HttpMethod.POST,
                                                             val expectedResponseType:Class[_])
			 extends HttpRequestExecutingMessageHandler("URI") {

  
  
  override def handleRequestMessage(requestMessage:Message[_]):Object = {
    
    val processor = new MethodInvokingMessageProcessor[Object](targetObject, targetMethod)
    val result = processor.processMessage(requestMessage)
    
    val field = classOf[HttpRequestExecutingMessageHandler].getDeclaredField("uri")
    field.setAccessible(true)
    field.set(this, result)
    this.setHttpMethod(httpMethod)
    if (this.expectedResponseType != null){
      this.setExpectedResponseType(this.expectedResponseType)
    }
    
    val response = super.handleRequestMessage(requestMessage)
    response
  }
}