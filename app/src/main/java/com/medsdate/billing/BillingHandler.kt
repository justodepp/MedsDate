package com.medsdate.billing

import android.app.Activity
import com.android.billingclient.api.*
import timber.log.Timber
import java.util.*

class BillingHandler(private val mActivity: Activity, private val mCb: BillingCallbacks, private val mSkuKeys: List<String>) {
    private val mBilling: BillingClient
    private val mSkus: MutableMap<String, SkuDetails> = HashMap()
    val skus: Collection<SkuDetails>
        get() = mSkus.values

    fun buy(sku: SkuDetails?) {
        val response = mBilling.launchBillingFlow(mActivity, BillingFlowParams.newBuilder()
                .setSkuDetails(sku)
                .build()).responseCode
        Timber.w("buy $response")
    }

    fun destroy() {
        mBilling.endConnection()
    }

    interface BillingCallbacks {
        fun onStateChanged(connected: Boolean)
        fun onPurchased(sku: SkuDetails?, isNew: Boolean)
    }

    private inner class Listener : BillingClientStateListener, SkuDetailsResponseListener, PurchasesUpdatedListener, PurchaseHistoryResponseListener {
        override fun onBillingSetupFinished(billingResult: BillingResult) {
            Timber.w("onBillingSetupFinished ${billingResult.responseCode}")
            mBilling.querySkuDetailsAsync(SkuDetailsParams.newBuilder()
                    .setSkusList(mSkuKeys)
                    .setType(BillingClient.SkuType.INAPP)
                    .build(), this)
        }

        override fun onBillingServiceDisconnected() {
            Timber.w("onBillingServiceDisconnected")
            mCb.onStateChanged(false)
        }

        override fun onPurchaseHistoryResponse(billingResult: BillingResult, purchasesList: List<PurchaseHistoryRecord>) {
            Timber.w("onPurchaseHistoryResponse ${billingResult.responseCode}")
            for (purchase in purchasesList) {
                val sku = mSkus[purchase.sku]
                mCb.onPurchased(sku, false)
            }
        }

        override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
            Timber.w( "onPurchaseUpdated ${billingResult.responseCode}")
            if (purchases != null) {
                for (purchase in purchases) {
                    val sku = mSkus[purchase.sku]
                    val params = ConsumeParams.newBuilder().setPurchaseToken(purchase.purchaseToken).build()
                    mBilling.consumeAsync(params) { billingResult1: BillingResult, _: String? ->
                        Timber.w("consumePurchase ${billingResult.responseCode}")
                        if (billingResult1.responseCode == BillingClient.BillingResponseCode.OK) {
                            mCb.onPurchased(sku, true)
                        }
                    }
                }
            }
        }

        override fun onSkuDetailsResponse(billingResult: BillingResult, skuDetailsList: List<SkuDetails>) {
            Timber.w("onSkuDetailsResponse ${billingResult.responseCode} ${skuDetailsList.size}")
            mSkus.clear()
            for (sku in skuDetailsList) {
                mSkus[sku.sku] = sku
            }
            mCb.onStateChanged(true)
            mBilling.queryPurchaseHistoryAsync(BillingClient.SkuType.INAPP, this)
        }
    }

    init {
        val listener = Listener()
        mBilling = BillingClient.newBuilder(mActivity)
                .setListener(listener)
                .build()
        mBilling.startConnection(listener)
    }
}