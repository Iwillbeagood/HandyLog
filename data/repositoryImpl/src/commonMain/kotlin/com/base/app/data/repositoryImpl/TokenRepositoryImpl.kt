package com.hand.log.data.repositoryImpl

import com.hand.log.data.datasoure.local.TokenLocalDataSource
import com.hand.log.domain.repository.TokenRepository
import javax.inject.Inject

internal class TokenRepositoryImpl @Inject constructor(
	private val tokenLocalDataSource: TokenLocalDataSource,
) : TokenRepository {

	override suspend fun getToken(): String {
		return tokenLocalDataSource.getToken()
	}

	override suspend fun updateToken(token: String) {
		tokenLocalDataSource.updateToken(token)
	}

	override suspend fun reset() {
		tokenLocalDataSource.resetToken()
	}
}
