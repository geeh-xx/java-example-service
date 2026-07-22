package com.fedex.exampleservice.infrastructure.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class WebSecurityConfig {

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http.authorizeHttpRequests((auth) -> auth
				.requestMatchers(HttpMethod.GET, "/actuator/**", "/management/**").permitAll()
				.anyRequest().authenticated())
		.csrf((csrf) -> csrf.disable())
		.httpBasic(Customizer.withDefaults());
		return http.build();
	}

}
