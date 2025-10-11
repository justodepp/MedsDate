package com.medsdate.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.medsdate.ui.components.MedsAppBar
import com.medsdate.ui.components.MedsBottomNavigation
import org.koin.androidx.compose.koinViewModel
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
    viewModel: SettingsViewModel = koinViewModel()
) {
    val settings by viewModel.settings.collectAsState()
    val saveState by viewModel.saveState.collectAsState()

    Scaffold(
        topBar = {
            MedsAppBar(
                title = "Settings",
                showAppIcon = true
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
                checked = settings.enableNotifications,
                onCheckedChange = viewModel::onNotificationsToggle
            )
        }

        if (settings.enableNotifications) {
            // First notification slider
            NotificationSlider(
                title = "First Notification",
                value = settings.firstNotificationDays,
                onValueChange = { viewModel.onFirstNotificationDaysChange(it.roundToInt()) }
            )

            Divider()

            // Second notification toggle
            SettingRow(
                title = "Enable Second Notification",
                description = "Receive a second reminder closer to expiry"
            ) {
                Switch(
                    checked = settings.enableSecondNotification,
                    onCheckedChange = viewModel::onSecondNotificationToggle
                )
            }

            // Second notification slider
            if (settings.enableSecondNotification) {
                NotificationSlider(
                    title = "Second Notification",
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
                    Text("Saved!")
                }
                else -> {
                    Text("Save Settings")
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
                text = "$value day${if (value != 1) "s" else ""}",
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
                text = "1 day",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "30 days",
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
