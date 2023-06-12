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
 *  CustomAuthenticationProvider.kt
 *
 *  Developed 2023 by LegoSoftSoluciones, S.C. www.legosoft.com.mx
 */
package com.ailegorreta.authservice.config

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.ailegorreta.authservice.service.LdapAuthService
import com.ailegorreta.authservice.service.UsuarioService
import com.ailegorreta.commons.utils.HasLogger import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Lazy
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Component
import java.util.*
import javax.naming.AuthenticationException
import javax.naming.CommunicationException

/**
 * This component receives first the petition for security and invokes the
 * authenticateUserAndGetInfo from the LdapAuthService class. Then reads the user
 * information and permits from Neo4j calling the method loadUserByUsername from UserDetailsService
 *
 * @author rlh
 * @project auth-service
 * @date May 2023
 */
@Component
class CustomAuthenticationProvider(@Lazy
                                   @Qualifier("lmsUserDetailsService")
                                   val userDetailsService: UsuarioService,
                                   private val ldapAuthService: LdapAuthService) : AuthenticationProvider, HasLogger {

    @Throws(AuthenticationException::class)
    override fun authenticate(authentication: Authentication): Authentication? {
        val name = authentication.name
        val password = authentication.credentials.toString()
        val authenticateResult: AuthenticateResult

        try {
            authenticateResult = ldapAuthService.authenticateUserAndGetInfo(name, password)
        } catch (e: CommunicationException) {
            logger.error("Error de conexión con LDAP ${e.message}")
            e.printStackTrace()
            throw BadCredentialsException("Error de conexión con LDAP")
        } catch (e: AuthenticationException) {
            throw UsernameNotFoundException("Usuario y/o password incorrectos $name")
        } catch (e: UsernameNotFoundException) {
            throw UsernameNotFoundException(e.message)
        } catch (e: BadCredentialsException) {
            throw e
        } catch (e: Exception) {
            e.printStackTrace()
            throw e             // does not know what happened
        }

        if (authenticateResult == AuthenticateResult.EMPLOYEE || authenticateResult == AuthenticateResult.EXTERNAL) {
            try {
                val user = userDetailsService.loadUserByUsernameAndAuthenticatedResult(name, authenticateResult)

                val res = UsernamePasswordAuthenticationToken(
                        EnhancedPrincipal(
                            name,
                            mapOf(
                                "company" to user.nombreCompania,
                                "zoneInfo" to user.zoneInfo,
                                "email" to user.email,
                                "givenName" to user.givenName,
                                "familyName" to user.familyName,
                                "administrator" to user.administrador,
                                "employee" to user.interno
                            )
                        ),
                        password, user.grantedAuthorities)

                return res
            } catch(e: Exception) {
                logger.error(e.message)
                throw BadCredentialsException(e.message)
            }
        } else
            throw UsernameNotFoundException("Usuario $name esta registrado erróneamente como empleado o externo")
    }

    override fun supports(authentication: Class<*>): Boolean {
        return authentication == UsernamePasswordAuthenticationToken::class.java
    }

}

enum class AuthenticateResult {
    EMPLOYEE, EXTERNAL, NOT_AUTHENTICATED, USER_NOT_EXISTS, ERROR
}

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
class EnhancedPrincipal(val name: String? = null, val extraData: Map<String, Any>? = null) {
    override fun toString() = name?: "null"
}
