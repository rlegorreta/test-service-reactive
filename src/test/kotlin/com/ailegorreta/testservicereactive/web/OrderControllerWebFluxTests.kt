package com.ailegorreta.testservicereactive.web

import org.junit.jupiter.api.Test
import org.mockito.BDDMockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Mono
import com.ailegorreta.testservicereactive.config.ResourceServerConfig
import com.ailegorreta.testservicereactive.order.domain.Order
import com.ailegorreta.testservicereactive.order.domain.OrderService
import com.ailegorreta.testservicereactive.order.domain.OrderStatus
import org.assertj.core.api.Assertions.assertThat

/**
 * Example just for testing Spring WebFlux only (no integration with SpringBootTest, Spring Security and
 * Testcontainers like KeyCloak)
 */
@WebFluxTest(OrderController::class)
@Import(ResourceServerConfig::class)
internal class OrderControllerWebFluxTests {
    @Autowired
    var webClient: WebTestClient? = null

    @MockBean
    var orderService: OrderService? = null

    @MockBean
    var reactiveJwtDecoder: ReactiveJwtDecoder? = null

    /**
     * note ¡: the ROLE_ADMINLEGO in the OrderController does not apply. Maybe is because the post method
     * and the jwt token.
     */
    @Test
    fun whenBookNotAvailableThenRejectOrder() {
        val orderRequest = OrderRequest("1234567890", 3)
        val expectedOrder = OrderService.buildRejectedOrder(orderRequest.isbn, orderRequest.quantity)

        BDDMockito.given(orderService!!.submitOrder(orderRequest.isbn, orderRequest.quantity))
                  .willReturn(Mono.just(expectedOrder))

        webClient!!.mutateWith(SecurityMockServerConfigurers.mockJwt()
                                .authorities(SimpleGrantedAuthority("SCOPE_iam.facultad"),
                                             SimpleGrantedAuthority("ROLE_ADMINLEGO")))
                    .post()
                    .uri("/orders")
                    .bodyValue(orderRequest)
                    .exchange()
                    .expectStatus().is2xxSuccessful()
                    .expectBody(Order::class.java).value { actualOrder ->
                        assertThat(actualOrder).isNotNull()
                        assertThat(actualOrder.status()).isEqualTo(OrderStatus.REJECTED)
                    }
    }
}

