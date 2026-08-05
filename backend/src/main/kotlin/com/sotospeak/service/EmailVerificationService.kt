package com.sotospeak.service

import com.sotospeak.entity.EmailVerificationToken
import com.sotospeak.entity.User
import com.sotospeak.repository.EmailVerificationTokenRepository
import com.sotospeak.repository.UserRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.security.SecureRandom
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.HexFormat

/** Выбрасывается при логине неподтверждённого пользователя (flag=on) → 403 EMAIL_NOT_VERIFIED. */
class EmailNotVerifiedException(message: String) : RuntimeException(message)

@Service
class EmailVerificationService(
    private val tokenRepository: EmailVerificationTokenRepository,
    private val userRepository: UserRepository,
    private val emailService: EmailService,
    @Value("\${app.email-verification.enabled:false}") val enabled: Boolean,
    @Value("\${app.email-verification.token-ttl-hours:24}") private val tokenTtlHours: Long,
    @Value("\${app.public-url:http://localhost:8080}") private val publicUrl: String
) {
    private val random = SecureRandom()

    /** Новый токен + письмо. Старые неподтверждённые токены пользователя инвалидируются. */
    @Transactional
    fun issueToken(user: User) {
        tokenRepository.deleteUnconfirmedByUserId(user.id)
        val token = EmailVerificationToken(
            userId = user.id,
            token = generateToken(),
            expiresAt = Instant.now().plus(tokenTtlHours, ChronoUnit.HOURS)
        )
        tokenRepository.save(token)
        emailService.sendVerificationEmail(user.email, user.displayName, verificationUrl(token.token))
    }

    /**
     * Подтверждение по токену. Возвращает true при успехе.
     * Невалидный/истёкший/использованный токен — false (детали не раскрываем).
     */
    @Transactional
    fun confirm(token: String): Boolean {
        val record = tokenRepository.findByToken(token) ?: return false
        if (record.confirmedAt != null) return false
        if (record.expiresAt.isBefore(Instant.now())) return false

        record.confirmedAt = Instant.now()
        tokenRepository.save(record)

        val user = userRepository.findById(record.userId).orElse(null) ?: return false
        user.emailVerified = true
        userRepository.save(user)
        return true
    }

    /** Resend: одинаковый результат независимо от существования email (anti-enumeration). */
    @Transactional
    fun resend(email: String) {
        val user = userRepository.findByEmail(email.trim().lowercase()) ?: return
        if (user.emailVerified) return
        issueToken(user)
    }

    private fun verificationUrl(token: String): String =
        "${publicUrl.trimEnd('/')}/api/auth/verify-email?token=$token"

    private fun generateToken(): String {
        val bytes = ByteArray(32) // 128+ бит энтропии
        random.nextBytes(bytes)
        return HexFormat.of().formatHex(bytes)
    }
}
