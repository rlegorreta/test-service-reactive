package com.ailegorreta.testservicereactive

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

/**
 * Testing service to show the following:
 *
 * - How to test reactive REST calls
 *
 * - How to test Postgres database container (with reactive Postgres R2DBC)
 * - Create the database using Flyway
 */
@SpringBootApplication
@ConfigurationPropertiesScan
class TestServiceReactiveApplication

fun main(args: Array<String>) {
	runApplication<TestServiceReactiveApplication>(*args)
}
