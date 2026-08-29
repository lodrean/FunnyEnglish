package com.sotospeak.service

import com.sotospeak.dto.*
import com.sotospeak.entity.AuthProvider
import com.sotospeak.entity.User
import com.sotospeak.repository.UserRepository
import com.sotospeak.security.JwtService
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService,
    private val emailVerificationService: EmailVerificationService,
    @Value("\${app.jwt.refresh-window:604800000}")
    private val refreshWindowMs: Long,
    /** OAuth-логин выключен по умолчанию до реализации верификации токена у провайдера (SEC Б3). */
    @Value("\${app.oauth.enabled:false}")
    val oauthEnabled: Boolean
) {
    @Transactional
    fun register(request: RegisterRequest): RegisterResponse {
        val email = request.email.trim().lowercase()
        if (userRepository.existsByEmail(email)) {
            throw IllegalArgumentException("Email already registered")
        }

        val verificationRequired = emailVerificationService.enabled
        val user = User(
            email = email,
            passwordHash = passwordEncoder.encode(request.password),
            displayName = request.displayName.trim(),
            authProvider = AuthProvider.EMAIL,
            emailVerified = !verificationRequired
        )

        val savedUser = userRepository.save(user)

        if (verificationRequired) {
            // Без auto-login: письмо со ссылкой, токен выдаст login после подтверждения
            emailVerificationService.issueToken(savedUser)
            return RegisterResponse(user = savedUser.toResponse(), emailSent = true)
        }

        val token = jwtService.generateToken(savedUser.id.toString(), savedUser.email, savedUser.role)
        return RegisterResponse(user = savedUser.toResponse(), emailSent = false, token = token)
    }

    fun login(request: LoginRequest): AuthResponse {
        val email = request.email.trim().lowercase()
        val user = userRepository.findByEmail(email)
            ?: throw IllegalArgumentException("Invalid credentials")

        if (user.passwordHash == null || !passwordEncoder.matches(request.password, user.passwordHash)) {
            throw IllegalArgumentException("Invalid credentials")
        }

        if (emailVerificationService.enabled && !user.emailVerified) {
            throw EmailNotVerifiedException("Email is not verified")
        }

        val token = jwtService.generateToken(user.id.toString(), user.email, user.role)

        return AuthResponse(
            token = token,
            user = user.toResponse()
        )
    }

    /**
     * ВНИМАНИЕ: endpoint выключен по умолчанию (`app.oauth.enabled=false`, SEC Б3).
     * TODO(security): перед включением реализовать верификацию [OAuthRequest.token] у провайдера
     * (Google tokeninfo / VK users.get / Telegram HMAC) — сейчас клиентский token принимается
     * как providerId без проверки, что даёт account-takeover.
     */
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
                // Link existing account with OAuth provider (мутация managed-entity, не copy() —
                // copy() на data class-entity ломает dirty-checking и делает лишний merge+SELECT)
                user.authProvider = authProvider
                user.providerId = request.token
                if (request.avatarUrl != null) {
                    user.avatarUrl = request.avatarUrl
                }
                user = userRepository.save(user)
            }
        }

        if (user == null) {
            // Create new user
            user = User(
                email = request.email ?: "${authProvider.name.lowercase()}_${request.token}@sotospeak.app",
                displayName = request.displayName ?: "User",
                avatarUrl = request.avatarUrl,
                authProvider = authProvider,
                providerId = request.token,
                emailVerified = true   // OAuth-провайдер уже подтвердил email
            )
            user = userRepository.save(user)
        }

        val token = jwtService.generateToken(user.id.toString(), user.email, user.role)

        return AuthResponse(
            token = token,
            user = user.toResponse()
        )
    }

    fun refreshToken(request: RefreshTokenRequest): AuthResponse {
        val claims = jwtService.extractClaimsAllowExpired(request.refreshToken)
            ?: throw IllegalArgumentException("Invalid refresh token")
        // Истёкший access-токен можно обменять только в пределах refresh-окна (по умолчанию 7 дней) —
        // иначе украденный токен продлевался бы бесконечно.
        val expiration = claims.expiration?.toInstant()
            ?: throw IllegalArgumentException("Invalid refresh token")
        if (expiration.isBefore(java.time.Instant.now().minusMillis(refreshWindowMs))) {
            throw IllegalArgumentException("Refresh window expired")
        }
        val userId = claims.subject ?: throw IllegalArgumentException("Invalid refresh token")
        val userUuid = runCatching { UUID.fromString(userId) }
            .getOrElse { throw IllegalArgumentException("Invalid refresh token") }
        val user = userRepository.findById(userUuid)
            .orElseThrow { IllegalArgumentException("Invalid refresh token") }

        val token = jwtService.generateToken(user.id.toString(), user.email, user.role)

        return AuthResponse(
            token = token,
            user = user.toResponse()
        )
    }
}
