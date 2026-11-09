package com.hand.log.data.repositoryImpl

import com.hand.log.data.datasoure.billing.BillingDataSource
import com.hand.log.data.datasoure.local.EntitlementLocalDataSource
import com.hand.log.domain.model.billing.ProProduct
import com.hand.log.domain.model.billing.PurchaseResult
import com.hand.log.domain.repository.ProEntitlementRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

internal class ProEntitlementRepositoryImpl(
	private val entitlementLocalDataSource: EntitlementLocalDataSource,
	private val billingDataSource: BillingDataSource,
) : ProEntitlementRepository {

	private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

	init {
		scope.launch {
			billingDataSource.purchaseCompleted.collect {
				entitlementLocalDataSource.setProEntitled(true)
			}
		}
	}

	override fun observeIsPro(): Flow<Boolean> =
		entitlementLocalDataSource.observeProEntitled()

	override suspend fun refresh() {
		val purchased = billingDataSource.isPurchased() ?: return
		entitlementLocalDataSource.setProEntitled(purchased)
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
