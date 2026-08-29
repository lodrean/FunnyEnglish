package com.sotospeak.controller

import com.sotospeak.entity.Message
import com.sotospeak.entity.MessageType
import com.sotospeak.repository.MessageRepository
import com.sotospeak.repository.UserRepository
import com.sotospeak.security.UserPrincipal
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*
import java.time.Instant
import java.util.UUID

// ==================== DTO ====================

data class SendMessageRequest(
    @field:NotBlank(message = "Текст сообщения обязателен")
    @field:Size(max = 2000, message = "Сообщение не длиннее 2000 символов")
    val text: String,

    val type: MessageType = MessageType.MESSAGE,

    /** Для COMMENT: к какому тесту привязан комментарий */
    val testId: UUID? = null
)

data class MessageResponse(
    val id: UUID,
    val senderId: UUID,
    val senderName: String,
    val recipientId: UUID,
    val text: String,
    val type: MessageType,
    val testId: UUID?,
    val createdAt: Instant,
    val readAt: Instant?
) {
    companion object {
        fun from(m: Message) = MessageResponse(
            id = m.id,
            senderId = m.sender.id,
            senderName = m.sender.displayName,
            recipientId = m.recipient.id,
            text = m.text,
            type = m.type,
            testId = m.testId,
            createdAt = m.createdAt,
            readAt = m.readAt
        )
    }
}

// ==================== Admin: отправка сообщений ученикам ====================

@RestController
@RequestMapping("/admin/users")
class AdminMessageController(
    private val messageRepository: MessageRepository,
    private val userRepository: UserRepository
) {
    /** Отправить сообщение/комментарий ученику */
    @PostMapping("/{userId}/messages")
    fun sendMessage(
        @PathVariable userId: UUID,
        @AuthenticationPrincipal principal: UserPrincipal,
        @Valid @RequestBody request: SendMessageRequest
    ): ResponseEntity<MessageResponse> {
        val sender = userRepository.findById(UUID.fromString(principal.userId))
            .orElseThrow { NoSuchElementException("Sender not found") }
        val recipient = userRepository.findById(userId)
            .orElseThrow { NoSuchElementException("User not found: $userId") }

        val message = messageRepository.save(
            Message(
                sender = sender,
                recipient = recipient,
                text = request.text.trim(),
                type = request.type,
                testId = request.testId
            )
        )
        return ResponseEntity.ok(MessageResponse.from(message))
    }

    /** История сообщений, отправленных ученику */
    @GetMapping("/{userId}/messages")
    @Transactional(readOnly = true)
    fun getMessagesForUser(@PathVariable userId: UUID): ResponseEntity<List<MessageResponse>> {
        val messages = messageRepository.findByRecipientIdOrderByCreatedAtDesc(userId)
        return ResponseEntity.ok(messages.map { MessageResponse.from(it) })
    }
}

// ==================== User: inbox ученика ====================

@RestController
@RequestMapping("/users/me/messages")
class UserMessageController(
    private val messageRepository: MessageRepository
) {
    /** Входящие сообщения ученика */
    @GetMapping
    @Transactional(readOnly = true)
    fun getInbox(@AuthenticationPrincipal principal: UserPrincipal): ResponseEntity<List<MessageResponse>> {
        val messages = messageRepository.findByRecipientIdOrderByCreatedAtDesc(
            UUID.fromString(principal.userId)
        )
        return ResponseEntity.ok(messages.map { MessageResponse.from(it) })
    }

    /** Количество непрочитанных (для бейджа) */
    @GetMapping("/unread-count")
    fun getUnreadCount(@AuthenticationPrincipal principal: UserPrincipal): ResponseEntity<Map<String, Long>> {
        val count = messageRepository.countByRecipientIdAndReadAtIsNull(
            UUID.fromString(principal.userId)
        )
        return ResponseEntity.ok(mapOf("count" to count))
    }

    /** Пометить прочитанным */
    @PostMapping("/{messageId}/read")
    @Transactional
    fun markAsRead(
        @PathVariable messageId: UUID,
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<MessageResponse> {
        val message = messageRepository.findById(messageId)
            .orElseThrow { NoSuchElementException("Message not found: $messageId") }

        val userId = UUID.fromString(principal.userId)
        if (message.recipient.id != userId) {
            throw IllegalArgumentException("Not your message")
        }

        if (message.readAt == null) {
            message.readAt = Instant.now()
            messageRepository.save(message)
        }
        return ResponseEntity.ok(MessageResponse.from(message))
    }
}
