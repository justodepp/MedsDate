package com.medsdate.ui.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview

/**
 * Standard top app bar for the MedsDate app.
 *
 * @param title The title text to display
 * @param onNavigationClick Callback for navigation icon click (back button)
 * @param showNavigationIcon Whether to show the navigation icon
 * @param actions Optional actions (like search, menu) to display
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedsAppBar(
    title: String,
    onNavigationClick: (() -> Unit)? = null,
    showNavigationIcon: Boolean = false,
    actions: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        navigationIcon = {
            if (showNavigationIcon && onNavigationClick != null) {
                IconButton(onClick = onNavigationClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Navigate back"
                    )
                }
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimary
        )
    )
}

/**
 * App bar with search icon action.
 *
 * @param title The title text to display
 * @param onSearchClick Callback when search icon is clicked
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedsAppBarWithSearch(
    title: String,
    onSearchClick: () -> Unit
) {
    MedsAppBar(
        title = title,
        showNavigationIcon = false,
        actions = {
            IconButton(onClick = onSearchClick) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search"
                )
            }
        }
    )
}

@Preview
@Composable
fun MedsAppBarPreview() {
    MedsAppBar(
        title = "My App",
        onNavigationClick = { /* Handle navigation click */ },
        showNavigationIcon = true
    )
}

@Preview
@Composable
fun MedsAppBarWithSearchPreview() {
    MedsAppBarWithSearch(
        title = "My App",
        onSearchClick = { /* Handle navigation click */ }
    )
}