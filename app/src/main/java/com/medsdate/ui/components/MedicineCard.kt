package com.medsdate.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Egg
import androidx.compose.material.icons.outlined.Error
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.medsdate.R
import com.medsdate.domain.model.ExpiryStatus
import com.medsdate.domain.model.Medicine
import com.medsdate.ui.theme.expiredColors
import com.medsdate.ui.theme.warningColors
import java.text.SimpleDateFormat
import java.util.Date

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
    modifier: Modifier = Modifier,
    expiringSoonDays: Int = 7
) {
    val cornerValue = 20.dp
    val expiryStatus = medicine.getExpiryStatus(expiringSoonDays)
    val backgroundColor = when (expiryStatus) {
        ExpiryStatus.EXPIRED -> expiredColors().container
        ExpiryStatus.EXPIRING_SOON -> warningColors().container
        ExpiryStatus.VALID -> MaterialTheme.colorScheme.surfaceContainerLow
    }

    var currentShape = when (index) {
        0 -> RoundedCornerShape(topStart = cornerValue, topEnd = cornerValue)
        medicineCount - 1 -> RoundedCornerShape(bottomStart = cornerValue, bottomEnd = cornerValue)
        else -> RectangleShape
    }
    currentShape = if (medicineCount == 1) RoundedCornerShape(cornerValue) else currentShape

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
                modifier = Modifier.size(56.dp).let {
                    if (medicine.imagePath != null) it.medicineImageTransition(medicine.id) else it
                }
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

                Spacer(modifier = Modifier.height(2.dp))

                DaysBadge(days = medicine.daysUntilExpiry(), status = expiryStatus)
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
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = contentDescription.take(2).uppercase(),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimary,
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
    val iconColor = when (status) {
        ExpiryStatus.EXPIRED -> expiredColors().accent
        ExpiryStatus.EXPIRING_SOON -> warningColors().accent
        ExpiryStatus.VALID -> return // No icon for valid medicines
    }

    val iconApplied = when (status) {
        ExpiryStatus.EXPIRED -> Icons.Outlined.Error
        ExpiryStatus.EXPIRING_SOON -> Icons.Outlined.Warning
        else -> return
    }

    val contentDescription = when (status) {
        ExpiryStatus.EXPIRED -> stringResource(R.string.medicine_expired_icon)
        ExpiryStatus.EXPIRING_SOON -> stringResource(R.string.medicine_expiring_soon_icon)
        ExpiryStatus.VALID -> ""
    }

    Icon(
        imageVector = iconApplied,
        contentDescription = contentDescription,
        tint = iconColor,
        modifier = Modifier.size(20.dp)
    )

}

/**
 * Small text showing days remaining or days since expiry.
 */
@Composable
private fun DaysBadge(days: Long, status: ExpiryStatus) {
    val expired = expiredColors().accent
    val warning = warningColors().accent
    val (text, color) = when {
        days == 0L -> stringResource(R.string.medicine_expires_today) to warning
        days < 0 -> {
            val absDays = (-days).toInt()
            val label = if (absDays == 1)
                stringResource(R.string.medicine_expired_days_ago_singular, absDays)
            else
                stringResource(R.string.medicine_expired_days_ago_plural, absDays)
            label to expired
        }
        else -> {
            val d = days.toInt()
            val label = if (d == 1)
                stringResource(R.string.medicine_days_left_singular, d)
            else
                stringResource(R.string.medicine_days_left_plural, d)
            val textColor = if (status == ExpiryStatus.EXPIRING_SOON) warning
            else MaterialTheme.colorScheme.onSurfaceVariant
            label to textColor
        }
    }
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = color,
        fontWeight = FontWeight.Medium
    )
}

/**
 * Formats expiry date for display.
 */
@Composable
private fun formatExpiryDate(date: Date): String {
    val locale = LocalConfiguration.current.locales[0]
    val formatter = remember(locale) { SimpleDateFormat("MMM dd, yyyy", locale) }
    return stringResource(R.string.medicine_expires_label, formatter.format(date))
}


// ==================== Previews ====================

/**
 * Preview for MedicineCard (expired medicine).
 */
@Preview(showBackground = true)
@Composable
fun MedicineCardExpiredPreview() {
    MaterialTheme {
        val calendar = java.util.Calendar.getInstance()
        calendar.add(java.util.Calendar.DAY_OF_MONTH, -30)

        MedicineCard(
            medicine = Medicine(
                id = 1,
                name = "Aspirin 500mg",
                imagePath = null,
                expiryDate = calendar.time,
                notes = "Take with food",
                updatedAt = Date()
            ),
            index = 0,
            medicineCount = 3,
            onClick = {}
        )
    }
}

/**
 * Preview for MedicineCard (expiring soon).
 */
@Preview(showBackground = true)
@Composable
fun MedicineCardExpiringSoonPreview() {
    MaterialTheme {
        val calendar = java.util.Calendar.getInstance()
        calendar.add(java.util.Calendar.DAY_OF_MONTH, 5)

        MedicineCard(
            medicine = Medicine(
                id = 2,
                name = "Vitamin D Supplements",
                imagePath = null,
                expiryDate = calendar.time,
                notes = null,
                updatedAt = Date()
            ),
            index = 1,
            medicineCount = 3,
            onClick = {}
        )
    }
}

/**
 * Preview for MedicineCard (valid medicine).
 */
@Preview(showBackground = true)
@Composable
fun MedicineCardValidPreview() {
    MaterialTheme {
        val calendar = java.util.Calendar.getInstance()
        calendar.add(java.util.Calendar.MONTH, 6)

        MedicineCard(
            medicine = Medicine(
                id = 3,
                name = "Ibuprofen",
                imagePath = null,
                expiryDate = calendar.time,
                notes = "For headaches",
                updatedAt = Date()
            ),
            index = 2,
            medicineCount = 3,
            onClick = {}
        )
    }
}

/**
 * Preview for MedicineImage (placeholder).
 */
@Preview(showBackground = true)
@Composable
private fun MedicineImagePlaceholderPreview() {
    MaterialTheme {
        MedicineImage(
            imagePath = null,
            contentDescription = "Aspirin",
            modifier = Modifier.size(56.dp)
        )
    }
}

/**
 * Preview for ExpiryWarningIcon (expired).
 */
@Preview(showBackground = true)
@Composable
private fun ExpiryWarningIconExpiredPreview() {
    MaterialTheme {
        Row(
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            ExpiryWarningIcon(status = ExpiryStatus.EXPIRED)
            ExpiryWarningIcon(status = ExpiryStatus.EXPIRING_SOON)
        }
    }
}