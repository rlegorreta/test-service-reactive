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
 *  StaticPagesController.kt
 *
 *  Developed 2023 by LegoSoftSoluciones, S.C. www.legosoft.com.mx
 */
package com.ailegorreta.authservice.controller

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam

/**
 * Controller for feeding the "login" static pages.
 *
 * @project: auth-service
 * @author: rlh
 * @date: March 2023
 */
@Controller
class StaticPagesController {

    @GetMapping("/login")
    fun login(): String {
        return "login"
    }

    @PostMapping("/login")
    fun loginFailed(): String {
        return "redirect:/authenticate?error=invalid username or password"
    }

    @GetMapping("/logout")
    fun logout( @RequestParam(required = false) post_logout_redirect_uri: String? = null,
                model: Model): String {
        if (!post_logout_redirect_uri.isNullOrBlank())
            if (post_logout_redirect_uri.contains("iamui"))
                model.addAttribute("iamui", post_logout_redirect_uri)
            else if (post_logout_redirect_uri.contains("sysui"))
                model.addAttribute("sysui", post_logout_redirect_uri)
            else if (post_logout_redirect_uri.contains("usfui"))
                model.addAttribute("udfui", post_logout_redirect_uri)
            else if (post_logout_redirect_uri.contains("acmeui"))
                model.addAttribute("acmeui", post_logout_redirect_uri)
            else if (post_logout_redirect_uri.contains("carteraui"))
                model.addAttribute("carteraui", post_logout_redirect_uri)

        return "logout"
    }
}
