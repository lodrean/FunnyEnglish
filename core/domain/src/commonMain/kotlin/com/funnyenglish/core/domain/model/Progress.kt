package com.funnyenglish.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ProgressSummary(
    val totalTests: Int,
    val completedTests: Int,
    val totalStars: Int,
    val maxPossibleStars: Int,
    val categoriesProgress: List<CategoryProgress>
)

@Serializable
data class CategoryProgress(
    val categoryId: String,
    val categoryName: String,
    val testsCount: Int,
    val completedCount: Int,
    val totalStars: Int,
    val maxStars: Int
)
