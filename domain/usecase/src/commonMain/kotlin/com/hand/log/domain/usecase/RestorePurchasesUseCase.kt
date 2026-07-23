package com.hand.log.domain.usecase

import com.hand.log.domain.model.billing.PurchaseResult
import com.hand.log.domain.repository.ProEntitlementRepository

class RestorePurchasesUseCase(
	private val proEntitlementRepository: ProEntitlementRepository,
) {
	suspend operator fun invoke(): PurchaseResult = proEntitlementRepository.restore()
}
