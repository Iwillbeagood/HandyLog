package com.hand.log.data.datasoure.billing

import android.content.Context
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.hand.log.domain.model.billing.ProProduct
import com.hand.log.domain.model.billing.PurchaseResult
import com.hand.log.domain.repository.ProEntitlementRepository
import com.hand.log.platform.activityProvider
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Google Play Billing 7 기반 서버리스 인앱 결제 구현.
 * 비소모성(one-time) 상품을 결제 후 acknowledge 하고, 소유 여부는 [queryPurchasesAsync]로 복원한다.
 */
internal class AndroidBillingDataSource(
	context: Context,
) : BillingDataSource {

	private val productId = ProEntitlementRepository.PRO_PRODUCT_ID

	/** 진행 중인 결제 플로우의 결과를 [PurchasesUpdatedListener] 에서 완료시키기 위한 채널. */
	private var pendingPurchase: CompletableDeferred<PurchaseResult>? = null

	private val purchasesListener = PurchasesUpdatedListener { result, purchases ->
		val deferred = pendingPurchase
		when (result.responseCode) {
			BillingClient.BillingResponseCode.OK -> {
				val purchased = purchases?.any { productId in it.products } == true
				deferred?.complete(if (purchased) PurchaseResult.Success else PurchaseResult.Cancelled)
			}
			BillingClient.BillingResponseCode.USER_CANCELED ->
				deferred?.complete(PurchaseResult.Cancelled)
			BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED ->
				deferred?.complete(PurchaseResult.AlreadyOwned)
			else ->
				deferred?.complete(PurchaseResult.Failure(result.debugMessage))
		}
	}

	private val billingClient: BillingClient = BillingClient.newBuilder(context.applicationContext)
		.setListener(purchasesListener)
		.enablePendingPurchases(
			PendingPurchasesParams.newBuilder().enableOneTimeProducts().build(),
		)
		.build()

	private suspend fun ensureReady(): Boolean {
		if (billingClient.isReady) return true
		return suspendCancellableCoroutine { cont ->
			billingClient.startConnection(object : BillingClientStateListener {
				override fun onBillingSetupFinished(result: BillingResult) {
					if (cont.isActive) {
						cont.resume(result.responseCode == BillingClient.BillingResponseCode.OK)
					}
				}

				override fun onBillingServiceDisconnected() {
					if (cont.isActive) cont.resume(false)
				}
			})
		}
	}

	private suspend fun queryProductDetails(): ProductDetails? {
		if (!ensureReady()) return null
		val params = QueryProductDetailsParams.newBuilder()
			.setProductList(
				listOf(
					QueryProductDetailsParams.Product.newBuilder()
						.setProductId(productId)
						.setProductType(BillingClient.ProductType.INAPP)
						.build(),
				),
			)
			.build()
		return suspendCancellableCoroutine { cont ->
			billingClient.queryProductDetailsAsync(params) { _, productDetailsList ->
				if (cont.isActive) cont.resume(productDetailsList.firstOrNull())
			}
		}
	}

	private suspend fun queryPurchases(): List<Purchase> {
		if (!ensureReady()) return emptyList()
		val params = QueryPurchasesParams.newBuilder()
			.setProductType(BillingClient.ProductType.INAPP)
			.build()
		return suspendCancellableCoroutine { cont ->
			billingClient.queryPurchasesAsync(params) { _, purchases ->
				if (cont.isActive) cont.resume(purchases)
			}
		}
	}

	private suspend fun acknowledgeIfNeeded(purchase: Purchase) {
		if (purchase.purchaseState != Purchase.PurchaseState.PURCHASED) return
		if (purchase.isAcknowledged) return
		val params = AcknowledgePurchaseParams.newBuilder()
			.setPurchaseToken(purchase.purchaseToken)
			.build()
		suspendCancellableCoroutine { cont ->
			billingClient.acknowledgePurchase(params) {
				if (cont.isActive) cont.resume(Unit)
			}
		}
	}

	override suspend fun queryProduct(): ProProduct? {
		val details = queryProductDetails() ?: return null
		val price = details.oneTimePurchaseOfferDetails?.formattedPrice ?: return null
		return ProProduct(id = details.productId, formattedPrice = price)
	}

	override suspend fun isPurchased(): Boolean =
		queryPurchases().any { purchase ->
			productId in purchase.products &&
				purchase.purchaseState == Purchase.PurchaseState.PURCHASED
		}

	override suspend fun purchase(): PurchaseResult {
		val activity = activityProvider()
			?: return PurchaseResult.Failure("No foreground activity")
		val details = queryProductDetails()
			?: return PurchaseResult.Failure("Product unavailable")

		val flowParams = BillingFlowParams.newBuilder()
			.setProductDetailsParamsList(
				listOf(
					BillingFlowParams.ProductDetailsParams.newBuilder()
						.setProductDetails(details)
						.build(),
				),
			)
			.build()

		val deferred = CompletableDeferred<PurchaseResult>()
		pendingPurchase = deferred
		val launch = billingClient.launchBillingFlow(activity, flowParams)
		if (launch.responseCode != BillingClient.BillingResponseCode.OK) {
			pendingPurchase = null
			return PurchaseResult.Failure(launch.debugMessage)
		}

		val result = deferred.await()
		pendingPurchase = null
		if (result is PurchaseResult.Success || result is PurchaseResult.AlreadyOwned) {
			queryPurchases()
				.filter { productId in it.products }
				.forEach { acknowledgeIfNeeded(it) }
		}
		return result
	}

	override suspend fun restore(): PurchaseResult {
		if (!ensureReady()) return PurchaseResult.Failure("Store unavailable")
		val owned = queryPurchases().filter {
			productId in it.products && it.purchaseState == Purchase.PurchaseState.PURCHASED
		}
		if (owned.isEmpty()) return PurchaseResult.NothingToRestore
		owned.forEach { acknowledgeIfNeeded(it) }
		return PurchaseResult.Success
	}
}
