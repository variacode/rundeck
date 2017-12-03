package rundeckapp


import grails.test.mixin.TestFor
import rundeck.interceptors.FormTokenInterceptor
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.web.ControllerUnitTestMixin} for usage instructions
 */
@TestFor(FormTokenInterceptor)
class FormTokenInterceptorSpec extends Specification {

    def setup() {
    }

    def cleanup() {

    }

    void "Test formToken interceptor matching"() {
        when:"A request matches the interceptor"
            withRequest(controller:"formToken")

        then:"The interceptor does match"
            interceptor.doesMatch()
    }
}
