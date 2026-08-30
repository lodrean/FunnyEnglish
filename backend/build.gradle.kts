plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.springBoot)
    alias(libs.plugins.springDependencyManagement)
    alias(libs.plugins.kover)
    alias(libs.plugins.detekt)
    kotlin("plugin.spring") version "2.1.0"
    kotlin("plugin.jpa") version "2.1.0"
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

dependencies {
    // Зависимость :shared снята 2026-08-29 (bd FunnyEnglish-0w3.2): использованные legacy-модели
    // скопированы в backend/src/main/kotlin/com/sotospeak/shared/model/ (те же FQN).

    // Spring Boot
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.security)
    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.spring.boot.starter.validation)
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    // implementation(libs.spring.boot.starter.oauth2.client) // Uncomment when OAuth credentials are configured

    // Kotlin
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    // jackson-module-kotlin: без него не биндятся is-префиксные Boolean (isCorrect),
    // не применяются Kotlin-дефолты (NPE -> 500) и non-null валидация даёт 500 вместо 400.
    // Версия управляется Spring Boot BOM.
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.datetime)

    // Database
    implementation(libs.postgresql)
    implementation(libs.flyway.core)
    implementation(libs.flyway.postgresql)

    // JWT
    implementation(libs.jjwt.api)
    runtimeOnly(libs.jjwt.impl)
    runtimeOnly(libs.jjwt.jackson)

    // AWS S3
    implementation(libs.aws.s3)

    // Caching (Caffeine)
    implementation("com.github.ben-manes.caffeine:caffeine:3.1.8")
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("org.springframework.boot:spring-boot-starter-mail")

    // Testing
    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.mockk)
    // Testcontainers-Postgres для интеграционных тестов (bd FunnyEnglish-wy7.4):
    // H2 + create-drop не ловит Postgres-специфику (грабли №31/81).
    testImplementation(libs.testcontainers.junit.jupiter)
    testImplementation(libs.testcontainers.postgresql)
    runtimeOnly(libs.h2)
}

tasks.withType<Test> {
    useJUnitPlatform()
}

springBoot {
    mainClass.set("com.sotospeak.SoToSpeakApplicationKt")
}

allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
}

// detekt подключён 2026-08-30 (bd FunnyEnglish-qbq.5, аудит AR-7): раньше плагин был
// объявлен только в корне (apply false) и ни один модуль его не применял (грабля №8).
// Baseline (config/detekt/baseline.xml) гасит существующие замечания — gate ловит новые.
// Обновление baseline: ./gradlew :backend:detektBaseline :composeApp:detektBaseline
detekt {
    config.setFrom(rootProject.file("config/detekt/detekt.yml"))
    baseline = rootProject.file("config/detekt/baseline.xml")
}

// Пороги покрытия (koverVerify). Стартовые значения консервативные — поднимать
// постепенно по факту измерений (прецедент: пороги vitest, грабля №88).
// DSL Kover 0.9.1: имя правила — параметр конструктора rule(...); в bound доступны
// только minValue/maxValue (metric/aggregation — не свойства Bound в 0.9.1).
kover {
    reports {
        verify {
            rule("Minimal line coverage") {
                bound {
                    minValue = 40
                }
            }
        }
    }
}
