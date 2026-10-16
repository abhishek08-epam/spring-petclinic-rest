/*
 * Copyright 2016-2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.samples.petclinic.rest.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration to allow public access to emergency tele-consultation endpoints.
 * Implements AC-1: no authentication required.
 *
 * @author PetClinic Team
 */
@Configuration
@EnableWebSecurity
public class PublicEndpointSecurityConfig {

	/**
	 * Configure security filter chain for public endpoints.
	 * Order(1) ensures this takes precedence over other security configurations.
	 *
	 * @param http the HttpSecurity to configure
	 * @return the configured SecurityFilterChain
	 * @throws Exception if configuration fails
	 */
	@Bean
	@Order(1)
	public SecurityFilterChain publicEndpointFilterChain(HttpSecurity http) throws Exception {
		http
			.securityMatcher("/api/public/**")
			.authorizeHttpRequests(auth -> auth
				.anyRequest().permitAll()
			)
			.csrf(csrf -> csrf.disable());
		return http.build();
	}

}
