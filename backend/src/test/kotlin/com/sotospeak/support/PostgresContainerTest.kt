package com.sotospeak.support

import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

/**
 * Базовый класс интеграционных тестов на реальном PostgreSQL через Testcontainers
 * (bd FunnyEnglish-wy7.4). H2 + create-drop не ловит Postgres-специфику:
 * native-запросы аналитики (CAST AS DATE), nullable-параметры JPQL (грабля №81),
 * типы колонок DDL (грабля №31).
 *
 * Профиль `integration-test`: Flyway V1–V26 + `ddl-auto: validate` — та же
 * схема, что на staging/prod (образ postgres:16-alpine, как в docker-compose).
 *
 * `disabledWithoutDocker = true`: без доступного Docker-демона тесты
 * пропускаются (aborted), а не падают — гейт `:backend:test` на H2-профиле
 * остаётся зелёным на машинах без Docker.
 *
 * Контейнер и Spring-контекст один на JVM: все наследники с профилем
 * `integration-test` попадают в общий кэш контекстов.
 */
@Testcontainers(disabledWithoutDocker = true)
@ActiveProfiles("integration-test")
abstract class PostgresContainerTest {

    companion object {
        @Container
        @JvmStatic
        val postgres: PostgreSQLContainer<*> =
            PostgreSQLContainer(DockerImageName.parse("postgres:16-alpine"))
            .withDatabaseName("sotospeak_integration")
            .withUsername("test")
            .withPassword("test")

        @JvmStatic
        @DynamicPropertySource
        fun datasourceProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgres::getJdbcUrl)
            registry.add("spring.datasource.username", postgres::getUsername)
            registry.add("spring.datasource.password", postgres::getPassword)
        }
    }
}
