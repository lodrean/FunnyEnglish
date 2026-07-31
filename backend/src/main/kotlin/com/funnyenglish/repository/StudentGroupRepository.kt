package com.funnyenglish.repository

import com.funnyenglish.entity.GroupJoinRequest
import com.funnyenglish.entity.GroupMember
import com.funnyenglish.entity.JoinRequestStatus
import com.funnyenglish.entity.StudentGroup
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface StudentGroupRepository : JpaRepository<StudentGroup, UUID> {
    fun findByTeacherId(teacherId: UUID): List<StudentGroup>
    fun findByInviteCode(inviteCode: String): StudentGroup?
    fun existsByInviteCode(inviteCode: String): Boolean
    fun countByTeacherId(teacherId: UUID): Long
}

@Repository
interface GroupMemberRepository : JpaRepository<GroupMember, UUID> {
    fun findByGroupId(groupId: UUID): List<GroupMember>
    fun findByUserId(userId: UUID): List<GroupMember>
    fun existsByGroupIdAndUserId(groupId: UUID, userId: UUID): Boolean
    fun countByGroupId(groupId: UUID): Long
    fun deleteByGroupIdAndUserId(groupId: UUID, userId: UUID)
}

@Repository
interface GroupJoinRequestRepository : JpaRepository<GroupJoinRequest, UUID> {
    fun findByGroupIdAndStatus(groupId: UUID, status: JoinRequestStatus): List<GroupJoinRequest>
    fun findByUserId(userId: UUID): List<GroupJoinRequest>
    fun existsByGroupIdAndUserIdAndStatus(groupId: UUID, userId: UUID, status: JoinRequestStatus): Boolean
}
