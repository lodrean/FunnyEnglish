package com.funnyenglish.repository.speaking

import com.funnyenglish.entity.speaking.Video
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional
import java.util.UUID

@Repository
interface VideoRepository : JpaRepository<Video, UUID> {

    fun findByTopicId(topicId: UUID): Optional<Video>
}
