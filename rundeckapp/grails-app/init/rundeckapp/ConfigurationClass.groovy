package rundeckapp

import com.dtolabs.rundeck.server.filters.AuthFilter
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
public class ConfigurationClass {

	@Bean
	public FilterRegistrationBean authFilterRegistration() {
		println '---------------------inside the filter registration---------------------'

		FilterRegistrationBean registration = new FilterRegistrationBean()
		registration.setFilter(new AuthFilter())
		registration.addUrlPatterns("/*")
		registration.setName("AuthFilter")
		registration.setOrder(1)
		return registration
	}

}
