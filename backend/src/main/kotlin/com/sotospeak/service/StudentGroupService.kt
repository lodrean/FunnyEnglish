package com.sotospeak.service

import com.sotospeak.dto.*
import com.sotospeak.entity.*
import com.sotospeak.repository.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.*
import kotlin.random.Random

@Service
class StudentGroupService(
    private val groupRepository: StudentGroupRepository,
    private val memberRepository: GroupMemberRepository,
    private val requestRepository: GroupJoinRequestRepository,
    private val userRepository: UserRepository,
    private val progressRepository: ProgressRepository,
    private val userAchievementRepository: UserAchievementRepository
) {

    // ==================== Teacher Methods ====================

    @Transactional
    fun createGroup(teacherId: UUID, request: CreateGroupRequest): GroupResponse {
        val inviteCode = generateUniqueInviteCode()
        
        val group = StudentGroup(
            name = request.name,
            description = request.description,
            teacherId = teacherId,
            inviteCode = inviteCode,
            maxStudents = request.maxStudents
        )
        
        val saved = groupRepository.save(group)
        return mapToGroupResponse(saved)
    }

    @Transactional
    fun updateGroup(groupId: UUID, teacherId: UUID, request: UpdateGroupRequest): GroupResponse {
        val group = groupRepository.findById(groupId)
            .orElseThrow { NoSuchElementException("Group not found") }
        
        if (group.teacherId != teacherId) {
            throw IllegalAccessException("Only teacher can update group")
        }
        
        request.name?.let { group.name = it }
        request.description?.let { group.description = it }
        request.maxStudents?.let { group.maxStudents = it }
        request.isActive?.let { group.isActive = it }
        group.updatedAt = Instant.now()
        
        val saved = groupRepository.save(group)
        return mapToGroupResponse(saved)
    }

    @Transactional
    fun deleteGroup(groupId: UUID, teacherId: UUID) {
        val group = groupRepository.findById(groupId)
            .orElseThrow { NoSuchElementException("Group not found") }
        
        if (group.teacherId != teacherId) {
            throw IllegalAccessException("Only teacher can delete group")
        }
        
        groupRepository.delete(group)
    }

    fun getTeacherGroups(teacherId: UUID): List<GroupResponse> {
        return groupRepository.findByTeacherId(teacherId)
            .map { mapToGroupResponse(it) }
    }

    fun getGroupDetail(groupId: UUID, teacherId: UUID): GroupDetailResponse {
        val group = groupRepository.findById(groupId)
            .orElseThrow { NoSuchElementException("Group not found") }
        
        if (group.teacherId != teacherId) {
            throw IllegalAccessException("Only teacher can view group details")
        }
        
        val members = memberRepository.findByGroupId(groupId)
        val memberResponses = members.map { mapToMemberResponse(it) }
        
        val pendingRequests = requestRepository.findByGroupIdAndStatus(groupId, JoinRequestStatus.PENDING)
        
        return GroupDetailResponse(
            id = group.id,
            name = group.name,
            description = group.description,
            teacherId = group.teacherId,
            teacherName = group.teacher?.displayName,
            inviteCode = group.inviteCode,
            maxStudents = group.maxStudents,
            isActive = group.isActive,
            createdAt = group.createdAt,
            members = memberResponses,
            pendingRequests = pendingRequests.size
        )
    }

    @Transactional
    fun removeStudent(groupId: UUID, teacherId: UUID, studentId: UUID) {
        val group = groupRepository.findById(groupId)
            .orElseThrow { NoSuchElementException("Group not found") }
        
        if (group.teacherId != teacherId) {
            throw IllegalAccessException("Only teacher can remove students")
        }
        
        memberRepository.deleteByGroupIdAndUserId(groupId, studentId)
    }

    fun getPendingRequests(groupId: UUID, teacherId: UUID): List<JoinRequestResponse> {
        val group = groupRepository.findById(groupId)
            .orElseThrow { NoSuchElementException("Group not found") }
        
        if (group.teacherId != teacherId) {
            throw IllegalAccessException("Only teacher can view requests")
        }
        
        return requestRepository.findByGroupIdAndStatus(groupId, JoinRequestStatus.PENDING)
            .map { req ->
                val user = userRepository.findById(req.userId).orElse(null)
                JoinRequestResponse(
                    id = req.id,
                    userId = req.userId,
                    userName = user?.displayName ?: "Unknown",
                    userEmail = user?.email ?: "",
                    requestedAt = req.requestedAt
                )
            }
    }

    @Transactional
    fun processJoinRequest(groupId: UUID, teacherId: UUID, requestId: UUID, approve: Boolean) {
        val group = groupRepository.findById(groupId)
            .orElseThrow { NoSuchElementException("Group not found") }
        
        if (group.teacherId != teacherId) {
            throw IllegalAccessException("Only teacher can process requests")
        }
        
        val request = requestRepository.findById(requestId)
            .orElseThrow { NoSuchElementException("Request not found") }
        
        if (request.groupId != groupId) {
            throw IllegalArgumentException("Request does not belong to this group")
        }
        
        request.status = if (approve) JoinRequestStatus.APPROVED else JoinRequestStatus.REJECTED
        request.processedAt = Instant.now()
        request.processedBy = teacherId
        requestRepository.save(request)
        
        if (approve) {
            // Check if not already member
            if (!memberRepository.existsByGroupIdAndUserId(groupId, request.userId)) {
                val member = GroupMember(
                    groupId = groupId,
                    userId = request.userId
                )
                memberRepository.save(member)
            }
        }
    }

    // ==================== Student Methods ====================

    @Transactional
    fun joinGroupByCode(userId: UUID, inviteCode: String): JoinByCodeResponse {
        val group = groupRepository.findByInviteCode(inviteCode)
            ?: return JoinByCodeResponse(
                success = false,
                message = "Invalid invite code"
            )
        
        if (!group.isActive) {
            return JoinByCodeResponse(
                success = false,
                message = "This group is not active"
            )
        }
        
        // Check if already member
        if (memberRepository.existsByGroupIdAndUserId(group.id, userId)) {
            return JoinByCodeResponse(
                success = false,
                groupId = group.id,
                groupName = group.name,
                message = "You are already a member of this group"
            )
        }
        
        // Check capacity
        val currentMembers = memberRepository.countByGroupId(group.id)
        if (currentMembers >= group.maxStudents) {
            return JoinByCodeResponse(
                success = false,
                message = "This group is full"
            )
        }
        
        // Check if already has pending request
        if (requestRepository.existsByGroupIdAndUserIdAndStatus(group.id, userId, JoinRequestStatus.PENDING)) {
            return JoinByCodeResponse(
                success = false,
                message = "You already have a pending request to join this group"
            )
        }
        
        // Create join request
        val request = GroupJoinRequest(
            groupId = group.id,
            userId = userId
        )
        requestRepository.save(request)
        
        return JoinByCodeResponse(
            success = true,
            groupId = group.id,
            groupName = group.name,
            message = "Join request sent. Waiting for teacher approval."
        )
    }

    fun getStudentGroups(userId: UUID): List<GroupResponse> {
        return memberRepository.findByUserId(userId)
            .map { it.group }
            .filterNotNull()
            .map { mapToGroupResponse(it) }
    }

    fun getStudentGroupDetail(groupId: UUID, userId: UUID): GroupDetailResponse {
        if (!memberRepository.existsByGroupIdAndUserId(groupId, userId)) {
            throw IllegalAccessException("You are not a member of this group")
        }
        
        val group = groupRepository.findById(groupId)
            .orElseThrow { NoSuchElementException("Group not found") }
        
        val members = memberRepository.findByGroupId(groupId)
        val memberResponses = members.map { mapToMemberResponse(it) }
        
        return GroupDetailResponse(
            id = group.id,
            name = group.name,
            description = group.description,
            teacherId = group.teacherId,
            teacherName = group.teacher?.displayName,
            inviteCode = "", // Hide invite code from students
            maxStudents = group.maxStudents,
            isActive = group.isActive,
            createdAt = group.createdAt,
            members = memberResponses,
            pendingRequests = 0
        )
    }

    @Transactional
    fun leaveGroup(groupId: UUID, userId: UUID) {
        if (!memberRepository.existsByGroupIdAndUserId(groupId, userId)) {
            throw IllegalAccessException("You are not a member of this group")
        }
        
        memberRepository.deleteByGroupIdAndUserId(groupId, userId)
    }

    // ==================== Progress Methods ====================

    fun getGroupProgress(groupId: UUID, teacherId: UUID): GroupProgressSummary {
        val group = groupRepository.findById(groupId)
            .orElseThrow { NoSuchElementException("Group not found") }
        
        if (group.teacherId != teacherId) {
            throw IllegalAccessException("Only teacher can view progress")
        }
        
        val members = memberRepository.findByGroupId(groupId)
        val studentProgress = members.map { getStudentProgress(it) }
        
        val sortedByPoints = studentProgress.sortedByDescending { it.totalPoints }
        val mostActive = sortedByPoints.take(5)
        
        // Students needing attention: low activity, no recent activity, or low scores
        val needingAttention = studentProgress
            .filter { 
                it.averageScore < 60 || 
                it.lastActivityAt?.isBefore(Instant.now().minusSeconds(7 * 24 * 60 * 60)) == true ||
                it.completedTests < 5
            }
            .sortedBy { it.totalPoints }
            .take(5)
        
        return GroupProgressSummary(
            groupId = group.id,
            groupName = group.name,
            totalStudents = members.size,
            averageLevel = studentProgress.map { it.level }.average().let { if (it.isNaN()) 0.0 else it },
            averagePoints = studentProgress.map { it.totalPoints }.average().let { if (it.isNaN()) 0.0 else it },
            totalCompletedTests = studentProgress.sumOf { it.completedTests },
            mostActiveStudents = mostActive,
            studentsNeedingAttention = needingAttention
        )
    }

    fun getStudentProgress(member: GroupMember): StudentProgressResponse {
        val user = member.user ?: return StudentProgressResponse(
            userId = member.userId,
            displayName = "Unknown",
            email = "",
            avatarUrl = null,
            level = 1,
            totalPoints = 0,
            currentStreak = 0,
            longestStreak = 0,
            completedTests = 0,
            averageScore = 0.0,
            totalTimeSpent = 0,
            achievementsCount = 0,
            lastActivityAt = null,
            joinedAt = member.joinedAt
        )
        
        val progress = progressRepository.findByUserId(member.userId)
        val completedTests = progress.count { it.bestScore != null }
        val averageScore = if (completedTests > 0) {
            progress.mapNotNull { it.bestScore }.average().let { if (it.isNaN()) 0.0 else it }
        } else 0.0
        
        val totalTimeSpent = progress.sumOf { (it.timeSpentSeconds ?: 0).toLong() } / 60 // Convert to minutes
        val achievementsCount = userAchievementRepository.countEarnedByUserId(member.userId)
        
        val lastActivity = progress
            .mapNotNull { it.lastAttemptAt }
            .maxOrNull()
        
        return StudentProgressResponse(
            userId = user.id,
            displayName = user.displayName,
            email = user.email,
            avatarUrl = user.avatarUrl,
            level = user.level,
            totalPoints = user.totalPoints,
            currentStreak = user.currentStreak,
            longestStreak = user.longestStreak,
            completedTests = completedTests,
            averageScore = averageScore,
            totalTimeSpent = totalTimeSpent,
            achievementsCount = achievementsCount.toInt(),
            lastActivityAt = lastActivity,
            joinedAt = member.joinedAt
        )
    }

    // ==================== Private Methods ====================

    private fun mapToGroupResponse(group: StudentGroup): GroupResponse {
        val currentStudents = memberRepository.countByGroupId(group.id).toInt()
        
        return GroupResponse(
            id = group.id,
            name = group.name,
            description = group.description,
            teacherId = group.teacherId,
            teacherName = group.teacher?.displayName,
            inviteCode = group.inviteCode,
            maxStudents = group.maxStudents,
            currentStudents = currentStudents,
            isActive = group.isActive,
            createdAt = group.createdAt
        )
    }

    private fun mapToMemberResponse(member: GroupMember): GroupMemberResponse {
        val user = member.user
        val progress = if (user != null) {
            progressRepository.findByUserId(user.id)
        } else emptyList()
        
        return GroupMemberResponse(
            id = member.id,
            userId = member.userId,
            displayName = user?.displayName ?: "Unknown",
            email = user?.email ?: "",
            avatarUrl = user?.avatarUrl,
            joinedAt = member.joinedAt,
            level = user?.level ?: 1,
            totalPoints = user?.totalPoints ?: 0,
            completedTests = progress.count { it.bestScore != null },
            currentStreak = user?.currentStreak ?: 0
        )
    }

    private fun generateUniqueInviteCode(): String {
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789" // Excluding similar looking chars
        var code: String
        var attempts = 0
        
        do {
            code = (1..6)
                .map { chars[Random.nextInt(chars.length)] }
                .joinToString("")
            attempts++
        } while (groupRepository.existsByInviteCode(code) && attempts < 100)
        
        if (attempts >= 100) {
            // Fallback with timestamp
            code = code + System.currentTimeMillis().toString(36).takeLast(4).uppercase()
        }
        
        return code
    }
}
