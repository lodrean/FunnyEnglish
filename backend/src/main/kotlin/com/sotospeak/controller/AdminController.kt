package com.sotospeak.controller

import com.sotospeak.dto.*
import com.sotospeak.service.AdminService
import com.sotospeak.service.AdminSettingsService
import com.sotospeak.service.StorageService
import org.slf4j.LoggerFactory
import com.sotospeak.service.TestService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/admin")
class AdminController(
    private val testService: TestService,
    private val storageService: StorageService,
    private val adminService: AdminService,
    private val adminSettingsService: AdminSettingsService
) {
    private val logger = LoggerFactory.getLogger(AdminController::class.java)
    // Tests management
    @GetMapping("/tests")
    fun getAllTests(): ResponseEntity<List<AdminTestDetailResponse>> {
        return ResponseEntity.ok(testService.getAllTestsForAdmin())
    }

    @GetMapping("/tests/{testId}")
    fun getTestById(@PathVariable testId: String): ResponseEntity<AdminTestDetailResponse> {
        return ResponseEntity.ok(testService.getTestByIdForAdmin(testId))
    }

    @PostMapping("/tests")
    fun createTest(
        @Valid @RequestBody request: CreateTestRequest
    ): ResponseEntity<AdminTestDetailResponse> {
        return ResponseEntity.ok(testService.createTest(request))
    }

    @PutMapping("/tests/{testId}")
    fun updateTest(
        @PathVariable testId: String,
        @Valid @RequestBody request: UpdateTestRequest
    ): ResponseEntity<AdminTestDetailResponse> {
        return ResponseEntity.ok(testService.updateTest(testId, request))
    }

    @DeleteMapping("/tests/{testId}")
    fun deleteTest(@PathVariable testId: String): ResponseEntity<Unit> {
        testService.deleteTest(testId)
        return ResponseEntity.noContent().build()
    }

    // Analytics
    @GetMapping("/analytics")
    fun getAnalytics(): ResponseEntity<AdminAnalyticsResponse> {
        return ResponseEntity.ok(adminService.getAnalytics())
    }

    @GetMapping("/analytics/guests")
    fun getGuestAnalytics(): ResponseEntity<GuestAnalyticsResponse> {
        return ResponseEntity.ok(adminService.getGuestAnalytics())
    }

    @GetMapping("/analytics/daily-activity", "/analytics/activity")
    fun getDailyActivity(
        @RequestParam(defaultValue = "7") days: Int
    ): ResponseEntity<List<DailyActivityResponse>> {
        return ResponseEntity.ok(adminService.getDailyActivity(days))
    }

    @GetMapping("/analytics/levels")
    fun getLevelDistribution(): ResponseEntity<List<LevelDistributionResponse>> {
        return ResponseEntity.ok(adminService.getLevelDistribution())
    }

    @GetMapping("/analytics/popular-tests")
    fun getPopularTests(): ResponseEntity<List<PopularTestResponse>> {
        return ResponseEntity.ok(adminService.getPopularTests())
    }

    @GetMapping("/analytics/recent-activity")
    fun getRecentActivity(): ResponseEntity<List<RecentActivityResponse>> {
        return ResponseEntity.ok(adminService.getRecentActivity())
    }

    // Settings
    @GetMapping("/settings")
    fun getSettings(): ResponseEntity<AdminSettingsResponse> {
        return ResponseEntity.ok(adminSettingsService.getSettings())
    }

    // Media upload
    @PostMapping("/media/upload")
    fun uploadMedia(
        @RequestParam("file") file: MultipartFile,
        @RequestParam("folder", defaultValue = "media") folder: String
    ): ResponseEntity<MediaUploadResponse> {
        logger.info("Received upload request: file=${file.originalFilename}, size=${file.size}, folder=$folder")
        return try {
            val url = storageService.uploadFile(file, folder)
            logger.info("Upload successful: $url")
            ResponseEntity.ok(MediaUploadResponse(url = url))
        } catch (e: Exception) {
            logger.error("Upload failed", e)
            throw e
        }
    }

    @DeleteMapping("/media")
    fun deleteMedia(@RequestParam("url") url: String): ResponseEntity<Unit> {
        storageService.deleteFile(url)
        return ResponseEntity.noContent().build()
    }
}

data class MediaUploadResponse(val url: String)
