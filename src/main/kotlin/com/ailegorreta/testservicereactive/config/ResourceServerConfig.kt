package com.ailegorreta.testservicereactive.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.savedrequest.NoOpServerRequestCache

@EnableWebFluxSecurity
@EnableMethodSecurity
@Configuration(proxyBeanMethods = false)
class ResourceServerConfig {

    /**
     *  -- This code is we want for develop purpose to use all REST calls without a token --
     *  -- For example: if want to run the REST from swagger and test the micro service
     * http.authorizeHttpRequests{ auth ->  auth
     *     .requestMatchers("/ **").permitAll()
     *     .anyRequest().authenticated()
     *
     * note: erse white space between '/ **' ) just for comment
     *
     **/

    // @formatter:off
    @Bean
    @Throws(Exception::class)
    fun securityWebFilterChain( http:ServerHttpSecurity): SecurityWebFilterChain {
        http.authorizeExchange{ exchange ->  exchange
            .pathMatchers("/actuator/**").permitAll()
            //.pathMatchers("/orders/**").hasAnyAuthority("SCOPE_iam.facultad")
            .pathMatchers("/orders/**").permitAll()
            .pathMatchers("/nosecurity/**").permitAll()
            .anyExchange().authenticated()
        }
            .oauth2ResourceServer{ server -> server.jwt { Customizer.withDefaults<Any>() }}
            .requestCache{ requestCacheSpec ->
                requestCacheSpec.requestCache(NoOpServerRequestCache.getInstance())
            }
            // ^ Each request must include an Access Token, so there’s no need to keep a user session alive between
            // requests. We want it to be stateless.
            .csrf { configuration -> configuration.disable() }
        // ^ Since the authentication strategy is stateless and does not involve a browser-based client, we can safely
        // disable the CSRF protection
        return http.build()
    }
    // @formatter:on

    /**
     * Extracting roles from the Access Token
     */
    @Bean
    fun jwtAuthenticationConverter(): JwtAuthenticationConverter {
        val jwtGrantedAuthoritiesConverter = JwtGrantedAuthoritiesConverter()

        jwtGrantedAuthoritiesConverter.setAuthorityPrefix("ROLE_");
        // ^ Applies the “ROLE_” prefix to each user role
        jwtGrantedAuthoritiesConverter.setAuthoritiesClaimName("roles")
        // ^ Extracts the list of roles from the roles claim

        var jwtAuthenticationConverter = JwtAuthenticationConverter()
        // ^ Defines a converter to map claims to GrantedAuthority objects

        jwtAuthenticationConverter .setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter)

        return jwtAuthenticationConverter
    }
}