package com.sotospeak.repository

import com.sotospeak.entity.ClientLog
import com.sotospeak.entity.ClientLogLevel
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.Instant
import java.util.UUID

interface ClientLogRepository : JpaRepository<ClientLog, UUID> {

    @Query(
        """
        SELECT c FROM ClientLog c
        WHERE (:level IS NULL OR c.level = :level)
          AND (:platform IS NULL OR c.platform = :platform)
          AND (CAST(:fromTs AS Instant) IS NULL OR c.createdAt >= :fromTs)
          AND (CAST(:toTs AS Instant) IS NULL OR c.createdAt <= :toTs)
          AND (CAST(:q AS String) IS NULL OR LOWER(c.message) LIKE LOWER(CONCAT('%', CAST(:q AS String), '%')))
        ORDER BY c.createdAt DESC
        """
    )
    fun search(
        @Param("level") level: ClientLogLevel?,
        @Param("platform") platform: String?,
        @Param("fromTs") fromTs: Instant?,
        @Param("toTs") toTs: Instant?,
        @Param("q") q: String?,
        pageable: Pageable
    ): Page<ClientLog>
}
