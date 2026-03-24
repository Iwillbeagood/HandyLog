package com.hand.log.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hand.log.domain.model.PokerTable
import com.hand.log.domain.repository.PokerTableRepository
import com.hand.log.domain.usecase.ObserveTableListItemsUseCase
import com.hand.log.home.contract.HomeEffect
import com.hand.log.home.contract.HomeModalEffect
import com.hand.log.home.contract.HomeState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class HomeViewModel(
	private val pokerTableRepository: PokerTableRepository,
	observeTableListItems: ObserveTableListItemsUseCase,
) : ViewModel() {

	val homeState: StateFlow<HomeState> = observeTableListItems()
		.map { items -> HomeState.HomeData(tables = items) }
		.stateIn(
			scope = viewModelScope,
			started = SharingStarted.WhileSubscribed(5_000),
			initialValue = HomeState.Loading,
		)

	private val _homeModalEffect = MutableStateFlow<HomeModalEffect>(HomeModalEffect.Idle)
	val homeModalEffect: StateFlow<HomeModalEffect> get() = _homeModalEffect

	private val _homeEffect = MutableSharedFlow<HomeEffect>()
	val homeEffect: SharedFlow<HomeEffect> get() = _homeEffect.asSharedFlow()

	fun onTableSaved(table: PokerTable) {
		viewModelScope.launch {
			_homeEffect.emit(HomeEffect.NavigateToTable(table.id))
		}
	}

	fun deleteTable(tableId: String) {
		viewModelScope.launch {
			pokerTableRepository.deleteTable(tableId) {
				viewModelScope.launch {
					_homeEffect.emit(HomeEffect.TableDeleted)
				}
			}
		}
	}

	fun dismissDialog() {
		_homeModalEffect.update { HomeModalEffect.Idle }
	}
}
