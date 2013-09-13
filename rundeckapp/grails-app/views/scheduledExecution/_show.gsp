<%@ page import="com.dtolabs.rundeck.server.authorization.AuthConstants; rundeck.Execution" %>
<g:javascript>
/** START history
         *
         */
        function loadHistory(){
            new Ajax.Updater('histcontent',"${createLink(controller: 'reports', action: 'eventsFragment')}",{
                parameters:{compact:true,nofilters:true,jobIdFilter:'${scheduledExecution.id}'},
                evalScripts:true,
                onComplete: function(transport) {
                    if (transport.request.success()) {
                        Element.show('histcontent');
                    }
                }
            });
        }

    function init(){
        <g:if test="${!(grailsApplication.config.rundeck?.gui?.enableJobHoverInfo in ['false', false])}">
        $$('.obs_bubblepopup').each(function(e) {
            new BubbleController(e,null,{offx:-14,offy:null}).startObserving();
        });
        </g:if>
    }
    Event.observe(window,'load',init);

</g:javascript>

<div class="row">
<div class="col-sm-12">
    <div class="jobHead">
        <g:render template="/scheduledExecution/showHead" model="[scheduledExecution:scheduledExecution,followparams:[mode:followmode,lastlines:params.lastlines]]"/>
    </div>
</div>
</div>
<g:if test="${auth.jobAllowedTest(job: scheduledExecution, action: AuthConstants.ACTION_RUN)}">
    <div class="row">
        <div class="col-sm-12">
            <ul class="nav nav-tabs">
                <li class="active"><a href="#runjob" data-toggle="tab">Run
                    <i class="glyphicon glyphicon-play"></i>
                </a></li>
                <li><a href="#schedExDetails${scheduledExecution?.id}" data-toggle="tab">Definition</a></li>
            </ul>
            <div class="tab-content">
                <div class="tab-pane active" id="runjob">
                    <tmpl:execOptionsForm
                            model="${[scheduledExecution: scheduledExecution, crontab: crontab, authorized: authorized]}"
                            hideHead="${true}"
                            hideCancel="${true}"
                            defaultFollow="${true}"/>
                </div>
                <div id="schedExDetails${scheduledExecution?.id}" class="tab-pane panel panel-default panel-tab-content">
                    <div class="panel-body">
                    <g:render template="showDetail"
                              model="[scheduledExecution: scheduledExecution, showEdit: true, hideOptions: true]"/>

                    </div>
                </div>
            </div>
        </div>
    </div>
</g:if>


<div class="row">
<div class="col-sm-12">
    <h4 class="text-muted"><g:message code="page.section.Activity"/></h4>
    <g:render template="/scheduledExecution/renderJobStats" model="${[scheduledExecution: scheduledExecution]}"/>

    <g:render template="/reports/historyTableContainer" model="[nowrunning:true]"/>
    <g:javascript>
        fireWhenReady('histcontent', loadHistory);
    </g:javascript>
</div>
</div>

<!--[if (gt IE 8)|!(IE)]><!--> <g:javascript library="ace/ace"/><!--<![endif]-->
<g:javascript>
    fireWhenReady('schedExecPage', function (z) {
        $$('.apply_ace').each(function (t) {
            _applyAce(t,'400px');
        })
    });
</g:javascript>
