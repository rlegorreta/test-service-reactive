/* Copyright (c) 2023, LegoSoft Soluciones, S.C.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are not permitted.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 *  SecurityConfig.kt
 *
 *  Developed 2023 by LegoSoftSoluciones, S.C. www.legosoft.com.mx
 */
package com.ailegorreta.authservice.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.Customizer.withDefaults
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.SecurityFilterChain

/**
 * Spring authorization server project. This server generates the Oauth token
 * The version of Oauth is 2.1.
 *
 * @project : auth-service
 * @author rlh
 * @date May 2023
 */
@EnableWebSecurity
@Configuration(proxyBeanMethods = false)
class DefaultSecurityConfig {
    private val LOGIN_MOBILE_PAGE_URI = "/loginmobile"

    @Bean
    @Throws(Exception::class)
    fun defaultSecurityFilterChain(http: HttpSecurity): SecurityFilterChain {
        /*
         * --------------------
         * Default endpoints
         * --------------------
         *
         * Authorization EndPoint           /oauth2/authorize
         * Token EndPoint                   /oauth2/token
         * Token Revocation                 /oauth2/revoke
         * Token Introspection              /oauth2/introspect
         * JWK Set EndPoint                 /oauth2/jwks
         * Authorization Server Metadata    /.well-known/oauth-authorization-server
         * OIDC Provider Configuration      /.well-known/openid-configuration
         */

        http.authorizeHttpRequests { authorize -> authorize.requestMatchers(LOGIN_MOBILE_PAGE_URI).permitAll()
                                                           .anyRequest().authenticated()
                                    }
            .formLogin(withDefaults())
            .cors(withDefaults())
            .formLogin { form ->
                            form.loginPage("/login").permitAll()
                            .loginProcessingUrl("/login")
                        }

        return http.build()
    }


}
