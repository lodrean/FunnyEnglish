package com.funnyenglish.profile.data

import com.funnyenglish.core.data.network.safeCall
import com.funnyenglish.core.domain.model.Achievement
import com.funnyenglish.core.domain.model.ProgressSummary
import com.funnyenglish.core.domain.model.UserProfile
import com.funnyenglish.core.domain.util.DataError
import com.funnyenglish.core.domain.util.Result
import com.funnyenglish.profile.domain.ProfileRepository

class ProfileRepositoryImpl(private val api: ProfileApi) : ProfileRepository {

    override suspend fun getUserProfile(): Result<UserProfile, DataError.Network> {
        return safeCall { api.getUserProfile() }
    }

    override suspend fun getProgressSummary(): Result<ProgressSummary, DataError.Network> {
        return safeCall { api.getProgressSummary() }
    }

    override suspend fun getAllAchievements(): Result<List<Achievement>, DataError.Network> {
        return safeCall { api.getAllAchievements() }
    }
}
