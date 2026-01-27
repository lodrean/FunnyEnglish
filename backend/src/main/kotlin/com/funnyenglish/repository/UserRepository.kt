package com.funnyenglish.repository

import com.funnyenglish.entity.AuthProvider
import com.funnyenglish.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface UserRepository : JpaRepository<User, UUID> {
    fun findByEmail(email: String): User?
    fun findByAuthProviderAndProviderId(provider: AuthProvider, providerId: String): User?
    fun existsByEmail(email: String): Boolean

    @Query("SELECT u FROM User u ORDER BY u.totalPoints DESC LIMIT :limit")
    fun findTopByTotalPoints(limit: Int): List<User>

    @Query("""
        SELECT u FROM User u
        WHERE u.totalPoints > (SELECT u2.totalPoints FROM User u2 WHERE u2.id = :userId)
        ORDER BY u.totalPoints ASC
        LIMIT 1
    """)
    fun findUserAbove(userId: UUID): User?

    @Query("""
        SELECT u FROM User u
        WHERE u.totalPoints < (SELECT u2.totalPoints FROM User u2 WHERE u2.id = :userId)
        ORDER BY u.totalPoints DESC
        LIMIT 1
    """)
    fun findUserBelow(userId: UUID): User?
}
