package com.sotospeak.controller.speaking

import com.sotospeak.dto.*
import com.sotospeak.entity.speaking.SubmissionStatus
import com.sotospeak.security.UserPrincipal
import com.sotospeak.service.speaking.PracticeSubmissionService
import com.sotospeak.service.speaking.SpeakingContentService
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.UUID

/**
 * Admin API speaking-тренажёра: CRUD контента + grading inbox/оценки.
 * Маппинг БЕЗ /api — context-path добавляет его сам (Part 1 §1.2).
 */
@RestController
@RequestMapping("/admin/speaking")
@PreAuthorize("hasRole('ADMIN')")
class SpeakingAdminController(
    private val contentService: SpeakingContentService,
    private val submissionService: PracticeSubmissionService
) {

    // ============== Libraries ==============

    @GetMapping("/libraries")
    fun getLibraries(): ResponseEntity<List<AdminLibraryResponse>> =
        ResponseEntity.ok(contentService.getAllLibraries())

    @PostMapping("/libraries")
    fun createLibrary(
        @Valid @RequestBody request: CreateLibraryRequest
    ): ResponseEntity<AdminLibraryResponse> =
        ResponseEntity.status(HttpStatus.CREATED).body(contentService.createLibrary(request))

    @PutMapping("/libraries/{id}")
    fun updateLibrary(
        @PathVariable id: UUID,
        @Valid @RequestBody request: UpdateLibraryRequest
    ): ResponseEntity<AdminLibraryResponse> =
        ResponseEntity.ok(contentService.updateLibrary(id, request))

    @DeleteMapping("/libraries/{id}")
    fun deleteLibrary(@PathVariable id: UUID): ResponseEntity<Void> {
        contentService.deleteLibrary(id)
        return ResponseEntity.noContent().build()
    }

    // ============== Topics ==============

    @GetMapping("/topics")
    fun getTopics(@RequestParam libraryId: UUID): ResponseEntity<List<AdminTopicResponse>> =
        ResponseEntity.ok(contentService.getTopics(libraryId))

    /** Детали топика (включая черновики и soft-deleted) — deep-link без N+1 (Part 3 §3.3) */
    @GetMapping("/topics/{id}")
    fun getTopic(@PathVariable id: UUID): ResponseEntity<AdminTopicResponse> =
        ResponseEntity.ok(contentService.getTopic(id))

    /** Точечный publish/unpublish без полного PUT (Part 3 §3.3) */
    @PatchMapping("/libraries/{id}/publish")
    fun publishLibrary(
        @PathVariable id: UUID,
        @Valid @RequestBody request: PublishRequest
    ): ResponseEntity<AdminLibraryResponse> =
        ResponseEntity.ok(contentService.publishLibrary(id, request.isPublished))

    @PatchMapping("/topics/{id}/publish")
    fun publishTopic(
        @PathVariable id: UUID,
        @Valid @RequestBody request: PublishRequest
    ): ResponseEntity<AdminTopicResponse> =
        ResponseEntity.ok(contentService.publishTopic(id, request.isPublished))

    @PostMapping("/topics")
    fun createTopic(
        @Valid @RequestBody request: CreateTopicRequest
    ): ResponseEntity<AdminTopicResponse> =
        ResponseEntity.status(HttpStatus.CREATED).body(contentService.createTopic(request))

    @PutMapping("/topics/{id}")
    fun updateTopic(
        @PathVariable id: UUID,
        @Valid @RequestBody request: UpdateTopicRequest
    ): ResponseEntity<AdminTopicResponse> =
        ResponseEntity.ok(contentService.updateTopic(id, request))

    /** Soft delete, идемпотентно */
    @DeleteMapping("/topics/{id}")
    fun deleteTopic(@PathVariable id: UUID): ResponseEntity<Void> {
        contentService.deleteTopic(id)
        return ResponseEntity.noContent().build()
    }

    // ============== Video (upsert) ==============

    @PutMapping("/topics/{id}/video")
    fun upsertVideo(
        @PathVariable id: UUID,
        @Valid @RequestBody request: UpsertVideoRequest
    ): ResponseEntity<AdminTopicResponse> =
        ResponseEntity.ok(contentService.upsertVideo(id, request))

    // ============== Questions ==============

    @PostMapping("/topics/{id}/questions")
    fun addQuestion(
        @PathVariable id: UUID,
        @Valid @RequestBody request: CreateSpeakingQuestionRequest
    ): ResponseEntity<SpeakingQuestionResponse> =
        ResponseEntity.status(HttpStatus.CREATED).body(contentService.addQuestion(id, request))

    @PutMapping("/questions/{id}")
    fun updateQuestion(
        @PathVariable id: UUID,
        @Valid @RequestBody request: CreateSpeakingQuestionRequest
    ): ResponseEntity<SpeakingQuestionResponse> =
        ResponseEntity.ok(contentService.updateQuestion(id, request))

    @DeleteMapping("/questions/{id}")
    fun deleteQuestion(@PathVariable id: UUID): ResponseEntity<Void> {
        contentService.deleteQuestion(id)
        return ResponseEntity.noContent().build()
    }

    /** Batch-reorder вопросов топика: полный упорядоченный список id (Part 3 §3.2) */
    @PostMapping("/topics/{id}/questions/reorder")
    fun reorderQuestions(
        @PathVariable id: UUID,
        @Valid @RequestBody request: ReorderSpeakingQuestionsRequest
    ): ResponseEntity<Void> {
        contentService.reorderQuestions(id, request.questionIds)
        return ResponseEntity.noContent().build()
    }

    // ============== Grading inbox ============== 

    /** Счётчик записей по статусу (badge «NEW» в сайдбаре, Part 3 §3.3) */
    @GetMapping("/submissions/count")
    fun getSubmissionsCount(
        @RequestParam(required = false) status: SubmissionStatus?
    ): ResponseEntity<SubmissionCountResponse> =
        ResponseEntity.ok(SubmissionCountResponse(submissionService.countSubmissions(status)))

    /** Детали записи — deep-link `/grading/submissions/:id` без страницы inbox (Part 3 §3.3) */
    @GetMapping("/submissions/{id}")
    fun getSubmission(@PathVariable id: UUID): ResponseEntity<AdminSubmissionResponse> =
        ResponseEntity.ok(submissionService.getSubmission(id))

    @GetMapping("/submissions")
    fun getSubmissions(
        @RequestParam(required = false) status: SubmissionStatus?,
        @RequestParam(required = false) userId: UUID?,
        @RequestParam(required = false) topicId: UUID?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) dateFrom: LocalDate?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) dateTo: LocalDate?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<Page<AdminSubmissionResponse>> {
        // сортировка зашита в JPQL (createdAt DESC) — pageable без Sort; size ≤ 100 (§7.5)
        val pageable = PageRequest.of(page, size.coerceIn(1, 100))
        // dateTo включительно → «следующий день 00:00» (§6.5)
        val dateFromInstant = dateFrom?.atStartOfDay()?.toInstant(ZoneOffset.UTC)
        val dateToInstant = dateTo?.plusDays(1)?.atStartOfDay()?.toInstant(ZoneOffset.UTC)
        return ResponseEntity.ok(
            submissionService.searchSubmissions(status, userId, topicId, dateFromInstant, dateToInstant, pageable)
        )
    }

    @PostMapping("/submissions/{id}/grade")
    fun gradeSubmission(
        @PathVariable id: UUID,
        @Valid @RequestBody request: GradeSubmissionRequest,
        @AuthenticationPrincipal userPrincipal: UserPrincipal
    ): ResponseEntity<GradeResponse> =
        ResponseEntity.status(HttpStatus.CREATED).body(
            submissionService.gradeSubmission(id, request, UUID.fromString(userPrincipal.userId))
        )

    @PutMapping("/submissions/{id}/grade")
    fun editGrade(
        @PathVariable id: UUID,
        @Valid @RequestBody request: GradeSubmissionRequest,
        @AuthenticationPrincipal userPrincipal: UserPrincipal
    ): ResponseEntity<GradeResponse> =
        ResponseEntity.ok(
            submissionService.editGrade(id, request, UUID.fromString(userPrincipal.userId))
        )
}
