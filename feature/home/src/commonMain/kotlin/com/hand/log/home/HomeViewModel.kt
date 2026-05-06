package com.hand.log.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hand.log.domain.model.PokerTable
import com.hand.log.domain.repository.AppSettingsRepository
import com.hand.log.domain.repository.PokerTableRepository
import com.hand.log.domain.usecase.ObserveAllHandsWithTableUseCase
import com.hand.log.domain.usecase.ObserveTableListItemsUseCase
import com.hand.log.home.contract.HomeEffect
import com.hand.log.home.contract.HomeModalEffect
import com.hand.log.home.contract.HomeState
import com.hand.log.domain.model.etc.HomeTab
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class HomeViewModel(
	private val pokerTableRepository: PokerTableRepository,
	private val appSettingsRepository: AppSettingsRepository,
	observeTableListItems: ObserveTableListItemsUseCase,
	observeAllHandsWithTable: ObserveAllHandsWithTableUseCase,
) : ViewModel() {

	val selectedTab: StateFlow<HomeTab> = appSettingsRepository.observeHomeTab()
		.stateIn(
			scope = viewModelScope,
			started = SharingStarted.WhileSubscribed(5_000),
			initialValue = HomeTab.TABLE,
		)

	val homeState: StateFlow<HomeState> = combine(
		observeTableListItems(),
		observeAllHandsWithTable(),
	) { tables, hands ->
		HomeState.HomeData(tables = tables, hands = hands)
	}.stateIn(
		scope = viewModelScope,
		started = SharingStarted.WhileSubscribed(5_000),
		initialValue = HomeState.Loading,
	)

	private val _homeModalEffect = MutableStateFlow<HomeModalEffect>(HomeModalEffect.Idle)
	val homeModalEffect: StateFlow<HomeModalEffect> get() = _homeModalEffect

	private val _homeEffect = MutableSharedFlow<HomeEffect>()
	val homeEffect: SharedFlow<HomeEffect> get() = _homeEffect.asSharedFlow()

	fun selectTab(tab: HomeTab) {
		viewModelScope.launch {
			appSettingsRepository.setHomeTab(tab)
		}
	}

	fun showTableEditSheet() {
		_homeModalEffect.update { HomeModalEffect.TableEditSheet }
	}

	fun onTableSaved(table: PokerTable) {
		dismissDialog()
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
