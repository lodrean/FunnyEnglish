package com.sotospeak.entity

import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "messages")
data class Message(
    @Id
    val id: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    val sender: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false)
    val recipient: User,

    @Column(nullable = false, columnDefinition = "TEXT")
    val text: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val type: MessageType = MessageType.MESSAGE,

    @Column(name = "test_id")
    val testId: UUID? = null,

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now(),

    @Column(name = "read_at")
    var readAt: Instant? = null
)

enum class MessageType {
    MESSAGE,  // обычное сообщение ученику
    COMMENT   // комментарий к результату теста
}
