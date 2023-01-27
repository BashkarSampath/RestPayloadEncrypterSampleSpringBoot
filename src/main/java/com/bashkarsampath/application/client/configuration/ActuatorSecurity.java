package com.bashkarsampath.application.client.configuration;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@EnableWebSecurity
public class ActuatorSecurity extends WebSecurityConfigurerAdapter {

	@NotNull
	@NotBlank
	@Value(value = "${spring.security.user.name}")
	private String username;

	@NotNull
	@NotBlank
	@Value(value = "${spring.security.user.password}")
	private String password;

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.csrf().disable();
		http.requestMatcher(EndpointRequest.toAnyEndpoint()).authorizeRequests()
				.mvcMatchers("/health", "/health.html", "/v1/**", "/api/**", "/v2/api-docs", "/swagger-ui/**")
				.permitAll().mvcMatchers("/actuator/**").hasRole("ADMIN").anyRequest().hasRole("ADMIN").and()
				.httpBasic();
	}

	@Autowired
	public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
		auth.inMemoryAuthentication().withUser(username).password("{noop}" + password).roles("ADMIN");
	}
}