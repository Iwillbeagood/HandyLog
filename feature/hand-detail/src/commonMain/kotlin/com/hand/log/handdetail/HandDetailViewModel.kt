package com.hand.log.handdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hand.log.domain.repository.HandRecordRepository
import com.hand.log.handdetail.contract.HandDetailEffect
import com.hand.log.handdetail.contract.HandDetailModalEffect
import com.hand.log.handdetail.contract.HandDetailState
import com.hand.log.handdetail.model.HandHistoryFormatter
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

internal class HandDetailViewModel(
	handId: String,
	private val handRecordRepository: HandRecordRepository,
) : ViewModel() {

	private val useBbUnit = MutableStateFlow(false)

	private val _effect = MutableSharedFlow<HandDetailEffect>()
	val effect: SharedFlow<HandDetailEffect> get() = _effect.asSharedFlow()

	private val _modalEffect = MutableStateFlow<HandDetailModalEffect>(HandDetailModalEffect.Idle)
	val modalEffect: StateFlow<HandDetailModalEffect> get() = _modalEffect

	val state: StateFlow<HandDetailState> = combine(
		handRecordRepository.observeHandById(handId),
		useBbUnit,
	) { hand, bbUnit ->
		if (hand != null) {
			HandDetailState.Success(hand = hand, useBbUnit = bbUnit)
		} else {
			HandDetailState.Error
		}
	}.stateIn(
		scope = viewModelScope,
		started = SharingStarted.WhileSubscribed(5_000),
		initialValue = HandDetailState.Loading,
	)

	fun toggleBbUnit() {
		useBbUnit.value = !useBbUnit.value
	}

	fun showDeleteConfirm() {
		_modalEffect.value = HandDetailModalEffect.ConfirmDelete
	}

	fun confirmDelete() {
		val success = state.value as? HandDetailState.Success ?: return
		dismissModal()
		viewModelScope.launch {
			handRecordRepository.deleteHand(success.hand.id) {
				viewModelScope.launch {
					_effect.emit(HandDetailEffect.HandDeleted)
				}
			}
		}
	}

	fun onPlayerClick(seat: Int) {
		val success = state.value as? HandDetailState.Success ?: return
		viewModelScope.launch {
			_effect.emit(HandDetailEffect.NavigateToPlayers(success.hand.tableId, seat))
		}
	}

	fun dismissModal() {
		_modalEffect.value = HandDetailModalEffect.Idle
	}

	fun shareText() {
		val success = state.value as? HandDetailState.Success ?: return
		val text = HandHistoryFormatter.format(success.hand)
		viewModelScope.launch {
			_effect.emit(HandDetailEffect.ShareText(text))
		}
	}

	fun shareImage() {
		val success = state.value as? HandDetailState.Success ?: return
		viewModelScope.launch {
			_effect.emit(HandDetailEffect.ShareImage("hand_${success.hand.id}.png"))
		}
	}

	fun downloadImage() {
		val success = state.value as? HandDetailState.Success ?: return
		viewModelScope.launch {
			_effect.emit(HandDetailEffect.DownloadImage("hand_${success.hand.id}.png"))
		}
	}
}
