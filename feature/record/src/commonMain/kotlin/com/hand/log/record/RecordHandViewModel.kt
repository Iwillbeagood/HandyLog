package com.hand.log.record

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hand.log.domain.model.Action
import com.hand.log.domain.model.ActionType
import com.hand.log.domain.model.Blinds
import com.hand.log.domain.model.Card
import com.hand.log.domain.model.HandRecord
import com.hand.log.domain.model.Street
import com.hand.log.domain.model.StreetData
import com.hand.log.domain.repository.HandRecordRepository
import com.hand.log.domain.repository.PokerTableRepository
import com.hand.log.record.contract.CardSelectorTarget
import com.hand.log.record.contract.RecordHandEffect
import com.hand.log.record.contract.RecordHandState
import com.hand.log.record.contract.RecordStep
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class RecordHandViewModel(
	private val tableId: String,
	private val pokerTableRepository: PokerTableRepository,
	private val handRecordRepository: HandRecordRepository,
) : ViewModel() {

	private val _state = MutableStateFlow<RecordHandState>(RecordHandState.Loading)
	val state: StateFlow<RecordHandState> get() = _state

	private val _effect = MutableSharedFlow<RecordHandEffect>()
	val effect: SharedFlow<RecordHandEffect> get() = _effect.asSharedFlow()

	init {
		loadTable()
	}

	private fun loadTable() {
		viewModelScope.launch {
			val table = pokerTableRepository.getTableById(tableId)
			_state.value = RecordHandState.Recording(
				tableId = tableId,
				table = table,
				heroStack = table?.startingStack ?: 0.0,
				buttonSeat = 1,
				blinds = table?.blinds,
			)
		}
	}

	fun selectHeroCard(index: Int) {
		updateRecording {
			copy(
				showCardSelector = true,
				cardSelectorTarget = CardSelectorTarget.HeroCard(index),
			)
		}
	}

	fun selectBoardCard(street: Street, index: Int) {
		updateRecording {
			copy(
				showCardSelector = true,
				cardSelectorTarget = CardSelectorTarget.BoardCard(street, index),
			)
		}
	}

	fun onCardSelected(card: Card) {
		val current = _state.value as? RecordHandState.Recording ?: return
		val target = current.cardSelectorTarget ?: return

		when (target) {
			is CardSelectorTarget.HeroCard -> {
				val updatedCards = current.heroCards.toMutableList()
				while (updatedCards.size <= target.index) {
					updatedCards.add(card)
				}
				updatedCards[target.index] = card

				updateRecording {
					copy(
						heroCards = updatedCards,
						selectedCards = selectedCards + card,
						showCardSelector = false,
						cardSelectorTarget = null,
					)
				}
			}

			is CardSelectorTarget.BoardCard -> {
				val streetData = current.streets[target.street] ?: StreetData()
				val updatedCards = streetData.cards.toMutableList()
				while (updatedCards.size <= target.index) {
					updatedCards.add(card)
				}
				updatedCards[target.index] = card

				val updatedStreets = current.streets.toMutableMap()
				updatedStreets[target.street] = streetData.copy(cards = updatedCards)

				updateRecording {
					copy(
						streets = updatedStreets,
						selectedCards = selectedCards + card,
						showCardSelector = false,
						cardSelectorTarget = null,
					)
				}
			}
		}
	}

	fun closeCardSelector() {
		updateRecording {
			copy(
				showCardSelector = false,
				cardSelectorTarget = null,
			)
		}
	}

	fun updateHeroStack(amount: String) {
		val stack = amount.toDoubleOrNull() ?: return
		updateRecording { copy(heroStack = stack) }
	}

	fun updateButtonSeat(seat: Int) {
		updateRecording { copy(buttonSeat = seat) }
	}

	fun updateBlinds(sb: String, bb: String) {
		val sbValue = sb.toDoubleOrNull() ?: 0.0
		val bbValue = bb.toDoubleOrNull() ?: 0.0
		updateRecording { copy(blinds = Blinds(sb = sbValue, bb = bbValue)) }
	}

	fun selectActionSeat(seat: Int) {
		updateRecording { copy(currentActionSeat = seat) }
	}

	fun selectActionType(type: ActionType) {
		updateRecording { copy(currentActionType = type) }
	}

	fun updateActionAmount(amount: String) {
		updateRecording { copy(currentActionAmount = amount) }
	}

	fun confirmAction() {
		val current = _state.value as? RecordHandState.Recording ?: return
		val seat = current.currentActionSeat ?: return
		val type = current.currentActionType ?: return
		val amount = current.currentActionAmount.toDoubleOrNull()

		val action = Action(
			playerSeat = seat,
			type = type,
			amount = amount,
		)

		val streetData = current.streets[current.currentStreet] ?: StreetData()
		val updatedStreetData = streetData.copy(actions = streetData.actions + action)
		val updatedStreets = current.streets.toMutableMap()
		updatedStreets[current.currentStreet] = updatedStreetData

		updateRecording {
			copy(
				streets = updatedStreets,
				currentActionSeat = null,
				currentActionType = null,
				currentActionAmount = "",
			)
		}
	}

	fun removeLastAction() {
		val current = _state.value as? RecordHandState.Recording ?: return
		val streetData = current.streets[current.currentStreet] ?: return
		if (streetData.actions.isEmpty()) return

		val updatedStreetData = streetData.copy(actions = streetData.actions.dropLast(1))
		val updatedStreets = current.streets.toMutableMap()
		updatedStreets[current.currentStreet] = updatedStreetData

		updateRecording { copy(streets = updatedStreets) }
	}

	fun nextStep() {
		updateRecording {
			val steps = RecordStep.entries
			val currentIndex = steps.indexOf(currentStep)
			if (currentIndex < steps.lastIndex) {
				val nextStep = steps[currentIndex + 1]
				val nextStreet = when (nextStep) {
					RecordStep.SETUP -> currentStreet
					RecordStep.PREFLOP -> Street.PREFLOP
					RecordStep.FLOP -> Street.FLOP
					RecordStep.TURN -> Street.TURN
					RecordStep.RIVER -> Street.RIVER
				}
				val updatedStreets = if (nextStep != RecordStep.SETUP && !streets.containsKey(nextStreet)) {
					streets.toMutableMap().apply { put(nextStreet, StreetData()) }
				} else {
					streets
				}
				copy(
					currentStep = nextStep,
					currentStreet = nextStreet,
					streets = updatedStreets,
					currentActionSeat = null,
					currentActionType = null,
					currentActionAmount = "",
				)
			} else {
				this
			}
		}
	}

	fun previousStep() {
		updateRecording {
			val steps = RecordStep.entries
			val currentIndex = steps.indexOf(currentStep)
			if (currentIndex > 0) {
				val prevStep = steps[currentIndex - 1]
				val prevStreet = when (prevStep) {
					RecordStep.SETUP -> currentStreet
					RecordStep.PREFLOP -> Street.PREFLOP
					RecordStep.FLOP -> Street.FLOP
					RecordStep.TURN -> Street.TURN
					RecordStep.RIVER -> Street.RIVER
				}
				copy(
					currentStep = prevStep,
					currentStreet = prevStreet,
					currentActionSeat = null,
					currentActionType = null,
					currentActionAmount = "",
				)
			} else {
				this
			}
		}
	}

	fun updateResult(amount: String) {
		updateRecording { copy(result = amount) }
	}

	fun updateMemo(text: String) {
		updateRecording { copy(memo = text) }
	}

	fun saveHand() {
		val current = _state.value as? RecordHandState.Recording ?: return

		viewModelScope.launch {
			try {
				val now = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
				val handRecord = HandRecord(
					id = generateId(),
					tableId = current.tableId,
					createdAt = now,
					blinds = current.blinds,
					heroCards = current.heroCards,
					heroStack = current.heroStack,
					buttonSeat = current.buttonSeat,
					streets = current.streets,
					result = current.result.toDoubleOrNull(),
					memo = current.memo.ifBlank { null },
				)

				handRecordRepository.saveHand(handRecord) {
					viewModelScope.launch {
						_effect.emit(RecordHandEffect.SaveSuccess)
					}
				}
			} catch (e: Exception) {
				_effect.emit(RecordHandEffect.SaveError(e.message ?: "저장 중 오류가 발생했습니다"))
			}
		}
	}

	private fun updateRecording(block: RecordHandState.Recording.() -> RecordHandState.Recording) {
		_state.update { state ->
			if (state is RecordHandState.Recording) {
				state.block()
			} else {
				state
			}
		}
	}

	private fun generateId(): String {
		val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
		return (1..20).map { chars.random() }.joinToString("")
	}
}
