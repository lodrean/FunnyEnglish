package com.funnyenglish.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.funnyenglish.shared.api.FunnyEnglishApi
import com.funnyenglish.shared.model.Category
import com.funnyenglish.shared.model.TestListItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CategoriesState(
    val isLoading: Boolean = false,
    val categories: List<Category> = emptyList(),
    val error: String? = null
)

data class CategoryTestsState(
    val isLoading: Boolean = false,
    val categoryName: String = "",
    val tests: List<TestListItem> = emptyList(),
    val error: String? = null
)

class CategoriesViewModel(
    private val api: FunnyEnglishApi
) : ViewModel() {

    private val _categoriesState = MutableStateFlow(CategoriesState())
    val categoriesState: StateFlow<CategoriesState> = _categoriesState.asStateFlow()

    private val _categoryTestsState = MutableStateFlow(CategoryTestsState())
    val categoryTestsState: StateFlow<CategoryTestsState> = _categoryTestsState.asStateFlow()

    fun loadCategories() {
        viewModelScope.launch {
            _categoriesState.value = _categoriesState.value.copy(isLoading = true, error = null)

            api.getCategories()
                .onSuccess { categories ->
                    _categoriesState.value = _categoriesState.value.copy(
                        isLoading = false,
                        categories = categories
                    )
                }
                .onFailure { error ->
                    _categoriesState.value = _categoriesState.value.copy(
                        isLoading = false,
                        error = error.message
                    )
                }
        }
    }

    fun loadCategoryTests(categoryId: String) {
        viewModelScope.launch {
            _categoryTestsState.value = _categoryTestsState.value.copy(isLoading = true, error = null)

            // Find category name
            val category = _categoriesState.value.categories.find { it.id == categoryId }
            _categoryTestsState.value = _categoryTestsState.value.copy(
                categoryName = category?.name ?: ""
            )

            api.getTestsByCategory(categoryId)
                .onSuccess { tests ->
                    _categoryTestsState.value = _categoryTestsState.value.copy(
                        isLoading = false,
                        tests = tests
                    )
                }
                .onFailure { error ->
                    _categoryTestsState.value = _categoryTestsState.value.copy(
                        isLoading = false,
                        error = error.message
                    )
                }
        }
    }

    fun clearError() {
        _categoriesState.value = _categoriesState.value.copy(error = null)
        _categoryTestsState.value = _categoryTestsState.value.copy(error = null)
    }
}
