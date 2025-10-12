package com.medsdate.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.medsdate.R
import com.medsdate.domain.model.Medicine
import com.medsdate.ui.theme.MedsDateTheme
import kotlinx.coroutines.launch
import kotlin.math.abs

/**
 * Swipeable card wrapper that reveals a delete button underneath when swiped left.
 *
 * @param medicine The medicine to display
 * @param index The index of the card in the list
 * @param medicineCount Total number of medicines
 * @param onDelete Callback when delete button is tapped
 * @param onClick Callback when card is clicked
 * @param modifier Modifier for customization
 */
@Composable
fun SwipeToDeleteCard(
    medicine: Medicine,
    index: Int,
    medicineCount: Int,
    onDelete: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current

    // Swipe state
    var offsetX by remember { mutableFloatStateOf(0f) }
    var isRevealed by remember { mutableStateOf(false) }

    // Maximum swipe distance (120dp button width)
    val maxSwipeDistance = with(density) { -120.dp.toPx() }
    val revealThreshold = abs(maxSwipeDistance) * 0.4f // 40% of max distance

    // Animated offset
    val animatedOffsetX by animateFloatAsState(
        targetValue = if (isRevealed) maxSwipeDistance else 0f,
        label = "swipe_offset"
    )

    val cornerValue = 20.dp
    var currentShape = when (index) {
        0 -> RoundedCornerShape(topStart = cornerValue, topEnd = cornerValue)
        medicineCount - 1 -> RoundedCornerShape(bottomStart = cornerValue, bottomEnd = cornerValue)
        else -> RectangleShape
    }
    currentShape = if (medicineCount == 1) RoundedCornerShape(cornerValue) else currentShape

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Max)
            .clip(currentShape)
    ) {
        // Delete button background (underneath)
        if (isRevealed || offsetX < 0f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.errorContainer),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.delete_medicine),
                    modifier = Modifier
                        .padding(16.dp)
                        .size(20.dp)
                        .clickable(
                            onClick = {
                                scope.launch {
                                    onDelete()
                                }
                            }
                        ),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }

        // Swipeable medicine card (on top)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationX = if (isRevealed) animatedOffsetX else offsetX
                }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            // Determine if we should reveal or hide based on threshold
                            if (abs(offsetX) > revealThreshold && offsetX < 0) {
                                // Swiped left past threshold - reveal delete button
                                isRevealed = true
                            } else {
                                // Not past threshold - hide delete button
                                isRevealed = false
                            }
                            offsetX = 0f
                        },
                        onDragCancel = {
                            isRevealed = false
                            offsetX = 0f
                        },
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()

                            if (!isRevealed) {
                                // Only allow left swipe (negative values)
                                val newOffset =
                                    (offsetX + dragAmount).coerceIn(maxSwipeDistance, 0f)
                                offsetX = newOffset
                            } else if (dragAmount > 0) {
                                // Allow right swipe to close when revealed
                                offsetX = (offsetX + dragAmount).coerceAtMost(0f)
                                if (offsetX > -10f) {
                                    isRevealed = false
                                    offsetX = 0f
                                }
                            }
                        }
                    )
                }
        ) {
            MedicineCard(
                medicine = medicine,
                index = index,
                medicineCount = medicineCount,
                onClick = {
                    if (isRevealed) {
                        // If revealed, close on tap
                        isRevealed = false
                    } else {
                        // Otherwise, handle normal click
                        onClick()
                    }
                }
            )
        }
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
