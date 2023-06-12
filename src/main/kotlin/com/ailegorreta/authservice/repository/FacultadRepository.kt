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
*  FacultadRepository
*
 *  Developed 2023 by LegoSoftSoluciones, S.C. www.legosoft.com.mx
*/
package com.ailegorreta.authservice.repository

import com.ailegorreta.authservice.model.entity.Facultad
import org.springframework.stereotype.Repository
import org.springframework.data.neo4j.repository.Neo4jRepository
import org.springframework.data.neo4j.repository.query.Query
import org.springframework.data.repository.query.Param
import java.util.*

/**
 * Interface de Neo4j repository for Entity Facultad.
 *
 * @see SpringData with Neo4j for more information
 *
 * @author rlh
 * @project : auth-service
 * @date May 2023
 *
 */
@Repository
interface FacultadRepository : Neo4jRepository<Facultad, String> {

    /**
     * Old version:
     *  call { MATCH (u:Usuario)-[:TIENE_PERFIL]->(p:Perfil)-[:TIENE_ROL]->(r:Rol)-[:TIENE_FACULTAD]->(f:Facultad)
                where u.nombreUsuario = 'adminIAM' AND
                      u.activo AND
                      p.activo AND
                      r.activo AND
                      f.activo AND
                NOT (u)-[:SIN_FACULTAD]->(f)
                RETURN f as ff
            UNION ALL
                MATCH (ue:Usuario)-[:FACULTAD_EXTRA]->(fe:Facultad)
                    where ue.nombreUsuario = 'adminIAM' AND
                          ue.activo AND
                          fe.activo AND
                    NOT (ue)-[:SIN_FACULTAD]->(fe)
                RETURN fe as ff
            }
            RETURN ff
     */
    @Query("call { MATCH (u:Usuario)-[:TIENE_PERFIL]->(p:Perfil)-[:TIENE_ROL]->(r:Rol)-[:TIENE_FACULTAD]->(f:Facultad) " +
            " where u.nombreUsuario = \$nombreUsuario AND " +
            "       u.activo AND " +
            "       p.activo AND " +
            "       r.activo AND " +
            "       f.activo AND " +
            "NOT exists((u)-[:SIN_FACULTAD]->(f))  " +
            "RETURN f as ff " +
            "UNION ALL " +
            "MATCH (ue:Usuario)-[:FACULTAD_EXTRA]->(fe:Facultad) " +
            " where ue.nombreUsuario = \$nombreUsuario AND " +
            "       ue.activo AND " +
            "       fe.activo AND " +
            "NOT exists((ue)-[:SIN_FACULTAD]->(fe))  " +
            "RETURN fe as ff " +
            "} " +
            "RETURN ff "
    )
    fun findUsuarioFacultades(@Param("nombreUsuario")nombreUsuario: String): Collection<Facultad>

    @Query("MATCH (u:Usuario)-[fe:FACULTAD_EXTRA]->(fex:Facultad)" +
            " where u.nombreUsuario = \$nombreUsuario " +
            " AND fex.activo" +
            " RETURN fex")
    fun findUsuarioFacultadesExtra(@Param("nombreUsuario")nombreUsuario: String): List<Facultad>

}
