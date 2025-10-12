package com.medsdate

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import com.medsdate.ui.navigation.NavGraph
import com.medsdate.ui.navigation.Screen
import com.medsdate.ui.theme.MedsDateTheme
import com.medsdate.util.LocaleManager
import com.medsdate.util.LocalePreferences

/**
 * Main Activity for MedsDate app using Jetpack Compose.
 *
 * This activity hosts the entire app navigation using Compose Navigation
 * and displays a bottom navigation bar for main sections.
 *
 * The app language is applied in attachBaseContext before any UI is created.
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

    override fun attachBaseContext(newBase: Context) {
        // Apply saved locale before creating the activity
        // This ensures the correct language is used from the start
        val languageCode = LocalePreferences.getLanguage(newBase)
        val localeContext = LocaleManager.setLocale(newBase, languageCode)
        super.attachBaseContext(localeContext)
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
