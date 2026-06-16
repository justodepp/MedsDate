package com.medsdate.ui.stats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Error
import androidx.compose.material.icons.outlined.MedicalServices
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.medsdate.R
import com.medsdate.ui.components.MedsAppBar
import com.medsdate.ui.theme.ExpiredRed
import com.medsdate.ui.theme.ExpiredRedLight
import com.medsdate.ui.theme.WarningYellow
import com.medsdate.ui.theme.WarningYellowLight
import org.koin.androidx.compose.koinViewModel

@Composable
fun StatsScreen(
    onNavigateBack: () -> Unit,
    viewModel: StatsViewModel = koinViewModel()
) {
    val stats by viewModel.stats.collectAsState()

    Scaffold(
        topBar = {
            MedsAppBar(
                title = stringResource(R.string.stats_title),
                showNavigationIcon = true,
                onNavigationClick = onNavigateBack
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Total — full width
            StatCard(
                label = stringResource(R.string.stats_total),
                count = stats.total,
                icon = Icons.Outlined.MedicalServices,
                iconTint = MaterialTheme.colorScheme.primary,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.fillMaxWidth()
            )

            // Expired + Expiring soon — 2 columns
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    label = stringResource(R.string.stats_expired),
                    count = stats.expired,
                    icon = Icons.Outlined.Error,
                    iconTint = ExpiredRed,
                    containerColor = ExpiredRedLight,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    label = stringResource(R.string.stats_expiring_soon),
                    count = stats.expiringSoon,
                    icon = Icons.Outlined.Warning,
                    iconTint = WarningYellow,
                    containerColor = WarningYellowLight,
                    modifier = Modifier.weight(1f)
                )
            }

            // Valid — full width
            StatCard(
                label = stringResource(R.string.stats_valid),
                count = stats.valid,
                icon = Icons.Outlined.CheckCircle,
                iconTint = MaterialTheme.colorScheme.tertiary,
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun StatCard(
    label: String,
    count: Int,
    icon: ImageVector,
    iconTint: Color,
    containerColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = count.toString(),
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = iconTint
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun StatsScreenPreview() {
    MaterialTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                label = "Totale Medicinali",
                count = 12,
                icon = Icons.Outlined.MedicalServices,
                iconTint = Color(0xFF6200EE),
                containerColor = Color(0xFFEDE7F6),
                modifier = Modifier.fillMaxWidth()
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    label = "Scaduti",
                    count = 3,
                    icon = Icons.Outlined.Error,
                    iconTint = ExpiredRed,
                    containerColor = ExpiredRedLight,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    label = "In Scadenza",
                    count = 2,
                    icon = Icons.Outlined.Warning,
                    iconTint = WarningYellow,
                    containerColor = WarningYellowLight,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
