package com.hand.log.tableedit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hand.log.domain.model.GameType
import com.hand.log.domain.model.PokerTable
import com.hand.log.domain.repository.PokerTableRepository
import com.hand.log.tableedit.contract.TableEditEffect
import com.hand.log.tableedit.contract.TableEditState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

internal class TableEditViewModel(
	private val pokerTableRepository: PokerTableRepository,
) : ViewModel() {

	private val _state = MutableStateFlow(TableEditState())
	val state: StateFlow<TableEditState> get() = _state

	private val _effect = MutableSharedFlow<TableEditEffect>()
	val effect: SharedFlow<TableEditEffect> get() = _effect.asSharedFlow()

	private var editingTableId: String? = null

	fun initialize(table: PokerTable?) {
		if (table != null) {
			editingTableId = table.id
			_state.value = TableEditState(
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
			editingTableId = null
			_state.value = TableEditState(date = todayString())
		}
	}

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
		val maxSeat = maxOf(count, 9)
		it.copy(playerCount = count, heroSeat = if (it.heroSeat > maxSeat) 1 else it.heroSeat)
	}
	fun updateHeroSeat(seat: Int) = _state.update { it.copy(heroSeat = seat) }

	@OptIn(ExperimentalTime::class)
	fun submit() {
		val s = _state.value
		if (!s.isSubmitEnabled) return

		viewModelScope.launch {
			val table = PokerTable(
				id = editingTableId ?: generateId(),
				date = LocalDate.parse(s.date),
				location = s.location.takeIf { it.isNotBlank() },
				gameType = s.gameType,
				startingStack = s.startingStack.toDoubleOrNull() ?: 0.0,
				blinds = s.buildBlinds(),
				playerCount = s.playerCount,
				heroSeat = s.heroSeat,
				createdAt = Clock.System.now().toEpochMilliseconds(),
			)
			if (s.isEditMode) {
				pokerTableRepository.updateTableInfo(table)
			} else {
				pokerTableRepository.saveTable(table)
			}
			_effect.emit(TableEditEffect.SaveComplete(table))
		}
	}

	companion object {
		@OptIn(ExperimentalTime::class)
		private fun todayString(): String {
			val epochMs = Clock.System.now().toEpochMilliseconds()
			return LocalDate.fromEpochDays((epochMs / 86400000).toInt()).toString()
		}

		private fun generateId(): String {
			val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
			return (1..20).map { chars.random() }.joinToString("")
		}
	}
}
