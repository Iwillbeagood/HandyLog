package com.hand.log.table

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hand.log.domain.model.Player
import com.hand.log.domain.repository.HandRecordRepository
import com.hand.log.domain.repository.PokerTableRepository
import com.hand.log.table.contract.TableDetailEffect
import com.hand.log.table.contract.TableDetailModalEffect
import com.hand.log.table.contract.TableDetailState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class TableDetailViewModel(
	private val tableId: String,
	private val tableRepository: PokerTableRepository,
	private val handRecordRepository: HandRecordRepository,
) : ViewModel() {

	private val _state = MutableStateFlow<TableDetailState>(TableDetailState.Loading)
	val state: StateFlow<TableDetailState> get() = _state

	private val _modalEffect = MutableStateFlow<TableDetailModalEffect>(TableDetailModalEffect.Idle)
	val modalEffect: StateFlow<TableDetailModalEffect> get() = _modalEffect

	private val _effect = MutableSharedFlow<TableDetailEffect>()
	val effect: SharedFlow<TableDetailEffect> get() = _effect.asSharedFlow()

	init {
		observeTableAndHands()
	}

	private fun observeTableAndHands() {
		viewModelScope.launch {
			combine(
				tableRepository.observeAllTables(),
				handRecordRepository.observeHandsByTableId(tableId),
			) { tables, hands ->
				val table = tables.find { it.id == tableId }
				if (table != null) {
					TableDetailState.TableData(
						table = table,
						hands = hands.sortedByDescending { it.createdAt },
					)
				} else {
					TableDetailState.Loading
				}
			}.collect { newState ->
				_state.update { newState }
			}
		}
	}

	fun updatePlayers(players: List<Player>) {
		val current = _state.value as? TableDetailState.TableData ?: return
		viewModelScope.launch {
			val updatedTable = current.table.copy(players = players)
			tableRepository.saveTable(updatedTable) {
				viewModelScope.launch {
					_effect.emit(TableDetailEffect.ShowSnackBar("플레이어 정보가 저장되었습니다"))
				}
			}
		}
	}

	fun deleteHand(handId: String) {
		viewModelScope.launch {
			handRecordRepository.deleteHand(handId) {
				viewModelScope.launch {
					_effect.emit(TableDetailEffect.ShowSnackBar("핸드가 삭제되었습니다"))
				}
			}
		}
	}

	fun showPlayerSetup(seat: Int) {
		val current = _state.value as? TableDetailState.TableData ?: return
		_modalEffect.update {
			TableDetailModalEffect.ShowPlayerSetup(
				initialSeat = seat,
				isHero = seat == current.table.heroSeat,
				startingStack = current.table.startingStack,
				players = current.table.players,
			)
		}
	}

	fun showTableEdit() {
		val current = _state.value as? TableDetailState.TableData ?: return
		_modalEffect.update { TableDetailModalEffect.ShowTableEdit(table = current.table) }
	}

	fun updateTable(
		date: String,
		location: String?,
		gameType: com.hand.log.domain.model.GameType,
		startingStack: Double,
		blinds: com.hand.log.domain.model.Blinds?,
		playerCount: Int,
		heroSeat: Int,
	) {
		val current = _state.value as? TableDetailState.TableData ?: return
		viewModelScope.launch {
			val updatedTable = current.table.copy(
				date = kotlinx.datetime.LocalDate.parse(date),
				location = location,
				gameType = gameType,
				startingStack = startingStack,
				blinds = blinds,
				playerCount = playerCount,
				heroSeat = heroSeat,
			)
			tableRepository.saveTable(updatedTable) {
				viewModelScope.launch {
					_effect.emit(TableDetailEffect.ShowSnackBar("테이블이 수정되었습니다"))
				}
			}
		}
	}

	fun dismissModal() {
		_modalEffect.update { TableDetailModalEffect.Idle }
	}
}
