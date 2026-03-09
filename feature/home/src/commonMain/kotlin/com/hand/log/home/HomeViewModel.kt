package com.hand.log.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hand.log.domain.model.etc.error.MessageType
import com.hand.log.home.contract.HomeEffect
import com.hand.log.home.contract.HomeModalEffect
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class HomeViewModel : ViewModel() {

	private val _homeState: MutableStateFlow<HomeState> = MutableStateFlow(HomeState.Loading)
	val homeState: StateFlow<HomeState> get() = _homeState

	private val _homeModalEffect = MutableStateFlow<HomeModalEffect>(HomeModalEffect.Idle)
	val homeModalEffect: StateFlow<HomeModalEffect> get() = _homeModalEffect

	private val _homeEffect = MutableSharedFlow<HomeEffect>()
	val homeEffect: SharedFlow<HomeEffect> get() = _homeEffect.asSharedFlow()

	fun dismissDialog() {
		_homeModalEffect.update { HomeModalEffect.Idle }
	}

	private fun showSnackBar(messageType: MessageType) {
		viewModelScope.launch {
			_homeEffect.emit(HomeEffect.ShowSnackBar(messageType))
		}
	}
}
