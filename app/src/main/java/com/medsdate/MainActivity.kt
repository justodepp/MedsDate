package com.medsdate

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import com.medsdate.ui.navigation.NavGraph
import com.medsdate.ui.navigation.Screen
import com.medsdate.ui.theme.MedsDateTheme
import com.medsdate.util.LocaleManager

/**
 * Main Activity for MedsDate app using Jetpack Compose.
 *
 * This activity hosts the entire app navigation using Compose Navigation
 * and displays a bottom navigation bar for main sections.
 *
 * Extends AppCompatActivity so AppCompatDelegate.setApplicationLocales works below API 33.
 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Needs an active AppCompat delegate: from Application.onCreate the call is silently dropped on API 33+
        LocaleManager.migrateLegacyLanguage(this)

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
