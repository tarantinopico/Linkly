package com.example.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.screens.AddEditLinkScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LinkDetailScreen
import com.example.ui.screens.ManageCategoriesScreen
import com.example.ui.screens.ManageTagsScreen
import com.example.ui.screens.SettingsScreen
import java.net.URLEncoder

@Composable
fun AppNavigation(sharedUrl: String? = null) {
    val navController = rememberNavController()

    LaunchedEffect(sharedUrl) {
        if (!sharedUrl.isNullOrEmpty()) {
            val encodedUrl = URLEncoder.encode(sharedUrl, "UTF-8")
            navController.navigate("add_edit?linkId=-1&sharedUrl=$encodedUrl")
        }
    }

    NavHost(
        navController = navController, 
        startDestination = "home",
        enterTransition = { fadeIn(animationSpec = tween(300)) + slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(300)) },
        exitTransition = { fadeOut(animationSpec = tween(300)) + slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(300)) },
        popEnterTransition = { fadeIn(animationSpec = tween(300)) + slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300)) },
        popExitTransition = { fadeOut(animationSpec = tween(300)) + slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300)) }
    ) {
        composable("home") {
            HomeScreen(
                onNavigateToAddEdit = { linkId ->
                    navController.navigate("add_edit${if (linkId != null) "?linkId=$linkId" else ""}")
                },
                onNavigateToSettings = {
                    navController.navigate("settings")
                },
                onNavigateToDetail = { linkId ->
                    navController.navigate("detail/$linkId")
                }
            )
        }
        composable(
            route = "add_edit?linkId={linkId}&sharedUrl={sharedUrl}",
            arguments = listOf(
                navArgument("linkId") { type = NavType.IntType; defaultValue = -1 },
                navArgument("sharedUrl") { type = NavType.StringType; nullable = true; defaultValue = null }
            )
        ) { backStackEntry ->
            val linkId = backStackEntry.arguments?.getInt("linkId") ?: -1
            val passedSharedUrl = backStackEntry.arguments?.getString("sharedUrl")
            
            AddEditLinkScreen(
                linkId = linkId,
                sharedUrl = passedSharedUrl,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(
            route = "detail/{linkId}",
            arguments = listOf(navArgument("linkId") { type = NavType.IntType })
        ) { backStackEntry ->
            val linkId = backStackEntry.arguments?.getInt("linkId") ?: return@composable
            LinkDetailScreen(
                linkId = linkId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEdit = { id -> navController.navigate("add_edit?linkId=$id") }
            )
        }
        composable("settings") {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToCategories = { navController.navigate("manage_categories") },
                onNavigateToTags = { navController.navigate("manage_tags") }
            )
        }
        composable("manage_categories") {
            ManageCategoriesScreen(onNavigateBack = { navController.popBackStack() })
        }
        composable("manage_tags") {
            ManageTagsScreen(onNavigateBack = { navController.popBackStack() })
        }
    }
}
