package com.hand.log.handdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hand.log.domain.repository.HandRecordRepository
import com.hand.log.handdetail.contract.HandDetailState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

internal class HandDetailViewModel(
	private val handId: String,
	private val handRecordRepository: HandRecordRepository,
) : ViewModel() {

	private val _state = MutableStateFlow<HandDetailState>(HandDetailState.Loading)
	val state: StateFlow<HandDetailState> get() = _state

	init {
		loadHand()
	}

	private fun loadHand() {
		viewModelScope.launch {
			val hand = handRecordRepository.getHandById(handId)
			_state.value = if (hand != null) {
				HandDetailState.Success(hand = hand)
			} else {
				HandDetailState.Error
			}
		}
	}
}
