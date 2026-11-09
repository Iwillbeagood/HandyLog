package com.hand.log.record.quick.component

import com.hand.log.domain.model.ActionType
import com.hand.log.domain.model.Blinds
import com.hand.log.domain.model.Card
import com.hand.log.domain.model.Position
import com.hand.log.domain.model.Rank
import com.hand.log.domain.model.Suit
import com.hand.log.domain.model.quick.BetSize
import com.hand.log.domain.model.quick.CardOwner
import com.hand.log.domain.model.quick.QuickHandConfig
import com.hand.log.domain.model.quick.QuickHandMachine
import com.hand.log.domain.model.quick.QuickInput
import com.hand.log.record.quick.contract.QuickRecordState
import com.hand.log.record.quick.contract.StreetCloseTarget
import com.hand.log.record.quick.model.HandKey
import com.hand.log.record.quick.model.QuickEditorRenderer
import com.hand.log.utils.poker.HandEvaluator

internal object QuickRecordPreviewData {

	private val config = QuickHandConfig(
		tableId = "preview",
		heroSeat = 4,
		seats = (1..6).toList(),
		blinds = Blinds(sb = 500.0, bb = 1000.0),
		stackInBb = 100.0,
	)

	private fun card(rank: Rank, suit: Suit) = listOf(
		QuickInput.CardRank(rank),
		QuickInput.CardSuit(suit),
	)

	private fun amount(text: String): List<QuickInput> = text.map {
		if (it == '.') QuickInput.Dot else QuickInput.Digit(it.digitToInt())
	}

	private fun done(type: ActionType): List<QuickInput> = listOf(
		QuickInput.Act(type),
		QuickInput.NextLine,
	)

	val heroSetup: List<QuickInput> = amount("3") + done(ActionType.RAISE) +
		QuickInput.Seat(Position.BTN) + QuickInput.Hero(Position.BTN) + QuickInput.CardRank(Rank.FIVE) + QuickInput.CardRank(Rank.FIVE)

	val stagedInput: List<QuickInput> = heroSetup + QuickInput.Act(ActionType.CALL)

	val actionInput: List<QuickInput> = stagedInput + QuickInput.NextLine

	val amountInput: List<QuickInput> = actionInput + QuickInput.Act(ActionType.RAISE) + amount("12")

	private val preflopDone: List<QuickInput> = actionInput + done(ActionType.FOLD) + done(ActionType.FOLD)

	val boardInput: List<QuickInput> = preflopDone + QuickInput.CardRank(Rank.ACE) + QuickInput.CardRank(Rank.KING)

	val reviewInput: List<QuickInput> = preflopDone +
		listOf(QuickInput.CardRank(Rank.ACE), QuickInput.CardRank(Rank.KING), QuickInput.CardRank(Rank.FIVE), QuickInput.NextLine) +
		QuickInput.PotSize(BetSize.HALF) + done(ActionType.BET) + done(ActionType.CALL) +
		card(Rank.ACE, Suit.SPADES) + QuickInput.NextLine + done(ActionType.CHECK) + done(ActionType.CHECK) +
		card(Rank.TEN, Suit.SPADES) + QuickInput.NextLine + done(ActionType.CHECK) + done(ActionType.ALL_IN) + done(ActionType.CALL)

	val doneInput: List<QuickInput> = reviewInput +
		QuickInput.FillCards(CardOwner.Opponent(Position.UTG), listOf(Card(Rank.QUEEN, Suit.CLUBS), Card(Rank.QUEEN, Suit.DIAMONDS))) +
		QuickInput.FillCards(CardOwner.Hero, listOf(Card(Rank.FIVE, Suit.HEARTS), Card(Rank.FIVE, Suit.CLUBS)))

	val heroMissingInput: List<QuickInput> = amount("2.5") + done(ActionType.RAISE) +
		List(5) { done(ActionType.FOLD) }.flatten()

	val streetCloseState: QuickRecordState.Editing
		get() = state(
			amount("2.5") + done(ActionType.RAISE) + QuickInput.Seat(Position.CO) + done(ActionType.CALL),
		).copy(
			streetCloseTarget = StreetCloseTarget.FLOP,
		)

	fun state(inputs: List<QuickInput>): QuickRecordState.Editing {
		val machine = QuickHandMachine(config)
		val snapshot = machine.replay(inputs)
		val finalRecord = machine.build(inputs, HandEvaluator::calculateShowdown)
		return QuickRecordState.Editing(
			blindsLabel = "500/1,000",
			stackLabel = "100BB",
			snapshot = snapshot,
			lines = QuickEditorRenderer.render(snapshot, finalRecord),
			inputs = inputs,
		)
	}

	val draftState: QuickRecordState.Editing
		get() {
			val draft = listOf(
				HandKey.Pos(Position.SB),
				HandKey.Face(Rank.QUEEN),
				HandKey.SuitKey(Suit.CLUBS),
				HandKey.Num(9),
				HandKey.Act(ActionType.RAISE),
			)
			val base = state(actionInput)
			return base.copy(
				draft = draft,
				lines = QuickEditorRenderer.render(base.snapshot, draft = draft),
				isDraftCommittable = true,
			)
		}
}
