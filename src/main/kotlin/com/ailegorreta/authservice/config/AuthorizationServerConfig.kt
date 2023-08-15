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
 *  AuthorizationServerConfig.kt
 *
 *  Developed 2023 by LegoSoftSoluciones, S.C. www.legosoft.com.mx
 */
package com.ailegorreta.authservice.config

import com.ailegorreta.authservice.jose.Jwks
import com.nimbusds.jose.jwk.JWKSelector
import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jose.jwk.source.JWKSource
import com.nimbusds.jose.proc.SecurityContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.oauth2.core.AuthorizationGrantType
import org.springframework.security.oauth2.core.ClientAuthenticationMethod
import org.springframework.security.oauth2.core.oidc.OidcScopes
import org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames
import org.springframework.security.oauth2.server.authorization.*
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationCodeRequestAuthenticationProvider
import org.springframework.security.oauth2.server.authorization.client.JdbcRegisteredClientRepository
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint
import java.time.Duration
import java.util.*
import java.util.stream.Collectors

/**
 * Spring authorization server configuration
 *
 * @project : auth-service
 * @author rlh
 * @date August 2023
 */
@Configuration(proxyBeanMethods = false)
class AuthorizationServerConfig {

    /**
     * Include the user granted authorities in the token and other data. Enhanced JWT with more data
     */
    @Bean
    fun jwtCustomizer(): OAuth2TokenCustomizer<JwtEncodingContext> {
        return OAuth2TokenCustomizer { context: JwtEncodingContext ->
            val principal: Authentication = context.getPrincipal()

            if (context.tokenType.value == OidcParameterNames.ID_TOKEN &&
                principal is UsernamePasswordAuthenticationToken &&
                principal.getPrincipal() is EnhancedPrincipal
            ) {
                val user = principal.getPrincipal() as EnhancedPrincipal
                val authorities: Set<String> = principal.getAuthorities().stream()
                                                        .map { obj: GrantedAuthority -> obj.authority }
                                                        .collect(Collectors.toSet())
                    context.claims.claim("authorities", authorities)
                    context.claims.claim("userId", principal.name)
                    context.claims.claim("extraData", user.extraData)
                }
            }
    }

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    @Throws(Exception::class)
    fun authorizationServerSecurityFilterChain(http: HttpSecurity): SecurityFilterChain {
        OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http)

        http.getConfigurer(OAuth2AuthorizationServerConfigurer::class.java)
            .authorizationEndpoint{ e-> e.authenticationProviders {         // Use a Custom RequestValidator for the uri´s. see class CustomRequestValidator
                authenticationProviders -> authenticationProviders.forEach {
                    authenticationProvider ->  if (authenticationProvider is OAuth2AuthorizationCodeRequestAuthenticationProvider) {
                            val validator = CustomRequestValidator()
                            authenticationProvider.setAuthenticationValidator(validator)
                        }
                    }
                }
            }
            .oidc(Customizer.withDefaults()) // Enable OpenID Connect 1.0


        // @formatter:off
        http
            .exceptionHandling{  exceptions ->
                exceptions.authenticationEntryPoint(LoginUrlAuthenticationEntryPoint("/login"))}
            // .oauth2ResourceServer(OAuth2ResourceServerConfigurer<*>::jwt)
            // .cors(Customizer.withDefaults())
            // .formLogin().loginPage("/custom-login").failureForwardUrl("/custom-login?error=true")
        // @formatter:on
        return http.build()

    }


    @Bean
    fun registeredClientRepository(jdbcTemplate: JdbcTemplate): RegisteredClientRepository {
        val registeredClientRepository = JdbcRegisteredClientRepository(jdbcTemplate)

        // Add if not exist all registered clients
        val iamui = build3ScopesRegisteredClient(
            registeredClientRepository,
            "iamui",
            "8190",
            UIApplications.secret("iamui")!!,
            "iam.compania",
            "iam.facultad",
            "iam.estadistica"
        )
        val udfui = build1ScopesRegisteredClient(
            registeredClientRepository,
            "udfui",
            "8210",
            UIApplications.secret("udfui")!!,
            "iam.facultad"
        )
        val carteraui = build3ScopesRegisteredClient(
            registeredClientRepository,
            "carteraui",
            "8510",
            UIApplications.secret("carteraui")!!,
            "cartera.read",
            "iam.compania",
            "iam.facultad",
        )
        val sysui = build1ScopesRegisteredClient(
            registeredClientRepository,
            "sysui",
            "8360",
            UIApplications.secret("sysui")!!,
            "sys.facultad"
        )
        val sysuimob = build1ScopesRegisteredMicroService(
            registeredClientRepository,
            "sysuimob",
            UIApplications.secret("sysuimob")!!,
            "sys.facultad"
        )
        val acmeui = build1ScopesRegisteredClient(
            registeredClientRepository,
            "acmeui",
            "8530",
            UIApplications.secret("acmeui")!!,
            "acme.facultad"
        )
        val preference = build1ScopesRegisteredMicroService(
            registeredClientRepository,
            "preference",
            UIApplications.secret("preference")!!,
            "cartera.read"
        )
        val audit = build5ScopesRegisteredMicroService(
            registeredClientRepository,
            "audit",
            UIApplications.secret("audit")!!,
            "sys.facultad",
            "acme.facultad",
            "iam.facultad",
            "iam.compania",
            "cartera.read"
        )
        val udf = build1ScopesRegisteredMicroService(
            registeredClientRepository,
            "udf",
            UIApplications.secret("udf")!!,
            "iam.facultad"
        )
        val cartera = build1ScopesRegisteredMicroService(
            registeredClientRepository,
            "cartera",
            UIApplications.secret("cartera")!!,
            "cartera.read"
        )
        val mail = build5ScopesRegisteredMicroService(
            registeredClientRepository,
            "mail",
            UIApplications.secret("mail")!!,
            "sys.facultad",
            "acme.facultad",
            "iam.facultad",
            "iam.compania",
            "cartera.read"
        )
        val bup = build1ScopesRegisteredMicroService(
            registeredClientRepository,
            "bup",
            UIApplications.secret("bup")!!,
            "sys.facultad"
        )
        val expediente = build1ScopesRegisteredMicroService(
            registeredClientRepository,
            "expediente",
            UIApplications.secret("expediente")!!,
            "sys.facultad"
        )
        val firstClient = buildRegisteredClient(
            registeredClientRepository,
            "messaging-client",
            "127.0.0.1",
            "8280",
            "secret",
            "message"
        )
        val secondClient = buildTokenRegisteredClient(
            registeredClientRepository,
            "messaging-clientx",
            "secret2",
            "messagex"
        )
        val iamService = build3ScopesRegisteredMicroService(
            registeredClientRepository,
            "iam-service",
            UIApplications.secret("iam-service")!!,
            "iam.compania",
            "iam.facultad",
            "iam.estadistica"
        )
        val paramService = build1ScopesRegisteredMicroService(
            registeredClientRepository,
            "param-service",
            UIApplications.secret("param-service")!!,
            "sys.facultad"
        )
        val cacheSeervice = build5ScopesRegisteredMicroService(
            registeredClientRepository,
            "cache-service",
            UIApplications.secret("cache-service")!!,
            "sys.facultad",
            "acme.facultad",
            "iam.facultad",
            "iam.compania",
            "cartera.read"
        )
        val gatewayService = build1ScopesRegisteredClient(
            registeredClientRepository,
            "gateway-service",
            "8072",
            UIApplications.secret("gateway-service")!!,
            "iam.facultad"
        )
        val testService = build1ScopesRegisteredMicroService(
            registeredClientRepository,
            "test-service",
            UIApplications.secret("test-service")!!,
            "iam.facultad"
        )
        val testServiceReactive = build1ScopesRegisteredMicroService(
            registeredClientRepository,
            "test-service-reactive",
            UIApplications.secret("test-service-reactive")!!,
            "iam.facultad"
        )
        // TODO if works with JDBC repository delete this comment. See bellow
        // return InMemoryRegisteredClientRepository(iamui, udfui, carteraui, sysui, acmeui,
        //                                           preference, udf, cartera, param, mail, bub, expediente,
        //                                           firstClient, secondClient,
        //                                           iamService, paramService, cacheService,
        //                                           gatewayService, testService, testServiceReactive)
        return registeredClientRepository
    }

    /**
     * This client register if for client name just for the demo purpose with scope read and write
     */
    //@formatter:off
    private fun buildRegisteredClient(registeredClientRepository: RegisteredClientRepository,
                                      clientId: String, ip: String, port: String, secret: String, scope: String): RegisteredClient {
        //@formatter:on
        var registeredClient = registeredClientRepository.findByClientId(clientId)

        if (registeredClient == null) {
            //@formatter:off
            registeredClient = RegisteredClient.withId(UUID.randomUUID().toString())
                .tokenSettings(TokenSettings.builder()
                    .accessTokenTimeToLive(Duration.ofHours(5))
                    .refreshTokenTimeToLive(java.time.Duration.ofMinutes(10))
                    .build())
                .clientId(clientId)
                .clientSecret("{noop}$secret")
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .redirectUri("http://$ip:$port/login/oauth2/code/$clientId-oidc")
                .redirectUri("http://$ip:$port/authorized")
                // .redirectUri("https://oauth.pstmn.io/v1/callback")
                // ^ this URI redirection is to test the token from Postman
                // see: https://medium.com/tech-takeaways/how-to-perform-oauth-2-0-authorization-with-postman-2bfbde062959
                .scope(OidcScopes.OPENID)
                .scope("$scope.read")
                .scope("$scope.write")
                .clientSettings(ClientSettings.builder().requireAuthorizationConsent(true).build())
                .build()
            //@formatter:on
            registeredClientRepository.save(registeredClient)
        }

        return registeredClient!!
    }

    /**
     * This is for client register for UI client with one scope
     */
    //@formatter:off
    private fun build1ScopesRegisteredClient(registeredClientRepository: RegisteredClientRepository,
                                             clientId: String, port: String, secret: String,
                                             scope1: String): RegisteredClient {
        //@formatter:on
        var registeredClient = registeredClientRepository.findByClientId(clientId)

        if (registeredClient == null) {
            //@formatter:off
            registeredClient = RegisteredClient.withId(UUID.randomUUID().toString())
                .tokenSettings(TokenSettings.builder()
                    .accessTokenTimeToLive(Duration.ofHours(1))
                    .refreshTokenTimeToLive(java.time.Duration.ofMinutes(10))
                    .build())
                .clientId(clientId)
                .clientSecret("{noop}$secret")
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .redirectUri("http://$clientId:$port/login/oauth2/code/$clientId-oidc")
                .redirectUri("http://$clientId:$port/authorized")
                //.redirectUri("https://oauth.pstmn.io/v1/callback")
                // ^ this URI redirection is to test the token from Postman
                // see: https://medium.com/tech-takeaways/how-to-perform-oauth-2-0-authorization-with-postman-2bfbde062959
                .scope(OidcScopes.OPENID)
                .scope(scope1)
                .clientSettings(ClientSettings.builder().requireAuthorizationConsent(true).build())
                .build()
            //@formatter:on
            registeredClientRepository.save(registeredClient)
        }

        return registeredClient!!
    }

    //@formatter:off
    private fun build2ScopesRegisteredClient(registeredClientRepository: RegisteredClientRepository,
                                             clientId: String, port: String, secret: String,
                                             scope1: String, scope2: String): RegisteredClient {
        //@formatter:on
        var registeredClient = registeredClientRepository.findByClientId(clientId)

        if (registeredClient == null) {
            //@formatter:off
            registeredClient = RegisteredClient.withId(UUID.randomUUID().toString())
                .tokenSettings(TokenSettings.builder()
                    .accessTokenTimeToLive(Duration.ofHours(1))
                    .refreshTokenTimeToLive(java.time.Duration.ofMinutes(10))
                    .build())
                .clientId(clientId)
                .clientSecret("{noop}$secret")
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .redirectUri("http://$clientId:$port/login/oauth2/code/$clientId-oidc")
                .redirectUri("http://$clientId:$port/authorized")
                //.redirectUri("https://oauth.pstmn.io/v1/callback")
                // ^ this URI redirection is to test the token from Postman
                // see: https://medium.com/tech-takeaways/how-to-perform-oauth-2-0-authorization-with-postman-2bfbde062959
                .scope(OidcScopes.OPENID)
                .scope(scope1)
                .scope(scope2)
                .clientSettings(ClientSettings.builder().requireAuthorizationConsent(true).build())
                .build()
            //@formatter:on
            registeredClientRepository.save(registeredClient)
        }

        return registeredClient!!
    }

    //@formatter:off
    private fun build3ScopesRegisteredClient(registeredClientRepository: RegisteredClientRepository,
                                          clientId: String, port: String, secret: String,
                                          scope1: String, scope2: String, scope3: String): RegisteredClient {
        //@formatter:on
        var registeredClient = registeredClientRepository.findByClientId(clientId)

        if (registeredClient == null) {
            //@formatter:off
            registeredClient = RegisteredClient.withId(UUID.randomUUID().toString())
                .tokenSettings(TokenSettings.builder()
                    .accessTokenTimeToLive(Duration.ofHours(1))
                    .refreshTokenTimeToLive(java.time.Duration.ofMinutes(10))
                    .build())
                .clientId(clientId)
                .clientSecret("{noop}$secret")
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .redirectUri("http://$clientId:$port/login/oauth2/code/$clientId-oidc")
                .redirectUri("http://$clientId:$port/authorized")
                //.redirectUri("https://oauth.pstmn.io/v1/callback")
                // ^ this URI redirection is to test the token from Postman
                // see: https://medium.com/tech-takeaways/how-to-perform-oauth-2-0-authorization-with-postman-2bfbde062959
                .scope(OidcScopes.OPENID)
                .scope(scope1)
                .scope(scope2)
                .scope(scope3)
                .clientSettings(ClientSettings.builder().requireAuthorizationConsent(true).build())
                .build()
            //@formatter:on
            registeredClientRepository.save(registeredClient)
        }

        return registeredClient!!
    }

    /**
     * This method register a resource server client with one scope + read & write
     */
    //@formatter:off
    private fun buildTokenRegisteredClient(registeredClientRepository: RegisteredClientRepository,
                                           clientId: String, secret: String, scope: String): RegisteredClient {
        //@formatter:on
        var registeredClient = registeredClientRepository.findByClientId(clientId)

        if (registeredClient == null) {
            //@formatter:off
            registeredClient = RegisteredClient.withId(UUID.randomUUID().toString())
                .tokenSettings(TokenSettings.builder()
                    .accessTokenTimeToLive(Duration.ofMinutes(5))
                    .refreshTokenTimeToLive(java.time.Duration.ofMinutes(10))
                    .build())
                .clientId(clientId)
                .clientSecret("{noop}$secret")
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .scope("$scope.read")
                .scope("$scope.write")
                .build()
            //@formatter:on
            registeredClientRepository.save(registeredClient)
        }

        return registeredClient!!
    }

    /**
     * This method register a resource server client with one scope
     */
    //@formatter:off
    private fun build1ScopesRegisteredMicroService(registeredClientRepository: RegisteredClientRepository,
                                                   clientId: String, secret: String,
                                                   scope1: String): RegisteredClient {
        //@formatter:on
        var registeredClient = registeredClientRepository.findByClientId(clientId)

        if (registeredClient == null) {
            //@formatter:off
            registeredClient = RegisteredClient.withId(UUID.randomUUID().toString())
                .tokenSettings(TokenSettings.builder()
                    .accessTokenTimeToLive(Duration.ofDays(1))
                    .refreshTokenTimeToLive(java.time.Duration.ofDays(1))
                    .build())
                .clientId(clientId)
                .clientSecret("{noop}$secret")
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .scope(scope1)
           //     .clientSettings(ClientSettings.builder().requireAuthorizationConsent(true).build())
                .build()
            //@formatter:on
            registeredClientRepository.save(registeredClient)
        }

        return registeredClient!!
    }

    /**
     * This method register a resource server client with one scope
     */
    //@formatter:off
    private fun build3ScopesRegisteredMicroService(registeredClientRepository: RegisteredClientRepository,
                                                   clientId: String, secret: String,
                                                   scope1: String, scope2: String, scope3: String): RegisteredClient {
        //@formatter:on
        var registeredClient = registeredClientRepository.findByClientId(clientId)

        if (registeredClient == null) {
            //@formatter:off
            registeredClient = RegisteredClient.withId(UUID.randomUUID().toString())
                .tokenSettings(TokenSettings.builder()
                    .accessTokenTimeToLive(Duration.ofDays(1))
                    .refreshTokenTimeToLive(java.time.Duration.ofDays(1))
                    .build())
                .clientId(clientId)
                .clientSecret("{noop}$secret")
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .scope(scope1)
                .scope(scope2)
                .scope(scope3)
                //     .clientSettings(ClientSettings.builder().requireAuthorizationConsent(true).build())
                .build()
            //@formatter:on
            registeredClientRepository.save(registeredClient)
        }

        return registeredClient!!
    }

    /**
     * This method register a resource server client with one scope
     */
    //@formatter:off
    private fun build5ScopesRegisteredMicroService(registeredClientRepository: RegisteredClientRepository,
                                                   clientId: String, secret: String,
                                                   scope1: String, scope2: String, scope3: String,
                                                   scope4: String, scope5: String): RegisteredClient {
        //@formatter:on
        var registeredClient = registeredClientRepository.findByClientId(clientId)

        if (registeredClient == null) {
            //@formatter:off
            registeredClient = RegisteredClient.withId(UUID.randomUUID().toString())
                .tokenSettings(TokenSettings.builder()
                    .accessTokenTimeToLive(Duration.ofDays(1))
                    .refreshTokenTimeToLive(java.time.Duration.ofDays(1))
                    .build())
                .clientId(clientId)
                .clientSecret("{noop}$secret")
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .scope(scope1)
                .scope(scope2)
                .scope(scope3)
                .scope(scope4)
                .scope(scope5)
                //     .clientSettings(ClientSettings.builder().requireAuthorizationConsent(true).build())
                .build()
            //@formatter:on
            registeredClientRepository.save(registeredClient)
        }

        return registeredClient!!
    }


    /*

    TODO : There is maybe a bug that when we use JdbcRegisteredClientRepository instead of memory repository
             InMemoryRegisteredClientRepository it get too many redirection error .
             see: https://huongdanjava.com/store-registeredclient-to-database-in-spring-authorization-server.html
             in discussion

     For now we use InMemoryRegisteredClientRepository but reading manually from database
    */
    @Bean
    fun authorizationService(jdbcTemplate: JdbcTemplate,
                             registeredClientRepository: RegisteredClientRepository): OAuth2AuthorizationService {
        /* Maybe for JWT Token serialization
        val authorizationService = JdbcOAuth2AuthorizationService(jdbcTemplate, registeredClientRepository)
        val rowMapper = OAuth2AuthorizationRowMapper(registeredClientRepository)
        val classLoader = JdbcOAuth2AuthorizationService::class.java.classLoader
        val objectMapper = ObjectMapper()

        objectMapper.registerModules(CoreJackson2Module())
        objectMapper.registerModules(SecurityJackson2Modules.getModules(classLoader))
        objectMapper.registerModule(OAuth2AuthorizationServerJackson2Module())
        objectMapper.addMixIn(org.springframework.data.neo4j.core.schema.ElementId::class.java, ElementIdMixin::class.java)

        objectMapper.registerModule(OAuth2AuthorizationServerJackson2Module())
        rowMapper.setObjectMapper(objectMapper)
        authorizationService.setAuthorizationRowMapper(rowMapper)

        return authorizationService
         */
        return JdbcOAuth2AuthorizationService(jdbcTemplate, registeredClientRepository)
    }

    @Bean
    fun authorizationConsentService(jdbcTemplate: JdbcTemplate,
                                    registeredClientRepository: RegisteredClientRepository): OAuth2AuthorizationConsentService {
        return JdbcOAuth2AuthorizationConsentService(jdbcTemplate, registeredClientRepository)
    }


    /**
     *  Because of the previous bug we use InMemoryOAuth2AuthorizationConsentService and InMemoryOAuth2AuthorizationService
     *
    @Bean
    fun authorizationConsentService() = InMemoryOAuth2AuthorizationConsentService()

    @Bean
    fun authorizationService() = InMemoryOAuth2AuthorizationService()
    */
    @Bean
    fun jwtDecoder(jwkSource: JWKSource<SecurityContext>) = OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource)!!

    @Bean
    fun tokenSettings() = TokenSettings.builder()
                                       .accessTokenTimeToLive(Duration.ofMinutes(30L))
                                       .build()!!

    @Bean
    fun jwkSource(): JWKSource<SecurityContext> {
        val rsaKey = Jwks.generateRsa()
        val jwkSet = JWKSet(rsaKey)

        return JWKSource { jwkSelector: JWKSelector, _ -> jwkSelector.select(jwkSet) }
    }

    @Bean
    fun authorizationServerSettings(): AuthorizationServerSettings =  AuthorizationServerSettings.builder().build()


}
