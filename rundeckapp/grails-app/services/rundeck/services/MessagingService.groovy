package rundeck.services

import rundeck.Messaging

class MessagingService {

    List getMessagesByNamespaceAndTopic(String namespace, String topic, String deleted = 'n'){
        def printableResult = []
        if(deleted && (deleted == 'true' || deleted == 'y' || deleted == 'yes')){
            Messaging.findAllByTopicAndNamespace(topic, namespace).each {
                printableResult << it.getMap()
            }
        }else{
            Messaging.findAllByTopicAndNamespaceAndStateNotEqual(topic, namespace,MessagingStatus.DELETED.value).each {
                printableResult << it.getMap()
            }
        }
        printableResult
    }

    Messaging getMessageByIdAndTopicAndNamespace(String id,String topic, String namespace){
        if(id) {
            Messaging.findByIdAndTopicAndNamespace(id.toLong(), topic, namespace)
        }
    }

    Messaging sendMessage(String namespace, String topic, String sender, String type, Map body){
        Date now = new Date()
        Messaging msg = Messaging.create()
        msg.topic = topic
        msg.namespace = namespace
        msg.sender = sender
        msg.state = MessagingStatus.SENT.value
        msg.type = type
        msg.setMessage(body)
        msg.setCreated(now)
        msg.setUpdated(now)
        msg.save()
        msg
    }

    Messaging sendMessageByJson(String topic, String namespace, String sender, String state, String type, String body){
        Date now = new Date()
        Messaging msg = Messaging.create()
        msg.topic = topic
        msg.namespace = namespace
        msg.sender = sender
        msg.state = state
        msg.type = type
        msg.setBody(body)
        msg.setCreated(now)
        msg.setUpdated(now)
        msg.save()
        msg
    }

    Messaging acceptMessage(long id, String topic, String namespace){
        Messaging msg = getMessageByIdAndTopicAndNamespace(id,topic,namespace)
        Date now = new Date()
        if(msg){
            msg.state = MessagingStatus.RECEIVED.value
            msg.setUpdated(now)
            msg.save()
        }
        msg
    }

    Messaging deleteMessage(long id, String topic, String namespace){
        Messaging msg = getMessageByIdAndTopicAndNamespace(id,topic,namespace)
        Date now = new Date()
        if(msg){
            msg.state = MessagingStatus.DELETED.value
            msg.setUpdated(now)
            msg.save()
        }
        msg
    }

    Messaging rejectMessage(long id, String topic, String namespace){
        Messaging msg = getMessageByIdAndTopicAndNamespace(id,topic,namespace)
        Date now = new Date()
        if(msg){
            msg.state = MessagingStatus.REJECTED.value
            msg.setUpdated(now)
            msg.save()
        }
        msg
    }

    Messaging updateMessage(long id, String topic, String namespace, String sender, String type, Map body, String state){
        Date now = new Date()
        Messaging msg = getMessageByIdAndTopicAndNamespace(id,topic,namespace)
        if(msg){
            msg.state = state
            msg.setMessage(body)
            msg.setUpdated(now)
            msg.save()
        }
        msg
    }




    enum MessagingStatus{
        DELETED("deleted"),
        SENT("sent"),
        RECEIVED("received"),
        REJECTED("rejected")


        String value

        MessagingStatus(String value){
            this.value = value
        }
    }
}
