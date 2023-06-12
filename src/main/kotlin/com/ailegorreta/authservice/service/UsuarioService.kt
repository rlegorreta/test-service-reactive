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
*  UsuarioService.kt
*
 *  Developed 2023 by LegoSoftSoluciones, S.C. www.legosoft.com.mx
*/
package com.ailegorreta.authservice.service

import com.ailegorreta.authservice.model.entity.Facultad
import com.ailegorreta.authservice.model.entity.Usuario
import com.ailegorreta.commons.utils.HasLogger
import com.ailegorreta.authservice.config.AuthenticateResult
import com.ailegorreta.authservice.model.entity.*
import com.ailegorreta.authservice.repository.*
import org.springframework.dao.NonTransientDataAccessResourceException
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.oauth2.core.oidc.user.OidcUser

/**
 * Usuario service that includes the Oauth server services
 *
 * @author rlh,
 * @project : auth-service
 * @date May 2022
 *
 */
@Service("lmsUserDetailsService")
class UsuarioService constructor (private val companiaRepository: CompaniaRepository,
                                  private val usuarioRepository: UsuarioRepository,
                                  private val facultadRepository: FacultadRepository): UsuarioOAuthService, HasLogger {

    override fun loadUserByUsername(username: String): OidcUser {
        try {
            val usuario = usuarioRepository.findByNombreUsuario(username)

            if (usuario != null) {
                if (!usuario.activo)
                    throw BadCredentialsException("Usuario $username no esta activo")

                logger.info("Read usuario: ${usuario.nombre}")
                // now read its granted authorities
                usuario.grantedAuthorities = getUsuarioFacultades(usuario.nombreUsuario)
                logger.info("Read granted authorities: ${usuario.grantedAuthorities}")

                // read the virtual link to compania
                val companias = companiaRepository.findCompaniasByEmpleado(usuario.id.toLong())

                logger.info("Read compañias: ${companias}")

                if (companias.isNotEmpty())
                    usuario.nombreCompania = companias.first().nombre
                // ^ JWT enhanced the token with company name
                logger.trace("Enhance JWT with more data for Spring Authorization Server for ${usuario.nombreCompania}")
                logger.info("User company: ${usuario.nombreCompania}")

                return usuario
            }
        } catch (e: NonTransientDataAccessResourceException) {
            logger.error("Error reading from database Neo4j($username):" + e.message)
            e.printStackTrace()
            throw BadCredentialsException("Error de conexión con la base de datos de Neo4j ${e.message}")
        } catch (e : Exception) {
            logger.error("Sucedió un error en la lectura del token ${e.message}")
            e.printStackTrace()
            throw BadCredentialsException("Error de conexión con la base de datos de Neo4j ${e.message}")
        }
        throw UsernameNotFoundException("El usuario $username existe en el LDAP mas no en el IAM")
    }

    fun loadUserByUsernameAndAuthenticatedResult(name: String, authenticateResult: AuthenticateResult): Usuario {
        val usuario = loadUserByUsername(name) as Usuario

        if (usuario.interno && authenticateResult == AuthenticateResult.EXTERNAL)
            throw BadCredentialsException("Error se espera que el usuario $name sea un usuario externo y no lo es.")
        else if (!usuario.interno && authenticateResult == AuthenticateResult.EMPLOYEE)
            throw BadCredentialsException("Error se espera que el usuario $name sea un empleado y no lo es.")

        return usuario
    }

    /*
     * Get all User Permits adding the extra permits and deleting the forbidden
     * permits
     */
    private fun getUsuarioFacultades(nombreUsuario: String): Collection<Facultad> {
        return  facultadRepository.findUsuarioFacultades(nombreUsuario)
    }

}
