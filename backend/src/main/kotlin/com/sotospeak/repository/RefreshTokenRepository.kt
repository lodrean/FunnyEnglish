package com.sotospeak.repository

import com.sotospeak.entity.RefreshToken
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface RefreshTokenRepository : JpaRepository<RefreshToken, UUID> {
    fun findByJti(jti: String): RefreshToken?
    fun findAllByUserIdAndRevokedAtIsNull(userId: UUID): List<RefreshToken>
}
