package com.sotospeak.service

import com.sotospeak.dto.*
import com.sotospeak.entity.AuthProvider
import com.sotospeak.entity.User
import com.sotospeak.exception.InvalidCredentialsException
import com.sotospeak.repository.UserRepository
import com.sotospeak.security.JwtService
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService,
    private val emailVerificationService: EmailVerificationService,
    private val refreshTokenService: RefreshTokenService,
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
        val refreshToken = refreshTokenService.issue(savedUser)
        return RegisterResponse(user = savedUser.toResponse(), emailSent = false, token = token, refreshToken = refreshToken)
    }

    @Transactional
    fun login(request: LoginRequest): AuthResponse {
        val email = request.email.trim().lowercase()
        val user = userRepository.findByEmail(email)
            ?: throw InvalidCredentialsException()

        if (user.passwordHash == null || !passwordEncoder.matches(request.password, user.passwordHash)) {
            throw InvalidCredentialsException()
        }

        if (emailVerificationService.enabled && !user.emailVerified) {
            throw EmailNotVerifiedException("Email is not verified")
        }

        val token = jwtService.generateToken(user.id.toString(), user.email, user.role)
        val refreshToken = refreshTokenService.issue(user)

        return AuthResponse(
            token = token,
            refreshToken = refreshToken,
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
        val refreshToken = refreshTokenService.issue(user)

        return AuthResponse(
            token = token,
            refreshToken = refreshToken,
            user = user.toResponse()
        )
    }

    /**
     * Обмен refresh-токена на новую пару access+refresh с ротацией (bd FunnyEnglish-nj2.7).
     * Старый refresh-токен одноразовый; reuse-detection отзывает всю цепочку (RefreshTokenService).
     */
    @Transactional
    fun refreshToken(request: RefreshTokenRequest): AuthResponse {
        val (user, newRefreshToken) = refreshTokenService.rotate(request.refreshToken)
        val token = jwtService.generateToken(user.id.toString(), user.email, user.role)

        return AuthResponse(
            token = token,
            refreshToken = newRefreshToken,
            user = user.toResponse()
        )
    }

    /** Logout: отзыв предъявленного refresh-токена. Идемпотентно (всегда успешно для клиента). */
    @Transactional
    fun logout(request: RefreshTokenRequest) {
        refreshTokenService.revoke(request.refreshToken)
    }
}
