package com.hand.log.preflop.chart.component

import androidx.compose.ui.graphics.Color
import com.hand.log.domain.model.preflop.PreflopAction
import handylog.core.res.generated.resources.Res
import handylog.core.res.generated.resources.preflop_action_3bet
import handylog.core.res.generated.resources.preflop_action_3bet_bluff
import handylog.core.res.generated.resources.preflop_action_4bet
import handylog.core.res.generated.resources.preflop_action_4bet_bluff
import handylog.core.res.generated.resources.preflop_action_allin
import handylog.core.res.generated.resources.preflop_action_call
import handylog.core.res.generated.resources.preflop_action_fold
import handylog.core.res.generated.resources.preflop_action_limp
import handylog.core.res.generated.resources.preflop_action_raise
import handylog.core.res.generated.resources.preflop_action_raise_bluff
import org.jetbrains.compose.resources.StringResource

/**
 * 차트 셀 전용 고정 컬러 — 다크/라이트 모드 모두 동일 (GTO Wizard 스타일).
 *
 * - 밸류 레이즈 계열: 레드 (오픈 → 3벳 → 4벳 순으로 진해짐)
 * - 블러프 레이즈 계열: 오렌지 (오픈 → 3벳 → 4벳 순으로 진해짐)
 * - 콜: 그린, 림프: 딥그린, 폴드: 슬레이트 그레이
 */
private val ChartRed = Color(0xFFD63A3A) // 오픈 레이즈 — 살짝 톤 낮춘 레드
private val ChartRedDeep = Color(0xFFC23030) // 3-벳
private val ChartRedDark = Color(0xFFA82626) // 4-벳
private val ChartOrange = Color(0xFFE88A30) // 오픈 블러프 — 테마 split(다크) 동일값
private val ChartOrangeDeep = Color(0xFFD97B1E) // 3-벳 블러프
private val ChartOrangeDark = Color(0xFFC46A10) // 4-벳 블러프
private val ChartPurple = Color(0xFF8E44AD) // 올인 — 레이즈 계열과 확실히 구분되는 퍼플
private val ChartGreen = Color(0xFF0FB67F) // 콜 — 테마 primary(다크) 동일값
private val ChartGreenDark = Color(0xFF2E9E6B) // 림프 — 콜보다 살짝 짙은 밝은 그린
private val ChartGray = Color(0xFF607D8B) // 폴드 — 슬레이트 그레이
private val ChartText = Color(0xFFFFFFFF) // 텍스트 (모든 셀 공통 흰색)

/** 액션별 (배경색, 글자색). 다크/라이트 모드 무관하게 동일한 고정 컬러를 반환한다. */
internal fun actionColors(action: PreflopAction): Pair<Color, Color> = when (action) {
	PreflopAction.RAISE -> ChartRed to ChartText
	PreflopAction.RAISE_BLUFF -> ChartOrange to ChartText
	PreflopAction.THREE_BET -> ChartRedDeep to ChartText
	PreflopAction.THREE_BET_BLUFF -> ChartOrangeDeep to ChartText
	PreflopAction.FOUR_BET -> ChartRedDark to ChartText
	PreflopAction.FOUR_BET_BLUFF -> ChartOrangeDark to ChartText
	PreflopAction.ALL_IN -> ChartPurple to ChartText
	PreflopAction.CALL -> ChartGreen to ChartText
	PreflopAction.LIMP -> ChartGreenDark to ChartText
	PreflopAction.FOLD -> ChartGray to ChartText
}

internal fun actionLabelRes(action: PreflopAction): StringResource = when (action) {
	PreflopAction.RAISE -> Res.string.preflop_action_raise
	PreflopAction.RAISE_BLUFF -> Res.string.preflop_action_raise_bluff
	PreflopAction.THREE_BET -> Res.string.preflop_action_3bet
	PreflopAction.THREE_BET_BLUFF -> Res.string.preflop_action_3bet_bluff
	PreflopAction.FOUR_BET -> Res.string.preflop_action_4bet
	PreflopAction.FOUR_BET_BLUFF -> Res.string.preflop_action_4bet_bluff
	PreflopAction.ALL_IN -> Res.string.preflop_action_allin
	PreflopAction.CALL -> Res.string.preflop_action_call
	PreflopAction.LIMP -> Res.string.preflop_action_limp
	PreflopAction.FOLD -> Res.string.preflop_action_fold
}
