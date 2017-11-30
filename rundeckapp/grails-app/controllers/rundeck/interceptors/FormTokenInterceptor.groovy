package rundeck.interceptors

import org.grails.web.servlet.mvc.SynchronizerTokensHolder


class FormTokenInterceptor {

    public static final String TOKEN_KEY_HEADER = 'X-RUNDECK-TOKEN-KEY'
    public static final String TOKEN_URI_HEADER = 'X-RUNDECK-TOKEN-URI'

    public FormTokenInterceptor() {
        matchAll()
    }

    boolean before() {
        println '----inside the form token interceptor--------'
        if(request.getHeader(TOKEN_KEY_HEADER) && request.getHeader(TOKEN_URI_HEADER)){
            params[SynchronizerTokensHolder.TOKEN_KEY]=request.getHeader(TOKEN_KEY_HEADER)
            params[SynchronizerTokensHolder.TOKEN_URI]=request.getHeader(TOKEN_URI_HEADER)
        }

//        params[_csrf.parameterName]=_csrf.token

        return true
    }

    boolean after() { true }

    void afterView() {
        // no-op
    }
}
