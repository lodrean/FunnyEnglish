package com.funnyenglish.controller.audio

import com.funnyenglish.dto.*
import com.funnyenglish.security.UserPrincipal
import com.funnyenglish.service.audio.AudioTestService
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/audio-tests")
class AudioTestController(
    private val audioTestService: AudioTestService
) {

    // ============== Public Endpoints ==============

    @GetMapping
    fun getPublishedAudioTests(
        @RequestParam(required = false) categoryId: UUID?,
        @RequestParam(required = false) difficulty: Int?,
        pageable: Pageable
    ): ResponseEntity<Page<AudioTestResponse>> {
        return ResponseEntity.ok(
            audioTestService.getPublishedAudioTests(categoryId, difficulty, pageable)
        )
    }

    @GetMapping("/{id}")
    fun getPublishedAudioTestById(@PathVariable id: UUID): ResponseEntity<AudioTestDetailResponse> {
        return ResponseEntity.ok(audioTestService.getPublishedAudioTestById(id))
    }

    @GetMapping("/my-progress")
    fun getMyProgress(
        @AuthenticationPrincipal userPrincipal: UserPrincipal
    ): ResponseEntity<List<AudioTestProgressResponse>> {
        return ResponseEntity.ok(
            audioTestService.getUserProgress(UUID.fromString(userPrincipal.userId))
        )
    }

    @GetMapping("/{id}/my-progress")
    fun getMyProgressForTest(
        @PathVariable id: UUID,
        @AuthenticationPrincipal userPrincipal: UserPrincipal
    ): ResponseEntity<AudioTestProgressResponse> {
        val progress = audioTestService.getUserProgressForTest(
            UUID.fromString(userPrincipal.userId), 
            id
        )
        return if (progress != null) {
            ResponseEntity.ok(progress)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @PostMapping("/submit")
    fun submitAudioTest(
        @Valid @RequestBody request: SubmitAudioTestRequest,
        @AuthenticationPrincipal userPrincipal: UserPrincipal
    ): ResponseEntity<SubmitAudioTestResponse> {
        return ResponseEntity.ok(
            audioTestService.submitAudioTest(userPrincipal.userId, request)
        )
    }

    // ============== Admin Endpoints ==============

    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    fun getAllAudioTests(pageable: Pageable): ResponseEntity<Page<AudioTestResponse>> {
        return ResponseEntity.ok(audioTestService.getAllAudioTests(pageable))
    }

    @GetMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    fun getAudioTestById(@PathVariable id: UUID): ResponseEntity<AudioTestDetailResponse> {
        return ResponseEntity.ok(audioTestService.getAudioTestById(id))
    }

    @PostMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    fun createAudioTest(
        @Valid @RequestBody request: CreateAudioTestRequest
    ): ResponseEntity<AudioTestDetailResponse> {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(audioTestService.createAudioTest(request))
    }

    @PutMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    fun updateAudioTest(
        @PathVariable id: UUID,
        @Valid @RequestBody request: UpdateAudioTestRequest
    ): ResponseEntity<AudioTestDetailResponse> {
        return ResponseEntity.ok(audioTestService.updateAudioTest(id, request))
    }

    @DeleteMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    fun deleteAudioTest(@PathVariable id: UUID): ResponseEntity<Void> {
        audioTestService.deleteAudioTest(id)
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/admin/{id}/publish")
    @PreAuthorize("hasRole('ADMIN')")
    fun publishAudioTest(@PathVariable id: UUID): ResponseEntity<AudioTestDetailResponse> {
        return ResponseEntity.ok(
            audioTestService.updateAudioTest(id, UpdateAudioTestRequest(isPublished = true))
        )
    }

    @PostMapping("/admin/{id}/unpublish")
    @PreAuthorize("hasRole('ADMIN')")
    fun unpublishAudioTest(@PathVariable id: UUID): ResponseEntity<AudioTestDetailResponse> {
        return ResponseEntity.ok(
            audioTestService.updateAudioTest(id, UpdateAudioTestRequest(isPublished = false))
        )
    }

    // ============== Waveform Endpoint ==============

    @GetMapping("/{id}/waveform")
    fun getWaveformData(@PathVariable id: UUID): ResponseEntity<WaveformDataResponse> {
        // For now, return synthetic waveform data
        // In production, this should be pre-computed and stored in DB or generated from audio file
        val syntheticData = generateSyntheticWaveform()
        return ResponseEntity.ok(
            WaveformDataResponse(
                audioTestId = id.toString(),
                samples = syntheticData,
                sampleRate = 100 // 100 samples for the whole duration
            )
        )
    }

    private fun generateSyntheticWaveform(): List<Float> {
        // Generate 100 samples with varied amplitudes to simulate realistic audio waveform
        return List(100) { index ->
            val normalized = index / 100.0
            // Create a varied pattern using sine waves and randomness
            val base = 0.3f + 0.5f * kotlin.math.sin(normalized * 8 * kotlin.math.PI).toFloat()
            val variation = 0.2f * kotlin.random.Random.nextFloat()
            (base + variation).coerceIn(0.1f, 1.0f)
        }
    }
}

/**
 * Response DTO for waveform data
 */
data class WaveformDataResponse(
    val audioTestId: String,
    val samples: List<Float>,
    val sampleRate: Int
)
