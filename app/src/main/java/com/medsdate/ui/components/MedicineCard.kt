package com.medsdate.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Egg
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.medsdate.domain.model.ExpiryStatus
import com.medsdate.domain.model.Medicine
import com.medsdate.ui.theme.ExpiredRed
import com.medsdate.ui.theme.ExpiredRedLight
import com.medsdate.ui.theme.WarningYellow
import java.text.SimpleDateFormat
import java.util.*

/**
 * Card component displaying medicine information.
 *
 * Shows medicine image, name, expiry date, and warning icon based on expiry status.
 *
 * @param medicine The medicine to display
 * @param onClick Callback when card is clicked
 * @param modifier Modifier for customization
 */
@Composable
fun MedicineCard(
    medicine: Medicine,
    index: Int,
    medicineCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cornerValue = 20.dp
    val expiryStatus = medicine.getExpiryStatus()
    val backgroundColor = when (expiryStatus) {
        ExpiryStatus.EXPIRED -> ExpiredRedLight.copy(alpha = 0.3f)
        ExpiryStatus.EXPIRING_SOON -> MaterialTheme.colorScheme.surfaceVariant
        ExpiryStatus.VALID -> MaterialTheme.colorScheme.surface
    }

    val currentShape = when (index) {
        0 -> RoundedCornerShape( topStart = cornerValue, topEnd = cornerValue)
        medicineCount - 1 -> RoundedCornerShape(bottomStart = cornerValue, bottomEnd = cornerValue)
        else -> RectangleShape
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = currentShape,
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Medicine image
            MedicineImage(
                imagePath = medicine.imagePath,
                contentDescription = medicine.name,
                modifier = Modifier.size(56.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Medicine info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = medicine.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = formatExpiryDate(medicine.expiryDate),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Warning/Error icon
            if (expiryStatus != ExpiryStatus.VALID) {
                Spacer(modifier = Modifier.width(8.dp))
                ExpiryWarningIcon(status = expiryStatus)
            }
        }
    }
}

/**
 * Displays medicine image or placeholder.
 */
@Composable
private fun MedicineImage(
    imagePath: String?,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    if (!imagePath.isNullOrEmpty()) {
        AsyncImage(
            model = imagePath,
            contentDescription = contentDescription,
            modifier = modifier.clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )
    } else {
        // Placeholder when no image
        Box(
            modifier = modifier
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = contentDescription.take(2).uppercase(),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Warning icon based on expiry status.
 */
@Composable
private fun ExpiryWarningIcon(status: ExpiryStatus) {
    val (iconColor, backgroundColor) = when (status) {
        ExpiryStatus.EXPIRED -> Pair(Color.White, ExpiredRed)
        ExpiryStatus.EXPIRING_SOON -> Pair(Color.Black, WarningYellow)
        ExpiryStatus.VALID -> return // No icon for valid medicines
    }

    val iconApplied = when (status) {
        ExpiryStatus.EXPIRED -> Icons.Outlined.Egg
        ExpiryStatus.EXPIRING_SOON -> Icons.Outlined.Warning
        else -> return
    }

    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = iconApplied,
            contentDescription = when (status) {
                ExpiryStatus.EXPIRED -> "Expired"
                ExpiryStatus.EXPIRING_SOON -> "Expiring soon"
                ExpiryStatus.VALID -> ""
            },
            tint = iconColor,
            modifier = Modifier.size(20.dp)
        )
    }
}

/**
 * Formats expiry date for display.
 */
private fun formatExpiryDate(date: Date): String {
    val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    return "Expires: ${formatter.format(date)}"
}


@Preview
@Composable
fun MedicineCardPreview() {
    MedicineCard(
        medicine = Medicine(
            id = 1,
            name = "Medicine Name",
            imagePath = "https://example.com/image.jpg",
            expiryDate = Date()
        ),
        index = 1,
        medicineCount = 3,
        onClick = {}
    )
}