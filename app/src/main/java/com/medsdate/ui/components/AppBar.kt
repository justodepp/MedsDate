package com.medsdate.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.medsdate.R

/**
 * Standard top app bar for the MedsDate app.
 *
 * @param title The title text to display
 * @param onNavigationClick Callback for navigation icon click (back button)
 * @param showNavigationIcon Whether to show the navigation icon
 * @param showAppIcon Whether to show the app icon instead of navigation icon
 * @param actions Optional actions (like search, menu) to display
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedsAppBar(
    title: String,
    onNavigationClick: (() -> Unit)? = null,
    showNavigationIcon: Boolean = false,
    showAppIcon: Boolean = false,
    actions: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (showAppIcon && !showNavigationIcon) {
                    Icon(
                        painter = painterResource(id = R.drawable.starter_image),
                        contentDescription = "MedsDate",
                        modifier = Modifier.size(28.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                }
                Text(
                    text = title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
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

// ==================== Previews ====================

/**
 * Preview for MedsAppBar with navigation icon.
 */
@Preview(showBackground = true)
@Composable
fun MedsAppBarPreview() {
    MaterialTheme {
        MedsAppBar(
            title = "Medicine Details",
            onNavigationClick = {},
            showNavigationIcon = true
        )
    }
}

/**
 * Preview for MedsAppBar without navigation icon.
 */
@Preview(showBackground = true)
@Composable
fun MedsAppBarNoNavPreview() {
    MaterialTheme {
        MedsAppBar(
            title = "Home",
            showNavigationIcon = false
        )
    }
}

/**
 * Preview for MedsAppBar with app icon.
 */
@Preview(showBackground = true)
@Composable
fun MedsAppBarWithIconPreview() {
    MaterialTheme {
        MedsAppBar(
            title = "MedsDate",
            showAppIcon = true
        )
    }
}

/**
 * Preview for MedsAppBarWithSearch.
 */
@Preview(showBackground = true)
@Composable
fun MedsAppBarWithSearchPreview() {
    MaterialTheme {
        MedsAppBarWithSearch(
            title = "Medicines",
            onSearchClick = {}
        )
    }
}