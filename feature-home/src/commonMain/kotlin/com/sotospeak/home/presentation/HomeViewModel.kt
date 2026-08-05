package com.sotospeak.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sotospeak.core.domain.repository.TokenProvider
import com.sotospeak.core.domain.util.Result
import com.sotospeak.core.domain.util.onError
import com.sotospeak.core.domain.util.onSuccess
import com.sotospeak.core.presentation.ui.UiText
import com.sotospeak.home.domain.HomeRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: HomeRepository,
    private val tokenProvider: TokenProvider
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    private val _events = Channel<HomeEvent>()
    val events = _events.receiveAsFlow()

    fun onAction(action: HomeAction) {
        when (action) {
            HomeAction.OnLoadData -> loadHomeData()
            is HomeAction.OnCategoryClick -> {
                viewModelScope.launch { _events.send(HomeEvent.NavigateToCategoryTests(action.categoryId)) }
            }
            is HomeAction.OnTestClick -> {
                viewModelScope.launch { _events.send(HomeEvent.NavigateToTestPlay(action.testId)) }
            }
            HomeAction.OnViewAllCategories -> {
                viewModelScope.launch { _events.send(HomeEvent.NavigateToCategories) }
            }
            HomeAction.OnProfileClick -> {
                viewModelScope.launch { _events.send(HomeEvent.NavigateToProfile) }
            }
            HomeAction.OnContinueLearning -> {
                val incompleteTest = _state.value.recentTests.firstOrNull { it.userProgress == null }
                viewModelScope.launch {
                    if (incompleteTest != null) {
                        _events.send(HomeEvent.NavigateToTestPlay(incompleteTest.id))
                    } else {
                        _events.send(HomeEvent.NavigateToCategories)
                    }
                }
            }
            HomeAction.OnAdaptiveLessonClick -> {
                viewModelScope.launch { _events.send(HomeEvent.NavigateToAdaptiveLesson) }
            }
            HomeAction.OnClearError -> _state.update { it.copy(error = null) }
        }
    }

    private fun loadHomeData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            val isAuthenticated = tokenProvider.getToken() != null

            if (isAuthenticated) {
                repository.getUserProfile()
                    .onSuccess { profile -> _state.update { it.copy(userProfile = profile) } }
            }

            repository.getCategories()
                .onSuccess { categories -> _state.update { it.copy(categories = categories) } }

            repository.getAllTests()
                .onSuccess { tests ->
                    _state.update {
                        it.copy(recentTests = tests.take(5), isLoading = false)
                    }
                }
                .onError { _ ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = UiText.plain("Не удалось загрузить данные")
                        )
                    }
                }
        }
    }
}
