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
*  Facultad.kt
*
 *  Developed 2023 by LegoSoftSoluciones, S.C. www.legosoft.com.mx
*/
package com.ailegorreta.authservice.model.entity

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIdentityInfo
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.ObjectIdGenerators
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.neo4j.core.schema.*

import java.time.LocalDateTime
import org.springframework.security.core.GrantedAuthority

/**
 * Entity for simple Permits.
 *
 * A Permit y what a User can do or cannot do.
 *
 * Simple permits are boolean capability for a User.
 *
 * Fasterxml is used for serialization and deserialization on the authDB.oauth2_authorization table.
 * This is a GrantedAuthority class
 *
 * important note: The field attribute oauth2_authorization is of type Text (Postgress) so it has a maximum limit
 *                 of characters. If one user will going to have many permits it can be overloaded.
 *                 To estrange things it sends a different error by the jdbc driver:
 *                 java.lang.ClassCastException: class java.lang.String cannot be cast to class org.springframework.security.core.GrantedAuthority
 *                 because the .deserialize from fasterxml.
 *                 To solve the problem (partially) we define attributes with @jsonIgnoreand also take care for
 *                 for the size in this attribute.
 *
 * We use ElementId instead of Long type in order to keep out the Repository warning from SDN Neo4j and for
 * future Neo4j version gor generated values or in a future version generate UIID types.
 *
 * @author rlh
 * @project : auth-service
 * @date May 2023
 *
 */
@Node("Facultad")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator::class, property = "id")
data class Facultad (@Id @GeneratedValue(GeneratedValue.InternalIdGenerator::class) @JsonIgnore var neo4jId: String,
                     @Property(name = "nombre") @JsonIgnore                        var nombre: String = "in DB",
                     @Property(name = "descripcion") @JsonIgnore                   var descripcion: String?,
                     @Property(name = "tipo") @JsonProperty("tipo")		    	   var tipo:  String,
                     // ^ Neo4j does not convert from Enum to string correctly.
                     @Property(name = "usuarioModificacion") @JsonIgnore           var usuarioModificacion: String?,
                     @LastModifiedDate
                     @Property(name = "fechaModificacion") @JsonIgnore             var fechaModificacion: LocalDateTime?,
                     @Property(name = "activo") @JsonIgnore		                   var activo: Boolean = true): GrantedAuthority {

    @JsonCreator
    constructor(@JsonProperty("id")id: String, @JsonProperty("nombre") nombre: String,
                @JsonProperty("tipo")tipo: String): this(
                id , nombre, null, tipo,null, null)

    // Spring security methods
    @JsonProperty("authority")
    override fun getAuthority() = "ROLE_" + nombre.uppercase()

    @JsonProperty("authority")
    fun setAuthority(value: String) {}

    @JsonProperty("id")
    fun getId() = neo4jId

    @JsonProperty("id")
    fun setId(id: String) { neo4jId = id }

    override fun toString() = """
        neo4jId = $neo4jId 
        nombre = $nombre
        descripcion = $descripcion
        tipo = $tipo
        activo = $activo
        authority = $authority
    """.trimIndent()
}

enum class FacultadTipo {
    HORARIO,
    FISICA,
    SISTEMA,
    SIMPLE         /* other FACULTAD_TYPES could be added here for future versions */
}



