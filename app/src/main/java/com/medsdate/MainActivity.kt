package com.medsdate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
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
 * Main app composable with navigation.
 */
@Composable
fun MedsDateApp() {
    val navController = rememberNavController()

    NavGraph(
        navController = navController,
        startDestination = Screen.Home.route
    )
}
