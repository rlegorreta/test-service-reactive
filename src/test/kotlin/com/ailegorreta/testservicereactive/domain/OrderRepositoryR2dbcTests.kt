package com.ailegorreta.testservicereactive.domain

import com.ailegorreta.testservicereactive.config.DataConfig
import com.ailegorreta.testservicereactive.order.domain.Order
import com.ailegorreta.testservicereactive.order.domain.OrderRepository
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest
import org.springframework.context.annotation.Import
import org.springframework.security.test.context.support.WithMockUser
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import reactor.test.StepVerifier
import java.util.*
import java.util.function.Predicate
import com.ailegorreta.testservicereactive.order.domain.OrderService
import com.ailegorreta.testservicereactive.order.domain.OrderStatus

/**
 * This is an example to do Testing for a Postgres repository
 *
 * - Also includes the auditory postgres variables like CreatedBy & ModifyBy with MockUser annotation
 */
@DataR2dbcTest
@Import(DataConfig::class)
@Testcontainers
class OrderRepositoryR2dbcTests {

    @Autowired
    private val orderRepository: OrderRepository? = null

    companion object {
        @Container
        var postgresql: PostgreSQLContainer<*> = PostgreSQLContainer(DockerImageName.parse("postgres:14.4"))

        private fun r2dbcUrl(): String {
            return String.format( "r2dbc:postgresql://%s:%s/%s", postgresql.host,
                                    postgresql.getMappedPort(PostgreSQLContainer.POSTGRESQL_PORT),
                                    postgresql.databaseName)
        }

        @AfterAll
        @JvmStatic
        fun closeContainers() {
            if (postgresql.isRunning) postgresql.close()
        }
    }

    init {
        /* Do not use @DynamicPropertySource annotation with static method because it is part of the Spring-Boot
         * context lifecycle. Better to use the singleton container pattern
            see: https://stackoverflow.com/questions/74110777/dynamicpropertysource-not-being-invoked-kotlin-spring-boot-and-testcontainers
        */
        System.setProperty("spring.r2dbc.url", r2dbcUrl())
        System.setProperty("spring.r2dbc.username", postgresql.username)
        System.setProperty("spring.r2dbc.password",postgresql.password)
        System.setProperty("spring.flyway.url",postgresql.jdbcUrl)

        postgresql.start()
    }

    @Test
    fun contextLoads() {
        println("[A] Postgres container: $postgresql")
    }

    @Test
    fun findOrderByIdWhenNotExisting() {
        StepVerifier.create(orderRepository!!.findById(394L))
                    .expectNextCount(0)
                    .verifyComplete()
    }

    @Test
    fun createRejectedOrder() {
        val rejectedOrder = OrderService.buildRejectedOrder("1234567890", 3)

        StepVerifier.create(orderRepository!!.save(rejectedOrder))
                    .expectNextMatches(Predicate<Order> { order -> order.status().equals(OrderStatus.REJECTED) })
                    .verifyComplete()
    }

    @Test
    fun whenCreateOrderNotAuthenticatedThenNoAuditMetadata() {
        val rejectedOrder = OrderService.buildRejectedOrder("1234567890", 3)

        StepVerifier.create(orderRepository!!.save(rejectedOrder))
                    .expectNextMatches { order ->
                                Objects.isNull(order.createdBy()) &&
                                Objects.isNull(order.lastModifiedBy())
                    }
            .verifyComplete()
    }

    @Test
    @WithMockUser("marlena")
    fun whenCreateOrderAuthenticatedThenAuditMetadata() {
        val rejectedOrder = OrderService.buildRejectedOrder("1234567890", 3)

        StepVerifier.create(orderRepository!!.save(rejectedOrder))
                    .expectNextMatches { order ->
                        order.createdBy().equals("marlena") &&
                        order.lastModifiedBy().equals("marlena")}
                    .verifyComplete()
    }

}

