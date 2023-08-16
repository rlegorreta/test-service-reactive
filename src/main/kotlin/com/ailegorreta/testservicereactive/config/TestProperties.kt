package com.ailegorreta.testservicereactive.config

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * Important : If we want other beans to refresh the scope (as this bean in @ConfigurationProperties)
 *             we need to include the @RefreshScope annotation.
 */
@ConfigurationProperties(prefix = "test-reactive")
class TestProperties {

    var greeting: String? = null

    var catalogServiceUri: String? = null

    var resilientTimeOut: Int = 3

}