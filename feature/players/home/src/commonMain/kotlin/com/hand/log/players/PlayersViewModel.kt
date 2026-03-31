package com.hand.log.players

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hand.log.domain.model.SavedPlayer
import com.hand.log.domain.repository.SavedPlayerRepository
import com.hand.log.players.contract.PlayersModalEffect
import com.hand.log.players.contract.PlayersState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class PlayersViewModel(
	private val savedPlayerRepository: SavedPlayerRepository,
) : ViewModel() {

	val state: StateFlow<PlayersState> = savedPlayerRepository.observeAllPlayers()
		.map { players -> PlayersState.Success(players = players) }
		.stateIn(
			scope = viewModelScope,
			started = SharingStarted.WhileSubscribed(5_000),
			initialValue = PlayersState.Loading,
		)

	private val _modalEffect = MutableStateFlow<PlayersModalEffect>(PlayersModalEffect.Idle)
	val modalEffect: StateFlow<PlayersModalEffect> get() = _modalEffect

	fun showPlayerEdit(player: SavedPlayer) {
		_modalEffect.update {
			PlayersModalEffect.ShowPlayerEdit(player)
		}
	}

	fun showAddPlayer() {
		_modalEffect.update {
			PlayersModalEffect.ShowAddPlayer
		}
	}

	fun dismissModal() {
		_modalEffect.update {
			PlayersModalEffect.Idle
		}
	}

	fun savePlayer(player: SavedPlayer) {
		viewModelScope.launch {
			if (player.id.isBlank()) {
				savedPlayerRepository.savePlayer(player)
			} else {
				savedPlayerRepository.updatePlayer(player)
			}
		}
		dismissModal()
	}

	fun deletePlayer(id: String) {
		viewModelScope.launch {
			savedPlayerRepository.deletePlayer(id)
		}
		dismissModal()
	}
}
