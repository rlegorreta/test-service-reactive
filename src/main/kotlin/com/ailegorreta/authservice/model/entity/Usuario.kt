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
*  Usuario.kt
*
 *  Developed 2023 by LegoSoftSoluciones, S.C. www.legosoft.com.mx
*/
package com.ailegorreta.authservice.model.entity

import java.util.*

import com.fasterxml.jackson.annotation.JsonIdentityInfo
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.ObjectIdGenerators
import org.springframework.data.neo4j.core.schema.*
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset

/**
 * User entity. This entity must be synchronized with an LDAP or
 * ActiveDirectory.
 *
 * We use ElementId instead of Long type in order to keep out the Repository warning from SDN Neo4j and for
 * future Neo4j version gor generated values or in a future version generate UIID types
 *
 * @author rlh
 * @project : auth-service
 * @date August 2023
 *
 */
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator::class, property = "id")
@Node("Usuario")
data class Usuario(@Id @GeneratedValue(GeneratedValue.InternalIdGenerator::class) var id: String,
                    @Property(name = "idUsuario")		var idUsuario: Long,
                    @Property(name = "nombreUsuario")	var nombreUsuario: String,
                    @Property(name = "nombre") 			var nombre: String,
                    @Property(name = "apellido") 		var apellido: String,
                    @Property(name = "telefono")		var telefono: String,
                    @Property(name = "mail")			var mail: String,
                    @Property(name = "interno")
                    @JsonProperty("interno")		    var interno: Boolean,
                    @Property(name = "activo")
                    @JsonProperty("activo")			    var activo: Boolean,
                    @Property(name = "administrador")
                    @JsonProperty("administrador")	    var administrador: Boolean,
                    @Property(name = "zonaHoraria")			var zonaHoraria: String? = null,
                    @Property(name = "fechaModificacion")	var fechaModificacion: LocalDateTime) : OidcUser {

    var grantedAuthorities: Collection<GrantedAuthority> = LinkedHashSet()
    var _password = nombre
    var nombreCompania = ""

    // Spring Security UserDetails interface methods
    /*
    override fun getAuthorities() = grantedAuthorities
    override fun isEnabled() = activo
    override fun getUsername() = nombre
    override fun isCredentialsNonExpired() = activo

     * Note: the password is obtained from the LDAP microservice
     *
    override fun getPassword() = _password
    override fun isAccountNonExpired() = activo
    override fun isAccountNonLocked() = activo
    */

    /*
     Oidc interface
     */
    override fun getEmail() = mail
    override fun getName() = nombreUsuario
    override fun getGivenName() = nombre
    override fun getFamilyName() = apellido
    override fun getFullName(): String {
        val firstName: String = nombre
        val lastName: String = apellido
        val sb = StringBuilder()

        if (firstName.isNotBlank()) sb.append(firstName)
        if (lastName.isNotBlank()) {
            if (sb.isNotEmpty()) sb.append(" ")
            sb.append(lastName)
        }
        if (sb.isEmpty()) sb.append(name)

        return sb.toString()
    }
    override fun getPhoneNumber() = telefono
    override fun getPreferredUsername() = nombreUsuario
    override fun getZoneInfo() = zonaHoraria ?: ZoneId.systemDefault().toString()
    override fun getUpdatedAt(): Instant = fechaModificacion.toInstant(ZoneOffset.UTC)
    // Noop fields
    override fun getAttributes() = emptyMap<String, Any>()
    override fun getAuthorities() = grantedAuthorities
    override fun getClaims() = emptyMap<String, Any>()
    override fun getUserInfo() = null
    override fun getIdToken() = null
}

/**
 * Asignado relationship. The relationship has an attribute that if is activo
 * or not activo. Not activo means that the assignment has not been approved
 *
 * The mapping is done to CompaniaAsignado and not to Companias to avoid circular
 * references and make Spring Data to slow.
 *
 * @author rlh
 * @project : auth
 * @date January 2023
 *
 */
@RelationshipProperties
data class Asignado (@Id @GeneratedValue var id: String,
                     @Property(name = "activo")
                     @JsonProperty("activo") var activo: Boolean,
                     @TargetNode val area: AreaAsignada
)
