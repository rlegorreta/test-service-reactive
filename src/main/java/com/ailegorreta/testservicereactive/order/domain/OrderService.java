package com.ailegorreta.testservicereactive.order.domain;


import com.ailegorreta.commons.utils.HasLogger;
import com.ailegorreta.testservicereactive.book.Book;
import com.ailegorreta.testservicereactive.book.BookClient;
import org.slf4j.Logger;
import jakarta.validation.constraints.NotNull;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService implements HasLogger {
	private final BookClient bookClient;
	private final OrderRepository orderRepository;

	public OrderService(BookClient bookClient, OrderRepository orderRepository) {
		this.bookClient = bookClient;
		this.orderRepository = orderRepository;
	}

	public Flux<Order> getAllOrders(String userId) {
		return orderRepository.findAllByCreatedBy(userId);
	}

	@Transactional
	public Mono<Order> submitOrder(String isbn, int quantity) {
		return bookClient.getBookByIsbn(isbn)
						.map(book -> buildAcceptedOrder(book, quantity))
						.defaultIfEmpty(buildRejectedOrder(isbn, quantity))
						// ^ If the book is not available or not exists reject the order
						.flatMap(orderRepository::save)
						.doOnNext(this::publishOrderAcceptedEvent);
	}

	public static Order buildAcceptedOrder(Book book, int quantity) {
		return Order.of(book.isbn(), book.title() + " - " + book.author(),
						book.price(), quantity, OrderStatus.ACCEPTED);
	}

	public static Order buildRejectedOrder(String bookIsbn, int quantity) {
		return Order.of(bookIsbn, null, null, quantity, OrderStatus.REJECTED);
	}

	private void publishOrderAcceptedEvent(Order order) {
		if (!order.status().equals(OrderStatus.ACCEPTED)) {
			return;
		}
		getLogger().info("Simultae Sending order accepted event with id: {}", order.id());
	}

	@NotNull
    @Override
    public Logger getLogger() { return HasLogger.DefaultImpls.getLogger(this); }
}
