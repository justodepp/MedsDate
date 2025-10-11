package com.medsdate.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.medsdate.ui.add.AddEditScreen
import com.medsdate.ui.camera.CameraScreen
import com.medsdate.ui.detail.DetailScreen
import com.medsdate.ui.home.HomeScreen
import com.medsdate.ui.settings.SettingsScreen

/**
 * Main navigation graph for the app.
 *
 * Defines all navigation routes and their corresponding composable screens.
 *
 * @param navController The navigation controller
 * @param startDestination The initial destination (defaults to Home)
 * @param modifier Modifier to apply to the NavHost
 */
@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Home.route,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // Home screen
        composable(route = Screen.Home.route) {
            HomeScreen(
                onMedicineClick = { medicineId ->
                    navController.navigate(Screen.Detail.createRoute(medicineId))
                },
                onSearchClick = {
                    // Search is handled within HomeScreen
                }
            )
        }

        // Add medicine screen
        composable(route = Screen.Add.route) { backStackEntry ->
            val capturedImageUri = navController.currentBackStackEntry
                ?.savedStateHandle
                ?.get<String>("captured_image_uri")

            // Clear the saved state after reading
            navController.currentBackStackEntry
                ?.savedStateHandle
                ?.remove<String>("captured_image_uri")

            AddEditScreen(
                medicineId = null,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToCamera = {
                    navController.navigate(Screen.Camera.route)
                },
                capturedImageUri = capturedImageUri,
                onSaveSuccess = {
                    navController.popBackStack()
                }
            )
        }

        // Edit medicine screen
        composable(
            route = Screen.Edit.route,
            arguments = listOf(
                navArgument("medicineId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val medicineId = backStackEntry.arguments?.getInt("medicineId")
            val capturedImageUri = navController.currentBackStackEntry
                ?.savedStateHandle
                ?.get<String>("captured_image_uri")

            // Clear the saved state after reading
            navController.currentBackStackEntry
                ?.savedStateHandle
                ?.remove<String>("captured_image_uri")

            AddEditScreen(
                medicineId = medicineId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToCamera = {
                    navController.navigate(Screen.Camera.route)
                },
                capturedImageUri = capturedImageUri,
                onSaveSuccess = {
                    navController.popBackStack()
                }
            )
        }

        // Detail screen
        composable(
            route = Screen.Detail.route,
            arguments = listOf(
                navArgument("medicineId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val medicineId = backStackEntry.arguments?.getInt("medicineId") ?: return@composable
            DetailScreen(
                medicineId = medicineId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onEdit = { id ->
                    navController.navigate(Screen.Edit.createRoute(id))
                },
                onDeleteSuccess = {
                    navController.popBackStack()
                }
            )
        }

        // Settings screen
        composable(route = Screen.Settings.route) {
            SettingsScreen()
        }

        // Camera screen
        composable(route = Screen.Camera.route) {
            CameraScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onImageCaptured = { uri ->
                    // Navigate back with result
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("captured_image_uri", uri.toString())
                    navController.popBackStack()
                }
            )
        }
    }
}
