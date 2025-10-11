package com.medsdate.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Sealed class representing all navigation destinations in the app.
 *
 * Each screen has a unique route used by Compose Navigation.
 */
sealed class Screen(val route: String) {
    /**
     * Home screen showing list of medicines.
     */
    object Home : Screen("home")

    /**
     * Add new medicine screen.
     */
    object Add : Screen("add")

    /**
     * Edit existing medicine screen.
     * Route includes medicine ID as parameter.
     */
    object Edit : Screen("edit/{medicineId}") {
        fun createRoute(medicineId: Int) = "edit/$medicineId"
    }

    /**
     * Medicine detail screen.
     * Route includes medicine ID as parameter.
     */
    object Detail : Screen("detail/{medicineId}") {
        fun createRoute(medicineId: Int) = "detail/$medicineId"
    }

    /**
     * Settings screen for notification preferences.
     */
    object Settings : Screen("settings")

    /**
     * Camera screen for taking medicine photos.
     */
    object Camera : Screen("camera")
}

/**
 * Bottom navigation items.
 */
sealed class BottomNavItem(
    val screen: Screen,
    val title: String,
    val icon: ImageVector
) {
    object Home : BottomNavItem(
        screen = Screen.Home,
        title = "Home",
        icon = Icons.Default.Home
    )

    object Add : BottomNavItem(
        screen = Screen.Add,
        title = "Add",
        icon = Icons.Default.Add
    )

    object Settings : BottomNavItem(
        screen = Screen.Settings,
        title = "Settings",
        icon = Icons.Default.Settings
    )
}
