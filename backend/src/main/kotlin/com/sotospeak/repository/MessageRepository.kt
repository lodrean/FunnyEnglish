package com.sotospeak.repository

import com.sotospeak.entity.Message
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface MessageRepository : JpaRepository<Message, UUID> {
    fun findByRecipientIdOrderByCreatedAtDesc(recipientId: UUID): List<Message>
    fun findBySenderIdOrderByCreatedAtDesc(senderId: UUID): List<Message>
    fun countByRecipientIdAndReadAtIsNull(recipientId: UUID): Long
}
