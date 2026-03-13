package com.hand.log.record.contract

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.hand.log.domain.model.ActionType
import com.hand.log.domain.model.Blinds
import com.hand.log.domain.model.Card
import com.hand.log.domain.model.PokerTable
import com.hand.log.domain.model.Street
import com.hand.log.domain.model.StreetData

@Stable
internal sealed interface RecordHandState {

	@Immutable
	data object Loading : RecordHandState

	@Immutable
	data class Recording(
		val tableId: String,
		val table: PokerTable? = null,
		// Setup
		val heroCards: List<Card> = emptyList(),
		val heroStack: Double = 0.0,
		val buttonSeat: Int = 1,
		val blinds: Blinds? = null,
		// Streets
		val streets: Map<Street, StreetData> = mapOf(Street.PREFLOP to StreetData()),
		val currentStreet: Street = Street.PREFLOP,
		// Current action being built
		val currentActionSeat: Int? = null,
		val currentActionType: ActionType? = null,
		val currentActionAmount: String = "",
		// Result
		val result: String = "",
		val memo: String = "",
		// UI state
		val currentStep: RecordStep = RecordStep.SETUP,
		val showCardSelector: Boolean = false,
		val cardSelectorTarget: CardSelectorTarget? = null,
		val selectedCards: Set<Card> = emptySet(),
	) : RecordHandState {

		val canProceedFromSetup: Boolean get() = heroCards.size == 2

		val currentPot: Double
			get() = streets.values.sumOf { streetData ->
				streetData.actions.sumOf { action ->
					when (action.type) {
						ActionType.CALL, ActionType.BET, ActionType.RAISE, ActionType.ALL_IN -> action.amount ?: 0.0
						else -> 0.0
					}
				}
			}
	}
}

enum class RecordStep {
	SETUP,
	PREFLOP,
	FLOP,
	TURN,
	RIVER,
	;

	val label: String
		get() = when (this) {
			SETUP -> "설정"
			PREFLOP -> "프리플랍"
			FLOP -> "플랍"
			TURN -> "턴"
			RIVER -> "리버"
		}
}

@Immutable
internal sealed interface CardSelectorTarget {
	data class HeroCard(val index: Int) : CardSelectorTarget
	data class BoardCard(val street: Street, val index: Int) : CardSelectorTarget
}
