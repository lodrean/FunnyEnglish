package com.sotospeak.dto

import com.sotospeak.entity.speaking.GradeAudit
import java.time.Instant

/** Аудит-лог изменений оценок (bd FunnyEnglish-h3l.10, PROJECT_AUDIT F-5). */
data class GradeAuditResponse(
    val id: String,
    val submissionId: String,
    val action: String,
    val reviewerName: String,
    val grammar: Int,
    val vocabulary: Int,
    val pronunciation: Int,
    val fluency: Int,
    val total: Double,
    val comment: String?,
    val createdAt: Instant?
)

@Suppress("MagicNumber")
fun GradeAudit.toResponse() = GradeAuditResponse(
    id = id.toString(),
    submissionId = submission?.id.toString(),
    action = action,
    reviewerName = reviewer?.displayName.orEmpty(),
    grammar = grammar,
    vocabulary = vocabulary,
    pronunciation = pronunciation,
    fluency = fluency,
    total = kotlin.math.round(
        ((grammar + vocabulary + pronunciation + fluency) / 4.0) * TOTAL_DECIMALS
    ) / TOTAL_DECIMALS,
    comment = comment,
    createdAt = createdAt
)

private const val TOTAL_DECIMALS = 10.0
