package com.hand.log.ui.table

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import com.hand.log.domain.model.Blinds
import com.hand.log.domain.model.GameType
import com.hand.log.domain.model.PokerTable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.datetime.LocalDate
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@Stable
class TableFormPresenter(
	table: PokerTable? = null,
) {
	private val _state = MutableStateFlow(
		if (table != null) {
			TableFormState(
				date = table.date.toString(),
				location = table.location ?: "",
				gameType = table.gameType,
				startingStack = table.startingStack.toLong().toString(),
				sbText = table.blinds?.sb?.toLong()?.toString() ?: "",
				bbText = table.blinds?.bb?.toLong()?.toString() ?: "",
				straddleEnabled = table.blinds?.straddle != null,
				straddleText = table.blinds?.straddle?.toLong()?.toString() ?: "",
				bigBlindAnteEnabled = table.blinds?.isBigBlindAnte ?: true,
				playerCount = table.playerCount,
				heroSeat = table.heroSeat,
				isEditMode = true,
			)
		} else {
			TableFormState(date = todayString())
		},
	)
	val state: StateFlow<TableFormState> = _state

	fun updateDate(date: String) = _state.update { it.copy(date = date) }
	fun updateLocation(location: String) = _state.update { it.copy(location = location) }
	fun updateGameType(gameType: GameType) = _state.update { it.copy(gameType = gameType) }
	fun updateStartingStack(stack: String) = _state.update { it.copy(startingStack = stack) }
	fun updateSb(sb: String) = _state.update { it.copy(sbText = sb) }
	fun updateBb(bb: String) = _state.update { it.copy(bbText = bb) }
	fun updateStraddleEnabled(enabled: Boolean) = _state.update { it.copy(straddleEnabled = enabled) }
	fun updateStraddle(straddle: String) = _state.update { it.copy(straddleText = straddle) }
	fun updateBigBlindAnte(enabled: Boolean) = _state.update { it.copy(bigBlindAnteEnabled = enabled) }
	fun updatePlayerCount(count: Int) = _state.update {
		it.copy(playerCount = count, heroSeat = if (it.heroSeat > count) 1 else it.heroSeat)
	}
	fun updateHeroSeat(seat: Int) = _state.update { it.copy(heroSeat = seat) }

	fun buildBlinds(): Blinds {
		val s = _state.value
		return when (s.gameType) {
			GameType.CASH -> Blinds(
				sb = s.sbText.toDoubleOrNull() ?: 0.0,
				bb = s.bbText.toDoubleOrNull() ?: 0.0,
				straddle = if (s.straddleEnabled) s.straddleText.toDoubleOrNull() else null,
			)
			GameType.TOURNAMENT -> Blinds(
				sb = 0.0,
				bb = 0.0,
				isBigBlindAnte = s.bigBlindAnteEnabled,
			)
		}
	}

	companion object {
		@OptIn(ExperimentalTime::class)
		private fun todayString(): String {
			val epochMs = Clock.System.now().toEpochMilliseconds()
			return LocalDate.fromEpochDays((epochMs / 86400000).toInt()).toString()
		}
	}
}

@Composable
fun rememberTableFormPresenter(table: PokerTable? = null): TableFormPresenter {
	return remember(table?.id) { TableFormPresenter(table) }
}
