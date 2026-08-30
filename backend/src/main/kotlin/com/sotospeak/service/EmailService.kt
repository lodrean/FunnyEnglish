package com.sotospeak.service

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import java.math.BigDecimal

/**
 * Отправка писем через SMTP (OpenSpec add-email-verification).
 * Ошибки отправки логируются и НЕ откатывают регистрацию — пользователь может запросить resend.
 */
@Service
class EmailService(
    private val mailSender: JavaMailSender,
    @Value("\${app.mail-from:noreply@sotospeak.local}") private val mailFrom: String
) {
    private val logger = LoggerFactory.getLogger(EmailService::class.java)

    @Async
    fun sendVerificationEmail(toEmail: String, displayName: String, verificationUrl: String) {
        runCatching {
            val message = SimpleMailMessage().apply {
                from = mailFrom
                setTo(toEmail)
                subject = "So to speak — подтверди почту"
                text = """
                    Привет, $displayName!

                    Подтверди свой email, чтобы войти в So to speak:
                    $verificationUrl

                    Ссылка действует 24 часа. Если ты не регистрировался — просто игнорируй это письмо.
                """.trimIndent()
            }
            mailSender.send(message)
            logger.info("Verification email sent to {}", toEmail)
        }.onFailure {
            logger.error("Failed to send verification email to {}: {}", toEmail, it.message, it)
        }
    }

    /**
     * Уведомление ученику «Ваша запись проверена» (bd h3l.1; метрика PRD «REVIEWED за 48ч»).
     * Ошибки отправки логируются и НЕ откатывают grading — оценка уже сохранена в той же транзакции.
     */
    @Async
    fun sendSubmissionReviewedEmail(toEmail: String, displayName: String, topicTitle: String, total: BigDecimal?) {
        runCatching {
            val totalLine = total?.let { "\n\nСредний балл: ${it.stripTrailingZeros().toPlainString()} из 10." } ?: ""
            val message = SimpleMailMessage().apply {
                from = mailFrom
                setTo(toEmail)
                subject = "So to speak — ваша запись проверена"
                text = """
                    Привет, $displayName!

                    Учитель проверил вашу запись по теме «$topicTitle».$totalLine

                    Откройте раздел «Мои записи» в приложении, чтобы увидеть оценку и комментарий учителя.
                """.trimIndent()
            }
            mailSender.send(message)
            logger.info("Submission reviewed email sent to {}", toEmail)
        }.onFailure {
            logger.error("Failed to send submission reviewed email to {}: {}", toEmail, it.message, it)
        }
    }
}
