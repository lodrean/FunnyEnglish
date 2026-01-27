package com.funnyenglish.service

import com.funnyenglish.dto.*
import com.funnyenglish.entity.AuthProvider
import com.funnyenglish.entity.User
import com.funnyenglish.repository.UserRepository
import com.funnyenglish.security.JwtService
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService
) {
    @Transactional
    fun register(request: RegisterRequest): AuthResponse {
        val email = request.email.trim().lowercase()
        if (userRepository.existsByEmail(email)) {
            throw IllegalArgumentException("Email already registered")
        }

        val user = User(
            email = email,
            passwordHash = passwordEncoder.encode(request.password),
            displayName = request.displayName.trim(),
            authProvider = AuthProvider.EMAIL
        )

        val savedUser = userRepository.save(user)
        val token = jwtService.generateToken(savedUser.id.toString(), savedUser.email, savedUser.role)

        return AuthResponse(
            token = token,
            user = savedUser.toResponse()
        )
    }

    fun login(request: LoginRequest): AuthResponse {
        val email = request.email.trim().lowercase()
        val user = userRepository.findByEmail(email)
            ?: throw IllegalArgumentException("Invalid credentials")

        if (user.passwordHash == null || !passwordEncoder.matches(request.password, user.passwordHash)) {
            throw IllegalArgumentException("Invalid credentials")
        }

        val token = jwtService.generateToken(user.id.toString(), user.email, user.role)

        return AuthResponse(
            token = token,
            user = user.toResponse()
        )
    }

    @Transactional
    fun oauthLogin(provider: String, request: OAuthRequest): AuthResponse {
        val authProvider = when (provider.lowercase()) {
            "google" -> AuthProvider.GOOGLE
            "vk" -> AuthProvider.VK
            "telegram" -> AuthProvider.TELEGRAM
            else -> throw IllegalArgumentException("Unsupported OAuth provider: $provider")
        }

        // Try to find existing user by provider
        var user = userRepository.findByAuthProviderAndProviderId(authProvider, request.token)

        if (user == null && request.email != null) {
            // Try to find by email
            user = userRepository.findByEmail(request.email)

            if (user != null) {
                // Link existing account with OAuth provider
                user = user.copy(
                    authProvider = authProvider,
                    providerId = request.token,
                    avatarUrl = request.avatarUrl ?: user.avatarUrl
                )
                user = userRepository.save(user)
            }
        }

        if (user == null) {
            // Create new user
            user = User(
                email = request.email ?: "${authProvider.name.lowercase()}_${request.token}@funnyenglish.app",
                displayName = request.displayName ?: "User",
                avatarUrl = request.avatarUrl,
                authProvider = authProvider,
                providerId = request.token
            )
            user = userRepository.save(user)
        }

        val token = jwtService.generateToken(user.id.toString(), user.email, user.role)

        return AuthResponse(
            token = token,
            user = user.toResponse()
        )
    }
}
