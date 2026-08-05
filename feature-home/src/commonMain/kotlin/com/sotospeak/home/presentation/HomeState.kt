package com.sotospeak.home.presentation

import com.sotospeak.core.domain.model.Category
import com.sotospeak.core.domain.model.TestListItem
import com.sotospeak.core.domain.model.UserProfile
import com.sotospeak.core.presentation.ui.UiText

data class HomeState(
    val isLoading: Boolean = false,
    val userProfile: UserProfile? = null,
    val categories: List<Category> = emptyList(),
    val recentTests: List<TestListItem> = emptyList(),
    val error: UiText? = null
)
