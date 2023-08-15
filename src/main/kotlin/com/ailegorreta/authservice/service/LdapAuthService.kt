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
*  LdapAuthService.kt
*
 *  Developed 2023 by LegoSoftSoluciones, S.C. www.legosoft.com.mx
*/

package com.ailegorreta.authservice.service

import com.ailegorreta.authservice.config.AuthenticateResult
import com.ailegorreta.authservice.config.ServiceConfig
import com.ailegorreta.commons.utils.HasLogger
import org.springframework.stereotype.Service
import java.util.*
import javax.naming.AuthenticationException
import javax.naming.CommunicationException
import javax.naming.Context
import javax.naming.directory.InitialDirContext
import javax.naming.directory.SearchControls

/**
 * Service that get Ldap information of the user
 *
 * @author lomd
 * @project : auth-service
 * @date May, 2023
 *
 */
@Service("lsLdapAuthService")
class LdapAuthService constructor(serviceConfig: ServiceConfig): HasLogger {
    private val ldapURI = serviceConfig.ldapURI
    private val adminUser = serviceConfig.ldapAdminuser
    private val credentials = serviceConfig.ldapCredentials

    @Throws(CommunicationException::class)
    fun authenticateUserAndGetInfo(username: String, password: String): AuthenticateResult {
        val environment = Properties()

        // First read from LDAP using the administrator to get the DN user
        environment[Context.INITIAL_CONTEXT_FACTORY] = "com.sun.jndi.ldap.LdapCtxFactory"
        environment[Context.PROVIDER_URL] = ldapURI
        environment[Context.SECURITY_AUTHENTICATION] = "simple"
        environment[Context.SECURITY_PRINCIPAL] = adminUser //adminuser - User with special privilege, dn user
        environment[Context.SECURITY_CREDENTIALS] = credentials // dn user password

        val adminContext = InitialDirContext(environment)       // Not in a try catch block since the administrator must exist
        val searchControls = SearchControls()

        searchControls.returningAttributes = arrayOf("cn")
        searchControls.searchScope = SearchControls.SUBTREE_SCOPE

        val searchResults = adminContext.search(
                                "dc=ailegorreta,dc=com",
                                "(&(objectClass=person)(cn=$username))",
                                searchControls
                            )
        val distinguishedName: String
        val isExternal: Boolean
        if (searchResults.hasMore()) {
            val result = searchResults.next()
            val attrs = result.attributes

            distinguishedName = result.nameInNamespace

            if (attrs.get("cn").get().toString() != username) {
                adminContext.close()
                logger.error("No se pudo autentificar con el administrador del LDAP al usuario")
                return AuthenticateResult.USER_NOT_EXISTS
            }
            isExternal = distinguishedName.contains("Externals")
        } else {
            adminContext.close()
            logger.error("No se pudo autentificar con el administrador del LDAP")
            return AuthenticateResult.NOT_AUTHENTICATED
        }
        adminContext.close()
        if (distinguishedName.isNullOrBlank())
            return AuthenticateResult.NOT_AUTHENTICATED

        return try {
                    environment[Context.SECURITY_PRINCIPAL] = distinguishedName
                    environment[Context.SECURITY_CREDENTIALS] = password
                    val userContext = InitialDirContext(environment)

                    userContext.close()
                    if (isExternal)
                        AuthenticateResult.EXTERNAL
                    else
                        AuthenticateResult.EMPLOYEE
                } catch (e: AuthenticationException) {
                    logger.info("No se pudo autentificar al usuario $username: ${e.message}")
                    AuthenticateResult.NOT_AUTHENTICATED
                } catch (e: Exception) {
                    logger.info("No se pudo autentificar al usuario $username: ${e.message}")
                    AuthenticateResult.ERROR
                }
    }
}

