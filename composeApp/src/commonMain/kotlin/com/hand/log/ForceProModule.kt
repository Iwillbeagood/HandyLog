package com.hand.log

import com.hand.log.domain.model.billing.ProProduct
import com.hand.log.domain.model.billing.PurchaseResult
import com.hand.log.domain.repository.ProEntitlementRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * 스토어 결제 없이 Pro 권한을 항상 활성으로 고정해 유료 기능을 테스트하는 모듈.
 * [ProEntitlementRepository] 바인딩을 오버라이드하므로 실제 BillingDataSource 는 사용되지 않는다.
 * Android 는 proTest flavor, iOS 는 디버그 바이너리에서 적용한다.
 */
val forceProModule: Module = module {
	single<ProEntitlementRepository> { ForceProEntitlementRepository() }
}

private class ForceProEntitlementRepository : ProEntitlementRepository {
	override fun observeIsPro(): Flow<Boolean> = flowOf(true)
	override suspend fun refresh() = Unit
	override suspend fun getProduct(): ProProduct? =
		ProProduct(id = ProEntitlementRepository.PRO_PRODUCT_ID, formattedPrice = "₩0")

	override suspend fun purchase(): PurchaseResult = PurchaseResult.Success
	override suspend fun restore(): PurchaseResult = PurchaseResult.Success
}
