package com.funnyenglish.home.presentation

sealed interface HomeAction {
    data object OnLoadData : HomeAction
    data class OnCategoryClick(val categoryId: String) : HomeAction
    data class OnTestClick(val testId: String) : HomeAction
    data object OnViewAllCategories : HomeAction
    data object OnProfileClick : HomeAction
    data object OnContinueLearning : HomeAction
    data object OnAdaptiveLessonClick : HomeAction
    data object OnClearError : HomeAction
}
