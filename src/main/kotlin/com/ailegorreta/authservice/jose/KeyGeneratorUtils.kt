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
 *  KeyGeneratorUtils.kt
 *
 *  Developed 2023 by LegoSoftSoluciones, S.C. www.legosoft.com.mx
 */
package com.ailegorreta.authservice.jose

import java.math.BigInteger
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.spec.ECFieldFp
import java.security.spec.ECParameterSpec
import java.security.spec.ECPoint
import java.security.spec.EllipticCurve
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

/**
 * KeyGeneratorUtils utilities
 * JOSE utilities: Javascript Object Signing and Encryption
 *
 * @project : auth-service
 * @author rlh
 * @date May 2023
 */
internal object KeyGeneratorUtils {

    fun generateSecretKey(): SecretKey {
        val hmacKey: SecretKey = try {
            KeyGenerator.getInstance("HmacSha256").generateKey()
        } catch (ex: Exception) {
            throw IllegalStateException(ex)
        }
        return hmacKey
    }

    fun generateRsaKey(): KeyPair {
        val keyPair: KeyPair = try {
            val keyPairGenerator = KeyPairGenerator.getInstance("RSA")

            keyPairGenerator.initialize(2048)
            keyPairGenerator.generateKeyPair()
        } catch (ex: Exception) {
            throw IllegalStateException(ex)
        }
        return keyPair
    }

    fun generateEcKey(): KeyPair {
        val ellipticCurve = EllipticCurve(
            ECFieldFp(BigInteger("115792089210356248762697446949407573530086143415290314195533631308867097853951")),
                      BigInteger("115792089210356248762697446949407573530086143415290314195533631308867097853948"),
                      BigInteger("41058363725152142129326129780047268409114441015993725554835256314039467401291"))
        val ecPoint = ECPoint(BigInteger("48439561293906451759052585252797914202762949526041747995844080717082404635286"),
                              BigInteger("36134250956749795798585127919587881956611106672985015071877198253568414405109"))
        val ecParameterSpec = ECParameterSpec(ellipticCurve,
                                              ecPoint,
                                              BigInteger("115792089210356248762697446949407573529996955224135760342422259061068512044369"),
                                           1)
        val keyPair: KeyPair = try {
            val keyPairGenerator = KeyPairGenerator.getInstance("EC")

            keyPairGenerator.initialize(ecParameterSpec)
            keyPairGenerator.generateKeyPair()
        } catch (ex: Exception) {
            throw IllegalStateException(ex)
        }
        return keyPair
    }

}
