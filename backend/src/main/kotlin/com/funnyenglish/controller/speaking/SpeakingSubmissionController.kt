package com.funnyenglish.controller.speaking

import com.funnyenglish.dto.SubmissionResponse
import com.funnyenglish.security.UserPrincipal
import com.funnyenglish.service.speaking.PracticeSubmissionService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import java.util.UUID

/**
 * User API speaking-тренажёра: practice-записи (только авторизованным).
 * Маппинг БЕЗ /api — context-path добавляет его сам (Part 1 §1.2).
 */
@RestController
@RequestMapping("/speaking")
class SpeakingSubmissionController(
    private val submissionService: PracticeSubmissionService
) {

    @PostMapping("/submissions", consumes = ["multipart/form-data"])
    fun createSubmission(
        @RequestParam("file") file: MultipartFile,
        @RequestParam("topicId") topicId: UUID,
        @RequestParam("durationSec") durationSec: Int,
        @AuthenticationPrincipal userPrincipal: UserPrincipal
    ): ResponseEntity<SubmissionResponse> =
        ResponseEntity.status(HttpStatus.CREATED).body(
            submissionService.createSubmission(
                userId = UUID.fromString(userPrincipal.userId),
                topicId = topicId,
                durationSec = durationSec,
                file = file
            )
        )

    @GetMapping("/submissions/my")
    fun getMySubmissions(
        @AuthenticationPrincipal userPrincipal: UserPrincipal
    ): ResponseEntity<List<SubmissionResponse>> =
        ResponseEntity.ok(
            submissionService.getMySubmissions(UUID.fromString(userPrincipal.userId))
        )
}
