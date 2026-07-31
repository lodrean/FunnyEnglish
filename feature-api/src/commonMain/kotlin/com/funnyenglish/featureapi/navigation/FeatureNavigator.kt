package com.funnyenglish.featureapi.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
/**
 * API for feature modules to register their navigation graphs
 */
interface FeatureNavigator {
    /**
     * Feature route identifier
     */
    val featureRoute: String
    
    /**
     * Register navigation routes for this feature
     */
    fun NavGraphBuilder.registerRoutes(
        navController: NavHostController,
        onNavigateToFeature: (FeatureRoute) -> Unit
    )
}

/**
 * Routes for all features
 */
sealed class FeatureRoute {
    data object Home : FeatureRoute()
    data object Categories : FeatureRoute()
    data class CategoryTests(val categoryId: String) : FeatureRoute()
    data class TestPlay(val testId: String) : FeatureRoute()
    data object Groups : FeatureRoute()
    data class GroupDetail(val groupId: String) : FeatureRoute()
    data object Leaderboard : FeatureRoute()
    data object Profile : FeatureRoute()
    data object Achievements : FeatureRoute()
    data object Settings : FeatureRoute()
    data class AdaptiveLesson(
        val categoryId: String? = null, 
        val durationMinutes: Int = 5
    ) : FeatureRoute()
    data object Login : FeatureRoute()
    data object Register : FeatureRoute()
}

/**
 * Navigation extensions
 */
fun NavController.navigateTo(route: FeatureRoute) {
    when (route) {
        is FeatureRoute.Home -> navigate("home")
        is FeatureRoute.Categories -> navigate("categories")
        is FeatureRoute.CategoryTests -> navigate("categories/${route.categoryId}/tests")
        is FeatureRoute.TestPlay -> navigate("tests/${route.testId}/play")
        is FeatureRoute.Groups -> navigate("groups")
        is FeatureRoute.GroupDetail -> navigate("groups/${route.groupId}")
        is FeatureRoute.Leaderboard -> navigate("leaderboard")
        is FeatureRoute.Profile -> navigate("profile")
        is FeatureRoute.Achievements -> navigate("achievements")
        is FeatureRoute.Settings -> navigate("settings")
        is FeatureRoute.AdaptiveLesson -> navigate(
            "adaptive?categoryId=${route.categoryId}&duration=${route.durationMinutes}"
        )
        is FeatureRoute.Login -> navigate("login")
        is FeatureRoute.Register -> navigate("register")
    }
}
