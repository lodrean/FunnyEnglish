package com.sotospeak.shared.api

import com.sotospeak.shared.contracts.GroupDetail
import com.sotospeak.shared.contracts.JoinGroupResponse
import com.sotospeak.shared.contracts.Message
import com.sotospeak.shared.contracts.StudentGroup
import com.sotospeak.shared.contracts.UnreadCountResponse

/**
 * Срез API: коммуникация с учителем — inbox сообщений и учебные группы.
 * См. [AuthApi] — разбор монолита [SoToSpeakApi] (bd FunnyEnglish-5tf.5).
 */
interface MessagingApi {
    suspend fun getMessages(): Result<List<Message>>
    suspend fun getUnreadMessagesCount(): Result<UnreadCountResponse>
    suspend fun markMessageAsRead(messageId: String): Result<Message>

    suspend fun getMyStudentGroups(): Result<List<StudentGroup>>
    suspend fun getStudentGroupDetail(groupId: String): Result<GroupDetail>
    suspend fun joinGroupByCode(inviteCode: String): Result<JoinGroupResponse>
    suspend fun leaveGroup(groupId: String): Result<Unit>
}
