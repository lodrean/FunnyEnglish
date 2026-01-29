package com.funnyenglish.repository

import java.sql.Date
import java.time.Instant
import java.util.UUID

interface DateCountProjection {
    val date: Date
    val count: Long
}

interface LevelCountProjection {
    val level: Int
    val count: Long
}

interface CategoryCompletionProjection {
    val categoryId: UUID
    val categoryName: String
    val completions: Long
}

interface PopularTestProjection {
    val id: UUID
    val name: String
    val completions: Long
    val category: String
}

interface RecentActivityProjection {
    val type: String
    val userName: String
    val details: String?
    val timestamp: Instant
}
