package com.funnyenglish.controller

import com.github.benmanes.caffeine.cache.Cache
import org.springframework.cache.CacheManager
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/admin/cache")
@PreAuthorize("hasRole('ADMIN')")
class CacheAdminController(
    private val cacheManager: CacheManager
) {

    /**
     * Get cache statistics for all caches
     */
    @GetMapping("/stats")
    fun getCacheStats(): ResponseEntity<Map<String, CacheStats>> {
        val stats = mutableMapOf<String, CacheStats>()
        
        cacheManager.cacheNames.forEach { cacheName ->
            val cache = cacheManager.getCache(cacheName)
            val nativeCache = cache?.nativeCache as? Cache<*, *>
            
            if (nativeCache != null) {
                val statsObj = nativeCache.stats()
                stats[cacheName] = CacheStats(
                    hitCount = statsObj.hitCount(),
                    missCount = statsObj.missCount(),
                    hitRate = statsObj.hitRate(),
                    evictionCount = statsObj.evictionCount(),
                    size = nativeCache.estimatedSize()
                )
            }
        }
        
        return ResponseEntity.ok(stats)
    }

    /**
     * Get cache statistics for specific cache
     */
    @GetMapping("/stats/{cacheName}")
    fun getCacheStats(@PathVariable cacheName: String): ResponseEntity<CacheStats> {
        val cache = cacheManager.getCache(cacheName)
            ?: return ResponseEntity.notFound().build()
        
        val nativeCache = cache.nativeCache as? Cache<*, *>
            ?: return ResponseEntity.badRequest().build()
        
        val statsObj = nativeCache.stats()
        return ResponseEntity.ok(
            CacheStats(
                hitCount = statsObj.hitCount(),
                missCount = statsObj.missCount(),
                hitRate = statsObj.hitRate(),
                evictionCount = statsObj.evictionCount(),
                size = nativeCache.estimatedSize()
            )
        )
    }

    /**
     * Clear all caches
     */
    @PostMapping("/clear")
    fun clearAllCaches(): ResponseEntity<Map<String, String>> {
        cacheManager.cacheNames.forEach { cacheName ->
            cacheManager.getCache(cacheName)?.clear()
        }
        return ResponseEntity.ok(mapOf("status" to "All caches cleared"))
    }

    /**
     * Clear specific cache
     */
    @PostMapping("/clear/{cacheName}")
    fun clearCache(@PathVariable cacheName: String): ResponseEntity<Map<String, String>> {
        val cache = cacheManager.getCache(cacheName)
            ?: return ResponseEntity.notFound().build()
        
        cache.clear()
        return ResponseEntity.ok(mapOf("status" to "Cache '$cacheName' cleared"))
    }

    /**
     * Get list of all cache names
     */
    @GetMapping("/names")
    fun getCacheNames(): ResponseEntity<List<String>> {
        return ResponseEntity.ok(cacheManager.cacheNames.toList())
    }

    data class CacheStats(
        val hitCount: Long,
        val missCount: Long,
        val hitRate: Double,
        val evictionCount: Long,
        val size: Long
    )
}
