package com.hand.log.players.hands

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hand.log.domain.model.HandRecord
import com.hand.log.domain.usecase.ObservePlayerHandsUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

internal class PlayerHandsViewModel(
	savedPlayerId: String,
	observePlayerHands: ObservePlayerHandsUseCase,
) : ViewModel() {

	val state: StateFlow<PlayerHandsState> = observePlayerHands(savedPlayerId)
		.map { hands -> PlayerHandsState.Success(hands = hands) }
		.stateIn(
			scope = viewModelScope,
			started = SharingStarted.WhileSubscribed(5_000),
			initialValue = PlayerHandsState.Loading,
		)
}

@Stable
internal sealed interface PlayerHandsState {
	data object Loading : PlayerHandsState

	@Immutable
	data class Success(
		val hands: List<HandRecord> = emptyList(),
	) : PlayerHandsState
}
