package com.funnyenglish.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.funnyenglish.shared.api.FunnyEnglishApi
import com.funnyenglish.shared.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomeState(
    val isLoading: Boolean = false,
    val userProfile: UserProfile? = null,
    val categories: List<Category> = emptyList(),
    val recentTests: List<TestListItem> = emptyList(),
    val error: String? = null
)

class HomeViewModel(
    private val api: FunnyEnglishApi
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    fun loadHomeData() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            // Load user profile
            api.getUserProfile()
                .onSuccess { profile ->
                    _state.value = _state.value.copy(userProfile = profile)
                }

            // Load categories
            api.getCategories()
                .onSuccess { categories ->
                    _state.value = _state.value.copy(categories = categories)
                }

            // Load recent tests
            api.getAllTests()
                .onSuccess { tests ->
                    _state.value = _state.value.copy(
                        recentTests = tests.take(5),
                        isLoading = false
                    )
                }
                .onFailure { error ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = error.message
                    )
                }
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
}
