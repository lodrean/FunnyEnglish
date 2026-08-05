package com.sotospeak.entity

import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

/**
 * Группа/класс учеников
 */
@Entity
@Table(name = "student_groups")
data class StudentGroup(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(name = "name", nullable = false)
    var name: String,

    @Column(name = "description")
    var description: String? = null,

    @Column(name = "teacher_id", nullable = false)
    val teacherId: UUID,

    @Column(name = "invite_code", unique = true, nullable = false)
    val inviteCode: String,

    @Column(name = "max_students", nullable = false)
    var maxStudents: Int = 30,

    @Column(name = "is_active", nullable = false)
    var isActive: Boolean = true,

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", insertable = false, updatable = false)
    val teacher: User? = null,

    @OneToMany(mappedBy = "group", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val members: MutableList<GroupMember> = mutableListOf()
)

/**
 * Член группы (ученик)
 */
@Entity
@Table(name = "group_members")
data class GroupMember(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(name = "group_id", nullable = false)
    val groupId: UUID,

    @Column(name = "user_id", nullable = false)
    val userId: UUID,

    @Column(name = "joined_at", nullable = false)
    val joinedAt: Instant = Instant.now(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", insertable = false, updatable = false)
    val group: StudentGroup? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    val user: User? = null
)

/**
 * Запрос на присоединение к группе
 */
@Entity
@Table(name = "group_join_requests")
data class GroupJoinRequest(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(name = "group_id", nullable = false)
    val groupId: UUID,

    @Column(name = "user_id", nullable = false)
    val userId: UUID,

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    var status: JoinRequestStatus = JoinRequestStatus.PENDING,

    @Column(name = "requested_at", nullable = false)
    val requestedAt: Instant = Instant.now(),

    @Column(name = "processed_at")
    var processedAt: Instant? = null,

    @Column(name = "processed_by")
    var processedBy: UUID? = null
)

enum class JoinRequestStatus {
    PENDING,
    APPROVED,
    REJECTED
}
