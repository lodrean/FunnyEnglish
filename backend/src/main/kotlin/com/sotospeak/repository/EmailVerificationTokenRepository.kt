package com.sotospeak.repository

import com.sotospeak.entity.EmailVerificationToken
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface EmailVerificationTokenRepository : JpaRepository<EmailVerificationToken, UUID> {
    fun findByToken(token: String): EmailVerificationToken?

    /** Инвалидация неподтверждённых токенов пользователя (resend выдаёт новый). */
    @Modifying
    @Query("DELETE FROM EmailVerificationToken t WHERE t.userId = :userId AND t.confirmedAt IS NULL")
    fun deleteUnconfirmedByUserId(@Param("userId") userId: UUID)
}
