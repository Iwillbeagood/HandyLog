package com.hand.log.table

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hand.log.domain.repository.HandRecordRepository
import com.hand.log.domain.repository.PokerTableRepository
import com.hand.log.domain.usecase.ApplyTableBalanceUseCase
import com.hand.log.domain.usecase.MarkPositionSetupShownUseCase
import com.hand.log.domain.usecase.SavePlayerPositionsUseCase
import com.hand.log.table.contract.TableEffect
import com.hand.log.table.contract.TableModalEffect
import com.hand.log.table.contract.TableState
import handylog.core.res.generated.resources.Res
import handylog.core.res.generated.resources.home_table_deleted
import handylog.core.res.generated.resources.table_detail_player_added
import handylog.core.res.generated.resources.table_detail_player_deleted
import handylog.core.res.generated.resources.table_detail_player_updated
import handylog.core.res.generated.resources.table_detail_table_created
import handylog.core.res.generated.resources.table_detail_table_updated
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class TableViewModel(
	private val tableId: String,
	private val tableRepository: PokerTableRepository,
	private val savePlayerPositionsUseCase: SavePlayerPositionsUseCase,
	private val applyTableBalanceUseCase: ApplyTableBalanceUseCase,
	private val markPositionSetupShownUseCase: MarkPositionSetupShownUseCase,
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

	init {
		viewModelScope.launch {
			state.filterIsInstance<TableState.TableData>().take(1).collect { data ->
				val table = data.table
				if (!table.hasShownPositionSetup && table.playerCount < table.maxPlayers) {
					_modalEffect.update {
						TableModalEffect.ShowPlayerPositionSetup(
							tableId = table.id,
							maxPlayers = table.maxPlayers,
							heroSeat = table.heroSeat,
							playerCount = table.playerCount,
						)
					}
				}
			}
		}
	}

	private val _modalEffect = MutableStateFlow<TableModalEffect>(TableModalEffect.Idle)
	val modalEffect: StateFlow<TableModalEffect> get() = _modalEffect

	private val _effect = MutableSharedFlow<TableEffect>()
	val effect: SharedFlow<TableEffect> get() = _effect.asSharedFlow()

	fun onPlayerSaved(isEditMode: Boolean) {
		val res = if (isEditMode) Res.string.table_detail_player_updated else Res.string.table_detail_player_added
		viewModelScope.launch { _effect.emit(TableEffect.ShowToast(res)) }
	}

	fun onPlayerDeleted() {
		viewModelScope.launch {
			_effect.emit(TableEffect.ShowToast(Res.string.table_detail_player_deleted))
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

	fun showTableBalance() {
		val current = state.value as? TableState.TableData ?: return
		_modalEffect.update { TableModalEffect.ShowTableBalance(table = current.table) }
	}

	fun showDeleteConfirm() {
		_modalEffect.update { TableModalEffect.ShowDeleteConfirm }
	}

	fun deleteTable() {
		val current = state.value as? TableState.TableData ?: return
		viewModelScope.launch {
			tableRepository.deleteTable(current.table.id) {
				viewModelScope.launch {
					_effect.emit(TableEffect.ShowToastAndPop(Res.string.home_table_deleted))
				}
			}
		}
	}

	fun navigateToRecordHand() {
		val current = state.value as? TableState.TableData ?: return
		viewModelScope.launch {
			_effect.emit(TableEffect.NavigateToRecordHand(current.table.id))
		}
	}

	fun onTableSaved(isEditMode: Boolean) {
		val res = if (isEditMode) Res.string.table_detail_table_updated else Res.string.table_detail_table_created
		viewModelScope.launch { _effect.emit(TableEffect.ShowToast(res)) }
	}

	fun dismissPositionSetup() {
		val current = state.value as? TableState.TableData ?: return
		viewModelScope.launch {
			markPositionSetupShownUseCase(current.table)
		}
	}

	fun savePlayerPositions(seats: Set<Int>) {
		val current = state.value as? TableState.TableData ?: return
		viewModelScope.launch {
			savePlayerPositionsUseCase(current.table, seats)
			_effect.emit(TableEffect.ShowToast(Res.string.table_detail_table_updated))
		}
	}

	fun applyTableBalance(heroSeat: Int, otherSeats: Set<Int>) {
		val current = state.value as? TableState.TableData ?: return
		viewModelScope.launch {
			applyTableBalanceUseCase(current.table, heroSeat, otherSeats)
			_effect.emit(TableEffect.ShowToast(Res.string.table_detail_table_updated))
		}
	}

	fun dismissModal() {
		_modalEffect.update { TableModalEffect.Idle }
	}
}
