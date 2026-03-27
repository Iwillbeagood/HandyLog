package com.hand.log.tableedit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hand.log.domain.model.GameType
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

	private var editingTableId: String? = null

	fun initialize(table: PokerTable?) {
		if (table != null) {
			editingTableId = table.id
			val cash = table.gameType as? GameType.Cash
			val tournament = table.gameType as? GameType.Tournament
			_state.value = TableEditState(
				date = table.date.toString(),
				location = table.location ?: "",
				isCash = cash != null,
				sbText = cash?.sb?.toLong()?.toString() ?: "",
				bbText = cash?.bb?.toLong()?.toString() ?: "",
				straddleEnabled = cash?.straddle != null,
				straddleText = cash?.straddle?.toLong()?.toString() ?: "",
				bigBlindAnteEnabled = tournament?.isBigBlindAnte ?: true,
				maxPlayers = table.maxPlayers.takeIf { it > 0 } ?: table.playerCount,
				playerCount = table.playerCount,
				heroSeat = table.heroSeat,
				isEditMode = true,
			)
		} else {
			editingTableId = null
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
			val table = PokerTable(
				id = editingTableId ?: "",
				date = LocalDate.parse(s.date),
				location = s.location.takeIf { it.isNotBlank() },
				gameType = s.buildGameType(),
				maxPlayers = s.maxPlayers,
				playerCount = s.playerCount,
				heroSeat = s.heroSeat,
				createdAt = 0L,
			)
			val savedTable = if (s.isEditMode) {
				val existing = pokerTableRepository.getTableById(editingTableId!!)
				val maxSeat = s.maxPlayers
				val filteredPlayers = existing?.players?.filter { it.seat <= maxSeat } ?: emptyList()
				pokerTableRepository.saveTable(
					table.copy(
						id = editingTableId!!,
						players = filteredPlayers,
						createdAt = existing?.createdAt ?: 0L,
					),
				)
			} else {
				createPokerTable(table)
			}
			_effect.emit(TableEditEffect.SaveComplete(savedTable))
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
