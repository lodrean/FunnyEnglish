package com.sotospeak.profile.data

import com.sotospeak.core.data.network.safeCall
import com.sotospeak.core.domain.model.Achievement
import com.sotospeak.core.domain.model.ProgressSummary
import com.sotospeak.core.domain.model.UserProfile
import com.sotospeak.core.domain.util.DataError
import com.sotospeak.core.domain.util.Result
import com.sotospeak.profile.domain.ProfileRepository

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
