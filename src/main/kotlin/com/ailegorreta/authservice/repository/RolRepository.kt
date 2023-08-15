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
*  RolRepository
*
 *  Developed 2023 by LegoSoftSoluciones, S.C. www.legosoft.com.mx
*/
package com.ailegorreta.authservice.repository

import com.ailegorreta.authservice.model.entity.Facultad
import com.ailegorreta.authservice.model.entity.Rol
import org.springframework.data.neo4j.repository.Neo4jRepository
import org.springframework.data.neo4j.repository.query.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

/**
 * Interface for Neo4j repository for Entity Rol
 *
 * @see SpringData with Neo4j for more information
 *
 * @author: rlh
 * @project: auth-service
 * @date August 202
 */
@Repository
interface RolRepository: Neo4jRepository<Rol, String> {

    @Query("MATCH (u:Usuario)-[:TIENE_PERFIL]->(p:Perfil)-[:TIENE_ROL]->(r:Rol) " +
            " where u.nombreUsuario = \$nombreUsuario AND " +
            "       u.activo AND " +
            "       p.activo AND " +
            "       r.activo " +
            "RETURN r "
    )
    fun findUsuarioRoles(@Param("nombreUsuario")nombreUsuario: String): Collection<Rol>

}