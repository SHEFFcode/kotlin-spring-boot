package com.sheffmachine.kotlinbootproject.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
class SecurityConfig {

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        return http
            .csrf { csrf -> csrf.disable() }  // Disable CSRF protection
            .authorizeHttpRequests { auth ->
                auth.requestMatchers("/api/**").permitAll()  // Allow all API requests
                    .requestMatchers("/actuator/**").permitAll()  // Allow actuator endpoints
                    .anyRequest().permitAll()  // Allow all other requests
            }
            .build()
    }
}