package com.ailegorreta.testservicereactive.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient


/**
 * This is just for simulate a webClient call to the books service.
 *
 * note: This is a reactive webClient.
 *
 */
@Configuration
class ClientConfig {
    @Bean
    fun webClient(testProperties: TestProperties, webClientBuilder: WebClient.Builder): WebClient {
        return webClientBuilder
                .baseUrl(testProperties.catalogServiceUri!!)
                .build()
    }
}
