package com.funnyenglish.controller

import com.funnyenglish.dto.*
import com.funnyenglish.service.StorageService
import com.funnyenglish.service.TestService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/admin")
class AdminController(
    private val testService: TestService,
    private val storageService: StorageService
) {
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

    // Media upload
    @PostMapping("/media/upload")
    fun uploadMedia(
        @RequestParam("file") file: MultipartFile,
        @RequestParam("folder", defaultValue = "media") folder: String
    ): ResponseEntity<MediaUploadResponse> {
        val url = storageService.uploadFile(file, folder)
        return ResponseEntity.ok(MediaUploadResponse(url = url))
    }

    @DeleteMapping("/media")
    fun deleteMedia(@RequestParam("url") url: String): ResponseEntity<Unit> {
        storageService.deleteFile(url)
        return ResponseEntity.noContent().build()
    }
}

data class MediaUploadResponse(val url: String)
