package com.funnyenglish.home.data

import com.funnyenglish.core.data.network.safeCall
import com.funnyenglish.core.domain.model.Category
import com.funnyenglish.core.domain.model.TestListItem
import com.funnyenglish.core.domain.model.UserProfile
import com.funnyenglish.core.domain.util.DataError
import com.funnyenglish.core.domain.util.Result
import com.funnyenglish.home.domain.HomeRepository

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
