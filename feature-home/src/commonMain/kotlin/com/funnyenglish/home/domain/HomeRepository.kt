package com.funnyenglish.home.domain

import com.funnyenglish.core.domain.model.Category
import com.funnyenglish.core.domain.model.TestListItem
import com.funnyenglish.core.domain.model.UserProfile
import com.funnyenglish.core.domain.util.DataError
import com.funnyenglish.core.domain.util.Result

interface HomeRepository {
    suspend fun getUserProfile(): Result<UserProfile, DataError.Network>
    suspend fun getCategories(): Result<List<Category>, DataError.Network>
    suspend fun getAllTests(): Result<List<TestListItem>, DataError.Network>
}
