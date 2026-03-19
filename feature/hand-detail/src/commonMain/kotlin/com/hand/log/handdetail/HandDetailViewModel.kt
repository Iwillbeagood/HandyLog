package com.hand.log.handdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hand.log.domain.repository.HandRecordRepository
import com.hand.log.handdetail.contract.HandDetailState
import com.hand.log.handdetail.model.HandDetailUiModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

internal class HandDetailViewModel(
	handId: String,
	handRecordRepository: HandRecordRepository,
) : ViewModel() {

	private val useBbUnit = MutableStateFlow(false)

	val state: StateFlow<HandDetailState> = combine(
		handRecordRepository.observeHandById(handId),
		useBbUnit,
	) { hand, bbUnit ->
		if (hand != null) {
			HandDetailState.Success(
				hand = hand,
				useBbUnit = bbUnit,
				uiModel = HandDetailUiModel.from(hand, bbUnit),
			)
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
}
