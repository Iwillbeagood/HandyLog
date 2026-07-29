package com.hand.log.domain.usecase

import com.hand.log.domain.model.billing.ProProduct
import com.hand.log.domain.model.billing.PurchaseResult
import com.hand.log.domain.repository.ProEntitlementRepository

class PurchaseProUseCase(
	private val proEntitlementRepository: ProEntitlementRepository,
) {
	suspend fun product(): ProProduct? = proEntitlementRepository.getProduct()

	suspend operator fun invoke(): PurchaseResult = proEntitlementRepository.purchase()
}
