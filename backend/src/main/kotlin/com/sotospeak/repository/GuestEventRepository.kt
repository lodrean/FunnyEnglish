package com.sotospeak.repository

import com.sotospeak.entity.GuestEvent
import com.sotospeak.entity.GuestEventType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.UUID

@Repository
interface GuestEventRepository : JpaRepository<GuestEvent, UUID> {

    @Query("SELECT COUNT(DISTINCT e.anonymousId) FROM GuestEvent e")
    fun countDistinctGuests(): Long

    @Query("SELECT COUNT(DISTINCT e.anonymousId) FROM GuestEvent e WHERE e.createdAt >= :since")
    fun countDistinctGuestsActiveSince(@Param("since") since: Instant): Long

    fun countByType(type: GuestEventType): Long

    @Query("SELECT COUNT(DISTINCT e.anonymousId) FROM GuestEvent e WHERE e.convertedUserId IS NOT NULL")
    fun countDistinctConvertedGuests(): Long

    /** Пометить все события гостя как конвертированные (при регистрации) */
    @Modifying
    @Query("UPDATE GuestEvent e SET e.convertedUserId = :userId WHERE e.anonymousId = :anonymousId AND e.convertedUserId IS NULL")
    fun markConverted(@Param("anonymousId") anonymousId: UUID, @Param("userId") userId: UUID): Int
}
