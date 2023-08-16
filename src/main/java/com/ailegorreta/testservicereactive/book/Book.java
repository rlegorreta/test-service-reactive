package com.ailegorreta.testservicereactive.book;


/**
 * Simulates a service with all the books. Just for testing the Ppstgres test container
 */
public record Book( String isbn, String title,  String author, Double price){

    public static Book of(String isbn, String title, String author, Double price) {
        return new Book(isbn, title, author, price);
    }
}