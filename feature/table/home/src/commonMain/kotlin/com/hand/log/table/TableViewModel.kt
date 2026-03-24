package com.hand.log.table

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hand.log.domain.model.Player
import com.hand.log.domain.repository.HandRecordRepository
import com.hand.log.domain.repository.PokerTableRepository
import com.hand.log.table.contract.TableEffect
import com.hand.log.table.contract.TableModalEffect
import com.hand.log.table.contract.TableState
import com.hand.log.utils.etc.Logger
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

internal class TableViewModel(
	private val tableId: String,
	private val tableRepository: PokerTableRepository,
	private val handRecordRepository: HandRecordRepository,
) : ViewModel() {

	val state: StateFlow<TableState> = combine(
		tableRepository.observeAllTables(),
		handRecordRepository.observeHandsByTableId(tableId),
	) { tables, hands ->

		val table = tables.find { it.id == tableId }
		if (table != null) {
			TableState.TableData(
				table = table,
				hands = hands.sortedByDescending { it.createdAt },
			)
		} else {
			TableState.Loading
		}
	}.stateIn(
		scope = viewModelScope,
		started = SharingStarted.WhileSubscribed(5_000),
		initialValue = TableState.Loading,
	)

	private val _modalEffect = MutableStateFlow<TableModalEffect>(TableModalEffect.Idle)
	val modalEffect: StateFlow<TableModalEffect> get() = _modalEffect

	private val _effect = MutableSharedFlow<TableEffect>()
	val effect: SharedFlow<TableEffect> get() = _effect.asSharedFlow()

	fun updatePlayers(players: List<Player>) {
		val current = state.value as? TableState.TableData ?: return
		viewModelScope.launch {
			val updatedTable = current.table.copy(players = players)
			tableRepository.saveTable(updatedTable)
			_effect.emit(TableEffect.PlayerSaved)
		}
	}

	fun deleteHand(handId: String) {
		viewModelScope.launch {
			handRecordRepository.deleteHand(handId) {
				viewModelScope.launch {
					_effect.emit(TableEffect.HandDeleted)
				}
			}
		}
	}

	fun showPlayerSetup(seat: Int) {
		val current = state.value as? TableState.TableData ?: return
		_modalEffect.update {
			TableModalEffect.ShowPlayerSetup(
				initialSeat = seat,
				isHero = seat == current.table.heroSeat,
				startingStack = current.table.startingStack,
				players = current.table.players,
			)
		}
	}

	fun showTableEdit() {
		val current = state.value as? TableState.TableData ?: return
		_modalEffect.update { TableModalEffect.ShowTableEdit(table = current.table) }
	}

	fun showDeleteConfirm() {
		_modalEffect.update { TableModalEffect.ShowDeleteConfirm }
	}

	fun deleteTable() {
		val current = state.value as? TableState.TableData ?: return
		viewModelScope.launch {
			tableRepository.deleteTable(current.table.id) {
				viewModelScope.launch {
					_effect.emit(TableEffect.TableDeleted)
				}
			}
		}
		dismissModal()
	}

	fun navigateToRecordHand() {
		val current = state.value as? TableState.TableData ?: return
		viewModelScope.launch {
			_effect.emit(TableEffect.NavigateToRecordHand(current.table.id))
		}
	}

	fun onTableSaved() {
		viewModelScope.launch {
			_effect.emit(TableEffect.TableUpdated)
		}
		dismissModal()
	}

	fun dismissModal() {
		_modalEffect.update { TableModalEffect.Idle }
	}
}
