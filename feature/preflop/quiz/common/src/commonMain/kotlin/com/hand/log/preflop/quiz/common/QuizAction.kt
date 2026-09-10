package com.hand.log.preflop.quiz.common

import com.hand.log.domain.model.preflop.PreflopAction
import handylog.core.res.generated.resources.Res
import handylog.core.res.generated.resources.preflop_action_3bet
import handylog.core.res.generated.resources.preflop_action_3bet_bluff
import handylog.core.res.generated.resources.preflop_action_3bet_call
import handylog.core.res.generated.resources.preflop_action_3bet_fold
import handylog.core.res.generated.resources.preflop_action_3bet_jam
import handylog.core.res.generated.resources.preflop_action_3bet_stackoff
import handylog.core.res.generated.resources.preflop_action_4bet
import handylog.core.res.generated.resources.preflop_action_4bet_bluff
import handylog.core.res.generated.resources.preflop_action_allin
import handylog.core.res.generated.resources.preflop_action_call
import handylog.core.res.generated.resources.preflop_action_check
import handylog.core.res.generated.resources.preflop_action_fold
import handylog.core.res.generated.resources.preflop_action_limp
import handylog.core.res.generated.resources.preflop_action_raise
import handylog.core.res.generated.resources.preflop_action_raise_4bet
import handylog.core.res.generated.resources.preflop_action_raise_bluff
import handylog.core.res.generated.resources.preflop_action_raise_call
import handylog.core.res.generated.resources.preflop_action_raise_fold
import handylog.core.res.generated.resources.preflop_action_raise_jam
import handylog.core.res.generated.resources.preflop_action_stackoff
import org.jetbrains.compose.resources.StringResource

/**
 * 퀴즈 정답·선택지는 차트의 [PreflopAction] 을 1:1 로 그대로 쓴다.
 * 차트 셀 라벨과 동일한 문자열 리소스를 사용해 표기를 일치시킨다.
 */
fun PreflopAction.answerLabelRes(): StringResource = when (this) {
	PreflopAction.RAISE -> Res.string.preflop_action_raise
	PreflopAction.RAISE_BLUFF -> Res.string.preflop_action_raise_bluff
	PreflopAction.RAISE_FOLD -> Res.string.preflop_action_raise_fold
	PreflopAction.RAISE_CALL -> Res.string.preflop_action_raise_call
	PreflopAction.RAISE_4BET -> Res.string.preflop_action_raise_4bet
	PreflopAction.RAISE_JAM -> Res.string.preflop_action_raise_jam
	PreflopAction.THREE_BET -> Res.string.preflop_action_3bet
	PreflopAction.THREE_BET_BLUFF -> Res.string.preflop_action_3bet_bluff
	PreflopAction.THREE_BET_STACKOFF -> Res.string.preflop_action_3bet_stackoff
	PreflopAction.THREE_BET_FOLD -> Res.string.preflop_action_3bet_fold
	PreflopAction.THREE_BET_CALL -> Res.string.preflop_action_3bet_call
	PreflopAction.THREE_BET_JAM -> Res.string.preflop_action_3bet_jam
	PreflopAction.FOUR_BET -> Res.string.preflop_action_4bet
	PreflopAction.FOUR_BET_BLUFF -> Res.string.preflop_action_4bet_bluff
	PreflopAction.ALL_IN -> Res.string.preflop_action_allin
	PreflopAction.CALL -> Res.string.preflop_action_call
	PreflopAction.LIMP -> Res.string.preflop_action_limp
	PreflopAction.CHECK -> Res.string.preflop_action_check
	PreflopAction.FOLD -> Res.string.preflop_action_fold
}

/**
 * 1차 선택(첫 액션) 식별자. 퀴즈는 밸류/블러프를 구분하지 않으므로 블러프 변형과
 * 리레이즈(오픈이면 3벳, 3벳이면 4벳) 대응 계획을 모두 벗겨낸 대표 액션을 돌려준다.
 */
fun PreflopAction.primaryAction(): PreflopAction = when (this) {
	PreflopAction.RAISE,
	PreflopAction.RAISE_BLUFF,
	PreflopAction.RAISE_FOLD,
	PreflopAction.RAISE_CALL,
	PreflopAction.RAISE_4BET,
	PreflopAction.RAISE_JAM,
	-> PreflopAction.RAISE
	PreflopAction.THREE_BET,
	PreflopAction.THREE_BET_BLUFF,
	PreflopAction.THREE_BET_STACKOFF,
	PreflopAction.THREE_BET_FOLD,
	PreflopAction.THREE_BET_CALL,
	PreflopAction.THREE_BET_JAM,
	-> PreflopAction.THREE_BET
	PreflopAction.FOUR_BET,
	PreflopAction.FOUR_BET_BLUFF,
	-> PreflopAction.FOUR_BET
	else -> this // 올인·콜·림프·체크·폴드는 그 자체가 1차 액션
}

/** 리레이즈 대응 계획이 붙은 복합 라인인지(레이즈/콜, 3벳/폴드 등). 밸류/블러프 변형은 포함하지 않는다. */
fun PreflopAction.hasResponsePlan(): Boolean = when (this) {
	PreflopAction.RAISE_FOLD,
	PreflopAction.RAISE_CALL,
	PreflopAction.RAISE_4BET,
	PreflopAction.RAISE_JAM,
	PreflopAction.THREE_BET_STACKOFF,
	PreflopAction.THREE_BET_FOLD,
	PreflopAction.THREE_BET_CALL,
	PreflopAction.THREE_BET_JAM,
	-> true
	else -> false
}

/** 퀴즈용 정규화 — 밸류/블러프를 구분하지 않으므로 블러프 변형은 기본 액션으로 접는다. */
fun PreflopAction.withoutBluff(): PreflopAction = when (this) {
	PreflopAction.RAISE_BLUFF -> PreflopAction.RAISE
	PreflopAction.THREE_BET_BLUFF -> PreflopAction.THREE_BET
	PreflopAction.FOUR_BET_BLUFF -> PreflopAction.FOUR_BET
	else -> this
}

fun answerOptionsOf(
	presentActions: Collection<PreflopAction>,
	correct: PreflopAction,
): List<PreflopAction> {
	val set = presentActions.mapTo(mutableSetOf()) { it.withoutBluff() }
	set.add(PreflopAction.FOLD)
	set.add(correct)
	return PreflopAction.entries.filter { it in set }
}

/** 2차 선택 버튼 라벨 — 리레이즈에 대한 대응만 짧게 표기(폴드·콜·4벳·올인·스택오프). */
fun PreflopAction.planLabelRes(): StringResource = when (this) {
	PreflopAction.RAISE_FOLD, PreflopAction.THREE_BET_FOLD -> Res.string.preflop_action_fold
	PreflopAction.RAISE_CALL, PreflopAction.THREE_BET_CALL -> Res.string.preflop_action_call
	PreflopAction.RAISE_4BET -> Res.string.preflop_action_4bet
	PreflopAction.RAISE_JAM, PreflopAction.THREE_BET_JAM -> Res.string.preflop_action_allin
	PreflopAction.THREE_BET_STACKOFF -> Res.string.preflop_action_stackoff
	else -> answerLabelRes()
}
