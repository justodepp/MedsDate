package com.medsdate.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.medsdate.R
import com.medsdate.domain.model.Medicine
import com.medsdate.ui.theme.MedsDateTheme

/**
 * Swipeable card wrapper that deletes a medicine when swiped left.
 *
 * Uses Material3's SwipeToDismissBox for standard swipe-to-dismiss behavior.
 * Swipe left to delete the medicine item.
 *
 * @param medicine The medicine to display
 * @param index The index of the card in the list
 * @param medicineCount Total number of medicines
 * @param onDelete Callback when item is dismissed (swiped away)
 * @param onClick Callback when card is clicked
 * @param showConfirmDialog Whether to show confirmation dialog before deleting (default: true)
 * @param modifier Modifier for customization
 */
@Composable
fun SwipeToDeleteCard(
    medicine: Medicine,
    index: Int,
    medicineCount: Int,
    onDelete: () -> Unit,
    onClick: () -> Unit,
    showConfirmDialog: Boolean = true,
    modifier: Modifier = Modifier
) {
    // Track if confirmation dialog should be shown
    var showDialog by remember { mutableStateOf(false) }

    // Calculate rounded corners based on position in list
    val cornerValue = 20.dp
    var currentShape = when (index) {
        0 -> RoundedCornerShape(topStart = cornerValue, topEnd = cornerValue)
        medicineCount - 1 -> RoundedCornerShape(bottomStart = cornerValue, bottomEnd = cornerValue)
        else -> RectangleShape
    }
    currentShape = if (medicineCount == 1) RoundedCornerShape(cornerValue) else currentShape

    // Swipe-to-dismiss state
    @Suppress("DEPRECATION")
    val swipeToDismissBoxState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            when (dismissValue) {
                SwipeToDismissBoxValue.EndToStart -> {
                    // Swiped left - show dialog or delete directly
                    if (showConfirmDialog) {
                        showDialog = true
                        false // Don't dismiss yet, wait for confirmation
                    } else {
                        onDelete()
                        true // Confirm the dismissal
                    }
                }
                else -> false // Don't allow swipe right
            }
        }
    )

    // Show confirmation dialog if needed
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = {
                Text(stringResource(R.string.delete_medicine_title))
            },
            text = {
                Text(
                    stringResource(
                        R.string.delete_medicine_message,
                        medicine.name
                    ),
                    color = MaterialTheme.colorScheme.primaryContainer
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDialog = false
                        onDelete()
                    }
                ) {
                    Text(
                        stringResource(R.string.detail_delete_confirm),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text(stringResource(R.string.detail_delete_cancel))
                }
            }
        )
    }

    SwipeToDismissBox(
        state = swipeToDismissBoxState,
        modifier = modifier.height(IntrinsicSize.Max),
        enableDismissFromStartToEnd = false, // Only allow left swipe
        backgroundContent = {
            // Show delete icon when swiping
            when (swipeToDismissBoxState.dismissDirection) {
                SwipeToDismissBoxValue.EndToStart -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                color = MaterialTheme.colorScheme.errorContainer,
                                shape = currentShape
                            ),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = stringResource(R.string.delete_medicine),
                            modifier = Modifier.padding(horizontal = 24.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
                else -> {}
            }
        }
    ) {
        // The medicine card itself
        MedicineCard(
            medicine = medicine,
            index = index,
            medicineCount = medicineCount,
            onClick = onClick
        )
    }
}

// ==================== Previews ====================

@Preview(showBackground = true)
@Composable
private fun SwipeToDeleteCardPreview() {
    MedsDateTheme {
        val calendar = java.util.Calendar.getInstance()
        calendar.add(java.util.Calendar.DAY_OF_MONTH, 5)

        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SwipeToDeleteCard(
                medicine = Medicine(
                    id = 1,
                    name = "Aspirin 500mg",
                    imagePath = null,
                    expiryDate = calendar.time,
                    notes = "Take with food",
                    updatedAt = java.util.Date()
                ),
                index = 0,
                medicineCount = 1,
                onDelete = {},
                onClick = {}
            )
        }
    }
}
