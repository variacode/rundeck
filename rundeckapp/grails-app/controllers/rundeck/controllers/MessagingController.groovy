package rundeck.controllers

import grails.converters.JSON
import grails.converters.XML
import rundeck.Messaging
import rundeck.services.FrameworkService
import rundeck.services.MessagingService
import com.dtolabs.rundeck.core.authorization.AuthContext
import rundeck.services.logging.ExecutionLogState
import rundeck.services.ApiService

import javax.servlet.http.HttpServletResponse

class MessagingController {

    MessagingService messagingService
    FrameworkService frameworkService
    ApiService apiService



    def list() {
        println('get message from topic:'+params.topic+' and namespace:'+params.namespace)
        AuthContext authContext = frameworkService.getAuthContextForSubject(session.subject)
        def ret = messagingService.getMessagesByTopicAndNamespace(params.topic, params.namespace, params.deleted)
        withFormat{
            xml{
                render ret as XML
            }
            json{
                render ret as JSON
            }
        }
    }


    def sendMessage(){
        //post method with json body
        AuthContext authContext = frameworkService.getAuthContextForSubject(session.subject)
        def body = ''
        if(request.format == 'json' && request.JSON){
            body = request.JSON
        }else{
            log.error('Message must have a JSON body')
            def errormap = [
                    status:HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE,
                    code: 'api.error.item.unsupported-format',
                    args: [response.format]]
            withFormat {
                json {
                    return apiService.renderErrorJson(response, errormap)
                }
                xml {
                    return apiService.renderErrorXml(response, errormap)
                }
            }
            return
        }
        def msg = messagingService.addMessageByMap(params.topic, params.namespace, params.sender, MessagingService.MessagingStatus.SENT.value,params.type,body)

        withFormat{
            xml{
                render msg.getMap() as XML
            }
            json{
                render msg.getMap() as JSON
            }
        }
    }

    def acceptMessage(){
        AuthContext authContext = frameworkService.getAuthContextForSubject(session.subject)
        Messaging msg = messagingService.getMessageByIdAndTopicAndNamespace(params.id, params.topic, params.namespace)
        if(msg){
            msg.state = MessagingService.MessagingStatus.RECEIVED.value
            msg.save()
        }else{
            def errormap = [status: HttpServletResponse.SC_NOT_FOUND, code: "api.error.item.doesnotexist",
                            args: ['Message ID', params.id]]
            withFormat {
                json {
                    return apiService.renderErrorJson(response, errormap)
                }
                xml {
                    return apiService.renderErrorXml(response, errormap)
                }
            }
            return
        }
        withFormat{
            xml{
                render msg.getMap() as XML
            }
            json{
                render msg.getMap() as JSON
            }
        }

    }

    def deleteMessage(){
        AuthContext authContext = frameworkService.getAuthContextForSubject(session.subject)
        Messaging msg = messagingService.getMessageByIdAndTopicAndNamespace(params.id, params.topic, params.namespace)
        if(msg){
            msg.state = MessagingService.MessagingStatus.DELETED.value
            msg.save()
        }else{
            def errormap = [status: HttpServletResponse.SC_NOT_FOUND, code: "api.error.item.doesnotexist",
                            args: ['Message ID', params.id]]
            withFormat {
                json {
                    return apiService.renderErrorJson(response, errormap)
                }
                xml {
                    return apiService.renderErrorXml(response, errormap)
                }
            }
            return
        }
        withFormat{
            xml{
                render msg.getMap() as XML
            }
            json{
                render msg.getMap() as JSON
            }
        }
    }

    def rejectMessage(){
        AuthContext authContext = frameworkService.getAuthContextForSubject(session.subject)
        Messaging msg = messagingService.getMessageByIdAndTopicAndNamespace(params.id, params.topic, params.namespace)
        if(msg){
            msg.state = MessagingService.MessagingStatus.REJECTED.value
            msg.save()
        }else{
            def errormap = [status: HttpServletResponse.SC_NOT_FOUND, code: "api.error.item.doesnotexist",
                            args: ['Message ID', params.id]]
            withFormat {
                json {
                    return apiService.renderErrorJson(response, errormap)
                }
                xml {
                    return apiService.renderErrorXml(response, errormap)
                }
            }
            return
        }
        withFormat{
            xml{
                render msg.getMap() as XML
            }
            json{
                render msg.getMap() as JSON
            }
        }
    }


}
