package com.funnyenglish.home.presentation

sealed interface HomeEvent {
    data class NavigateToCategoryTests(val categoryId: String) : HomeEvent
    data class NavigateToTestPlay(val testId: String) : HomeEvent
    data object NavigateToCategories : HomeEvent
    data object NavigateToProfile : HomeEvent
    data object NavigateToAdaptiveLesson : HomeEvent
}
