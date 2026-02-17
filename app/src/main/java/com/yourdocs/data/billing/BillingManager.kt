package com.yourdocs.data.billing

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

sealed interface BillingEvent {
    data object PurchaseSuccess : BillingEvent
    data class PurchaseError(val message: String) : BillingEvent
    data object PurchaseCancelled : BillingEvent
}

@Singleton
class BillingManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val premiumRepository: PremiumRepository
) : PurchasesUpdatedListener {

    companion object {
        private const val TAG = "BillingManager"
        const val PRODUCT_ID = "yourdocs_premium"
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val billingClient: BillingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases()
        .build()

    private val _productDetails = MutableStateFlow<ProductDetails?>(null)
    val productDetails: StateFlow<ProductDetails?> = _productDetails.asStateFlow()

    private val _billingEvents = MutableSharedFlow<BillingEvent>()
    val billingEvents: SharedFlow<BillingEvent> = _billingEvents.asSharedFlow()

    fun startConnection() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.d(TAG, "Billing client connected")
                    queryProductDetails()
                    verifyExistingPurchases()
                } else {
                    Log.e(TAG, "Billing setup failed: ${billingResult.debugMessage}")
                }
            }

            override fun onBillingServiceDisconnected() {
                Log.d(TAG, "Billing service disconnected")
            }
        })
    }

    fun endConnection() {
        billingClient.endConnection()
    }

    private fun queryProductDetails() {
        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(PRODUCT_ID)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        )

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        billingClient.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                _productDetails.value = productDetailsList.firstOrNull()
            } else {
                Log.e(TAG, "Failed to query product details: ${billingResult.debugMessage}")
            }
        }
    }

    fun verifyExistingPurchases() {
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()

        billingClient.queryPurchasesAsync(params) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val premiumPurchase = purchases.firstOrNull { purchase ->
                    purchase.products.contains(PRODUCT_ID) &&
                            purchase.purchaseState == Purchase.PurchaseState.PURCHASED
                }
                scope.launch {
                    if (premiumPurchase != null) {
                        if (!premiumPurchase.isAcknowledged) {
                            acknowledgePurchase(premiumPurchase)
                        }
                        premiumRepository.setPremiumStatus(
                            isPremium = true,
                            token = premiumPurchase.purchaseToken,
                            time = premiumPurchase.purchaseTime
                        )
                    } else {
                        premiumRepository.clearPremiumStatus()
                    }
                }
            }
        }
    }

    fun launchPurchaseFlow(activity: Activity) {
        val details = _productDetails.value ?: run {
            scope.launch {
                _billingEvents.emit(BillingEvent.PurchaseError("Product not available. Please try again."))
            }
            return
        }

        val offerToken = details.subscriptionOfferDetails?.firstOrNull()?.offerToken
        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(details)
                .apply { if (offerToken != null) setOfferToken(offerToken) }
                .build()
        )

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()

        billingClient.launchBillingFlow(activity, billingFlowParams)
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                purchases?.forEach { purchase ->
                    if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                        scope.launch {
                            acknowledgePurchase(purchase)
                            premiumRepository.setPremiumStatus(
                                isPremium = true,
                                token = purchase.purchaseToken,
                                time = purchase.purchaseTime
                            )
                            _billingEvents.emit(BillingEvent.PurchaseSuccess)
                        }
                    }
                }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                scope.launch { _billingEvents.emit(BillingEvent.PurchaseCancelled) }
            }
            else -> {
                scope.launch {
                    _billingEvents.emit(
                        BillingEvent.PurchaseError(
                            billingResult.debugMessage ?: "Purchase failed"
                        )
                    )
                }
            }
        }
    }

    private suspend fun acknowledgePurchase(purchase: Purchase) {
        if (purchase.isAcknowledged) return
        val params = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()
        billingClient.acknowledgePurchase(params) { result ->
            if (result.responseCode != BillingClient.BillingResponseCode.OK) {
                Log.e(TAG, "Failed to acknowledge purchase: ${result.debugMessage}")
            }
        }
    }

    fun restorePurchases(onResult: (Boolean) -> Unit) {
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()

        billingClient.queryPurchasesAsync(params) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val premiumPurchase = purchases.firstOrNull { purchase ->
                    purchase.products.contains(PRODUCT_ID) &&
                            purchase.purchaseState == Purchase.PurchaseState.PURCHASED
                }
                scope.launch {
                    if (premiumPurchase != null) {
                        premiumRepository.setPremiumStatus(
                            isPremium = true,
                            token = premiumPurchase.purchaseToken,
                            time = premiumPurchase.purchaseTime
                        )
                        onResult(true)
                    } else {
                        premiumRepository.clearPremiumStatus()
                        onResult(false)
                    }
                }
            } else {
                onResult(false)
            }
        }
    }
}
