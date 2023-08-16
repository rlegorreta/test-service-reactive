package com.ailegorreta.testservicereactive.web

import com.ailegorreta.testservicereactive.order.domain.Order
import com.ailegorreta.testservicereactive.order.domain.OrderService
import jakarta.validation.Valid
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono


@RestController
@RequestMapping("orders")
class OrderController(val orderService: OrderService) {

    @GetMapping
    fun getAllOrders(@AuthenticationPrincipal jwt: Jwt): Flux<Order> {
        return orderService.getAllOrders(jwt.subject)
    }

    @PostMapping
    // @PreAuthorize("hasAnyRole('employee', 'ADMINLEGO')")
    // @PreAuthorize("hasAnyAuthority('SCOPE_iam.facultad')")
    fun submitOrder(@RequestBody @Valid orderRequest: OrderRequest): Mono<Order> {
        return orderService.submitOrder(orderRequest.isbn, orderRequest.quantity)
    }
}


@JvmRecord
data class OrderRequest(
    @field:NotBlank(message = "The book ISBN must be defined.")
    val isbn: String,

    @field:Max(value = 5, message = "You cannot order more than 5 items.")
    @field:Min(value = 1, message = "You must order at least 1 item.")
    @param:NotNull(message = "The book quantity must be defined.")
    val quantity: Int)
