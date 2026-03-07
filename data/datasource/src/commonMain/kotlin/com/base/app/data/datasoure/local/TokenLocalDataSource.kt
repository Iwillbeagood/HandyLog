package com.hand.log.data.datasoure.local

interface TokenLocalDataSource {
	suspend fun getToken(): String
	suspend fun updateToken(token: String)
	suspend fun resetToken()
}
