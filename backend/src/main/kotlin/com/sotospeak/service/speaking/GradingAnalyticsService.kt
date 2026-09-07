package com.sotospeak.service.speaking

import com.sotospeak.dto.CriterionAverages
import com.sotospeak.dto.GradingAnalyticsResponse
import com.sotospeak.dto.TopicGradeDistribution
import com.sotospeak.entity.speaking.SubmissionStatus
import com.sotospeak.repository.speaking.GradeRepository
import com.sotospeak.repository.speaking.PracticeSubmissionRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

/**
 * Grading-аналитика для админки (bd FunnyEnglish-h3l.4, PROJECT-REVIEW §4.2.1):
 * средний балл по рубрике, время NEW→REVIEWED, очередь NEW, распределение по
 * топикам + экспорт CSV. Читается из той же модели grades/submissions — без
 * денормализаций.
 */
@Service
class GradingAnalyticsService(
    private val gradeRepository: GradeRepository,
    private val submissionRepository: PracticeSubmissionRepository
) {

    @Transactional(readOnly = true)
    fun getAnalytics(): GradingAnalyticsResponse {
        val averages = gradeRepository.aggregateAverages()
        val reviewed = submissionRepository.findReviewedTimestamps()
        val avgReviewMinutes = reviewed
            .map { Duration.between(it.getSubmittedAt(), it.getReviewedAt()).toMillis() / MILLIS_PER_MINUTE }
            .takeIf { it.isNotEmpty() }
            ?.average()

        return GradingAnalyticsResponse(
            averages = CriterionAverages(
                grammar = averages?.getAvgGrammar(),
                vocabulary = averages?.getAvgVocabulary(),
                pronunciation = averages?.getAvgPronunciation(),
                fluency = averages?.getAvgFluency(),
                total = averages?.getAvgTotal()
            ),
            newCount = submissionRepository.countByStatus(SubmissionStatus.NEW),
            reviewedCount = reviewed.size.toLong(),
            avgReviewTimeMinutes = avgReviewMinutes?.let { roundToTenth(it) },
            byTopic = gradeRepository.aggregateByTopic().map { row ->
                TopicGradeDistribution(
                    topicId = row.getTopicId().toString(),
                    topicTitle = row.getTopicTitle(),
                    gradeCount = row.getGradeCount(),
                    avgTotal = row.getAvgTotal()
                )
            }
        )
    }

    /** CSV-экспорт оценённых записей (Excel-совместимый: BOM + CRLF). */
    @Transactional(readOnly = true)
    fun exportCsv(): String {
        val rows = submissionRepository.findAllReviewedWithDetails(SubmissionStatus.REVIEWED)
        val sb = StringBuilder("\uFEFF")
        sb.append(CSV_HEADER).append("\r\n")
        for (s in rows) {
            val grade = s.grade
            sb.append(csvRow(listOf(
                s.id.toString(),
                s.topic?.title.orEmpty(),
                s.user?.email.orEmpty(),
                format(s.createdAt),
                format(grade?.createdAt),
                grade?.grammar?.toString().orEmpty(),
                grade?.vocabulary?.toString().orEmpty(),
                grade?.pronunciation?.toString().orEmpty(),
                grade?.fluency?.toString().orEmpty(),
                grade?.total?.toPlainString().orEmpty(),
                grade?.comment.orEmpty()
            )))
            sb.append("\r\n")
        }
        return sb.toString()
    }

    private fun csvRow(values: List<String>): String =
        values.joinToString(",") { v ->
            if (v.any { it in CSV_ESCAPE_CHARS }) {
                "\"" + v.replace("\"", "\"\"") + "\""
            } else {
                v
            }
        }

    private fun format(instant: Instant?): String =
        instant?.atZone(ZoneOffset.UTC)?.format(CSV_DATE_FORMAT).orEmpty()

    private fun roundToTenth(value: Double): Double = kotlin.math.round(value * ROUND_FACTOR) / ROUND_FACTOR

    companion object {
        private const val MILLIS_PER_MINUTE = 60_000.0
        private const val ROUND_FACTOR = 10.0
        private val CSV_ESCAPE_CHARS = charArrayOf('"', ',', '\n', '\r')
        private val CSV_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        private const val CSV_HEADER =
            "submission_id,topic,user_email,submitted_at,reviewed_at," +
                "grammar,vocabulary,pronunciation,fluency,total,comment"
    }
}
