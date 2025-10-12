package com.medsdate.ui.components

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.android.billingclient.api.ProductDetails
import com.medsdate.R
import com.medsdate.billing.BillingManager
import com.medsdate.billing.PurchaseState
import com.medsdate.ui.theme.MedsDateTheme

/**
 * Dialog for showing donation options via Google Play Billing.
 *
 * @param billingManager The billing manager instance
 * @param onDismiss Callback when dialog is dismissed
 */
@Composable
fun DonateDialog(
    billingManager: BillingManager,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val donationProducts by billingManager.donationProducts.collectAsState()
    val purchaseState by billingManager.purchaseState.collectAsState()

    // Handle purchase state changes
    when (purchaseState) {
        is PurchaseState.Success -> {
            // Show success and dismiss
            billingManager.resetPurchaseState()
            onDismiss()
        }

        is PurchaseState.Canceled -> {
            billingManager.resetPurchaseState()
        }

        is PurchaseState.Error -> {
            // Could show error snackbar here
            billingManager.resetPurchaseState()
        }

        else -> {}
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(48.dp)
            )
        },
        title = {
            Text(
                text = stringResource(R.string.donate_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.donate_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )

                when {
                    purchaseState is PurchaseState.BillingUnavailable -> {
                        // Show PayPal fallback when billing is unavailable
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.donate_billing_unavailable),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )

                            OutlinedButton(
                                onClick = {
                                    val intent = Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse("https://www.paypal.me/GCavalli")
                                    )
                                    context.startActivity(intent)
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.OpenInNew,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(stringResource(R.string.donate_via_paypal))
                            }
                        }
                    }

                    donationProducts.isEmpty() -> {
                        // Loading state
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(32.dp))
                        }
                    }

                    else -> {
                        // Show donation options
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            donationProducts.forEach { product ->
                                DonationButton(
                                    product = product,
                                    onClick = {
                                        activity?.let {
                                            billingManager.launchDonationFlow(
                                                it,
                                                product
                                            )
                                        }
                                    },
                                    enabled = purchaseState !is PurchaseState.Processing
                                )
                            }
                        }
                    }
                }

                if (purchaseState is PurchaseState.Processing) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    stringResource(R.string.detail_delete_cancel),
                    color = MaterialTheme.colorScheme.primaryContainer
                )
            }
        }
    )
}

/**
 * Button for a single donation option.
 */
@Composable
private fun DonationButton(
    product: ProductDetails,
    onClick: () -> Unit,
    enabled: Boolean
) {
    val price = product.oneTimePurchaseOfferDetails?.formattedPrice ?: ""
    val title = when (product.productId) {
        BillingManager.DONATION_SMALL -> stringResource(R.string.donate_small)
        BillingManager.DONATION_MEDIUM -> stringResource(R.string.donate_medium)
        BillingManager.DONATION_LARGE -> stringResource(R.string.donate_large)
        else -> product.name
    }

    ElevatedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        enabled = enabled,
        colors = ButtonDefaults.elevatedButtonColors(
            containerColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primaryContainer
            )
            Text(
                text = price,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primaryContainer
            )
        }
    }
}

// ==================== Previews ====================

@Preview(showBackground = true)
@Composable
private fun DonationButtonPreview() {
    MedsDateTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Note: Cannot preview with real ProductDetails, using mock representation
            Text("Donation Button Preview")
        }
    }
}
