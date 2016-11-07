package rundeck.services

import rundeck.Messaging

class HeartbeatService {

    def MessagingService messagingService

    public static final long MS = 1000
    public static final long REPEAT_INTERVAL_SEC = 30
    public static final long REPEAT_INTERVAL_MS = REPEAT_INTERVAL_SEC*MS

    def NAMESPACE = 'rundeck-system'
    def TOPIC = 'rundeck.cluster.heartbeat'
    def TYPE = 'sysinfo'
    def MAX_INACTIVE_TIME_SEC = 5*REPEAT_INTERVAL_SEC





    Messaging generateNodeMessage(String uuid, String nodeName){

        //get the messages of this topic/namespace and sender (nodeuuid)
        Messaging msg = Messaging.findByNamespaceAndTopicAndSender(NAMESPACE, TOPIC, uuid)

        def body = [:]
        body.uptime = 0
        body.nodeName = nodeName

        //if not exist, create one
        if(!msg){
            log.info("Node server ${nodeName} is active for first time.")
            return messagingService.sendMessage(NAMESPACE, TOPIC,uuid,TYPE,body)
        }else{
            //if exist, verify last status,
            Date now = new Date()

            def secondsSinceLastStatus = (now.getTime() - msg.updated.getTime())/MS
            def uptimeInSeconds = (now.getTime() - msg.created.getTime())/MS

            if(secondsSinceLastStatus > (MAX_INACTIVE_TIME_SEC)){
                //more than 10 heartbeats? was offline, mark as new one
                msg.state = MessagingService.MessagingStatus.SENT.value
                msg.setMessage(body)
                msg.setUpdated(now)
                msg.setCreated(now)
                msg.save()
                //log server were dead
                log.info("Node server ${nodeName} was offline more than ${MAX_INACTIVE_TIME_SEC} seconds.")
            }else{
                //less? update time
                body.uptime = uptimeInSeconds
                msg.setUpdated(now)
                msg.setMessage(body)
                msg.save()
            }

            return msg
        }

    }

    List<Map<String,String>> getNodes(){
        def activeNodes = []
        def nodes = messagingService.getMessagesByNamespaceAndTopic(NAMESPACE, TOPIC)
        Date now = new Date()
        nodes.each {
            def secondsSinceLastStatus = (now.getTime() - it.updated.getTime())/MS
            if(secondsSinceLastStatus < MAX_INACTIVE_TIME_SEC){
                activeNodes.add(it)
            }
        }
        return activeNodes


    }


}
