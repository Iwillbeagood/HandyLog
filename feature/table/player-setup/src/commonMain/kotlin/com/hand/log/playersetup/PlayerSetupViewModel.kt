package com.hand.log.playersetup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hand.log.domain.model.Player
import com.hand.log.domain.model.PlayerTendency
import com.hand.log.domain.model.SavedPlayer
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
		initialSeat: Int,
		isHero: Boolean,
		startingStack: Double,
		players: List<Player>,
	) {
		_state.value = PlayerSetupState.from(
			initialSeat = initialSeat,
			isHero = isHero,
			startingStack = startingStack,
			players = players,
		)
	}

	fun updateName(name: String) {
		updateCurrentPlayer { copy(name = name.ifBlank { null }) }
	}

	fun updateStack(stack: String) {
		val value = stack.toDoubleOrNull() ?: 0.0
		updateCurrentPlayer { copy(stack = value) }
	}

	fun updateTendency(tendency: PlayerTendency?) {
		updateCurrentPlayer { copy(tendency = tendency) }
	}

	fun updateMemo(memo: String) {
		updateCurrentPlayer { copy(memo = memo.ifBlank { null }) }
	}

	fun loadSavedPlayer(saved: SavedPlayer) {
		updateCurrentPlayer {
			copy(name = saved.name, tendency = saved.tendency, memo = saved.memo)
		}
	}

	fun loadSavedPlayerAndSave(saved: SavedPlayer) {
		loadSavedPlayer(saved)
		emitSave()
	}

	fun toggleSaveToMarking() {
		_state.update { it.copy(saveToMarking = !it.saveToMarking) }
	}

	fun resetAndSave() {
		val seat = _state.value.initialSeat
		val startingStack = _state.value.startingStack
		updateCurrentPlayer { Player(seat = seat, stack = startingStack) }
		emitSave()
	}

	fun save() {
		val current = _state.value
		if (current.saveToMarking) {
			val player = current.currentPlayer
			if (player.name != null) {
				viewModelScope.launch {
					savedPlayerRepository.savePlayer(
						SavedPlayer(
							id = "",
							name = player.name!!,
							tendency = player.tendency,
							memo = player.memo,
						),
					)
				}
			}
		}
		emitSave()
	}

	private fun emitSave() {
		viewModelScope.launch {
			_effect.emit(PlayerSetupEffect.SaveComplete(_state.value.editingPlayers))
		}
	}

	private fun updateCurrentPlayer(block: Player.() -> Player) {
		val seat = _state.value.initialSeat
		_state.update { state ->
			state.copy(
				editingPlayers = state.editingPlayers.map {
					if (it.seat == seat) it.block() else it
				},
			)
		}
	}
}
