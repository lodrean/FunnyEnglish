package com.funnyenglish.controller

import com.funnyenglish.dto.*
import com.funnyenglish.security.UserPrincipal
import com.funnyenglish.service.StudentGroupService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.util.UUID

// Extension function to convert String userId to UUID
private fun String.toUUID(): UUID = UUID.fromString(this)

@RestController
@RequestMapping("/api/groups")
class StudentGroupController(
    private val groupService: StudentGroupService
) {

    // ==================== Teacher Endpoints ====================

    @PostMapping
    fun createGroup(
        @AuthenticationPrincipal principal: UserPrincipal,
        @RequestBody request: CreateGroupRequest
    ): ResponseEntity<GroupResponse> {
        val group = groupService.createGroup(principal.userId.toUUID(), request)
        return ResponseEntity.status(HttpStatus.CREATED).body(group)
    }

    @PutMapping("/{groupId}")
    fun updateGroup(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable groupId: UUID,
        @RequestBody request: UpdateGroupRequest
    ): ResponseEntity<GroupResponse> {
        val group = groupService.updateGroup(groupId, principal.userId.toUUID(), request)
        return ResponseEntity.ok(group)
    }

    @DeleteMapping("/{groupId}")
    fun deleteGroup(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable groupId: UUID
    ): ResponseEntity<Void> {
        groupService.deleteGroup(groupId, principal.userId.toUUID())
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/teacher/my-groups")
    fun getMyTeacherGroups(
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<List<GroupResponse>> {
        val groups = groupService.getTeacherGroups(principal.userId.toUUID())
        return ResponseEntity.ok(groups)
    }

    @GetMapping("/{groupId}/detail")
    fun getGroupDetail(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable groupId: UUID
    ): ResponseEntity<GroupDetailResponse> {
        val group = groupService.getGroupDetail(groupId, principal.userId.toUUID())
        return ResponseEntity.ok(group)
    }

    @DeleteMapping("/{groupId}/students/{studentId}")
    fun removeStudent(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable groupId: UUID,
        @PathVariable studentId: UUID
    ): ResponseEntity<Void> {
        groupService.removeStudent(groupId, principal.userId.toUUID(), studentId)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/{groupId}/join-requests")
    fun getPendingRequests(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable groupId: UUID
    ): ResponseEntity<List<JoinRequestResponse>> {
        val requests = groupService.getPendingRequests(groupId, principal.userId.toUUID())
        return ResponseEntity.ok(requests)
    }

    @PostMapping("/{groupId}/join-requests/{requestId}")
    fun processJoinRequest(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable groupId: UUID,
        @PathVariable requestId: UUID,
        @RequestBody request: ProcessJoinRequest
    ): ResponseEntity<Void> {
        groupService.processJoinRequest(groupId, principal.userId.toUUID(), requestId, request.approve)
        return ResponseEntity.ok().build()
    }

    @GetMapping("/{groupId}/progress")
    fun getGroupProgress(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable groupId: UUID
    ): ResponseEntity<GroupProgressSummary> {
        val progress = groupService.getGroupProgress(groupId, principal.userId.toUUID())
        return ResponseEntity.ok(progress)
    }

    // ==================== Student Endpoints ====================

    @PostMapping("/join")
    fun joinGroupByCode(
        @AuthenticationPrincipal principal: UserPrincipal,
        @RequestBody request: JoinGroupRequest
    ): ResponseEntity<JoinByCodeResponse> {
        val result = groupService.joinGroupByCode(principal.userId.toUUID(), request.inviteCode)
        return if (result.success) {
            ResponseEntity.ok(result)
        } else {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result)
        }
    }

    @GetMapping("/student/my-groups")
    fun getMyStudentGroups(
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<List<GroupResponse>> {
        val groups = groupService.getStudentGroups(principal.userId.toUUID())
        return ResponseEntity.ok(groups)
    }

    @GetMapping("/student/{groupId}")
    fun getStudentGroupDetail(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable groupId: UUID
    ): ResponseEntity<GroupDetailResponse> {
        val group = groupService.getStudentGroupDetail(groupId, principal.userId.toUUID())
        return ResponseEntity.ok(group)
    }

    @PostMapping("/student/{groupId}/leave")
    fun leaveGroup(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable groupId: UUID
    ): ResponseEntity<Void> {
        groupService.leaveGroup(groupId, principal.userId.toUUID())
        return ResponseEntity.ok().build()
    }
}
