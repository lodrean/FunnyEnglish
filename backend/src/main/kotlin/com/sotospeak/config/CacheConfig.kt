package com.sotospeak.config

import com.github.benmanes.caffeine.cache.Caffeine
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.caffeine.CaffeineCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.TimeUnit

/** Имена кэшей публичного speaking-контента (bd FunnyEnglish-wy7.7, §4.3.3). */
const val SPEAKING_PUBLIC_LIBRARIES = "speakingPublicLibraries"
const val SPEAKING_PUBLIC_TOPICS = "speakingPublicTopics"
const val SPEAKING_PUBLIC_TOPIC_DETAILS = "speakingPublicTopicDetails"

/**
 * Инвалидация публичного speaking-кэша: любые admin-мутации контента
 * (publish/unpublish, CRUD, reorder, upsert video) сбрасывают все три кэша.
 * TTL в CacheConfig — лишь страховка, первичный механизм свежести — эта аннотация.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@CacheEvict(
    cacheNames = [SPEAKING_PUBLIC_LIBRARIES, SPEAKING_PUBLIC_TOPICS, SPEAKING_PUBLIC_TOPIC_DETAILS],
    allEntries = true
)
annotation class EvictSpeakingPublicCache

@Configuration
@EnableCaching
class CacheConfig {

    @Bean
    fun cacheManager(): CacheManager {
        return CaffeineCacheManager().apply {
            // Configure individual caches
            registerCache("categories", buildCache(100, 1, TimeUnit.HOURS))
            registerCache("tests", buildCache(200, 30, TimeUnit.MINUTES))
            registerCache("userProfiles", buildCache(1000, 5, TimeUnit.MINUTES))
            registerCache("leaderboard", buildCache(10, 1, TimeUnit.MINUTES))
            registerCache("testDetails", buildCache(500, 15, TimeUnit.MINUTES))
            // Публичный speaking-контент (bd FunnyEnglish-wy7.7): инвалидируется при
            // publish/мутациях через @EvictSpeakingPublicCache, TTL — страховка.
            registerCache(SPEAKING_PUBLIC_LIBRARIES, buildCache(maxSize = 10, duration = 10, unit = TimeUnit.MINUTES))
            registerCache(SPEAKING_PUBLIC_TOPICS, buildCache(maxSize = 500, duration = 10, unit = TimeUnit.MINUTES))
            registerCache(
                SPEAKING_PUBLIC_TOPIC_DETAILS,
                buildCache(maxSize = 2000, duration = 10, unit = TimeUnit.MINUTES)
            )
        }
    }

    private fun buildCache(
        maxSize: Long,
        duration: Long,
        unit: TimeUnit
    ): com.github.benmanes.caffeine.cache.Cache<Any, Any> {
        return Caffeine.newBuilder()
            .maximumSize(maxSize)
            .expireAfterWrite(duration, unit)
            .recordStats()
            .build()
    }

    private fun CaffeineCacheManager.registerCache(
        name: String,
        cache: com.github.benmanes.caffeine.cache.Cache<Any, Any>
    ) {
        registerCustomCache(name, cache)
    }
}
