package com.example.shoppingmall

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Bean
import org.testcontainers.containers.PostgreSQLContainer

// Integration tests run against a real Postgres via Testcontainers, not H2.
// @ServiceConnection wires the container's JDBC url/credentials into the context,
// and Flyway migrates it on startup. See ADR-0004.
@TestConfiguration(proxyBeanMethods = false)
class TestcontainersConfiguration {
    @Bean
    @ServiceConnection
    fun postgresContainer(): PostgreSQLContainer<*> = PostgreSQLContainer("postgres:17-alpine")
}
