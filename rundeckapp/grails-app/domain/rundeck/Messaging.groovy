package rundeck

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.ObjectMapper

class Messaging {
    String namespace
    String topic
    String type
    String sender
    String state

    String body
    Date created
    Date updated

    static constraints = {
        topic(nullable: false)
        type(nullable: false)
        sender(nullable: false)
        namespace(nullable: true)
        body(nullable: true)
        created(nullable: false)
        updated(nullable: false)
    }


    static transients = ['message']

    public Map getMessage() {
        if (null != body) {
            final ObjectMapper objMapper = new ObjectMapper()
            try{
                return objMapper.readValue(body, Map.class)
            }catch (JsonParseException e){
                return null
            }
        } else {
            return null
        }

    }

    public void setMessage(Map obj) {
        if (null != obj) {
            final ObjectMapper objMapper = new ObjectMapper()
            body = objMapper.writeValueAsString(obj)
        } else {
            body = null
        }
    }

    public Map getMap(){
        def msgMap = [:]
        msgMap.id = this.id
        msgMap.created = this.created
        msgMap.updated = this.updated
        msgMap.topic = this.topic
        msgMap.type = this.type
        msgMap.sender = this.sender
        msgMap.state = this.state
        msgMap.namespace = this.namespace
        msgMap.body = this.message
        msgMap
    }

    @Override
    String toString() {
        return "id: ${this.id}; " +
                "namespace: ${this.namespace}; " +
                "topic: ${this.topic} " +
                "type: ${this.type} " +
                "sender: ${this.sender} " +
                "state: ${this.state} " +
                "created: ${this.created} " +
                "updated: ${this.updated} " +
                "body: ${this.body}"
    }
}
