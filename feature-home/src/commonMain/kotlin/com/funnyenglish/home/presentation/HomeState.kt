package com.funnyenglish.home.presentation

import com.funnyenglish.core.domain.model.Category
import com.funnyenglish.core.domain.model.TestListItem
import com.funnyenglish.core.domain.model.UserProfile
import com.funnyenglish.core.presentation.ui.UiText

data class HomeState(
    val isLoading: Boolean = false,
    val userProfile: UserProfile? = null,
    val categories: List<Category> = emptyList(),
    val recentTests: List<TestListItem> = emptyList(),
    val error: UiText? = null
)
