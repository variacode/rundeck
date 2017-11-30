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
		/*http
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
			*/



		http
			.authorizeRequests()
				.antMatchers("/user/login", "/user/error", "/user/logout", "/user/loggedout", "/images/**",
				"/css/**", "/js/**", "/feed/**", "/test/**", "/test/**", "/api/**", "/static/**", "/assets/**", "/font/**")
				.permitAll()
				.antMatchers("/").authenticated()
		.and()
			.formLogin()
				.loginPage("/user/login")
				.failureForwardUrl("/user/error")
				.defaultSuccessUrl("/")
				.usernameParameter("username").passwordParameter("password")
				.permitAll()

		http.csrf().disable()
		/*.and()
			.exceptionHandling().accessDeniedPage("/accessDenied")*/
		/*.and()
			.httpBasic()
*/
		/*http
			.authorizeRequests()
				.antMatchers("/user/login", "/user/error", "/user/logout", "/user/loggedout", "/images/**",
				"/css/**", "/js/**", "/feed/**", "/test/**", "/test/**", "/api/**", "/static/**", "/assets/**", "/font/**")
			.permitAll()
			.antMatchers("/").authenticated()
		.and()
			.formLogin().permitAll()
		.and()
			.logout().permitAll()
		*/
	}

	@Autowired
	public void configureGGlobal(AuthenticationManagerBuilder auth) throws Exception {
		auth
			.inMemoryAuthentication()
			.withUser("admin").password("secret").roles("admin")
	}

}
