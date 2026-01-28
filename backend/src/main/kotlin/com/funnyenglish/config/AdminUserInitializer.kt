package com.funnyenglish.config

import com.funnyenglish.entity.User
import com.funnyenglish.repository.UserRepository
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
    @Value("\${app.admin.email:admin@funnyenglish.app}") private val adminEmail: String,
    @Value("\${app.admin.password:}") private val adminPassword: String,
    @Value("\${app.admin.display-name:Admin}") private val adminDisplayName: String
) : ApplicationRunner {
    private val logger = LoggerFactory.getLogger(AdminUserInitializer::class.java)

    override fun run(args: ApplicationArguments) {
        if (adminPassword.isBlank()) {
            logger.warn("Admin user not created: app.admin.password is empty")
            return
        }

        if (userRepository.existsByEmail(adminEmail)) {
            logger.info("Admin user already exists: {}", adminEmail)
            return
        }

        val adminUser = User(
            email = adminEmail,
            passwordHash = passwordEncoder.encode(adminPassword),
            displayName = adminDisplayName,
            role = "ADMIN"
        )

        userRepository.save(adminUser)
        logger.info("Admin user created: {}", adminEmail)
    }
}
