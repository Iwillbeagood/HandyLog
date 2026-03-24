package com.hand.log.domain.usecase

import com.hand.log.domain.model.PokerTable
import com.hand.log.domain.repository.AppSettingsRepository
import com.hand.log.domain.repository.PokerTableRepository
import kotlinx.coroutines.flow.first

data class RecordInitData(
	val table: PokerTable?,
	val preflopPresets: List<Double>,
	val postflopPresets: List<Int>,
)

class LoadRecordDataUseCase(
	private val pokerTableRepository: PokerTableRepository,
	private val appSettingsRepository: AppSettingsRepository,
) {

	suspend operator fun invoke(tableId: String): RecordInitData {
		val table = pokerTableRepository.getTableById(tableId)
		val preflopPresets = appSettingsRepository.observeBetSizePresets().first()
		val postflopPresets = appSettingsRepository.observePotPercentPresets().first()
		return RecordInitData(
			table = table,
			preflopPresets = preflopPresets,
			postflopPresets = postflopPresets,
		)
	}
}
