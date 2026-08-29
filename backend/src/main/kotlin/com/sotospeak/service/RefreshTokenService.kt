package com.sotospeak.service

import com.sotospeak.entity.RefreshToken
import com.sotospeak.entity.User
import com.sotospeak.exception.InvalidRefreshTokenException
import com.sotospeak.repository.RefreshTokenRepository
import com.sotospeak.repository.UserRepository
import com.sotospeak.security.JwtService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.security.MessageDigest
import java.time.Instant

/**
 * Жизненный цикл refresh-токенов (bd FunnyEnglish-nj2.7, SEC AR-6):
 * выдача при login/register, ротация при каждом обмене (токен одноразовый),
 * отзыв при logout и reuse-detection (повторное использование ротированного/отозванного
 * токена = компрометация цепочки → отзываем ВСЕ refresh-токены пользователя).
 * В БД хранится только SHA-256-хэш — утечка таблицы не даёт валидных токенов.
 */
@Service
class RefreshTokenService(
    private val refreshTokenRepository: RefreshTokenRepository,
    private val userRepository: UserRepository,
    private val jwtService: JwtService
) {
    private val logger = LoggerFactory.getLogger(RefreshTokenService::class.java)

    /** Результат ротации: пользователь + новый сырой refresh-токен. */
    data class RotationResult(val user: User, val newRefreshToken: String)

    /** Выдать новый refresh-токен пользователю (login/register/oauth). */
    @Transactional
    fun issue(user: User): String {
        val data = jwtService.generateRefreshToken(user.id.toString())
        refreshTokenRepository.save(
            RefreshToken(
                userId = user.id,
                jti = data.jti,
                tokenHash = hash(data.raw),
                expiresAt = data.expiresAt
            )
        )
        return data.raw
    }

    /**
     * Обменять refresh-токен на новую пару (ротация). Старый токен помечается rotated.
     * Повторное предъявление ротированного/отозванного токена — reuse-detection:
     * отзыв всей цепочки пользователя + 401.
     */
    @Transactional
    fun rotate(rawToken: String): RotationResult {
        val claims = jwtService.parseRefreshToken(rawToken)
            ?: throw InvalidRefreshTokenException()
        val jti = claims.id ?: throw InvalidRefreshTokenException()

        val stored = refreshTokenRepository.findByJti(jti)
            ?: throw InvalidRefreshTokenException()

        if (!MessageDigest.isEqual(stored.tokenHash.toByteArray(), hash(rawToken).toByteArray())) {
            throw InvalidRefreshTokenException()
        }

        if (stored.revokedAt != null || stored.rotatedAt != null) {
            // Токен уже использован/отозван, но предъявлен снова — цепочка скомпрометирована.
            logger.warn("Refresh token reuse detected for user ${stored.userId}, revoking all sessions")
            revokeAllForUser(stored.userId)
            throw InvalidRefreshTokenException("Refresh token reuse detected")
        }

        if (stored.expiresAt.isBefore(Instant.now())) {
            throw InvalidRefreshTokenException("Refresh token expired")
        }

        val user = userRepository.findById(stored.userId)
            .orElseThrow { InvalidRefreshTokenException() }

        stored.rotatedAt = Instant.now()
        refreshTokenRepository.save(stored)

        return RotationResult(user, issue(user))
    }

    /** Отзыв конкретного refresh-токена (logout). Идемпотентно: невалидный токен — no-op. */
    @Transactional
    fun revoke(rawToken: String) {
        val claims = jwtService.parseRefreshToken(rawToken) ?: return
        val jti = claims.id ?: return
        val stored = refreshTokenRepository.findByJti(jti) ?: return
        if (stored.revokedAt == null) {
            stored.revokedAt = Instant.now()
            refreshTokenRepository.save(stored)
        }
    }

    /** Отозвать все активные refresh-токены пользователя (reuse-detection, смена пароля). */
    @Transactional
    fun revokeAllForUser(userId: java.util.UUID) {
        val now = Instant.now()
        refreshTokenRepository.findAllByUserIdAndRevokedAtIsNull(userId).forEach {
            it.revokedAt = now
            refreshTokenRepository.save(it)
        }
    }

    private fun hash(rawToken: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        return digest.digest(rawToken.toByteArray()).joinToString("") { "%02x".format(it) }
    }
}
