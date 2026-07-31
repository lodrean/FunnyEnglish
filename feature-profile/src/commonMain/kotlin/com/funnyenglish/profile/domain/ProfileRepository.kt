package com.funnyenglish.profile.domain

import com.funnyenglish.core.domain.model.Achievement
import com.funnyenglish.core.domain.model.ProgressSummary
import com.funnyenglish.core.domain.model.UserProfile
import com.funnyenglish.core.domain.util.DataError
import com.funnyenglish.core.domain.util.Result

interface ProfileRepository {
    suspend fun getUserProfile(): Result<UserProfile, DataError.Network>
    suspend fun getProgressSummary(): Result<ProgressSummary, DataError.Network>
    suspend fun getAllAchievements(): Result<List<Achievement>, DataError.Network>
}
