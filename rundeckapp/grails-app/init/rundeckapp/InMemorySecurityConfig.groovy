package rundeckapp

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter

@Configuration
@EnableWebSecurity
public class InMemorySecurityConfig extends WebSecurityConfigurerAdapter {

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http
			.authorizeRequests()
				.antMatchers("/user/login",
					"/user/error",
					"/user/logout",
					"/user/loggedout",
					"/images/*",
					"/css/*",
					"/js/*",
					"/feed/*",
					"/test/*",
					"/api/*",
					"/static/**",
					"/assets/*",
					"/fonts/*",
				)
				.permitAll()
				.and()
			.antMatcher("/*")
			.authorizeRequests()
			.anyRequest().authenticated()
		.and()
			.httpBasic()
	}

	@Autowired
	public void configureGGlobal(AuthenticationManagerBuilder auth) throws Exception {
		auth
			.inMemoryAuthentication()
			.withUser("parth").password("secret").roles("user")
	}

}
