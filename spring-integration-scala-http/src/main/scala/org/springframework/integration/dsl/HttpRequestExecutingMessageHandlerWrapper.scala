package org.springframework.integration.dsl
import org.springframework.http.HttpMethod
import org.springframework.integration.handler.MethodInvokingMessageProcessor
import org.springframework.integration.http.outbound.HttpRequestExecutingMessageHandler
import org.springframework.integration.Message

private[dsl] class HttpRequestExecutingMessageHandlerWrapper(val targetObjectName:String, 
                                                             val targetMethod:String, 
                                                             val httpMethod:HttpMethod = HttpMethod.POST,
                                                             val expectedResponseType:Class[_]) extends HttpRequestExecutingMessageHandler("uri") {
  
  override def handleRequestMessage(requestMessage:Message[_]):Object = {
    
    //val handler = new HttpRequestExecutingMessageHandler()
    
    val target = this.getBeanFactory.getBean(targetObjectName)
    val processor = new MethodInvokingMessageProcessor[Object](target, targetMethod)
    val result = processor.processMessage(requestMessage)
    
    val field = classOf[HttpRequestExecutingMessageHandler].getDeclaredField("uri")
    field.setAccessible(true)
    field.set(this, result)
    this.setHttpMethod(httpMethod)
    if (this.expectedResponseType != null){
      this.setExpectedResponseType(this.expectedResponseType)
    }
    println
    val response = super.handleRequestMessage(requestMessage)
    response
    //null
  }
}