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
 *  UserLookupController.kt
 *
 *  Developed 2023 by LegoSoftSoluciones, S.C. www.legosoft.com.mx
 */
package com.ailegorreta.authservice.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.ailegorreta.authservice.config.CustomAuthenticationProvider
import com.ailegorreta.authservice.config.EnhancedPrincipal
import com.ailegorreta.authservice.service.UsuarioService
import com.ailegorreta.commons.utils.HasLogger
import jakarta.annotation.security.PermitAll
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Lazy
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

/**
 * Controller for getting information about the users directly once the user has been authenticated
 *
 * @project: auth-service
 * @author: rlh
 * @date: May 2023
 */
@Controller
class UserLookupController(@Lazy
                           @Qualifier("lmsUserDetailsService")
                           val service: UsuarioService,
                           val customAuthenticationProvider: CustomAuthenticationProvider,
                           val mapper: ObjectMapper): HasLogger {

    @PermitAll
    @GetMapping("/users")
    fun loadUserByUsername(@RequestParam(required = true) id: String) = service.loadUserByUsername(id)

    /**
     * The mobile apps does not use authentication_code grant. They use simple Vaadin security login panel.
     * When the Vaadin panel is used it calls this REST to verify is the user exists and if it has a valid
     * password.
     * TODO: encode password
     */
    @GetMapping("/loginmobile")
    fun loginMobile(model: Model,
                    @RequestParam(required = true) name: String,
                    @RequestParam(required = true) password: String): String {
        try {
            val res = customAuthenticationProvider.authenticate(MobileAuthentication(name_ = name, credentials_ = password))
                        ?: throw BadCredentialsException("Illegal user name or password")

            model.addAttribute("loginResult", mapper.writeValueAsString(res))
        } catch (e : Exception) {
            val resError = UsernamePasswordAuthenticationToken(
                                EnhancedPrincipal(name,
                                                  mapOf("errorMessages" to e.message!!)),
                                        null , emptyList())
            resError.isAuthenticated = false
            model.addAttribute("loginResult", mapper.writeValueAsString(resError))
            logger.info("User $name cannot logged. User name or password invalid.")
        }

        return "loginmobile"
    }
}

/**
 * Custom authentication data class for mobile applications authentication
 */
data class MobileAuthentication constructor(val name_: String,
                                            val credentials_: String,
                                            var authenticated_: Boolean = false): Authentication {
    override fun getName() = name_

    override fun getAuthorities() = null

    override fun getCredentials() = credentials_

    override fun getDetails() = null

    override fun getPrincipal(): Any {
        TODO("Not yet implemented")
    }

    override fun isAuthenticated() = authenticated_

    override fun setAuthenticated(isAuthenticated: Boolean) {
        this.authenticated_ = isAuthenticated
    }

}