package com.hand.log.tableedit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hand.log.domain.model.GameType
import com.hand.log.domain.model.Player
import com.hand.log.domain.model.PokerTable
import com.hand.log.domain.repository.PokerTableRepository
import com.hand.log.domain.usecase.CreatePokerTableUseCase
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
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

internal class TableEditViewModel(
	private val pokerTableRepository: PokerTableRepository,
	private val createPokerTable: CreatePokerTableUseCase,
) : ViewModel() {

	private val _state = MutableStateFlow(TableEditState())
	val state: StateFlow<TableEditState> get() = _state

	private val _effect = MutableSharedFlow<TableEditEffect>()
	val effect: SharedFlow<TableEditEffect> get() = _effect.asSharedFlow()

	private var editingTable: PokerTable? = null

	fun initialize(table: PokerTable?) {
		editingTable = table
		if (table != null) {
			val cash = table.gameType as? GameType.Cash
			val tournament = table.gameType as? GameType.Tournament
			_state.value = TableEditState(
				date = table.date.toString(),
				location = table.location ?: "",
				isCash = cash != null,
				sbText = cash?.sb?.toInputText() ?: "",
				bbText = cash?.bb?.toInputText() ?: "",
				straddleEnabled = cash?.straddle != null,
				straddleText = cash?.straddle?.toInputText() ?: "",
				bigBlindAnteEnabled = tournament?.isBigBlindAnte ?: true,
				maxPlayers = table.maxPlayers.takeIf { it > 0 } ?: table.playerCount,
				playerCount = table.playerCount,
				heroSeat = table.heroSeat,
				isEditMode = true,
			)
		} else {
			_state.value = TableEditState(date = todayString())
		}
	}

	fun updateDateMillis(millis: Long) {
		val localDate = LocalDate.fromEpochDays((millis / 86400000).toInt())
		_state.update { it.copy(date = localDate.toString()) }
	}
	fun updateLocation(location: String) = _state.update { it.copy(location = location) }
	fun updateIsCash(isCash: Boolean) = _state.update { it.copy(isCash = isCash) }
	fun updateSb(sb: String) = _state.update { it.copy(sbText = sb) }
	fun updateBb(bb: String) = _state.update { it.copy(bbText = bb) }
	fun updateStraddleEnabled(enabled: Boolean) = _state.update { it.copy(straddleEnabled = enabled) }
	fun updateStraddle(straddle: String) = _state.update { it.copy(straddleText = straddle) }
	fun updateBigBlindAnte(enabled: Boolean) = _state.update { it.copy(bigBlindAnteEnabled = enabled) }
	fun updateMaxPlayers(count: Int) = _state.update {
		it.copy(
			maxPlayers = count,
			playerCount = it.playerCount.coerceAtMost(count),
			heroSeat = if (it.heroSeat > count) 1 else it.heroSeat,
		)
	}
	fun updatePlayerCount(count: Int) = _state.update {
		it.copy(playerCount = count, heroSeat = if (it.heroSeat > it.maxPlayers) 1 else it.heroSeat)
	}
	fun updateHeroSeat(seat: Int) = _state.update { it.copy(heroSeat = seat) }

	fun submit() {
		val s = _state.value
		if (!s.isSubmitEnabled) return

		viewModelScope.launch {
			val original = editingTable
			val savedTable = if (original != null) {
				updateTable(original, s)
			} else {
				createPokerTable(
					PokerTable(
						id = "",
						date = LocalDate.parse(s.date),
						location = s.location.takeIf { it.isNotBlank() },
						gameType = s.buildGameType(),
						maxPlayers = s.maxPlayers,
						heroSeat = s.heroSeat,
						players = (1..s.playerCount).map { seat -> Player(seat = seat) },
						createdAt = 0L,
					),
				)
			}
			_effect.emit(TableEditEffect.SaveComplete(savedTable, original != null))
		}
	}

	private suspend fun updateTable(editing: PokerTable, s: TableEditState): PokerTable {
		val original = pokerTableRepository.getTableById(editing.id) ?: editing
		val resize = original.resizeSeats(s.maxPlayers)
		resize.removedSeats.forEach { seat -> pokerTableRepository.deletePlayer(original.id, seat) }
		if (resize.heroSeat != original.heroSeat) {
			val heroRow = original.players.find {
				it.seat == original.heroSeat
			} ?: Player(seat = original.heroSeat)
			pokerTableRepository.upsertPlayer(original.id, heroRow.copy(seat = resize.heroSeat))
		}
		val updated = original.copy(
			date = LocalDate.parse(s.date),
			location = s.location.takeIf { it.isNotBlank() },
			gameType = s.buildGameType(),
			maxPlayers = s.maxPlayers,
			heroSeat = resize.heroSeat,
		)
		pokerTableRepository.updateTableInfo(updated)
		return pokerTableRepository.getTableById(original.id) ?: updated
	}

	companion object {
		@OptIn(ExperimentalTime::class)
		private fun todayString(): String =
			Clock.System.now()
				.toLocalDateTime(TimeZone.currentSystemDefault())
				.date
				.toString()

		private fun Double.toInputText(): String =
			if (this % 1.0 == 0.0) toLong().toString() else toString()
	}
}
