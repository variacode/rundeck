package rundeck.interceptors


import grails.test.mixin.TestFor
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.web.ControllerUnitTestMixin} for usage instructions
 */
@TestFor(AA_TimerInterceptor)
class AA_TimerInterceptorSpec extends Specification {

    def setup() {
    }

    def cleanup() {

    }

    void "Test AA_Timer interceptor matching"() {
        when:"A request matches the interceptor"
            withRequest(controller:"AA_Timer")

        then:"The interceptor does match"
            interceptor.doesMatch()
    }
}
