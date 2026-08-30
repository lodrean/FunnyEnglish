package com.sotospeak.shared.model

import kotlinx.serialization.Serializable

// Group Models

@Serializable
data class StudentGroup(
    val id: String,
    val name: String,
    val description: String? = null,
    val teacherId: String,
    val teacherName: String? = null,
    val inviteCode: String,
    val maxStudents: Int,
    val currentStudents: Int,
    val isActive: Boolean,
    val createdAt: String
)

@Serializable
data class GroupDetail(
    val id: String,
    val name: String,
    val description: String? = null,
    val teacherId: String,
    val teacherName: String? = null,
    val inviteCode: String,
    val maxStudents: Int,
    val isActive: Boolean,
    val createdAt: String,
    val members: List<GroupMember>,
    val pendingRequests: Int
)

@Serializable
data class GroupMember(
    val id: String,
    val userId: String,
    val displayName: String,
    val email: String,
    val avatarUrl: String? = null,
    val joinedAt: String,
    val level: Int,
    val totalPoints: Int,
    val completedTests: Int,
    val currentStreak: Int
)

@Serializable
data class CreateGroupRequest(
    val name: String,
    val description: String? = null,
    val maxStudents: Int = 30
)

@Serializable
data class UpdateGroupRequest(
    val name: String? = null,
    val description: String? = null,
    val maxStudents: Int? = null,
    val isActive: Boolean? = null
)

@Serializable
data class JoinGroupRequest(
    val inviteCode: String
)

@Serializable
data class JoinGroupResponse(
    val success: Boolean,
    val groupId: String? = null,
    val groupName: String? = null,
    val message: String
)

@Serializable
data class JoinRequest(
    val id: String,
    val userId: String,
    val userName: String,
    val userEmail: String,
    val requestedAt: String
)

@Serializable
data class ProcessJoinRequest(
    val approve: Boolean
)

// Progress Models

@Serializable
data class StudentProgress(
    val userId: String,
    val displayName: String,
    val email: String,
    val avatarUrl: String? = null,
    val level: Int,
    val totalPoints: Int,
    val currentStreak: Int,
    val longestStreak: Int,
    val completedTests: Int,
    val averageScore: Double,
    val totalTimeSpent: Long, // in minutes
    val achievementsCount: Int,
    val lastActivityAt: String? = null,
    val joinedAt: String
)

@Serializable
data class GroupProgressSummary(
    val groupId: String,
    val groupName: String,
    val totalStudents: Int,
    val averageLevel: Double,
    val averagePoints: Double,
    val totalCompletedTests: Int,
    val mostActiveStudents: List<StudentProgress>,
    val studentsNeedingAttention: List<StudentProgress>
)
