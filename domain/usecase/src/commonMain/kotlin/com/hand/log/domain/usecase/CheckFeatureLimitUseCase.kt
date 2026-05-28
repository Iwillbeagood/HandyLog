package com.hand.log.domain.usecase

import com.hand.log.common.AppConfig
import com.hand.log.domain.repository.HandRecordRepository
import com.hand.log.domain.repository.PokerTableRepository
import com.hand.log.domain.repository.SavedPlayerRepository
import kotlinx.coroutines.flow.first

class CheckFeatureLimitUseCase(
	private val tableRepository: PokerTableRepository,
	private val handRecordRepository: HandRecordRepository,
	private val savedPlayerRepository: SavedPlayerRepository,
) {

	suspend fun canCreateTable(): Boolean {
		if (AppConfig.isProBuild) return true
		return tableRepository.observeAllTables().first().size < MAX_FREE_TABLES
	}

	suspend fun canRecordHand(tableId: String): Boolean {
		if (AppConfig.isProBuild) return true
		return handRecordRepository.getHandCountByTableId(tableId) < MAX_FREE_HANDS_PER_TABLE
	}

	suspend fun canSavePlayer(): Boolean {
		if (AppConfig.isProBuild) return true
		return savedPlayerRepository.observeAllPlayers().first().size < MAX_FREE_PLAYERS
	}

	fun canCustomizePresets(): Boolean = AppConfig.isProBuild

	companion object {
		const val MAX_FREE_TABLES = 2
		const val MAX_FREE_HANDS_PER_TABLE = 5
		const val MAX_FREE_PLAYERS = 5
	}
}
