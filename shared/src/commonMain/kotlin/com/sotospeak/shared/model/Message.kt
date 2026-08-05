package com.sotospeak.shared.model

import kotlinx.serialization.Serializable

@Serializable
data class Message(
    val id: String,
    val senderId: String,
    val senderName: String,
    val recipientId: String,
    val text: String,
    val type: MessageType = MessageType.MESSAGE,
    val testId: String? = null,
    val createdAt: String,
    val readAt: String? = null
)

@Serializable
enum class MessageType {
    MESSAGE,  // обычное сообщение от учителя
    COMMENT   // комментарий к результату теста
}

@Serializable
data class UnreadCountResponse(
    val count: Long
)
