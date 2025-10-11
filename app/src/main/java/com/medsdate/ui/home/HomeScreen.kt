package com.medsdate.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.medsdate.ui.components.MedicineCard
import com.medsdate.ui.theme.ExpiredRedLight
import org.koin.androidx.compose.koinViewModel

/**
 * Home screen displaying list of medicines grouped by expiry status.
 *
 * Features:
 * - Search functionality
 * - Grouped display (expired first, then active)
 * - Empty state when no medicines
 *
 * @param onMedicineClick Callback when medicine card is clicked
 * @param onSearchClick Callback when search icon is clicked
 * @param viewModel The HomeViewModel
 */
@Composable
fun HomeScreen(
    onMedicineClick: (Int) -> Unit,
    onSearchClick: () -> Unit,
    viewModel: HomeViewModel = koinViewModel()
) {
    val groupedMedicines by viewModel.groupedMedicines.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    var isSearchActive by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Search bar or regular state
        if (isSearchActive) {
            SearchBar(
                query = searchQuery,
                onQueryChange = viewModel::onSearchQueryChange,
                onClose = {
                    isSearchActive = false
                    viewModel.clearSearch()
                }
            )
        }

        // Content
        if (groupedMedicines.expired.isEmpty() && groupedMedicines.active.isEmpty()) {
            EmptyState()
        } else {
            MedicineList(
                groupedMedicines = groupedMedicines,
                onMedicineClick = onMedicineClick,
                isSearchActive = isSearchActive,
                onSearchClick = { isSearchActive = true }
            )
        }
    }
}

/**
 * Search bar composable.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClose: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.primary,
        tonalElevation = 3.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.padding(start = 8.dp)
            )

            TextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp),
                placeholder = { Text("Search medicines...") },
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.primary,
                    unfocusedContainerColor = MaterialTheme.colorScheme.primary,
                    focusedIndicatorColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f),
                    unfocusedIndicatorColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.3f),
                    focusedTextColor = MaterialTheme.colorScheme.onPrimary,
                    unfocusedTextColor = MaterialTheme.colorScheme.onPrimary
                )
            )

            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close search",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

/**
 * List of medicines grouped by status.
 */
@Composable
private fun MedicineList(
    groupedMedicines: GroupedMedicines,
    onMedicineClick: (Int) -> Unit,
    isSearchActive: Boolean,
    onSearchClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Search button when not searching
        if (!isSearchActive && (groupedMedicines.expired.isNotEmpty() || groupedMedicines.active.isNotEmpty())) {
            item {
                OutlinedButton(
                    onClick = onSearchClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Search medicines")
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        // Expired medicines section
        if (groupedMedicines.expired.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "Expired Medicines",
                    count = groupedMedicines.expired.size,
                    isExpired = true
                )
            }

            items(groupedMedicines.expired, key = { it.id }) { medicine ->
                MedicineCard(
                    medicine = medicine,
                    onClick = { onMedicineClick(medicine.id) }
                )
            }

            if (groupedMedicines.active.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }

        // Active medicines section
        if (groupedMedicines.active.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "Active Medicines",
                    count = groupedMedicines.active.size,
                    isExpired = false
                )
            }

            items(groupedMedicines.active, key = { it.id }) { medicine ->
                MedicineCard(
                    medicine = medicine,
                    onClick = { onMedicineClick(medicine.id) }
                )
            }
        }
    }
}

/**
 * Section header for grouped lists.
 */
@Composable
private fun SectionHeader(
    title: String,
    count: Int,
    isExpired: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .then(
                if (isExpired) {
                    Modifier.background(
                        ExpiredRedLight.copy(alpha = 0.2f),
                        shape = MaterialTheme.shapes.small
                    ).padding(horizontal = 12.dp, vertical = 8.dp)
                } else {
                    Modifier.padding(horizontal = 4.dp)
                }
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = if (isExpired) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.onSurface
            }
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "($count)",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Empty state when no medicines exist.
 */
@Composable
private fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "📦",
            style = MaterialTheme.typography.displayLarge
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No Medicines Yet",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Add your first medicine using the + button below",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
