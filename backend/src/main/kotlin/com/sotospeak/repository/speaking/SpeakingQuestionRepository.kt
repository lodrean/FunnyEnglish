package com.sotospeak.repository.speaking

import com.sotospeak.entity.speaking.SpeakingQuestion
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface SpeakingQuestionRepository : JpaRepository<SpeakingQuestion, UUID> {

    fun findByTopicIdOrderByDisplayOrderAsc(topicId: UUID): List<SpeakingQuestion>
}
