package rundeck.interceptors


import grails.test.mixin.TestFor
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.web.ControllerUnitTestMixin} for usage instructions
 */
@TestFor(APIRequestInterceptor)
class APIRequestInterceptorSpec extends Specification {

    def setup() {
    }

    def cleanup() {

    }

    void "Test APIRequest interceptor matching"() {
        when:"A request matches the interceptor"
            withRequest(controller:"APIRequest")

        then:"The interceptor does match"
            interceptor.doesMatch()
    }
}
