package com.sotospeak.dto

import java.time.Instant
import java.util.UUID

// Group DTOs

data class CreateGroupRequest(
    val name: String,
    val description: String? = null,
    val maxStudents: Int = 30
)

data class UpdateGroupRequest(
    val name: String? = null,
    val description: String? = null,
    val maxStudents: Int? = null,
    val isActive: Boolean? = null
)

data class GroupResponse(
    val id: UUID,
    val name: String,
    val description: String?,
    val teacherId: UUID,
    val teacherName: String?,
    val inviteCode: String,
    val maxStudents: Int,
    val currentStudents: Int,
    val isActive: Boolean,
    val createdAt: Instant
)

data class GroupDetailResponse(
    val id: UUID,
    val name: String,
    val description: String?,
    val teacherId: UUID,
    val teacherName: String?,
    val inviteCode: String,
    val maxStudents: Int,
    val isActive: Boolean,
    val createdAt: Instant,
    val members: List<GroupMemberResponse>,
    val pendingRequests: Int
)

// Member DTOs

data class GroupMemberResponse(
    val id: UUID,
    val userId: UUID,
    val displayName: String,
    val email: String,
    val avatarUrl: String?,
    val joinedAt: Instant,
    val level: Int,
    val totalPoints: Int,
    val completedTests: Int,
    val currentStreak: Int
)

data class JoinGroupRequest(
    val inviteCode: String
)

data class JoinByCodeResponse(
    val success: Boolean,
    val groupId: UUID? = null,
    val groupName: String? = null,
    val message: String
)

// Join Request DTOs

data class JoinRequestResponse(
    val id: UUID,
    val userId: UUID,
    val userName: String,
    val userEmail: String,
    val requestedAt: Instant
)

data class ProcessJoinRequest(
    val approve: Boolean
)

// Student Progress DTO

data class StudentProgressResponse(
    val userId: UUID,
    val displayName: String,
    val email: String,
    val avatarUrl: String?,
    val level: Int,
    val totalPoints: Int,
    val currentStreak: Int,
    val longestStreak: Int,
    val completedTests: Int,
    val averageScore: Double,
    val totalTimeSpent: Long, // in minutes
    val achievementsCount: Int,
    val lastActivityAt: Instant?,
    val joinedAt: Instant
)

data class GroupProgressSummary(
    val groupId: UUID,
    val groupName: String,
    val totalStudents: Int,
    val averageLevel: Double,
    val averagePoints: Double,
    val totalCompletedTests: Int,
    val mostActiveStudents: List<StudentProgressResponse>,
    val studentsNeedingAttention: List<StudentProgressResponse>
)
