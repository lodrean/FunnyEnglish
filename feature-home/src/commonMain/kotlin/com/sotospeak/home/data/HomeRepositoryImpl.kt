package com.sotospeak.home.data

import com.sotospeak.core.data.network.safeCall
import com.sotospeak.core.domain.model.Category
import com.sotospeak.core.domain.model.TestListItem
import com.sotospeak.core.domain.model.UserProfile
import com.sotospeak.core.domain.util.DataError
import com.sotospeak.core.domain.util.Result
import com.sotospeak.home.domain.HomeRepository

class HomeRepositoryImpl(private val api: HomeApi) : HomeRepository {

    override suspend fun getUserProfile(): Result<UserProfile, DataError.Network> {
        return safeCall { api.getUserProfile() }
    }

    override suspend fun getCategories(): Result<List<Category>, DataError.Network> {
        return safeCall { api.getCategories() }
    }

    override suspend fun getAllTests(): Result<List<TestListItem>, DataError.Network> {
        return safeCall { api.getAllTests() }
    }
}
