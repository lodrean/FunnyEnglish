package com.funnyenglish.controller.speaking

import com.funnyenglish.dto.LibraryResponse
import com.funnyenglish.dto.TopicDetailResponse
import com.funnyenglish.dto.TopicListItemResponse
import com.funnyenglish.service.speaking.SpeakingContentService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

/**
 * Public API speaking-тренажёра (гость может читать контент).
 * Маппинг БЕЗ /api — context-path добавляет его сам (Part 1 §1.2).
 */
@RestController
@RequestMapping("/public/speaking")
class SpeakingPublicController(
    private val contentService: SpeakingContentService
) {

    @GetMapping("/libraries")
    fun getLibraries(): ResponseEntity<List<LibraryResponse>> =
        ResponseEntity.ok(contentService.getPublishedLibraries())

    @GetMapping("/libraries/{id}/topics")
    fun getTopics(@PathVariable id: UUID): ResponseEntity<List<TopicListItemResponse>> =
        ResponseEntity.ok(contentService.getPublishedTopics(id))

    @GetMapping("/topics/{id}")
    fun getTopic(@PathVariable id: UUID): ResponseEntity<TopicDetailResponse> =
        ResponseEntity.ok(contentService.getTopicDetail(id))
}
