package com.funnyenglish.entity.speaking

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "videos")
class Video(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id", nullable = false, unique = true)
    var topic: Topic? = null,

    @Column(name = "video_url", nullable = false, length = 500)
    var videoUrl: String,

    /** WebVTT (.vtt) в MinIO; null = субтитров нет */
    @Column(name = "subtitle_url", length = 500)
    var subtitleUrl: String? = null,

    @Column(name = "duration_seconds", nullable = false)
    var durationSeconds: Int,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant? = null,

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant? = null
)
