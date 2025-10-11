package com.medsdate.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.medsdate.ui.navigation.Screen
import com.medsdate.ui.theme.MedsDateTheme

/**
 * Bottom navigation bar for main app navigation.
 *
 * Displays three navigation items: Home, Add, and Settings.
 *
 * @param navController Navigation controller for handling navigation
 */
@Composable
fun MedsBottomNavigation(navController: NavController) {
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Add,
        BottomNavItem.Settings
    )

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surfaceVariant
    ){
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        items.forEach { item ->
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label
                    )
                },
                alwaysShowLabel = false,
                label = { Text(item.label) },
                selected = currentRoute == item.route,
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            // Pop up to the start destination to avoid building up a large stack
                            popUpTo(Screen.Home.route) {
                                saveState = true
                            }
                            // Avoid multiple copies of the same destination
                            launchSingleTop = true
                            // Restore state when navigating back
                            restoreState = true
                        }
                    }
                }
            )
        }
    }
}

/**
 * Data class representing a bottom navigation item.
 */
private data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    companion object {
        val Home = BottomNavItem(
            route = Screen.Home.route,
            label = "Home",
            icon = Icons.Default.Home
        )

        val Add = BottomNavItem(
            route = Screen.Add.route,
            label = "Add",
            icon = Icons.Default.Add
        )

        val Settings = BottomNavItem(
            route = Screen.Settings.route,
            label = "Settings",
            icon = Icons.Default.Settings
        )
    }
}

// ==================== Previews ====================

/**
 * Preview for MedsBottomNavigation.
 */
@Preview(showBackground = true)
@Composable
fun MedsBottomNavigationPreview() {
    MedsDateTheme {
        MedsBottomNavigation(
            navController = rememberNavController()
        )
    }
}
