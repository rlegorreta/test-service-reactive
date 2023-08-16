package com.ailegorreta.testservicereactive

import com.ailegorreta.testservicereactive.book.Book
import com.ailegorreta.testservicereactive.book.BookClient
import com.ailegorreta.testservicereactive.order.domain.Order
import com.ailegorreta.testservicereactive.web.OrderRequest
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import dasniko.testcontainers.keycloak.KeycloakContainer
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import reactor.core.publisher.Mono
import java.io.IOException
import java.util.stream.Collectors

import org.assertj.core.api.Assertions.assertThat
import org.springframework.test.web.reactive.server.WebTestClient.ListBodySpec

/**
 * In this example text we include SpringBoot, Spring Security and TestContainers;
 *
 * note: There is no Spring Security Server Test container (so far) so we use the Keycloak test container as
 * an excellent substitute and the configuration es in the file 'test-real-config.json'.
 *
 * see: page 431 of Cloud native Spring in Action' book or for a simpler realm see:
 * https://www.baeldung.com/spring-boot-keycloak-integration-testing
 *
 * note: the Oauth2 SCOPE is not stored in Keycloak, so the testing must be just for Roles
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("local")
@Testcontainers
class TestServiceReactiveApplicationTests {

	@Autowired
	private val webTestClient: WebTestClient? = null

	@Autowired
	private val objectMapper: ObjectMapper? = null

	@MockBean
	private val bookClient: BookClient? = null
	companion object {
		private var bjornTokens: KeycloakToken? = null

		// Customer and employee
		private var  isabelleTokens: KeycloakToken? = null

		@Container
		val keycloakContainer: KeycloakContainer = KeycloakContainer("quay.io/keycloak/keycloak:19.0")
													.withRealmImportFile("test-realm-config.json")

		@Container
		var postgresql: PostgreSQLContainer<*> = PostgreSQLContainer(DockerImageName.parse("postgres:14.4"))

		@BeforeAll
		@JvmStatic
		fun generateAccessTokens() {
			val webClient = WebClient.builder()
									 .baseUrl(keycloakContainer.authServerUrl + "realms/TestService/protocol/openid-connect/token")
									 .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
									 .build()

			isabelleTokens = authenticateWith("isabelle","password", webClient)
			bjornTokens = authenticateWith("bjorn","password", webClient)
		}

		private fun authenticateWith(username: String, password: String, webClient: WebClient): KeycloakToken? {
			return webClient.post()
				.body(BodyInserters.fromFormData("grant_type", "password")
									.with("client_id", "polar-test")
									.with("username", username)
									.with("password", password))
				.retrieve()
				.bodyToMono(KeycloakToken::class.java)
				.block()
		}

		@AfterAll
		@JvmStatic
		fun closeContainers() {
			if (keycloakContainer.isRunning) keycloakContainer.close()
		}

		private fun r2dbcUrl(): String {
			return String.format( "r2dbc:postgresql://%s:%s/%s", postgresql.host,
									postgresql.getMappedPort(PostgreSQLContainer.POSTGRESQL_PORT),
									postgresql.databaseName)
		}
	}

	init {
		System.setProperty("spring.security.oauth2.resourceserver.jwt.issuer-uri", keycloakContainer.authServerUrl + "realms/TestService")
		keycloakContainer.start()

		/* Do not use @DynamicPropertySource annotation with static method because it is part of the Spring-Boot
         * context lifecycle. Better to use the singleton container pattern
            see: https://stackoverflow.com/questions/74110777/dynamicpropertysource-not-being-invoked-kotlin-spring-boot-and-testcontainers
        */

		// Now Postgres repository
		System.setProperty("spring.r2dbc.url", r2dbcUrl())
		System.setProperty("spring.r2dbc.username", postgresql.username)
		System.setProperty("spring.r2dbc.password", postgresql.password)
		System.setProperty("spring.flyway.url", postgresql.jdbcUrl)

		postgresql.start()
	}


	@Test
	fun contextLoads() {
		println("[A] Postgres container: ${postgresql}")
		println("[B] keycloakContainer:${keycloakContainer}")
		println("[C] isabelleTokens:${isabelleTokens}")
		println("[D] bjornTokens:${bjornTokens}")
	}


	@Test
	@Throws(IOException::class)
	fun whenGetOwnOrdersThenReturn() {
		val bookIsbn = "1234567893"
		val book = Book(bookIsbn, "Title", "Author", 9.90)

		BDDMockito.given(bookClient!!.getBookByIsbn(bookIsbn)).willReturn(Mono.just(book))

		val orderRequest = OrderRequest(bookIsbn, 1)
		val expectedOrder = webTestClient!!.post()
										   .uri("/orders")
											.headers { headers: HttpHeaders ->
														headers.setBearerAuth(isabelleTokens!!.accessToken)
														}
											.bodyValue(orderRequest)
											.exchange()
											.expectStatus().is2xxSuccessful()
											.expectBody(Order::class.java)
											.returnResult()
											.responseBody

		assertThat(expectedOrder).isNotNull()


		webTestClient!!.get()
					   .uri("/orders")
					   .headers { headers: HttpHeaders ->
									headers.setBearerAuth( isabelleTokens!!.accessToken)
								}
					  .exchange()
					  .expectStatus().is2xxSuccessful()
					  .expectBodyList(Order::class.java).value<ListBodySpec<Order>> { orders ->
						val orderIds: List<Long> = orders.stream()
														 .map(Order::id)
														 .collect(Collectors.toList())

						assertThat(orderIds).contains(expectedOrder!!.id())
					  }
	}

	@JvmRecord
	private data class KeycloakToken @JsonCreator private constructor(
		@param:JsonProperty("access_token") val accessToken: String	)
}
