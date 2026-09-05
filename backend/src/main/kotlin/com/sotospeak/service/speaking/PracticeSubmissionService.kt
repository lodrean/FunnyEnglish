package com.sotospeak.service.speaking

import com.sotospeak.dto.AdminSubmissionResponse
import com.sotospeak.dto.GradeResponse
import com.sotospeak.dto.GradeSubmissionRequest
import com.sotospeak.dto.SubmissionResponse
import com.sotospeak.dto.toAdminResponse
import com.sotospeak.dto.toResponse
import com.sotospeak.entity.speaking.Grade
import com.sotospeak.entity.speaking.PracticeSubmission
import com.sotospeak.entity.speaking.SubmissionStatus
import com.sotospeak.exception.DuplicateSubmissionException
import com.sotospeak.repository.UserRepository
import com.sotospeak.repository.speaking.GradeRepository
import com.sotospeak.repository.speaking.PracticeSubmissionRepository
import com.sotospeak.repository.speaking.TopicRepository
import com.sotospeak.service.EmailService
import com.sotospeak.service.MediaUrlService
import com.sotospeak.service.StorageService
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.time.Instant
import java.util.UUID

/**
 * Speaking Trainer: practice-записи учеников + grading (Part 1 §6.2, §6.3).
 * Порядок upload: валидация → MinIO → INSERT (файл-сирота при падении INSERT допустим).
 */
@Service
@Transactional
class PracticeSubmissionService(
    private val submissionRepository: PracticeSubmissionRepository,
    private val gradeRepository: GradeRepository,
    private val topicRepository: TopicRepository,
    private val userRepository: UserRepository,
    private val storageService: StorageService,
    private val mediaUrlService: MediaUrlService,
    private val emailService: EmailService
) {
    @PersistenceContext
    private lateinit var entityManager: EntityManager

    companion object {
        const val MAX_AUDIO_SIZE_BYTES = 5L * 1024 * 1024 // 5 МБ (PRD: ~1–2 МБ на запись)
        const val MAX_DURATION_SEC = 60                    // клиент шлёт ~30, допуск с запасом
    }

    // ============== User: submissions ==============

    @Transactional
    fun createSubmission(userId: UUID, topicId: UUID, durationSec: Int, file: MultipartFile): SubmissionResponse {
        // 1. Топик должен существовать, быть опубликован и не удалён → иначе 404
        val topic = topicRepository.findByIdAndIsPublishedTrueAndDeletedAtIsNull(topicId)
            .orElseThrow { NoSuchElementException("Topic not found") }

        // 1a. Одна попытка Practice на топик (Part 2 §2.6) — дубли отклоняем на уровне backend
        submissionRepository.findFirstByUserIdAndTopicId(userId, topicId)?.let {
            throw DuplicateSubmissionException("Practice submission already exists for this topic")
        }

        // 2. Валидация ДО upload → 400
        require(durationSec in 1..MAX_DURATION_SEC) {
            "durationSec must be between 1 and $MAX_DURATION_SEC"
        }
        require(file.size <= MAX_AUDIO_SIZE_BYTES) {
            "Audio file too large (max 5 MB)"
        }
        // 2b. Magic-bytes: контент должен соответствовать заявленному аудио-формату
        // (bd FunnyEnglish-nj2.8 — расширение/content-type подделываются тривиально).
        // Расширение/content-type дополнительно проверит StorageService.uploadFile → 400.
        require(AudioSignatureValidator.isAllowedAudio(AudioSignatureValidator.readHeader(file.inputStream))) {
            "Audio content does not match allowed formats (m4a/aac/mp3/wav/ogg)"
        }

        // 3. Upload в MinIO (публичный URL из S3_PUBLIC_URL, BUG-004)
        val audioUrl = storageService.uploadFile(file, "speaking/submissions/u_$userId")

        // 4. INSERT (saveAndFlush — сразу в БД, чтобы UNIQUE отработал внутри try)
        val submission = PracticeSubmission(
            user = userRepository.getReferenceById(userId),
            topic = topic,
            audioUrl = audioUrl,
            durationSec = durationSec,
            status = SubmissionStatus.NEW
        )
        return try {
            submissionRepository.saveAndFlush(submission).toResponse().normalized()
        } catch (e: DataIntegrityViolationException) {
            // Fallback на race двух параллельных POST: UNIQUE (user_id, topic_id) (V25) → тот же 409-гейт
            throw DuplicateSubmissionException("Practice submission already exists for this topic")
        }
    }

    @Transactional(readOnly = true)
    fun getMySubmissions(userId: UUID): List<SubmissionResponse> =
        submissionRepository.findByUserIdWithGrade(userId)
            .map { it.toResponse().normalized() }

    // ============== Admin: grading inbox ==============

    @Transactional(readOnly = true)
    fun searchSubmissions(
        status: SubmissionStatus?,
        userId: UUID?,
        topicId: UUID?,
        dateFrom: Instant?,
        dateTo: Instant?,
        pageable: Pageable
    ): Page<AdminSubmissionResponse> =
        submissionRepository.search(status, userId, topicId, dateFrom, dateTo, pageable)
            .map { it.toAdminResponse().normalized() }

    /** Детали записи для admin deep-link (Part 3 §3.3) — без выборки страницы inbox */
    @Transactional(readOnly = true)
    fun getSubmission(id: UUID): AdminSubmissionResponse =
        submissionRepository.findByIdWithDetails(id)
            .orElseThrow { NoSuchElementException("Submission not found") }
            .toAdminResponse().normalized()

    /** Счётчик записей по статусу (badge «NEW» в сайдбаре, Part 3 §3.3); status=null → все */
    @Transactional(readOnly = true)
    fun countSubmissions(status: SubmissionStatus?): Long =
        status?.let { submissionRepository.countByStatus(it) } ?: submissionRepository.count()

    // ============== Admin: grading ==============

    fun gradeSubmission(submissionId: UUID, request: GradeSubmissionRequest, reviewerId: UUID): GradeResponse {
        val submission = submissionRepository.findByIdWithDetails(submissionId)
            .orElseThrow { NoSuchElementException("Submission not found") }
        require(submission.grade == null) { "Submission already graded; use PUT to edit" }

        val grade = Grade(
            submission = submission,
            grammar = request.grammar,
            vocabulary = request.vocabulary,
            pronunciation = request.pronunciation,
            fluency = request.fluency,
            comment = request.comment,
            reviewer = userRepository.getReferenceById(reviewerId)
        )
        submission.grade = grade
        submission.status = SubmissionStatus.REVIEWED // статус-машина NEW → REVIEWED, в той же транзакции
        // grade — новая сущность: save = persist → тот же инстанс становится managed
        // (saveAndFlush(submission) был бы merge → refresh на нашем инстансе падал бы «Entity not managed»)
        gradeRepository.saveAndFlush(grade)
        submissionRepository.save(submission)
        // total — generated column: refresh, чтобы подтянуть вычисленное в БД значение
        entityManager.refresh(grade)
        // Уведомление ученику «Ваша запись проверена» (bd h3l.1; метрика PRD «REVIEWED за 48ч»).
        // Только первичный grading (POST); editGrade (PUT) письмо не шлёт, чтобы не спамить при правках.
        // @Async + runCatching внутри EmailService — сбой SMTP не откатывает grading.
        submission.user?.let { student ->
            emailService.sendSubmissionReviewedEmail(
                toEmail = student.email,
                displayName = student.displayName,
                topicTitle = submission.topic?.title ?: "",
                total = grade.total
            )
        }
        return grade.toResponse()
    }

    fun editGrade(submissionId: UUID, request: GradeSubmissionRequest, reviewerId: UUID): GradeResponse {
        val grade = gradeRepository.findBySubmissionId(submissionId)
            .orElseThrow { NoSuchElementException("Grade not found") }
        grade.grammar = request.grammar
        grade.vocabulary = request.vocabulary
        grade.pronunciation = request.pronunciation
        grade.fluency = request.fluency
        grade.comment = request.comment
        grade.reviewer = userRepository.getReferenceById(reviewerId)
        // статус НЕ трогаем — остаётся REVIEWED; updatedAt проставит @UpdateTimestamp (аудит)
        gradeRepository.saveAndFlush(grade)
        // total — generated column: refresh после пересчёта в БД
        entityManager.refresh(grade)
        return grade.toResponse()
    }

    // ============== Helpers ==============

    private fun SubmissionResponse.normalized() =
        copy(audioUrl = mediaUrlService.normalize(audioUrl) ?: audioUrl)

    private fun AdminSubmissionResponse.normalized() =
        copy(audioUrl = mediaUrlService.normalize(audioUrl) ?: audioUrl)
}
