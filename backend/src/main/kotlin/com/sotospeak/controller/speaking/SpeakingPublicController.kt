package com.sotospeak.controller.speaking

import com.sotospeak.dto.LibraryResponse
import com.sotospeak.dto.TopicDetailResponse
import com.sotospeak.dto.TopicListItemResponse
import com.sotospeak.service.speaking.SpeakingContentService
import org.springframework.http.CacheControl
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.context.request.WebRequest
import java.util.UUID
import java.util.concurrent.TimeUnit

/**
 * Public API speaking-тренажёра (гость может читать контент).
 * Маппинг БЕЗ /api — context-path добавляет его сам (Part 1 §1.2).
 *
 * HTTP-кэш (bd FunnyEnglish-wy7.7, §4.3.3): ETag + Cache-Control: public, max-age=60
 * (предложение Part 1 §7.2). Контент почти всегда отдаётся из Caffeine-кэша
 * сервиса, поэтому ETag считается дёшево по хэшу тела; If-None-Match → 304 без тела.
 */
@RestController
@RequestMapping("/public/speaking")
class SpeakingPublicController(
    private val contentService: SpeakingContentService
) {

    // bd wy7.6: аддитивные limit/offset (необязательные; без них — полный список, контракт не ломает-
    // ся). Контент учительский (десятки записей), ETag+кэш ограничивают нагрузку; параметры — задел
    // на рост библиотеки.
    @GetMapping("/libraries")
    fun getLibraries(
        webRequest: WebRequest,
        @RequestParam(required = false) limit: Int?,
        @RequestParam(required = false) offset: Int?,
    ): ResponseEntity<List<LibraryResponse>> =
        cached(applySlice(contentService.getPublishedLibraries(), offset, limit), webRequest)

    @GetMapping("/libraries/{id}/topics")
    fun getTopics(
        webRequest: WebRequest,
        @PathVariable id: UUID,
        @RequestParam(required = false) limit: Int?,
        @RequestParam(required = false) offset: Int?,
    ): ResponseEntity<List<TopicListItemResponse>> =
        cached(applySlice(contentService.getPublishedTopics(id), offset, limit), webRequest)

    private fun <T> applySlice(list: List<T>, offset: Int?, limit: Int?): List<T> {
        val from = (offset ?: 0).coerceAtLeast(0)
        val to = limit?.takeIf { it >= 0 }
            ?.let { (from + it).coerceAtMost(list.size) }
            ?: list.size
        return if (from in list.indices && to > from) list.subList(from, to) else emptyList()
    }

    @GetMapping("/topics/{id}")
    fun getTopic(webRequest: WebRequest, @PathVariable id: UUID): ResponseEntity<TopicDetailResponse> =
        cached(contentService.getTopicDetail(id), webRequest)

    /**
     * ETag = хэш DTO (data class → стабилен для равного содержимого).
     * Коллизия хэша теоретически возможна — принятый риск для публичного read-only контента.
     */
    private fun <T : Any> cached(body: T, webRequest: WebRequest): ResponseEntity<T> {
        val etag = "\"${Integer.toHexString(body.hashCode())}\""
        if (webRequest.checkNotModified(etag)) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED)
                .cacheControl(PUBLIC_CACHE)
                .eTag(etag)
                .build()
        }
        return ResponseEntity.ok()
            .cacheControl(PUBLIC_CACHE)
            .eTag(etag)
            .body(body)
    }

    private companion object {
        val PUBLIC_CACHE: CacheControl = CacheControl.maxAge(60, TimeUnit.SECONDS).cachePublic()
    }
}
