package com.hand.log.preflop.quiz.session.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.hand.log.designsystem.etc.ThemePreview
import com.hand.log.designsystem.etc.ThemePreviews
import com.hand.log.designsystem.theme.HandyTheme
import com.hand.log.domain.model.Card
import com.hand.log.domain.model.PocketCards
import com.hand.log.domain.model.Position
import com.hand.log.domain.model.Rank
import com.hand.log.domain.model.Suit
import com.hand.log.domain.model.preflop.HandShape
import com.hand.log.domain.model.preflop.PreflopAction
import com.hand.log.domain.model.preflop.PreflopHand
import com.hand.log.domain.model.preflop.PreflopScenario
import com.hand.log.domain.model.preflop.PreflopStack
import com.hand.log.preflop.quiz.common.PreflopQuizQuestion
import com.hand.log.preflop.quiz.common.answerLabelRes
import com.hand.log.preflop.quiz.common.hasResponsePlan
import com.hand.log.preflop.quiz.common.planLabelRes
import com.hand.log.preflop.quiz.common.primaryAction
import com.hand.log.ui.poker.CardSize
import com.hand.log.ui.poker.HoleCards
import handylog.core.res.generated.resources.Res
import handylog.core.res.generated.resources.preflop_action_3bet
import handylog.core.res.generated.resources.preflop_action_limp
import handylog.core.res.generated.resources.preflop_action_raise
import handylog.core.res.generated.resources.quiz_answer_plan_format
import handylog.core.res.generated.resources.quiz_situation_facing_rfi
import handylog.core.res.generated.resources.quiz_situation_rfi
import handylog.core.res.generated.resources.quiz_situation_vs_3bet
import handylog.core.res.generated.resources.quiz_situation_vs_limp
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun SpotChip(text: String) {
	val colors = HandyTheme.colorScheme
	Box(
		modifier = Modifier
			.clip(RoundedCornerShape(14.dp))
			.background(colors.muted)
			.border(1.dp, colors.border, RoundedCornerShape(14.dp))
			.padding(horizontal = 12.dp, vertical = 6.dp),
	) {
		Text(
			text = text,
			style = HandyTheme.typography.bold12,
			color = colors.textPrimary,
		)
	}
}

/** 프리플랍 핸드(notation)를 공용 [HoleCards] 로 표시. 페어/수딧/오프수딧을 무늬 색으로 구분한다. */
@Composable
internal fun HandCards(
	hand: PreflopHand,
	size: CardSize = CardSize.LG,
	modifier: Modifier = Modifier,
) {
	val (first, second) = holeCardsOf(hand)
	HoleCards(cards = PocketCards(first, second), size = size, modifier = modifier)
}

internal fun holeCardsOf(hand: PreflopHand): Pair<Card, Card> = when (hand.shape) {
	HandShape.PAIR -> Card(hand.high, Suit.SPADES) to Card(hand.high, Suit.HEARTS)
	HandShape.SUITED -> Card(hand.high, Suit.SPADES) to Card(hand.low, Suit.SPADES)
	HandShape.OFFSUIT -> Card(hand.high, Suit.SPADES) to Card(hand.low, Suit.HEARTS)
}

/** 선택지 라벨은 차트 액션과 1:1 로, 정확한 액션 표기(레이즈/폴드·3벳/콜 등)를 그대로 보여준다. */
@Composable
internal fun answerText(action: PreflopAction): String =
	stringResource(action.answerLabelRes())

@Composable
internal fun planText(action: PreflopAction): String =
	stringResource(action.planLabelRes())

/** 리뷰용 정답·답변 표기 — 복합 라인은 "첫 액션 → 대응"(예: 레이즈 → 폴드)으로 펼쳐 보여준다. */
@Composable
internal fun answerReviewText(action: PreflopAction): String =
	if (action.hasResponsePlan()) {
		stringResource(
			Res.string.quiz_answer_plan_format,
			stringResource(action.primaryAction().answerLabelRes()),
			stringResource(action.planLabelRes()),
		)
	} else {
		stringResource(action.answerLabelRes())
	}

/** 문제 상황을 압축 포커 표기로 — 예: `HERO(UTG+1) open`, `CO open → HERO(BTN)`. 테이블 시각 표현을 텍스트로 보완. */
@Composable
internal fun situationDescription(question: PreflopQuizQuestion): String {
	val hero = question.hero.label
	val villain = question.villain?.label ?: ""
	return when (question.scenario) {
		PreflopScenario.RFI -> stringResource(Res.string.quiz_situation_rfi, hero)
		PreflopScenario.FACING_RFI -> stringResource(Res.string.quiz_situation_facing_rfi, villain, hero)
		PreflopScenario.VS_3BET -> stringResource(Res.string.quiz_situation_vs_3bet, hero, villain)
		PreflopScenario.VS_LIMP -> stringResource(Res.string.quiz_situation_vs_limp, villain, hero)
	}
}

/** 빌런(레이저/림퍼)이 취한 액션 라벨. RFI 는 앞 액션이 없어 null. */
@Composable
internal fun villainActionLabel(scenario: PreflopScenario): String? = when (scenario) {
	PreflopScenario.RFI -> null
	PreflopScenario.FACING_RFI -> stringResource(Res.string.preflop_action_raise)
	PreflopScenario.VS_3BET -> stringResource(Res.string.preflop_action_3bet)
	PreflopScenario.VS_LIMP -> stringResource(Res.string.preflop_action_limp)
}

/** 히어로가 이미 취한 액션 라벨. VS_3BET 은 내가 오픈(레이즈)한 뒤 3벳을 당한 상황이므로 '레이즈'. */
@Composable
internal fun heroActionLabel(scenario: PreflopScenario): String? = when (scenario) {
	PreflopScenario.VS_3BET -> stringResource(Res.string.preflop_action_raise)
	else -> null
}

/** RFI(내가 첫 오픈) 샘플. */
internal val previewQuestion = PreflopQuizQuestion(
	stack = PreflopStack.BB100,
	scenario = PreflopScenario.RFI,
	hero = Position.CO,
	villain = null,
	hand = PreflopHand(Rank.ACE, Rank.KING, HandShape.SUITED),
	correct = PreflopAction.RAISE_CALL,
	options = listOf(PreflopAction.RAISE, PreflopAction.FOLD),
	planOptions = listOf(
		PreflopAction.RAISE_FOLD,
		PreflopAction.RAISE_CALL,
		PreflopAction.RAISE_4BET,
	),
)

/** SB 첫 오픈(레이즈/림프/폴드) 샘플. */
internal val previewSbLimpQuestion = PreflopQuizQuestion(
	stack = PreflopStack.BB100,
	scenario = PreflopScenario.RFI,
	hero = Position.SB,
	villain = null,
	hand = PreflopHand(Rank.NINE, Rank.SEVEN, HandShape.SUITED),
	correct = PreflopAction.LIMP,
	options = listOf(PreflopAction.RAISE, PreflopAction.LIMP, PreflopAction.FOLD),
)

/** vs 오픈(앞 포지션의 오픈에 대응) 샘플. */
internal val previewFacingQuestion = PreflopQuizQuestion(
	stack = PreflopStack.BB40,
	scenario = PreflopScenario.FACING_RFI,
	hero = Position.BTN,
	villain = Position.CO,
	hand = PreflopHand(Rank.ACE, Rank.QUEEN, HandShape.SUITED),
	correct = PreflopAction.THREE_BET_CALL,
	options = listOf(PreflopAction.THREE_BET, PreflopAction.CALL, PreflopAction.FOLD),
	planOptions = listOf(
		PreflopAction.THREE_BET_FOLD,
		PreflopAction.THREE_BET_CALL,
		PreflopAction.THREE_BET_JAM,
	),
)

/** vs 3벳(내 오픈에 상대가 3벳) 샘플. */
internal val previewVs3betQuestion = PreflopQuizQuestion(
	stack = PreflopStack.BB100,
	scenario = PreflopScenario.VS_3BET,
	hero = Position.CO,
	villain = Position.BTN,
	hand = PreflopHand(Rank.KING, Rank.KING, HandShape.PAIR),
	correct = PreflopAction.FOUR_BET,
	options = listOf(
		PreflopAction.FOUR_BET,
		PreflopAction.ALL_IN,
		PreflopAction.CALL,
		PreflopAction.FOLD,
	),
)

@ThemePreviews
@Composable
private fun SpotChipPreview() {
	ThemePreview {
		Row(
			horizontalArrangement = Arrangement.spacedBy(8.dp),
			modifier = Modifier
				.background(HandyTheme.colorScheme.background)
				.padding(16.dp),
		) {
			SpotChip("100BB")
			SpotChip("CO")
			SpotChip("vs BTN")
		}
	}
}

@ThemePreviews
@Composable
private fun HandCardsPreview() {
	ThemePreview {
		Row(
			horizontalArrangement = Arrangement.spacedBy(16.dp),
			modifier = Modifier
				.background(HandyTheme.colorScheme.background)
				.padding(16.dp),
		) {
			HandCards(PreflopHand(Rank.ACE, Rank.ACE, HandShape.PAIR))
			HandCards(PreflopHand(Rank.KING, Rank.QUEEN, HandShape.SUITED))
			HandCards(PreflopHand(Rank.ACE, Rank.JACK, HandShape.OFFSUIT))
		}
	}
}
