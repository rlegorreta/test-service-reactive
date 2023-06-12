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
 *  ValidApplications.kt
 *
 *  Developed 2023 by LegoSoftSoluciones, S.C. www.legosoft.com.mx
 */
package com.ailegorreta.authservice.config

/**
 *  Stores the valid applications that uses the SSO cookie and not the
 *  one came from Security Authorization Server
 *
 * @author rlh
 * @project : auth-service
 * @date May 2023
 */
class UIApplications() {

    companion object {
        private val validApplications = mutableMapOf("iamui" to "iamuisecret",
                                                     "udfui" to "udfuisecret",
                                                     "carteraui" to "carterauisecret",
                                                     "sysui" to "sysuisecret",
                                                     "sysuimob" to "sysuimobsecret",
                                                     "acmeui" to "acmeuisecret",
                                                     "iam" to "iamsecret",
                                                     "preference" to "preferencesecret",
                                                     "audit" to "auditsecret",
                                                     "udf" to "udfsecret",
                                                     "cartera" to "carterasecret",
                                                     "param" to "paramsecret",
                                                     "cache" to "cachesecret",
                                                     "mail" to "mailsecret",
                                                     "bup" to "bupsecret",
                                                     "expediente" to "expedientesecret"
                                                    )

        fun isValidApplication(appName: String, secret: String) =
            if (validApplications[appName] == null)
                false
            else validApplications[appName].equals(secret)

        fun secret(appName: String) = validApplications[appName]
    }
}
