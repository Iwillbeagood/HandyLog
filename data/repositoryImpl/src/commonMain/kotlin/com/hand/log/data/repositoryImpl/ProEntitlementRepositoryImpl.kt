package com.hand.log.data.repositoryImpl

import com.hand.log.data.datasoure.billing.BillingDataSource
import com.hand.log.data.datasoure.local.EntitlementLocalDataSource
import com.hand.log.domain.model.billing.ProProduct
import com.hand.log.domain.model.billing.PurchaseResult
import com.hand.log.domain.repository.ProEntitlementRepository
import kotlinx.coroutines.flow.Flow

internal class ProEntitlementRepositoryImpl(
	private val entitlementLocalDataSource: EntitlementLocalDataSource,
	private val billingDataSource: BillingDataSource,
) : ProEntitlementRepository {

	override fun observeIsPro(): Flow<Boolean> =
		entitlementLocalDataSource.observeProEntitled()

	override suspend fun refresh() {
		// 서버가 없으므로 환불 회수는 불가 — 스토어가 소유를 확인해줄 때만 권한을 부여한다.
		if (billingDataSource.isPurchased()) {
			entitlementLocalDataSource.setProEntitled(true)
		}
	}

	override suspend fun getProduct(): ProProduct? = billingDataSource.queryProduct()

	override suspend fun purchase(): PurchaseResult =
		billingDataSource.purchase().alsoGrantIfOwned()

	override suspend fun restore(): PurchaseResult =
		billingDataSource.restore().alsoGrantIfOwned()

	private suspend fun PurchaseResult.alsoGrantIfOwned(): PurchaseResult {
		if (this is PurchaseResult.Success || this is PurchaseResult.AlreadyOwned) {
			entitlementLocalDataSource.setProEntitled(true)
		}
		return this
	}
}
