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
*  Area.kt
*
 *  Developed 2023 by LegoSoftSoluciones, S.C. www.legosoft.com.mx
*/

package com.ailegorreta.authservice.model.entity

import org.springframework.data.neo4j.core.schema.*
import com.fasterxml.jackson.annotation.JsonIdentityInfo
import com.fasterxml.jackson.annotation.ObjectIdGenerators
import org.springframework.data.annotation.LastModifiedDate
import java.time.LocalDateTime

/**
 * Entity Area.
 *
 * The Area is a part of a Company
 *
 * We use Id instead of Long type in order to keep out the Repository warning from SDN Neo4j and for
 * future Neo4j version gor generated values or in a future version generate UIID types
 *
 * @author rlh
 * @project : auth-service
 * @date May 2023
 */
@Node("Area")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator::class, property = "id")
data class Area(@Id @GeneratedValue(GeneratedValue.InternalIdGenerator::class) var id: String,
                 @Property(name = "nombre") 	            var nombre: String,
                 @Property(name = "usuarioModificacion")    var usuarioModificacion: String,
                 @LastModifiedDate
                 @Property(name = "fechaModificacion")      var fechaModificacion: LocalDateTime,
                 @Property(name = "activo")                 var activo: Boolean,
                 @Property(name = "idArea")                 var idArea: Long,
                 @Property(name = "idPersona")              var idPersona: Long,
                )

/**
 * Entity AreaAsignadas.
 *
 * This is a replica for Area to avoid circular references with Usuario-[:ASIGNADO]->(a:Area)
 * and make Spring Data very slow.
 *
 * - Every time an Area is added or updated AreaAsignada is updates b the service.
 * - The queries for ASIGNADO are done to the AreaAsignado entity instead for Area entity.
 *
 * We use ElementId instead of Long type in order to keep out the Repository warning from SDN Neo4j and for
 * future Neo4j version gor generated values or in a future version generate UIID types
 *
 * @author rlh
 * @project : auth-service
 * @date May 2023
 *
 */
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator::class, property = "id")
@Node("AreaAsignada")
data class AreaAsignada(@Id @GeneratedValue(GeneratedValue.InternalIdGenerator::class) var id: String,
                         @Property(name = "nombre") 	          var nombre: String,
                         @Property(name = "usuarioModificacion")  var usuarioModificacion: String,
                         @LastModifiedDate
                         @Property(name = "fechaModificacion")    var fechaModificacion: LocalDateTime,
                         @Property(name = "activo")               var activo: Boolean,
                         @Property(name = "idArea")               var idArea: Long,
                         @Property(name = "idPersona")            var idPersona: Long
                    ) {

    /* This is to convert AreaAsignada to Area and simplify the DTO creation */
    fun toArea() = Area(id = this.id, nombre = this.nombre, usuarioModificacion = this.usuarioModificacion,
                        fechaModificacion = this.fechaModificacion, activo = this.activo,
                        idArea = this.idArea, idPersona = this.idPersona)
}
