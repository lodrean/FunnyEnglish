package com.sotospeak.entity.speaking

import com.sotospeak.entity.User
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import java.time.Instant
import java.util.UUID

/**
 * Аудит-лог изменений оценок (bd FunnyEnglish-h3l.10, PROJECT_AUDIT F-5):
 * запись при первичной оценке (CREATE) и каждом редактировании (EDIT).
 * Хранит снапшот НОВОГО состояния; предыдущее восстанавливается по порядку.
 */
@Suppress("LongParameterList")
@Entity
@Table(name = "grade_audit")
class GradeAudit(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "submission_id", nullable = false)
    val submission: PracticeSubmission? = null,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reviewer_id", nullable = false)
    val reviewer: User? = null,

    /** CREATE — первичная оценка, EDIT — редактирование через PUT. */
    @Column(nullable = false, length = 16)
    val action: String,

    @Column(nullable = false)
    val grammar: Int,

    @Column(nullable = false)
    val vocabulary: Int,

    @Column(nullable = false)
    val pronunciation: Int,

    @Column(nullable = false)
    val fluency: Int,

    @Column(columnDefinition = "TEXT")
    val comment: String? = null,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant? = null
) {
    companion object {
        const val ACTION_CREATE = "CREATE"
        const val ACTION_EDIT = "EDIT"
    }
}
