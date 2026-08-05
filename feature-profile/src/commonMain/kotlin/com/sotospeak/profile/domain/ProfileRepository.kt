package com.sotospeak.profile.domain

import com.sotospeak.core.domain.model.Achievement
import com.sotospeak.core.domain.model.ProgressSummary
import com.sotospeak.core.domain.model.UserProfile
import com.sotospeak.core.domain.util.DataError
import com.sotospeak.core.domain.util.Result

interface ProfileRepository {
    suspend fun getUserProfile(): Result<UserProfile, DataError.Network>
    suspend fun getProgressSummary(): Result<ProgressSummary, DataError.Network>
    suspend fun getAllAchievements(): Result<List<Achievement>, DataError.Network>
}
