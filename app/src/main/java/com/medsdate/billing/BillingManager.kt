package com.medsdate.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber

/**
 * Manager for handling Google Play Billing donations.
 *
 * Supports one-time donation purchases through Google Play's in-app billing.
 */
class BillingManager(private val context: Context) {

    private val _donationProducts = MutableStateFlow<List<ProductDetails>>(emptyList())
    val donationProducts: StateFlow<List<ProductDetails>> = _donationProducts.asStateFlow()

    private val _purchaseState = MutableStateFlow<PurchaseState>(PurchaseState.Idle)
    val purchaseState: StateFlow<PurchaseState> = _purchaseState.asStateFlow()

    private var billingClient: BillingClient? = null

    /**
     * Available donation amounts as product IDs.
     * These need to be configured in Google Play Console.
     */
    companion object {
        const val DONATION_SMALL = "donation.small"   // e.g., $2.99
        const val DONATION_MEDIUM = "donation.regular" // e.g., $4.99
        const val DONATION_LARGE = "donation.large"   // e.g., $9.99
    }

    /**
     * Initialize the billing client and connection.
     */
    fun initialize() {
        billingClient = BillingClient.newBuilder(context)
            .setListener { billingResult, purchases ->
                handlePurchaseUpdates(billingResult, purchases)
            }
            .enablePendingPurchases()
            .build()

        connectToBilling()
    }

    /**
     * Connect to Google Play Billing service.
     */
    private fun connectToBilling() {
        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                when (billingResult.responseCode) {
                    BillingClient.BillingResponseCode.OK -> {
                        Timber.d("Billing client connected successfully")
                        queryDonationProducts()
                    }
                    BillingClient.BillingResponseCode.BILLING_UNAVAILABLE -> {
                        Timber.w("Billing unavailable - likely debug build or emulator")
                        _purchaseState.value = PurchaseState.BillingUnavailable
                    }
                    else -> {
                        Timber.e("Billing client connection failed: ${billingResult.debugMessage}")
                        _purchaseState.value = PurchaseState.Error(billingResult.debugMessage)
                    }
                }
            }

            override fun onBillingServiceDisconnected() {
                Timber.w("Billing service disconnected")
                // Don't automatically reconnect if billing is unavailable
                if (_purchaseState.value !is PurchaseState.BillingUnavailable) {
                    connectToBilling()
                }
            }
        })
    }

    /**
     * Query available donation products from Google Play.
     */
    private fun queryDonationProducts() {
        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(DONATION_SMALL)
                .setProductType(BillingClient.ProductType.INAPP)
                .build(),
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(DONATION_MEDIUM)
                .setProductType(BillingClient.ProductType.INAPP)
                .build(),
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(DONATION_LARGE)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        )

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        billingClient?.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                _donationProducts.value = productDetailsList
                Timber.d("Found ${productDetailsList.size} donation products")
            } else {
                Timber.e("Failed to query products: ${billingResult.debugMessage}")
                _purchaseState.value = PurchaseState.Error(billingResult.debugMessage)
            }
        }
    }

    /**
     * Launch the purchase flow for a donation.
     */
    fun launchDonationFlow(activity: Activity, productDetails: ProductDetails) {
        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(productDetails)
                .build()
        )

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()

        _purchaseState.value = PurchaseState.Processing

        val billingResult = billingClient?.launchBillingFlow(activity, billingFlowParams)
        if (billingResult?.responseCode != BillingClient.BillingResponseCode.OK) {
            Timber.e("Failed to launch billing flow: ${billingResult?.debugMessage}")
            _purchaseState.value = PurchaseState.Error(billingResult?.debugMessage ?: "Unknown error")
        }
    }

    /**
     * Handle purchase updates from Google Play.
     */
    private fun handlePurchaseUpdates(billingResult: BillingResult, purchases: List<Purchase>?) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                    // Acknowledge the purchase
                    acknowledgePurchase(purchase)
                }
            }
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            Timber.d("User canceled the purchase")
            _purchaseState.value = PurchaseState.Canceled
        } else {
            Timber.e("Purchase failed: ${billingResult.debugMessage}")
            _purchaseState.value = PurchaseState.Error(billingResult.debugMessage)
        }
    }

    /**
     * Acknowledge a successful purchase.
     */
    private fun acknowledgePurchase(purchase: Purchase) {
        if (!purchase.isAcknowledged) {
            val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()

            billingClient?.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Timber.d("Purchase acknowledged successfully")
                    _purchaseState.value = PurchaseState.Success
                } else {
                    Timber.e("Failed to acknowledge purchase: ${billingResult.debugMessage}")
                    _purchaseState.value = PurchaseState.Error(billingResult.debugMessage)
                }
            }
        } else {
            _purchaseState.value = PurchaseState.Success
        }
    }

    /**
     * Reset the purchase state to idle.
     */
    fun resetPurchaseState() {
        _purchaseState.value = PurchaseState.Idle
    }

    /**
     * Clean up billing client resources.
     */
    fun destroy() {
        billingClient?.endConnection()
        billingClient = null
    }
}

/**
 * Represents the current state of a purchase.
 */
sealed class PurchaseState {
    object Idle : PurchaseState()
    object Processing : PurchaseState()
    object Success : PurchaseState()
    object Canceled : PurchaseState()
    object BillingUnavailable : PurchaseState()
    data class Error(val message: String) : PurchaseState()
}
