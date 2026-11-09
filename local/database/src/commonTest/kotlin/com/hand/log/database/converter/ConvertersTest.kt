package com.hand.log.database.converter

import com.hand.log.domain.model.Position
import com.hand.log.domain.model.Rank
import com.hand.log.domain.model.preflop.HandShape
import com.hand.log.domain.model.preflop.PreflopAction
import com.hand.log.domain.model.preflop.PreflopHand
import com.hand.log.domain.model.preflop.PreflopScenario
import com.hand.log.domain.model.preflop.PreflopStack
import com.hand.log.domain.model.preflop.QuizRecordQuestion
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ConvertersTest {
	private val converters = Converters()

	@Test
	fun legacyThreeBetStackoffDecodesAsThreeBetCall() {
		val legacy = """
			[{"stack":"BB100","scenario":"FACING_RFI","hero":"BTN","villain":"CO",
			"hand":{"high":"ACE","low":"KING","shape":"SUITED"},
			"correct":"THREE_BET_STACKOFF",
			"options":["FOLD","CALL","THREE_BET_STACKOFF"],
			"userAnswer":"THREE_BET_STACKOFF"}]
		""".trimIndent()

		val questions = converters.toQuizRecordQuestions(legacy)

		assertEquals(
			listOf(
				QuizRecordQuestion(
					stack = PreflopStack.BB100,
					scenario = PreflopScenario.FACING_RFI,
					hero = Position.BTN,
					villain = Position.CO,
					hand = PreflopHand(Rank.ACE, Rank.KING, HandShape.SUITED),
					correct = PreflopAction.THREE_BET_JAM,
					options = listOf(PreflopAction.FOLD, PreflopAction.CALL, PreflopAction.THREE_BET_JAM),
					userAnswer = PreflopAction.THREE_BET_JAM,
				),
			),
			questions,
		)
	}

	@Test
	fun roundTripPreservesQuestions() {
		val question = QuizRecordQuestion(
			stack = PreflopStack.BB40,
			scenario = PreflopScenario.RFI,
			hero = Position.CO,
			villain = null,
			hand = PreflopHand(Rank.QUEEN, Rank.QUEEN, HandShape.PAIR),
			correct = PreflopAction.RAISE,
			options = listOf(PreflopAction.FOLD, PreflopAction.RAISE),
			userAnswer = null,
		)

		val decoded = converters.toQuizRecordQuestions(
			converters.fromQuizRecordQuestions(listOf(question)),
		)

		assertEquals(listOf(question), decoded)
	}

	@Test
	fun corruptQuestionsDecodeAsEmpty() {
		assertTrue(converters.toQuizRecordQuestions("""[{"correct":"UNKNOWN_ACTION"}]""").isEmpty())
	}
}
