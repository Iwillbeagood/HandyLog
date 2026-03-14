package com.hand.log.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hand.log.domain.model.Blinds
import com.hand.log.domain.model.GameType
import com.hand.log.domain.model.Player
import com.hand.log.domain.model.PokerTable
import com.hand.log.domain.repository.HandRecordRepository
import com.hand.log.domain.repository.PokerTableRepository
import com.hand.log.home.contract.HomeEffect
import com.hand.log.home.contract.HomeModalEffect
import com.hand.log.home.contract.HomeState
import com.hand.log.home.contract.TableListItem
import kotlinx.datetime.LocalDate
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

internal class HomeViewModel(
	private val pokerTableRepository: PokerTableRepository,
	private val handRecordRepository: HandRecordRepository,
) : ViewModel() {

	private val _homeState: MutableStateFlow<HomeState> = MutableStateFlow(HomeState.Loading)
	val homeState: StateFlow<HomeState> get() = _homeState

	private val _homeModalEffect = MutableStateFlow<HomeModalEffect>(HomeModalEffect.Idle)
	val homeModalEffect: StateFlow<HomeModalEffect> get() = _homeModalEffect

	private val _homeEffect = MutableSharedFlow<HomeEffect>()
	val homeEffect: SharedFlow<HomeEffect> get() = _homeEffect.asSharedFlow()

	init {
		observeTables()
	}

	private fun observeTables() {
		viewModelScope.launch {
			pokerTableRepository.observeAllTables().collect { tables ->
				val items = tables.map { table ->
					val handCount = handRecordRepository.getHandCountByTableId(table.id)
					TableListItem(table = table, handCount = handCount)
				}
				_homeState.update {
					HomeState.HomeData(tables = items)
				}
			}
		}
	}

	@OptIn(ExperimentalUuidApi::class, ExperimentalTime::class)
	fun saveTable(
		date: String,
		location: String?,
		gameType: GameType,
		startingStack: Double,
		blinds: Blinds?,
		playerCount: Int,
		heroSeat: Int,
	) {
		viewModelScope.launch {
			val defaultPlayers = (1..playerCount).map { seat ->
				Player(
					seat = seat,
					stack = startingStack,
					name = if (seat == heroSeat) "Hero" else "Player $seat",
				)
			}
			val table = PokerTable(
				id = Uuid.random().toString(),
				date = LocalDate.parse(date),
				location = location?.takeIf { it.isNotBlank() },
				gameType = gameType,
				startingStack = startingStack,
				blinds = blinds,
				playerCount = playerCount,
				heroSeat = heroSeat,
				players = defaultPlayers,
				createdAt = Clock.System.now().toEpochMilliseconds(),
			)
			pokerTableRepository.saveTable(table) {
				viewModelScope.launch {
					_homeEffect.emit(HomeEffect.ShowSnackBar("테이블이 생성되었습니다"))
				}
			}
		}
	}

	fun deleteTable(tableId: String) {
		viewModelScope.launch {
			pokerTableRepository.deleteTable(tableId) {
				viewModelScope.launch {
					_homeEffect.emit(HomeEffect.ShowSnackBar("테이블이 삭제되었습니다"))
				}
			}
		}
	}

	fun dismissDialog() {
		_homeModalEffect.update { HomeModalEffect.Idle }
	}
}
