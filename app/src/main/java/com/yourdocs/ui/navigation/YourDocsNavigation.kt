package com.yourdocs.ui.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.yourdocs.ui.folder.FolderDetailScreen
import com.yourdocs.ui.home.HomeScreen
import com.yourdocs.ui.settings.SettingsScreen
import com.yourdocs.ui.viewer.DocumentViewerScreen

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Folder : Screen("folder/{folderId}") {
        fun createRoute(folderId: String) = "folder/$folderId"
    }
    data object Settings : Screen("settings")
    data object DocumentViewer : Screen("viewer/{documentId}") {
        fun createRoute(documentId: String) = "viewer/$documentId"
    }
}

@Composable
fun YourDocsNavigation(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        enterTransition = { fadeIn() + slideInHorizontally(initialOffsetX = { it / 4 }) },
        exitTransition = { fadeOut() + slideOutHorizontally(targetOffsetX = { -it / 4 }) },
        popEnterTransition = { fadeIn() + slideInHorizontally(initialOffsetX = { -it / 4 }) },
        popExitTransition = { fadeOut() + slideOutHorizontally(targetOffsetX = { it / 4 }) }
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onFolderClick = { folderId ->
                    navController.navigate(Screen.Folder.createRoute(folderId))
                },
                onSettingsClick = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }

        composable(
            route = Screen.Folder.route,
            arguments = listOf(
                navArgument("folderId") { type = NavType.StringType }
            )
        ) {
            FolderDetailScreen(
                onNavigateBack = { navController.popBackStack() },
                onDocumentClick = { documentId ->
                    navController.navigate(Screen.DocumentViewer.createRoute(documentId))
                }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.DocumentViewer.route,
            arguments = listOf(
                navArgument("documentId") { type = NavType.StringType }
            )
        ) {
            DocumentViewerScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
