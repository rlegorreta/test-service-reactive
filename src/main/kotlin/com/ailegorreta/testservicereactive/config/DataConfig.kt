package com.ailegorreta.testservicereactive.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.domain.ReactiveAuditorAware
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.security.core.context.SecurityContext

/**
 * This is just to enable auditing for Postgres database.
 */
@Configuration
@EnableR2dbcAuditing
class DataConfig {
    @Bean
    fun auditorAware(): ReactiveAuditorAware<String> {
        return ReactiveAuditorAware {
            ReactiveSecurityContextHolder.getContext()
                        .map { obj: SecurityContext -> obj.authentication }
                        // ^ Extracts the SecurityContext object for the currently authenticated user from SecurityContextHolder
                        .filter { obj: Authentication -> obj.isAuthenticated }
                        // ^ Handles the case where a user is not authenticated, but is manipulating data. Since we
                        // protected all the endpoints, this case should never happen, but we’ll include it for completeness
                        .map { obj: Authentication -> obj.name }
                        // ^ Extracts the username for the currently authenticated user from the Authentication object
        }
    }
}
