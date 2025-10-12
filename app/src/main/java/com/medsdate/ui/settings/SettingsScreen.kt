package com.medsdate.ui.settings

import android.app.Activity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.medsdate.R
import com.medsdate.billing.BillingManager
import com.medsdate.ui.components.DonateDialog
import com.medsdate.ui.components.MedsAppBar
import com.medsdate.ui.components.MedsBottomNavigation
import com.medsdate.util.LocaleManager
import com.medsdate.util.LocalePreferences
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import kotlin.math.roundToInt

/**
 * Settings screen for notification preferences.
 *
 * Features:
 * - Enable/disable notifications toggle
 * - First notification slider (1-30 days)
 * - Second notification slider (1-30 days)
 * - Enable/disable second notification toggle
 * - Save button with feedback
 *
 * @param navController Navigation controller for bottom nav
 * @param viewModel The SettingsViewModel
 */
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = koinViewModel(),
    billingManager: BillingManager = koinInject()
) {
    val settings by viewModel.settings.collectAsState()
    val saveState by viewModel.saveState.collectAsState()
    val context = LocalContext.current
    val activity = context as? Activity

    var showMenu by remember { mutableStateOf(false) }
    var showLanguageSubmenu by remember { mutableStateOf(false) }
    var showDonateDialog by remember { mutableStateOf(false) }

    // Initialize billing manager
    LaunchedEffect(Unit) {
        billingManager.initialize()
    }

    // Cleanup billing manager on dispose
    DisposableEffect(Unit) {
        onDispose {
            billingManager.destroy()
        }
    }

    Scaffold(
        topBar = {
            MedsAppBar(
                title = stringResource(R.string.settings_title),
                showAppIcon = true,
                actions = {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = stringResource(R.string.menu_more)
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        // Language option
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.language_menu_title)) },
                            onClick = {
                                showMenu = false
                                showLanguageSubmenu = true
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Language,
                                    contentDescription = null
                                )
                            }
                        )

                        // Donate option
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.menu_donate)) },
                            onClick = {
                                showMenu = false
                                showDonateDialog = true
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Favorite,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        )
                    }

                    // Language submenu
                    DropdownMenu(
                        expanded = showLanguageSubmenu,
                        onDismissRequest = { showLanguageSubmenu = false }
                    ) {
                        Text(
                            text = stringResource(R.string.language_menu_title),
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Divider()

                        LocaleManager.Language.entries.forEach { language ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = language.flag,
                                        style = MaterialTheme.typography.headlineSmall
                                    )
                                },
                                onClick = {
                                    // Save to both database and SharedPreferences
                                    viewModel.onLanguageChange(language.code)
                                    LocalePreferences.saveLanguage(context, language.code)
                                    showLanguageSubmenu = false
                                    // Recreate activity to apply new language
                                    activity?.let { LocaleManager.applyLocaleAndRecreate(it, language.code) }
                                },
                                trailingIcon = if (settings.languageCode == language.code) {
                                    { Icon(Icons.Default.Check, contentDescription = null) }
                                } else null
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            MedsBottomNavigation(navController = navController)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Title
            Text(
                text = stringResource(R.string.settings_notification_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Divider()

        // Enable notifications toggle
        SettingRow(
            title = stringResource(R.string.settings_enable_notifications),
            description = stringResource(R.string.settings_enable_notifications_desc)
        ) {
            Switch(
                checked = settings.enableNotifications,
                onCheckedChange = viewModel::onNotificationsToggle
            )
        }

        if (settings.enableNotifications) {
            // First notification slider
            NotificationSlider(
                title = stringResource(R.string.settings_first_notification),
                value = settings.firstNotificationDays,
                onValueChange = { viewModel.onFirstNotificationDaysChange(it.roundToInt()) }
            )

            Divider()

            // Second notification toggle
            SettingRow(
                title = stringResource(R.string.settings_enable_second_notification),
                description = stringResource(R.string.settings_enable_second_notification_desc)
            ) {
                Switch(
                    checked = settings.enableSecondNotification,
                    onCheckedChange = viewModel::onSecondNotificationToggle
                )
            }

            // Second notification slider
            if (settings.enableSecondNotification) {
                NotificationSlider(
                    title = stringResource(R.string.settings_second_notification),
                    value = settings.secondNotificationDays,
                    onValueChange = { viewModel.onSecondNotificationDaysChange(it.roundToInt()) }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Save button
        Button(
            onClick = viewModel::saveSettings,
            modifier = Modifier.fillMaxWidth(),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 2.dp,
                pressedElevation = 0.dp
            ),
            enabled = saveState !is SaveState.Saving
        ) {
            when (saveState) {
                is SaveState.Saving -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
                is SaveState.Success -> {
                    Text(stringResource(R.string.settings_saved))
                }
                else -> {
                    Text(stringResource(R.string.settings_save))
                }
            }
        }

        // Info card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.settings_how_it_works),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.settings_how_it_works_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        }
    }

    // Donate dialog
    if (showDonateDialog) {
        DonateDialog(
            billingManager = billingManager,
            onDismiss = { showDonateDialog = false }
        )
    }
}

/**
 * Generic setting row with title, description, and action.
 */
@Composable
private fun SettingRow(
    title: String,
    description: String,
    action: @Composable () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        action()
    }
}

/**
 * Notification days slider.
 */
@Composable
private fun NotificationSlider(
    title: String,
    value: Int,
    onValueChange: (Float) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = if (value == 1) {
                    stringResource(R.string.settings_days_singular, value)
                } else {
                    stringResource(R.string.settings_days_plural, value)
                },
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Slider(
            value = value.toFloat(),
            onValueChange = onValueChange,
            valueRange = 1f..30f,
            steps = 28, // 30 - 1 (start) - 1 (end) = 28
            modifier = Modifier.fillMaxWidth()
        )

        // Min/Max labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(R.string.settings_days_min),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = stringResource(R.string.settings_days_max),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ==================== Previews ====================

/**
 * Preview for SettingRow with toggle.
 */
@Preview(showBackground = true)
@Composable
private fun SettingRowPreview() {
    MaterialTheme {
        SettingRow(
            title = "Enable Notifications",
            description = "Receive alerts before medicines expire"
        ) {
            Switch(
                checked = true,
                onCheckedChange = {}
            )
        }
    }
}

/**
 * Preview for NotificationSlider.
 */
@Preview(showBackground = true)
@Composable
private fun NotificationSliderPreview() {
    MaterialTheme {
        NotificationSlider(
            title = "First Notification",
            value = 7,
            onValueChange = {}
        )
    }
}

/**
 * Preview for Settings screen content (notifications enabled).
 */
@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun SettingsScreenContentPreview() {
    MaterialTheme {
        Scaffold(
            topBar = {
                MedsAppBar(
                    title = "Settings",
                    showAppIcon = true
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Title
                Text(
                    text = "Notification Settings",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                Divider()

                // Enable notifications toggle
                SettingRow(
                    title = "Enable Notifications",
                    description = "Receive alerts before medicines expire"
                ) {
                    Switch(
                        checked = true,
                        onCheckedChange = {}
                    )
                }

                // First notification slider
                NotificationSlider(
                    title = "First Notification",
                    value = 7,
                    onValueChange = {}
                )

                Divider()

                // Second notification toggle
                SettingRow(
                    title = "Enable Second Notification",
                    description = "Receive a second reminder closer to expiry"
                ) {
                    Switch(
                        checked = true,
                        onCheckedChange = {}
                    )
                }

                // Second notification slider
                NotificationSlider(
                    title = "Second Notification",
                    value = 3,
                    onValueChange = {}
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Save button
                Button(
                    onClick = {},
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save Settings")
                }

                // Info card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "How it works",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "You will receive notifications before your medicines expire based on the days you set above. " +
                                    "For example, if set to 7 days, you'll be notified one week before expiry.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

/**
 * Preview for Settings screen (notifications disabled).
 */
@Preview(showBackground = true)
@Composable
private fun SettingsScreenDisabledPreview() {
    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = "Notification Settings",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Divider()

            // Enable notifications toggle (disabled)
            SettingRow(
                title = "Enable Notifications",
                description = "Receive alerts before medicines expire"
            ) {
                Switch(
                    checked = false,
                    onCheckedChange = {}
                )
            }

            // Save button
            Button(
                onClick = {},
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Settings")
            }
        }
    }
}

/**
 * Preview for Save button loading state.
 */
@Preview(showBackground = true)
@Composable
private fun SaveButtonLoadingPreview() {
    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Saving state
            Button(
                onClick = {},
                modifier = Modifier.fillMaxWidth(),
                enabled = false
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }

            // Success state
            Button(
                onClick = {},
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Saved!")
            }

            // Normal state
            Button(
                onClick = {},
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Settings")
            }
        }
    }
}
