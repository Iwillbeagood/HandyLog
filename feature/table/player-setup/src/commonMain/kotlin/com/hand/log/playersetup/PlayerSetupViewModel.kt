package com.hand.log.playersetup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hand.log.domain.model.Player
import com.hand.log.domain.model.PlayerTendency
import com.hand.log.domain.model.SavedPlayer
import com.hand.log.domain.repository.PokerTableRepository
import com.hand.log.domain.repository.SavedPlayerRepository
import com.hand.log.domain.usecase.GetOrCreateSavedPlayerUseCase
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
	private val getOrCreateSavedPlayer: GetOrCreateSavedPlayerUseCase,
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
		player: Player?,
		occupiedSeats: Set<Int>,
	) {
		_state.value = PlayerSetupState(
			tableId = tableId,
			player = player ?: Player(seat = initialSeat),
			occupiedSeats = occupiedSeats - initialSeat,
			isEditMode = player != null,
		)
	}

	fun updateName(name: String) {
		updatePlayer { copy(name = name.ifBlank { null }) }
	}

	fun selectTendency(tendency: PlayerTendency) {
		updatePlayer {
			copy(tendency = if (this.tendency == tendency) null else tendency)
		}
	}

	fun updateMemo(memo: String) {
		updatePlayer { copy(memo = memo.ifBlank { null }) }
	}

	fun updateSeat(newSeat: Int) {
		_state.update { it.copy(player = it.player.copy(seat = newSeat)) }
	}

	fun loadSavedPlayer(saved: SavedPlayer) {
		updatePlayer {
			copy(
				name = saved.name,
				tendency = saved.tendency,
				memo = saved.memo,
				savedPlayerId = saved.id,
			)
		}
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
			_effect.emit(PlayerSetupEffect.DeleteComplete)
		}
	}

	fun save() {
		val current = _state.value
		val player = current.player
		val name = player.name

		if (current.saveToMarking && name.isNullOrBlank()) {
			viewModelScope.launch { _effect.emit(PlayerSetupEffect.NameRequired) }
			return
		}

		viewModelScope.launch {
			val toSave = if (current.saveToMarking && name != null) {
				val saved = SavedPlayer(
					id = player.savedPlayerId.orEmpty(),
					name = name,
					tendency = player.tendency,
					memo = player.memo,
				)
				when (val result = getOrCreateSavedPlayer(saved)) {
					is GetOrCreateSavedPlayerUseCase.Result.Found -> player.copy(savedPlayerId = result.player.id)
					GetOrCreateSavedPlayerUseCase.Result.LimitReached -> {
						_effect.emit(PlayerSetupEffect.PlayerLimitReached)
						return@launch
					}
					GetOrCreateSavedPlayerUseCase.Result.Failed -> player
				}
			} else {
				player
			}
			pokerTableRepository.upsertPlayer(current.tableId, toSave)
			_effect.emit(PlayerSetupEffect.SaveComplete(current.isEditMode))
		}
	}

	private fun updatePlayer(block: Player.() -> Player) {
		_state.update { it.copy(player = it.player.block()) }
	}
}
