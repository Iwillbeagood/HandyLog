package com.hand.log.local.datastore.impl

import com.hand.log.data.datasoure.local.EntitlementLocalDataSource
import com.hand.log.local.datastore.EntitlementDataSource
import kotlinx.coroutines.flow.Flow

internal class EntitlementLocalDataSourceImpl(
	private val entitlementDataSource: EntitlementDataSource,
) : EntitlementLocalDataSource {

	override fun observeProEntitled(): Flow<Boolean> =
		entitlementDataSource.observeProEntitled()

	override suspend fun setProEntitled(entitled: Boolean) =
		entitlementDataSource.setProEntitled(entitled)
}
