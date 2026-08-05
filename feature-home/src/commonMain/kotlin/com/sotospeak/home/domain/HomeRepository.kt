package com.sotospeak.home.domain

import com.sotospeak.core.domain.model.Category
import com.sotospeak.core.domain.model.TestListItem
import com.sotospeak.core.domain.model.UserProfile
import com.sotospeak.core.domain.util.DataError
import com.sotospeak.core.domain.util.Result

interface HomeRepository {
    suspend fun getUserProfile(): Result<UserProfile, DataError.Network>
    suspend fun getCategories(): Result<List<Category>, DataError.Network>
    suspend fun getAllTests(): Result<List<TestListItem>, DataError.Network>
}
