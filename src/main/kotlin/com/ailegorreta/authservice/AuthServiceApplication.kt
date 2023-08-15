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
 *  AuthServiceApplication.kt
 *
 *  Developed 2023 by LegoSoftSoluciones, S.C. www.legosoft.com.mx
 */
package com.ailegorreta.authservice

import com.ailegorreta.authservice.config.ServiceConfig
import com.ailegorreta.authservice.repository.FacultadRepository
import com.ailegorreta.authservice.util.Neo4jIdTypeConverter
import com.ailegorreta.commons.utils.HasLogger
import org.neo4j.driver.Driver
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.core.convert.converter.GenericConverter
import org.springframework.data.neo4j.core.convert.Neo4jConversions
import org.springframework.stereotype.Component
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.*

/**
 * Spring authorization server project. This server generates the Oauth token
 * The version of Oauth is 2.1.
 *
 * @project : auth-service
 * @author rlh
 * @date June 2023
 */
@SpringBootApplication
class AuthServiceApplication

fun main(args: Array<String>) {
	runApplication<AuthServiceApplication>(*args)
}

/**
 * SDN Neo4j converters. Just add the Neo4jId converter from ElementId
 */
@Bean
fun neo4jConversions(): Neo4jConversions {
	val additionalConverters: Set<GenericConverter> = Collections.singleton(Neo4jIdTypeConverter())
	return Neo4jConversions(additionalConverters)
}

/**
 * Class that checks if we need to initialize the Neo4j database for the first time.
 * We check if exists a permit (facultad). If it is empty then we delete all nodes
 * and initializes the data defined in the neo4j Flyway script.
 *
 * note: This class must be commented for production
 */
@Component
class DataInitializer constructor(private val facultadRepository: FacultadRepository,
								  private val driver: Driver,
								  private val serviceConfig: ServiceConfig): ApplicationRunner, HasLogger {

	override fun run(args: ApplicationArguments?) {
		if (facultadRepository.count() == 0L) {
			logger.info("The Noe4j database is empty... We fill it with minimum security data")
			BufferedReader(InputStreamReader(this.javaClass.getResourceAsStream(
				  "${serviceConfig.neo4jFlywayLocations}/iamDBstart.cypher"))).use { testReader ->
				driver.session().use { session ->
					// NOTE: This command will erase ALL database data. Comment if we don´t want to
					//       delete all data since the iamDBstart.cypher utilizes MERGE instead of CREATE
					session.run("MATCH (n) DETACH DELETE n")

					val startCypher = testReader.readText()

					// consume all results from the driver
					session.run(startCypher)
						   .consume()
				}
			}

			logger.info("The database Neo4j has been initialized...")
			logger.info("Check that the constraints exists:")
			logger.info("  CREATE CONSTRAINT unique_compania FOR (compania:Compania) REQUIRE compania.nombre IS UNIQUE")
			logger.info("  CREATE CONSTRAINT unique_usuario FOR (usuario:Usuario) REQUIRE usuario.idUsuario IS UNIQUE")
			logger.info("  CREATE CONSTRAINT unique_usuario2 FOR (usuario:Usuario) REQUIRE usuario.nombreUsuario IS UNIQUE")
		} else
			logger.debug("The database Neo4j has already data...")
	}

}
