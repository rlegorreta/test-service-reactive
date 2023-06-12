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
 *  AuthServiceApplicationTests.kt
 *
 *  Developed 2023 by LegoSoftSoluciones, S.C. www.legosoft.com.mx
 */
package com.ailegorreta.authservice

import com.ailegorreta.authservice.repository.CompaniaRepository
import com.ailegorreta.authservice.repository.FacultadRepository
import com.ailegorreta.authservice.repository.UsuarioRepository
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.Neo4jContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
/* ^ Disables the default behavior of relying on an embedded test database since we want to use Testcontainers */
@ActiveProfiles("integration")  /* Enables the “integration” profile to load configuration from application-integration.yml */
@Testcontainers
class AuthServiceApplicationTests() {

	companion object {
		@Container
		private val neo4jContainer = Neo4jContainer("neo4j:4.4.5")
											.withAdminPassword("verysecret")

		@DynamicPropertySource
		@JvmStatic
		fun testProperties(registry : DynamicPropertyRegistry) {
			registry.add("spring.neo4j.uri", neo4jContainer::getBoltUrl)
			registry.add("spring.neo4j.authentication.username") { "neo4j" }
			registry.add("spring.neo4j.authentication.password", neo4jContainer::getAdminPassword)
		}

		/** This is the case where we do not fill the database with initial data.
		 *  But in this case we declared in the main class a class DataInitializer that it is an
		 *  Application Runner, so we do not need to fill the database.
		 */
		@BeforeAll
		@JvmStatic
		internal fun initDatabase() {
			println("No initialization of database is done. It is done byeDataInitializer class")
		}
	}

	@Autowired
	var facultadRepository: FacultadRepository? = null

	@Autowired
	var usuarioRepository: UsuarioRepository? = null

	@Autowired
	var companiaRepository: CompaniaRepository? = null

	@Test
	fun contextLoads() {
	}

	@Test
	fun findPermitsForUsers() {
		val allPermits = facultadRepository!!.findUsuarioFacultades("adminALL")

		Assertions.assertThat(allPermits).isNotEmpty
	}

	@Test
	fun findUserByName() {
		val user = usuarioRepository!!.findByNombreUsuario("adminIAM")

		Assertions.assertThat(user).isNotNull

		// Now look for user companies
		val companies = companiaRepository!!.findCompaniasByEmpleado(user!!.id.toLong())

		Assertions.assertThat(companies.size).isEqualTo(1)
	}

}
