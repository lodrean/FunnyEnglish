package com.funnyenglish.config

import com.github.benmanes.caffeine.cache.Caffeine
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.caffeine.CaffeineCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.TimeUnit

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
