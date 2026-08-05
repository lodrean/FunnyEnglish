package com.sotospeak.entity.speaking

import com.sotospeak.entity.User
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "grades")
class Grade(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submission_id", nullable = false, unique = true)
    var submission: PracticeSubmission? = null,

    @Column(nullable = false)
    var grammar: Int,

    @Column(nullable = false)
    var vocabulary: Int,

    @Column(nullable = false)
    var pronunciation: Int,

    @Column(nullable = false)
    var fluency: Int,

    /** Generated column в БД — только чтение.
     *  columnDefinition нужен для H2 (test profile, ddl-auto=create-drop): генерирует такую же
     *  вычисляемую колонку, как Flyway-DDL в PostgreSQL. В prod (ddl-auto=validate) не используется. */
    @Column(
        nullable = false, insertable = false, updatable = false,
        columnDefinition = "NUMERIC(4,2) GENERATED ALWAYS AS ((grammar + vocabulary + pronunciation + fluency) / 4.0)"
    )
    var total: BigDecimal? = null,

    @Column(columnDefinition = "TEXT")
    var comment: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id", nullable = false)
    var reviewer: User? = null,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant? = null,

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant? = null
)
