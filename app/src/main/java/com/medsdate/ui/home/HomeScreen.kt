package com.medsdate.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.medsdate.R
import com.medsdate.domain.model.Medicine
import com.medsdate.ui.components.MedsBottomNavigation
import com.medsdate.ui.components.SwipeToDeleteCard
import com.medsdate.ui.navigation.Screen
import com.medsdate.ui.theme.ExpiredRedLight
import org.koin.androidx.compose.koinViewModel
import java.util.Calendar
import java.util.Date

/**
 * Home screen displaying list of medicines grouped by expiry status.
 *
 * Features:
 * - Search functionality
 * - Grouped display (expired first, then active)
 * - Empty state when no medicines
 *
 * @param navController Navigation controller for bottom nav
 * @param onMedicineClick Callback when medicine card is clicked
 * @param onSearchClick Callback when search icon is clicked
 * @param viewModel The HomeViewModel
 */
@Composable
fun HomeScreen(
    navController: NavController,
    onMedicineClick: (Int) -> Unit,
    onSearchClick: () -> Unit,
    viewModel: HomeViewModel = koinViewModel()
) {
    val groupedMedicines by viewModel.groupedMedicines.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val sortOrder by viewModel.sortOrder.collectAsState()
    val expiringSoonDays by viewModel.expiringSoonDays.collectAsState()
    var isSearchActive by remember { mutableStateOf(false) }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val snackbarHostState = remember { SnackbarHostState() }
    val resources = LocalResources.current

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is HomeEvent.ShowUndoSnackbar -> {
                    val result = snackbarHostState.showSnackbar(
                        message = resources.getString(R.string.medicine_deleted_snackbar, event.medicineName),
                        actionLabel = resources.getString(R.string.action_undo),
                        duration = SnackbarDuration.Short
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        viewModel.undoDelete()
                    }
                }
            }
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            if (!isSearchActive) {
                HomeTopBar(
                    scrollBehavior = scrollBehavior,
                    actions = {
                        IconButton(onClick = { navController.navigate(Screen.Stats.route) }) {
                            Icon(
                                imageVector = Icons.Default.BarChart,
                                contentDescription = stringResource(R.string.stats_open)
                            )
                        }
                        IconButton(onClick = { isSearchActive = true }) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = stringResource(R.string.home_search_description)
                            )
                        }
                    }
                )
            } else {
                SearchBar(
                    query = searchQuery,
                    onQueryChange = viewModel::onSearchQueryChange,
                    onClose = {
                        isSearchActive = false
                        viewModel.clearSearch()
                    }
                )
            }
        },
        bottomBar = {
            MedsBottomNavigation(navController = navController)
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(color = MaterialTheme.colorScheme.background)
        ) {
            // Content
            val grouped = groupedMedicines
            if (grouped == null) {
                LoadingState()
            } else if (grouped.expired.isEmpty() && grouped.active.isEmpty()) {
                EmptyState()
            } else {
                MedicineList(
                    groupedMedicines = grouped,
                    sortOrder = sortOrder,
                    onSortOrderChange = viewModel::onSortOrderChange,
                    onMedicineClick = onMedicineClick,
                    isSearchActive = isSearchActive,
                    onSearchClick = { isSearchActive = true },
                    expiringSoonDays = expiringSoonDays
                )
            }
        }
    }
}

/**
 * Search bar composable that replaces the app bar.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClose: () -> Unit
) {
    // Use TopAppBar for proper status bar handling
    TopAppBar(
        title = {
            TextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(stringResource(R.string.home_search_placeholder)) },
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.primary,
                    unfocusedContainerColor = MaterialTheme.colorScheme.primary,
                    focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                    unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                    focusedTextColor = MaterialTheme.colorScheme.onPrimary,
                    unfocusedTextColor = MaterialTheme.colorScheme.onPrimary,
                    cursorColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        navigationIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = stringResource(R.string.home_search_description),
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.padding(start = 8.dp)
            )
        },
        actions = {
            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(R.string.home_close_search),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimary
        )
    )
}

/**
 * List of medicines grouped by status.
 */
@Composable
private fun MedicineList(
    groupedMedicines: GroupedMedicines,
    sortOrder: SortOrder,
    onSortOrderChange: (SortOrder) -> Unit,
    onMedicineClick: (Int) -> Unit,
    isSearchActive: Boolean,
    onSearchClick: () -> Unit,
    expiringSoonDays: Int = 7,
    viewModel: HomeViewModel = koinViewModel()
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        item(key = "sort") {
            SortRow(
                sortOrder = sortOrder,
                onSortOrderChange = onSortOrderChange,
                modifier = Modifier.animateItem()
            )
        }

        // Expired medicines section
        if (groupedMedicines.expired.isNotEmpty()) {
            item(key = "header-expired") {
                SectionHeader(
                    title = stringResource(R.string.home_expired_medicines),
                    count = groupedMedicines.expired.size,
                    isExpired = true,
                    modifier = Modifier.animateItem()
                )
            }

            itemsIndexed(
                groupedMedicines.expired,
                key = { index, medicine -> medicine.id }) { index, medicine ->
                SwipeToDeleteCard(
                    medicine = medicine,
                    index = index,
                    medicineCount = groupedMedicines.expired.size,
                    onDelete = { viewModel.deleteMedicine(medicine) },
                    onClick = { onMedicineClick(medicine.id) },
                    showConfirmDialog = false,
                    expiringSoonDays = expiringSoonDays,
                    modifier = Modifier.animateItem()
                )
            }

            if (groupedMedicines.active.isNotEmpty()) {
                item(key = "spacer") {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }

        // Active medicines section
        if (groupedMedicines.active.isNotEmpty()) {
            item(key = "header-active") {
                SectionHeader(
                    title = stringResource(R.string.home_active_medicines),
                    count = groupedMedicines.active.size,
                    isExpired = false,
                    modifier = Modifier.animateItem()
                )
            }

            itemsIndexed(groupedMedicines.active, key = { index, medicine -> medicine.id }) { index, medicine ->
                SwipeToDeleteCard(
                    medicine = medicine,
                    index = index,
                    medicineCount = groupedMedicines.active.size,
                    onDelete = { viewModel.deleteMedicine(medicine) },
                    onClick = { onMedicineClick(medicine.id) },
                    showConfirmDialog = false,
                    expiringSoonDays = expiringSoonDays,
                    modifier = Modifier.animateItem()
                )
            }
        }
    }
}

/**
 * Large collapsing top bar: the title shrinks when the list scrolls.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun HomeTopBar(
    scrollBehavior: TopAppBarScrollBehavior,
    actions: @Composable RowScope.() -> Unit
) {
    LargeFlexibleTopAppBar(
        title = { Text("MedsDate") },
        navigationIcon = {
            Icon(
                painter = painterResource(id = R.drawable.starter_image),
                contentDescription = null,
                modifier = Modifier.padding(start = 16.dp, end = 4.dp).size(28.dp)
            )
        },
        actions = actions,
        scrollBehavior = scrollBehavior,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            scrolledContainerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimary
        )
    )
}

/**
 * Sort by expiry or name; tapping the active one flips the direction.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SortRow(
    sortOrder: SortOrder,
    onSortOrderChange: (SortOrder) -> Unit,
    modifier: Modifier = Modifier
) {
    val byExpiry = sortOrder == SortOrder.EXPIRY_ASC || sortOrder == SortOrder.EXPIRY_DESC
    val ascending = sortOrder == SortOrder.EXPIRY_ASC || sortOrder == SortOrder.NAME_ASC

    fun toggle(expiry: Boolean) = onSortOrderChange(
        when {
            expiry && byExpiry -> if (ascending) SortOrder.EXPIRY_DESC else SortOrder.EXPIRY_ASC
            expiry -> SortOrder.EXPIRY_ASC
            !byExpiry -> if (ascending) SortOrder.NAME_DESC else SortOrder.NAME_ASC
            else -> SortOrder.NAME_ASC
        }
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)
    ) {
        listOf(true to R.string.home_sort_by_expiry, false to R.string.home_sort_by_name).forEachIndexed { i, (expiry, label) ->
            val checked = byExpiry == expiry
            ToggleButton(
                checked = checked,
                onCheckedChange = { toggle(expiry) },
                modifier = Modifier.weight(1f),
                shapes = if (i == 0) ButtonGroupDefaults.connectedLeadingButtonShapes()
                else ButtonGroupDefaults.connectedTrailingButtonShapes(),
                colors = ToggleButtonDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    checkedContainerColor = MaterialTheme.colorScheme.primary,
                    checkedContentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                if (checked) {
                    Icon(
                        imageVector = if (ascending) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                }
                Text(stringResource(label))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun LoadingState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        LoadingIndicator(color = MaterialTheme.colorScheme.secondary)
    }
}

/**
 * Section header for grouped lists.
 */
@Composable
private fun SectionHeader(
    title: String,
    count: Int,
    isExpired: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$title ($count)".uppercase(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = if (isExpired) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.onSurface
            }
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
            text = stringResource(R.string.home_empty_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.home_empty_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ==================== Previews ====================

/**
 * Preview for EmptyState.
 */
@Preview(showBackground = true)
@Composable
private fun EmptyStatePreview() {
    MaterialTheme {
        EmptyState()
    }
}

/**
 * Preview for SectionHeader (expired).
 */
@Preview(showBackground = true)
@Composable
private fun SectionHeaderExpiredPreview() {
    MaterialTheme {
        SectionHeader(
            title = "Expired Medicines",
            count = 3,
            isExpired = true
        )
    }
}

/**
 * Preview for SectionHeader (active).
 */
@Preview(showBackground = true)
@Composable
private fun SectionHeaderActivePreview() {
    MaterialTheme {
        SectionHeader(
            title = "Active Medicines",
            count = 5,
            isExpired = false
        )
    }
}

/**
 * Preview for SearchBar.
 */
@Preview(showBackground = true)
@Composable
private fun SearchBarPreview() {
    MaterialTheme {
        SearchBar(
            query = "Aspirin",
            onQueryChange = {},
            onClose = {}
        )
    }
}

/**
 * Helper function to create sample medicines for previews.
 */
private fun createSampleMedicines(): List<Medicine> {
    val calendar = Calendar.getInstance()

    // Expired medicine
    calendar.add(Calendar.DAY_OF_MONTH, -30)
    val expiredMedicine = Medicine(
        id = 1,
        name = "Aspirin",
        expiryDate = calendar.time,
        imagePath = null,
        notes = "Take with food",
        updatedAt = Date()
    )

    // Medicine expiring soon
    calendar.time = Date()
    calendar.add(Calendar.DAY_OF_MONTH, 15)
    val expiringSoonMedicine = Medicine(
        id = 2,
        name = "Vitamin D",
        expiryDate = calendar.time,
        imagePath = null,
        notes = null,
        updatedAt = Date()
    )

    // Active medicine
    calendar.time = Date()
    calendar.add(Calendar.MONTH, 6)
    val activeMedicine = Medicine(
        id = 3,
        name = "Ibuprofen",
        expiryDate = calendar.time,
        imagePath = null,
        notes = "For headaches",
        updatedAt = Date()
    )

    return listOf(expiredMedicine, expiringSoonMedicine, activeMedicine)
}

/**
 * Preview for MedicineList with medicines.
 */
@Preview(showBackground = true)
@Composable
private fun MedicineListPreview() {
    val medicines = createSampleMedicines()
    MaterialTheme {
        MedicineList(
            groupedMedicines = GroupedMedicines(
                expired = listOf(medicines[0]),
                active = listOf(medicines[1], medicines[2])
            ),
            sortOrder = SortOrder.EXPIRY_ASC,
            onSortOrderChange = {},
            onMedicineClick = {},
            isSearchActive = false,
            onSearchClick = {}
        )
    }
}
