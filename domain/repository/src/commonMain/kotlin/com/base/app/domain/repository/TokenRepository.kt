package com.hand.log.domain.repository

interface TokenRepository {
	suspend fun getToken(): String
	suspend fun updateToken(token: String)
	suspend fun reset()
}
