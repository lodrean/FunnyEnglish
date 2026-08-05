package com.sotospeak.controller.speaking

import com.sotospeak.dto.LibraryResponse
import com.sotospeak.dto.TopicDetailResponse
import com.sotospeak.dto.TopicListItemResponse
import com.sotospeak.service.speaking.SpeakingContentService
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
