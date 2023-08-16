package com.ailegorreta.testservicereactive.book;

import java.time.Duration;

import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

/**
 * Simulates a reactive call to a service with all books
 */
@Component
public class BookClient {
    private final WebClient webClient;

    public BookClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public Mono<Book> getBookByIsbn(String isbn) {
        if (isbn.equals("1234567893"))
            return Mono.fromSupplier(() -> Book.of("1234567893","Java on Action", "Golberg", 1000.1));
        else if (isbn.equals("0002"))
            return Mono.fromSupplier(() -> Book.of("0002","Kotlin on Action", "Date J", 2000.1));
        else if (isbn.equals("0003"))
            return Mono.fromSupplier(() -> Book.of("0003","Scala on Action", "Knuth", 3000.1));
        else if (isbn.equals("0004"))
            return Mono.fromSupplier(() -> Book.of("0004","Python on Action", "Diskstra", 4000.1));

        return Mono.fromSupplier(() -> Book.of("xxxx","Undefined", "Anonymous", 1000.1));

        /* Is used the service and with Resilence see page 282:

        return webClient.get()
				        .uri(BOOKS_ROOT_API + isbn)
				        .retrieve()
				        .bodyToMono(Book.class)
				        .timeout(Duration.ofSeconds(testProperties.resilienceTimeout), Mono.empty())
				        // ^ The fallback returns an empty Mono object
				        .onErrorResume(WebClientResponseException.NotFound.class, exception -> Mono.empty())
				        .retryWhen(Retry.backoff(testProperties.resilienceRetries, Duration.ofMillis(100)))
				        // ^ Exponential backoff is used as the retry strategy. Three attempts are allowed with a 100 ms initial backoff.
				        .onErrorResume(Exception.class, exception -> Mono.empty());
				        // ^ If any error happens after the  retry attempts, catch the exception and return an empty object.
         */
    }

}