package com.hand.log.domain.usecase

import com.hand.log.domain.repository.ProEntitlementRepository
import kotlinx.coroutines.flow.Flow

class ObserveProStatusUseCase(
	private val proEntitlementRepository: ProEntitlementRepository,
) {
	operator fun invoke(): Flow<Boolean> = proEntitlementRepository.observeIsPro()
}
