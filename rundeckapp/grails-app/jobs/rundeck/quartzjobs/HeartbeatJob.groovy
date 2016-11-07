package rundeck.quartzjobs


import org.quartz.JobExecutionContext
import rundeck.Messaging
import rundeck.ScheduledExecution
import rundeck.services.FrameworkService
import rundeck.services.HeartbeatService
import rundeck.services.MessagingService
import rundeck.services.ScheduledExecutionService


class HeartbeatJob {
    def HeartbeatService heartbeatService
    def FrameworkService frameworkService
    def ScheduledExecutionService scheduledExecutionService


    def grailsApplication
    static triggers = {
        simple repeatInterval: HeartbeatService.REPEAT_INTERVAL_MS
    }
    void execute(JobExecutionContext context) {
        //TODO commment for cluster
        if(frameworkService.isClusterModeEnabled()){
            //I'm alive heartbeat
            def msg = heartbeatService.generateNodeMessage(frameworkService.serverUUID, frameworkService.frameworkNodeName)
            def msg1 = heartbeatService.generateNodeMessage('27f48776-8168-4ab0-8415-fc0b665ddd88', 'wintermute')
            def msg2 = heartbeatService.generateNodeMessage('27f48776-8168-4ab0-8415-fc0b665ddd887', 'neuromancer')
            println(msg.toString())
            //check job with !scheduleOwnerClaimed
            /*List<Messaging> msgs = messagingService.getJobMessages(frameworkService.serverUUID)
            msgs.each {it->
                ScheduledExecution se = ScheduledExecution.findById(it.scheduledExecutionId)
                if(!se.scheduleOwnerClaimed){
                    def oldSched = scheduledExecution.scheduled
                    def oldJobName = scheduledExecution.generateJobScheduledName()
                    def oldJobGroup = scheduledExecution.generateJobGroupName()
                    scheduledExecutionService.rescheduleJob(se,oldSched,oldJobName,oldJobGroup)
                    se.scheduleOwnerClaimed = true
                    se.save()
                }
            }*/

        }
    }
}
