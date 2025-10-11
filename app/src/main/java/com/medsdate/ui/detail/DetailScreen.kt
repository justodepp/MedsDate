package com.medsdate.ui.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * Detail screen showing full medicine information.
 *
 * @param medicineId Medicine ID to display
 * @param onNavigateBack Callback to navigate back
 * @param onEdit Callback to edit medicine
 * @param onDeleteSuccess Callback when delete is successful
 * @param viewModel The DetailViewModel
 */
@Composable
fun DetailScreen(
    medicineId: Int,
    onNavigateBack: () -> Unit,
    onEdit: (Int) -> Unit,
    onDeleteSuccess: () -> Unit,
    viewModel: DetailViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Load medicine
    LaunchedEffect(medicineId) {
        viewModel.loadMedicine(medicineId)
    }

    // Handle events
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is DetailEvent.DeleteSuccess -> onDeleteSuccess()
                is DetailEvent.DeleteError -> {
                    // Show error snackbar
                }
            }
        }
    }

    Scaffold(
        topBar = {
            DetailAppBar(
                onNavigateBack = onNavigateBack,
                onEdit = {
                    (uiState as? DetailUiState.Success)?.medicine?.id?.let { onEdit(it) }
                },
                onDelete = { showDeleteDialog = true }
            )
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is DetailUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is DetailUiState.Success -> {
                DetailContent(
                    medicine = state.medicine,
                    modifier = Modifier.padding(paddingValues)
                )
            }

            is DetailUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }

    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Medicine?") },
            text = { Text("Are you sure you want to delete this medicine? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteMedicine()
                        showDeleteDialog = false
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

/**
 * Top app bar with edit and delete actions.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DetailAppBar(
    onNavigateBack: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    TopAppBar(
        title = { Text("Medicine Details") },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back"
                )
            }
        },
        actions = {
            IconButton(onClick = onEdit) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit"
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete"
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
 * Content displaying medicine details.
 */
@Composable
private fun DetailContent(
    medicine: com.medsdate.domain.model.Medicine,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Medicine image
        if (medicine.imagePath != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            ) {
                AsyncImage(
                    model = medicine.imagePath,
                    contentDescription = medicine.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp),
                    contentScale = ContentScale.Crop
                )
            }
        }

        // Name
        DetailField(
            label = "Name",
            value = medicine.name
        )

        // Expiry date
        val dateFormatter = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
        DetailField(
            label = "Expiry Date",
            value = dateFormatter.format(medicine.expiryDate)
        )

        // Days until expiry
        val daysUntilExpiry = medicine.daysUntilExpiry()
        val expiryText = when {
            daysUntilExpiry < 0 -> "Expired ${-daysUntilExpiry} days ago"
            daysUntilExpiry == 0L -> "Expires today"
            daysUntilExpiry == 1L -> "Expires tomorrow"
            else -> "Expires in $daysUntilExpiry days"
        }
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = when {
                    daysUntilExpiry < 0 -> MaterialTheme.colorScheme.errorContainer
                    daysUntilExpiry <= 7 -> MaterialTheme.colorScheme.tertiaryContainer
                    else -> MaterialTheme.colorScheme.primaryContainer
                }
            )
        ) {
            Text(
                text = expiryText,
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }

        // Notes
        if (!medicine.notes.isNullOrBlank()) {
            DetailField(
                label = "Notes",
                value = medicine.notes
            )
        }
    }
}

/**
 * Detail field component.
 */
@Composable
private fun DetailField(
    label: String,
    value: String
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
