package com.hand.log.preflop.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hand.log.domain.model.ProFeature
import com.hand.log.domain.usecase.CheckFeatureLimitUseCase
import com.hand.log.preflop.home.contract.PreflopHomeEffect
import com.hand.log.preflop.home.contract.PreflopHomeModalEffect
import com.hand.log.preflop.home.contract.PreflopHomeState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class PreflopHomeViewModel(
	private val checkFeatureLimit: CheckFeatureLimitUseCase,
) : ViewModel() {

	private val _state = MutableStateFlow(PreflopHomeState())
	val state: StateFlow<PreflopHomeState> get() = _state

	private val _modalEffect = MutableStateFlow<PreflopHomeModalEffect>(PreflopHomeModalEffect.Idle)
	val modalEffect: StateFlow<PreflopHomeModalEffect> get() = _modalEffect

	private val _effect = MutableSharedFlow<PreflopHomeEffect>()
	val effect: SharedFlow<PreflopHomeEffect> = _effect.asSharedFlow()

	init {
		viewModelScope.launch {
			_state.update { it.copy(quizLocked = !checkFeatureLimit.canUsePreflopQuiz()) }
		}
	}

	fun onQuizClick() {
		viewModelScope.launch {
			if (checkFeatureLimit.canUsePreflopQuiz()) {
				_effect.emit(PreflopHomeEffect.NavigateToQuiz)
			} else {
				_modalEffect.update { PreflopHomeModalEffect.ShowPaywall(ProFeature.PREFLOP_QUIZ) }
			}
		}
	}

	fun dismissModal() {
		_modalEffect.update { PreflopHomeModalEffect.Idle }
	}
}
