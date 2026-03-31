package com.hand.log.table

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hand.log.domain.repository.HandRecordRepository
import com.hand.log.domain.repository.PokerTableRepository
import com.hand.log.table.contract.TableEffect
import com.hand.log.table.contract.TableModalEffect
import com.hand.log.table.contract.TableState
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
	tableId: String,
	private val tableRepository: PokerTableRepository,
	handRecordRepository: HandRecordRepository,
) : ViewModel() {

	val state: StateFlow<TableState> = combine(
		tableRepository.observeTableById(tableId),
		handRecordRepository.observeHandsByTableId(tableId),
	) { table, hands ->
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

	fun onPlayerSaved(isEditMode: Boolean) {
		viewModelScope.launch {
			_effect.emit(if (isEditMode) TableEffect.PlayerUpdated else TableEffect.PlayerAdded)
		}
	}

	fun onPlayerDeleted() {
		viewModelScope.launch {
			_effect.emit(TableEffect.PlayerDeleted)
		}
	}

	fun showPlayerSetup(seat: Int) {
		val current = state.value as? TableState.TableData ?: return
		val table = current.table
		_modalEffect.update {
			TableModalEffect.ShowPlayerSetup(
				tableId = table.id,
				initialSeat = seat,
				player = table.players.find { it.seat == seat },
				occupiedSeats = table.players.map { it.seat }.toSet(),
				maxPlayers = table.maxPlayers,
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

	fun onTableSaved(isEditMode: Boolean) {
		viewModelScope.launch {
			_effect.emit(if (isEditMode) TableEffect.TableUpdated else TableEffect.TableCreated)
		}
		dismissModal()
	}

	fun dismissModal() {
		_modalEffect.update { TableModalEffect.Idle }
	}
}
