package com.medsdate.ui.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.Notes
import androidx.compose.material.icons.outlined.Today
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.medsdate.R
import com.medsdate.domain.model.Medicine
import com.medsdate.ui.theme.MedsDateTheme
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

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
            title = { Text(stringResource(R.string.detail_delete_dialog_title)) },
            text = { Text(stringResource(R.string.detail_delete_dialog_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteMedicine()
                        showDeleteDialog = false
                    }
                ) {
                    Text(stringResource(R.string.detail_delete_confirm), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.detail_delete_cancel))
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
        title = { Text(stringResource(R.string.detail_screen_title)) },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = stringResource(R.string.detail_back)
                )
            }
        },
        actions = {
            IconButton(onClick = onEdit) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = stringResource(R.string.detail_edit)
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.detail_delete)
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
    medicine: Medicine,
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
            type = Type.Name,
            label = stringResource(R.string.detail_field_name),
            value = medicine.name
        )

        // Expiry date
        val dateFormatter = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
        DetailField(
            type = Type.ExpiryDate,
            label = stringResource(R.string.detail_field_expiry_date),
            value = dateFormatter.format(medicine.expiryDate)
        )

        // Notes
        if (!medicine.notes.isNullOrBlank()) {
            DetailField(
                type = Type.Notes,
                label = stringResource(R.string.detail_field_notes),
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
    type: Type,
    label: String,
    value: String
) {
    val iconType = when (type) {
        Type.Name -> return
        Type.ExpiryDate -> Icons.Outlined.Today
        Type.DaysUntilExpiry -> Icons.Outlined.Today
        Type.Notes -> Icons.Outlined.Notes
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(8.dp)
        ) {
            Icon(iconType, contentDescription = stringResource(R.string.detail_calendar_icon))
            Spacer(modifier = Modifier.width(8.dp))
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
    }
}

// ==================== Previews ====================

/**
 * Helper function to create sample medicine for previews.
 */
private fun createSampleMedicine(daysOffset: Int): Medicine {
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.DAY_OF_MONTH, daysOffset)

    return Medicine(
        id = 1,
        name = "Aspirin",
        expiryDate = calendar.time,
        imagePath = null,
        notes = "Take with food. Maximum 2 tablets per day.",
        updatedAt = Date()
    )
}

enum class Type {
    Name,
    ExpiryDate,
    DaysUntilExpiry,
    Notes
}

/**
 * Preview for DetailContent (expired medicine).
 */
@Preview(showBackground = true)
@Composable
private fun DetailContentExpiredPreview() {
    MedsDateTheme {
        DetailContent(
            medicine = createSampleMedicine(-30) // 30 days expired
        )
    }
}

/**
 * Preview for DetailContent (expiring soon).
 */
@Preview(showBackground = true)
@Composable
private fun DetailContentExpiringSoonPreview() {
    MedsDateTheme {
        DetailContent(
            medicine = createSampleMedicine(5) // Expires in 5 days
        )
    }
}

/**
 * Preview for DetailContent (active).
 */
@Preview(showBackground = true)
@Composable
private fun DetailContentActivePreview() {
    MedsDateTheme {
        DetailContent(
            medicine = createSampleMedicine(180) // Expires in 6 months
        )
    }
}

/**
 * Preview for DetailField.
 */
@Preview(showBackground = true)
@Composable
private fun DetailFieldPreview() {
    MedsDateTheme {
        DetailField(
            type = Type.Name,
            label = "Medicine Name",
            value = "Aspirin 500mg"
        )
    }
}

/**
 * Preview for DetailAppBar.
 */
@Preview(showBackground = true)
@Composable
private fun DetailAppBarPreview() {
    MedsDateTheme {
        DetailAppBar(
            onNavigateBack = {},
            onEdit = {},
            onDelete = {}
        )
    }
}
