package rundeck.controllers

import com.dtolabs.client.utils.Constants
import com.dtolabs.rundeck.app.support.ScheduledExecutionQuery
import com.dtolabs.rundeck.core.authorization.AuthContext
import com.dtolabs.rundeck.core.authorization.UserAndRolesAuthContext
import com.dtolabs.rundeck.server.authorization.AuthConstants
import grails.converters.JSON
import grails.converters.XML
import groovy.xml.MarkupBuilder
import rundeck.Messaging
import rundeck.ScheduledExecution
import rundeck.User
import rundeck.codecs.JobsXMLCodec
import rundeck.codecs.JobsYAMLCodec
import rundeck.services.ApiService
import rundeck.services.FrameworkService
import rundeck.services.HeartbeatService
import rundeck.services.MessagingService
import rundeck.services.ScheduledExecutionService
import rundeck.services.UserService

import javax.servlet.http.HttpServletResponse

class HeartbeatController {

    def MessagingService messagingService
    def FrameworkService frameworkService
    def ApiService apiService
    def UserService userService
    def ScheduledExecutionService scheduledExecutionService
    def HeartbeatService heartbeatService

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

    def clearJobsFilter = { ScheduledExecutionQuery query ->
        return redirect(action: 'jobs', params: [project: params.project])
    }

    def jobs (ScheduledExecutionQuery query ){

        def User u = userService.findOrCreateUser(session.user)
        if(params.size()<1 && !params.filterName && u ){
            Map filterpref = userService.parseKeyValuePref(u.filterPref)
            if(filterpref['workflows']){
                params.filterName=filterpref['workflows']
            }
        }
        if(!params.project){
            return redirect(controller: 'menu',action: 'home')
        }

        UserAndRolesAuthContext authContext
        authContext = frameworkService.getAuthContextForSubjectAndProject(session.subject, params.project)
        query.setProjFilter(params.project)
        query.setScheduledFilter(new Boolean(true))

        def results=listWorkflows(query,authContext,session.user)
        results.execQueryParams=query.asExecQueryParams()
        results.reportQueryParams=query.asReportQueryParams()
        if(results.warning){
            request.warn=results.warning
        }

        params.nodelist = heartbeatService.getNodes()

        withFormat{
            html {
                results
            }
        }
    }

    private def listWorkflows(ScheduledExecutionQuery query,AuthContext authContext,String user) {
        long start=System.currentTimeMillis()
        if(null!=query){
            query.configureFilter()
        }
        def qres = scheduledExecutionService.listWorkflows(query)
        log.debug("service.listWorkflows: "+(System.currentTimeMillis()-start));
        long rest=System.currentTimeMillis()
        def schedlist=qres.schedlist
        def total=qres.total
        def filters=qres._filters

        def finishq=scheduledExecutionService.finishquery(query,params,qres)

        def allScheduled = schedlist.findAll { it.scheduled }
        def nextExecutions=scheduledExecutionService.nextExecutionTimes(allScheduled)
        def clusterMap=scheduledExecutionService.clusterScheduledJobs(allScheduled)
        log.debug("listWorkflows(nextSched): "+(System.currentTimeMillis()-rest));
        long preeval=System.currentTimeMillis()

        //collect all jobs and authorize the user for the set of available Job actions
        def jobnames=[:]
        Set res = new HashSet()
        schedlist.each{ ScheduledExecution sched->
            if(!jobnames[sched.generateFullName()]){
                jobnames[sched.generateFullName()]=[]
            }
            jobnames[sched.generateFullName()]<<sched.id.toString()
            res.add(frameworkService.authResourceForJob(sched))
        }
        // Filter the groups by what the user is authorized to see.

        def decisions = frameworkService.authorizeProjectResources(authContext,res, new HashSet([AuthConstants.ACTION_READ, AuthConstants.ACTION_DELETE, AuthConstants.ACTION_RUN, AuthConstants.ACTION_UPDATE, AuthConstants.ACTION_KILL]),query.projFilter)
        log.debug("listWorkflows(evaluate): "+(System.currentTimeMillis()-preeval));

        long viewable=System.currentTimeMillis()

        def authCreate = frameworkService.authorizeProjectResource(authContext,
                AuthConstants.RESOURCE_TYPE_JOB,
                AuthConstants.ACTION_CREATE, query.projFilter)


        def Map jobauthorizations=[:]

        //produce map: [actionName:[id1,id2,...],actionName2:[...]] for all allowed actions for jobs
        decisions.findAll { it.authorized}.groupBy { it.action }.each{k,v->
            jobauthorizations[k] = new HashSet(v.collect {
                jobnames[ScheduledExecution.generateFullName(it.resource.group,it.resource.name)]
            }.flatten())
        }

        jobauthorizations[AuthConstants.ACTION_CREATE]=authCreate
        def authorizemap=[:]
        def pviewmap=[:]
        def newschedlist=[]
        def unauthcount=0
        def readauthcount=0

        /*
         'group/name' -> [ jobs...]
         */
        def jobgroups=[:]
        schedlist.each{ ScheduledExecution se->
            authorizemap[se.id.toString()]=jobauthorizations[AuthConstants.ACTION_READ]?.contains(se.id.toString())
            if(authorizemap[se.id.toString()]){
                newschedlist<<se
                if(!jobgroups[se.groupPath?:'']){
                    jobgroups[se.groupPath?:'']=[se]
                }else{
                    jobgroups[se.groupPath?:'']<<se
                }
            }
            if(!authorizemap[se.id.toString()]){
                log.debug("Unauthorized job: ${se}")
                unauthcount++
            }

        }
        readauthcount= newschedlist.size()

        if(grailsApplication.config.rundeck?.gui?.realJobTree != "false") {
            //Adding group entries for empty hierachies to have a "real" tree
            def missinggroups = [:]
            jobgroups.each { k, v ->
                def splittedgroups = k.split('/')
                splittedgroups.eachWithIndex { item, idx ->
                    def thepath = splittedgroups[0..idx].join('/')
                    if(!jobgroups.containsKey(thepath)) {
                        missinggroups[thepath]=[]
                    }
                }
            }
            //sorting is done in the view
            jobgroups.putAll(missinggroups)
        }

        schedlist=newschedlist
        log.debug("listWorkflows(viewable): "+(System.currentTimeMillis()-viewable));
        long last=System.currentTimeMillis()

        log.debug("listWorkflows(last): "+(System.currentTimeMillis()-last));
        log.debug("listWorkflows(total): "+(System.currentTimeMillis()-start));

        return [
                nextScheduled:schedlist,
                nextExecutions: nextExecutions,
                clusterMap: clusterMap,
                jobauthorizations:jobauthorizations,
                authMap:authorizemap,
                jobgroups:jobgroups,
                paginateParams:finishq.paginateParams,
                displayParams:finishq.displayParams,
                total: total,
                max: finishq.max,
                offset:finishq.offset,
                unauthorizedcount:unauthcount,
                totalauthorized: readauthcount,
        ]
    }

    def admin () {
        println(params.idlist)
        println(params.serverNode)
        def success = []
        success[0] = [message: scheduledExecutionService.lookupMessage('api.project.updateResources.succeeded', [params.project] as Object[])]
        flash.bulkJobResult = [success: success]
        log.debug("ScheduledExecutionController: flipScheduleEnabledBulk : params: " + params)
        println(flash.bulkJobResult.success.message)
        redirect(action: 'jobs', params: [project: params.project])
    }

}
