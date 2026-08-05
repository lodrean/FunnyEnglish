package com.sotospeak.config

import com.sotospeak.entity.User
import com.sotospeak.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component

@Component
class AdminUserInitializer(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    @Value("\${app.admin.email:admin@sotospeak.app}") private val adminEmail: String,
    @Value("\${app.admin.password:}") private val adminPassword: String,
    @Value("\${app.admin.display-name:Admin}") private val adminDisplayName: String,
    @Value("\${app.demo-user.enabled:false}") private val demoUserEnabled: Boolean
) : ApplicationRunner {
    private val logger = LoggerFactory.getLogger(AdminUserInitializer::class.java)

    override fun run(args: ApplicationArguments) {
        if (adminPassword.isBlank()) {
            logger.warn("Admin user not created: app.admin.password is empty")
            return
        }

        val existingAdmin = userRepository.findByEmail(adminEmail)
            ?: userRepository.findByRole("ADMIN")

        if (existingAdmin != null) {
            val needsUpdate = existingAdmin.email != adminEmail
                    || existingAdmin.role != "ADMIN"
                    || existingAdmin.emailVerified != true
                    || !passwordEncoder.matches(adminPassword, existingAdmin.passwordHash)

            if (needsUpdate) {
                val updatedAdmin = existingAdmin.copy(
                    email = adminEmail,
                    passwordHash = passwordEncoder.encode(adminPassword),
                    displayName = adminDisplayName,
                    role = "ADMIN",
                    emailVerified = true   // системный пользователь — не блокируем email-верификацией
                )
                userRepository.save(updatedAdmin)
                logger.info("Admin user updated: {}", adminEmail)
            } else {
                logger.info("Admin user already exists: {}", adminEmail)
            }
        } else {
            val adminUser = User(
                email = adminEmail,
                passwordHash = passwordEncoder.encode(adminPassword),
                displayName = adminDisplayName,
                role = "ADMIN",
                emailVerified = true
            )

            userRepository.save(adminUser)
            logger.info("Admin user created: {}", adminEmail)
        }

        // Create demo user independently of admin (dev only: app.demo-user.enabled=true)
        createDemoUserIfEnabled()
    }

    private fun createDemoUserIfEnabled() {
        if (!demoUserEnabled) {
            return
        }

        val demoEmail = "demo@sotospeak.app"
        val demoPassword = "demo123"

        val existingDemo = userRepository.findByEmail(demoEmail)
        if (existingDemo != null) {
            // Синхронизируем пароль и флаг верификации: dev-БД могла устареть
            // (старый hash, emailVerified=false после включения верификации).
            val needsUpdate = !passwordEncoder.matches(demoPassword, existingDemo.passwordHash)
                    || !existingDemo.emailVerified
            if (needsUpdate) {
                val updatedDemo = existingDemo.copy(
                    passwordHash = passwordEncoder.encode(demoPassword),
                    emailVerified = true
                )
                userRepository.save(updatedDemo)
                logger.info("Demo user updated: {}", demoEmail)
            } else {
                logger.info("Demo user already exists: {}", demoEmail)
            }
            return
        }

        val demoUser = User(
            email = demoEmail,
            passwordHash = passwordEncoder.encode(demoPassword),
            displayName = "Demo User",
            role = "USER",
            emailVerified = true   // demo для E2E/приёмки — не блокируем email-верификацией
        )

        userRepository.save(demoUser)
        // Пароль НЕ логируем (security)
        logger.info("Demo user created: {}", demoEmail)
    }
}
