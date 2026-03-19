package com.hand.log.players

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hand.log.domain.model.SavedPlayer
import com.hand.log.domain.repository.SavedPlayerRepository
import com.hand.log.players.contract.PlayersModalEffect
import com.hand.log.players.contract.PlayersState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

internal class PlayersViewModel(
	private val savedPlayerRepository: SavedPlayerRepository,
) : ViewModel() {

	private val _state = MutableStateFlow<PlayersState>(PlayersState.Loading)
	val state: StateFlow<PlayersState> get() = _state

	private val _modalEffect = MutableStateFlow<PlayersModalEffect>(PlayersModalEffect.Idle)
	val modalEffect: StateFlow<PlayersModalEffect> get() = _modalEffect

	init {
		observePlayers()
	}

	private fun observePlayers() {
		savedPlayerRepository.observeAllPlayers()
			.map { players ->
				PlayersState.Success(players = players)
			}
			.onEach { _state.value = it }
			.launchIn(viewModelScope)
	}

	fun showPlayerEdit(player: SavedPlayer) {
		_modalEffect.value = PlayersModalEffect.ShowPlayerEdit(player)
	}

	fun showAddPlayer() {
		_modalEffect.value = PlayersModalEffect.ShowAddPlayer
	}

	fun dismissModal() {
		_modalEffect.value = PlayersModalEffect.Idle
	}

	@OptIn(ExperimentalTime::class)
	fun savePlayer(player: SavedPlayer) {
		viewModelScope.launch {
			val toSave = if (player.id.isBlank()) {
				player.copy(
					id = generateId(),
					createdAt = Clock.System.now().toEpochMilliseconds(),
				)
			} else {
				player
			}
			savedPlayerRepository.savePlayer(toSave)
		}
		dismissModal()
	}

	fun deletePlayer(id: String) {
		viewModelScope.launch {
			savedPlayerRepository.deletePlayer(id)
		}
		dismissModal()
	}

	private fun generateId(): String {
		val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
		return (1..20).map { chars.random() }.joinToString("")
	}
}
