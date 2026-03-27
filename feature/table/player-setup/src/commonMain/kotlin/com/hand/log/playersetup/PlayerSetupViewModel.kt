package com.hand.log.playersetup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hand.log.domain.model.Player
import com.hand.log.domain.model.PlayerTendency
import com.hand.log.domain.model.SavedPlayer
import com.hand.log.domain.repository.PokerTableRepository
import com.hand.log.domain.repository.SavedPlayerRepository
import com.hand.log.playersetup.contract.PlayerSetupEffect
import com.hand.log.playersetup.contract.PlayerSetupState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PlayerSetupViewModel(
	private val savedPlayerRepository: SavedPlayerRepository,
	private val pokerTableRepository: PokerTableRepository,
) : ViewModel() {

	private val _state = MutableStateFlow(PlayerSetupState())
	val state: StateFlow<PlayerSetupState> get() = _state

	private val _effect = MutableSharedFlow<PlayerSetupEffect>()
	val effect: SharedFlow<PlayerSetupEffect> get() = _effect.asSharedFlow()

	val savedPlayers: StateFlow<List<SavedPlayer>> = savedPlayerRepository.observeAllPlayers()
		.stateIn(
			scope = viewModelScope,
			started = SharingStarted.WhileSubscribed(5_000),
			initialValue = emptyList(),
		)

	fun initialize(
		tableId: String,
		initialSeat: Int,
		isHero: Boolean,
		player: Player?,
		occupiedSeats: Set<Int>,
	) {
		_state.value = PlayerSetupState(
			tableId = tableId,
			player = player ?: Player(seat = initialSeat),
			isHero = isHero,
			occupiedSeats = occupiedSeats - initialSeat,
		)
	}

	fun updateName(name: String) {
		updatePlayer { copy(name = name.ifBlank { null }) }
	}

	fun updateTendency(tendency: PlayerTendency?) {
		updatePlayer { copy(tendency = tendency) }
	}

	fun updateMemo(memo: String) {
		updatePlayer { copy(memo = memo.ifBlank { null }) }
	}

	fun updateSeat(newSeat: Int) {
		_state.update { it.copy(player = it.player.copy(seat = newSeat)) }
	}

	fun loadSavedPlayer(saved: SavedPlayer) {
		updatePlayer { copy(name = saved.name, tendency = saved.tendency, memo = saved.memo) }
	}

	fun loadSavedPlayerAndSave(saved: SavedPlayer) {
		loadSavedPlayer(saved)
		save()
	}

	fun toggleSaveToMarking() {
		_state.update { it.copy(saveToMarking = !it.saveToMarking) }
	}

	fun clearSeatAndSave() {
		val current = _state.value
		viewModelScope.launch {
			pokerTableRepository.deletePlayer(current.tableId, current.player.seat)
			_effect.emit(PlayerSetupEffect.SaveComplete)
		}
	}

	fun save() {
		val current = _state.value
		val player = current.player

		if (current.saveToMarking) {
			if (player.name.isNullOrBlank()) {
				viewModelScope.launch { _effect.emit(PlayerSetupEffect.NameRequired) }
				return
			}
			viewModelScope.launch {
				savedPlayerRepository.savePlayer(
					SavedPlayer(
						name = player.name!!,
						tendency = player.tendency,
						memo = player.memo,
					),
				)
			}
		}

		viewModelScope.launch {
			pokerTableRepository.upsertPlayer(current.tableId, player)
			_effect.emit(PlayerSetupEffect.SaveComplete)
		}
	}

	private fun updatePlayer(block: Player.() -> Player) {
		_state.update { it.copy(player = it.player.block()) }
	}
}
