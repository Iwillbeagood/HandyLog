package com.hand.log.data.datasoure.local

import kotlinx.coroutines.flow.Flow

interface EntitlementLocalDataSource {
	fun observeProEntitled(): Flow<Boolean>
	suspend fun setProEntitled(entitled: Boolean)
}
