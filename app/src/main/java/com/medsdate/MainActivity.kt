package com.medsdate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.medsdate.ui.components.MedsBottomNavigation
import com.medsdate.ui.navigation.NavGraph
import com.medsdate.ui.navigation.Screen
import com.medsdate.ui.theme.MedsDateTheme

/**
 * Main Activity for MedsDate app using Jetpack Compose.
 *
 * This activity hosts the entire app navigation using Compose Navigation
 * and displays a bottom navigation bar for main sections.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MedsDateTheme {
                MedsDateApp()
            }
        }
    }
}

/**
 * Main app composable with navigation and bottom bar.
 */
@Composable
fun MedsDateApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Determine if bottom bar should be shown
    val showBottomBar = when (currentRoute) {
        Screen.Home.route,
        Screen.Add.route,
        Screen.Settings.route -> true
        else -> false
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                MedsBottomNavigation(navController = navController)
            }
        }
    ) { paddingValues ->
        NavGraph(
            navController = navController,
            startDestination = Screen.Home.route
        )
    }
}
