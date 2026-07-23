package com.hand.log.data.datasoure.billing

import com.hand.log.domain.model.billing.ProProduct
import com.hand.log.domain.model.billing.PurchaseResult
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * StoreKit 2(Swift) 구현을 [StoreKitBridge] 를 통해 호출하는 iOS 인앱 결제 구현.
 * 실제 서명 검증은 StoreKit 이 온디바이스(JWS)로 수행하므로 서버가 필요 없다.
 */
internal class IosBillingDataSource : BillingDataSource {

	private val bridge: StoreKitBridge?
		get() = StoreKitBridgeRegistry.bridge

	override suspend fun queryProduct(): ProProduct? {
		val bridge = bridge ?: return null
		return suspendCancellableCoroutine { cont ->
			bridge.queryProduct { id, price ->
				cont.resume(if (id != null && price != null) ProProduct(id, price) else null)
			}
		}
	}

	override suspend fun isPurchased(): Boolean {
		val bridge = bridge ?: return false
		return suspendCancellableCoroutine { cont ->
			bridge.isPurchased { purchased -> cont.resume(purchased) }
		}
	}

	override suspend fun purchase(): PurchaseResult {
		val bridge = bridge ?: return PurchaseResult.Failure("StoreKit unavailable")
		return suspendCancellableCoroutine { cont ->
			bridge.purchase { code, message -> cont.resume(code.toPurchaseResult(message)) }
		}
	}

	override suspend fun restore(): PurchaseResult {
		val bridge = bridge ?: return PurchaseResult.Failure("StoreKit unavailable")
		return suspendCancellableCoroutine { cont ->
			bridge.restore { code, message -> cont.resume(code.toPurchaseResult(message)) }
		}
	}

	private fun String.toPurchaseResult(message: String?): PurchaseResult = when (this) {
		"success" -> PurchaseResult.Success
		"already_owned" -> PurchaseResult.AlreadyOwned
		"cancelled" -> PurchaseResult.Cancelled
		"pending" -> PurchaseResult.Pending
		"nothing" -> PurchaseResult.NothingToRestore
		else -> PurchaseResult.Failure(message)
	}
}
