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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.resume

/**
 * Google Play Billing 9 기반 서버리스 인앱 결제 구현.
 * 비소모성(one-time) 상품을 결제 후 acknowledge 하고, 소유 여부는 [queryPurchasesAsync]로 복원한다.
 */
internal class AndroidBillingDataSource(
	context: Context,
) : BillingDataSource {

	private val productId = ProEntitlementRepository.PRO_PRODUCT_ID

	private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

	private val connectionMutex = Mutex()

	private val _purchaseCompleted = MutableSharedFlow<Unit>(extraBufferCapacity = 8)
	override val purchaseCompleted: Flow<Unit> = _purchaseCompleted.asSharedFlow()

	/** 진행 중인 결제 플로우의 결과를 [PurchasesUpdatedListener] 에서 완료시키기 위한 채널. */
	private var pendingPurchase: CompletableDeferred<PurchaseResult>? = null

	private val purchasesListener = PurchasesUpdatedListener { result, purchases ->
		val ours = purchases.orEmpty().filter { productId in it.products }
		if (result.responseCode == BillingClient.BillingResponseCode.OK) {
			ours.filter { it.purchaseState == Purchase.PurchaseState.PURCHASED }.forEach { purchase ->
				scope.launch {
					acknowledgeIfNeeded(purchase)
					_purchaseCompleted.emit(Unit)
				}
			}
		}
		val deferred = pendingPurchase ?: return@PurchasesUpdatedListener
		when (result.responseCode) {
			BillingClient.BillingResponseCode.OK -> deferred.complete(ours.toPurchaseResult())
			BillingClient.BillingResponseCode.USER_CANCELED ->
				deferred.complete(PurchaseResult.Cancelled)
			BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED ->
				deferred.complete(PurchaseResult.AlreadyOwned)
			else ->
				deferred.complete(PurchaseResult.Failure(result.debugMessage))
		}
	}

	private val billingClient: BillingClient = BillingClient.newBuilder(context.applicationContext)
		.setListener(purchasesListener)
		.enablePendingPurchases(
			PendingPurchasesParams.newBuilder().enableOneTimeProducts().build(),
		)
		.build()

	private fun List<Purchase>.toPurchaseResult(): PurchaseResult = when {
		any { it.purchaseState == Purchase.PurchaseState.PURCHASED } -> PurchaseResult.Success
		any { it.purchaseState == Purchase.PurchaseState.PENDING } -> PurchaseResult.Pending
		else -> PurchaseResult.Cancelled
	}

	private suspend fun ensureReady(): Boolean = connectionMutex.withLock {
		if (billingClient.isReady) return@withLock true
		suspendCancellableCoroutine { cont ->
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
			billingClient.queryProductDetailsAsync(params) { _, result ->
				if (cont.isActive) cont.resume(result.productDetailsList.firstOrNull())
			}
		}
	}

	private suspend fun queryOwnPurchases(): List<Purchase>? {
		if (!ensureReady()) return null
		val params = QueryPurchasesParams.newBuilder()
			.setProductType(BillingClient.ProductType.INAPP)
			.build()
		return suspendCancellableCoroutine { cont ->
			billingClient.queryPurchasesAsync(params) { result, purchases ->
				if (!cont.isActive) return@queryPurchasesAsync
				if (result.responseCode == BillingClient.BillingResponseCode.OK) {
					cont.resume(purchases.filter { productId in it.products })
				} else {
					cont.resume(null)
				}
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

	override suspend fun isPurchased(): Boolean? {
		// 앱 밖에서 리딤된 구매(프로모 코드 등)도 이 경로로 처음 감지되므로 여기서 승인한다.
		// 3일 내 미승인 시 Play 가 자동 환불하므로, restore 뿐 아니라 시작 시 동기화에서도 반드시 승인해야
		// 재설치·기기 변경 후에도 소유가 유지된다.
		val owned = queryOwnPurchases()
			?.filter { it.purchaseState == Purchase.PurchaseState.PURCHASED }
			?: return null
		owned.forEach { acknowledgeIfNeeded(it) }
		return owned.isNotEmpty()
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
		return if (result == PurchaseResult.AlreadyOwned) resolveAlreadyOwned() else result
	}

	private suspend fun resolveAlreadyOwned(): PurchaseResult {
		val purchases = queryOwnPurchases()
			?: return PurchaseResult.Failure("Store unavailable")
		purchases.forEach { acknowledgeIfNeeded(it) }
		return when (purchases.toPurchaseResult()) {
			PurchaseResult.Success -> PurchaseResult.AlreadyOwned
			PurchaseResult.Pending -> PurchaseResult.Pending
			else -> PurchaseResult.Failure("Owned purchase not found")
		}
	}

	override suspend fun restore(): PurchaseResult {
		val purchases = queryOwnPurchases()
			?: return PurchaseResult.Failure("Store unavailable")
		purchases.forEach { acknowledgeIfNeeded(it) }
		return when (purchases.toPurchaseResult()) {
			PurchaseResult.Success -> PurchaseResult.Success
			PurchaseResult.Pending -> PurchaseResult.Pending
			else -> PurchaseResult.NothingToRestore
		}
	}
}
