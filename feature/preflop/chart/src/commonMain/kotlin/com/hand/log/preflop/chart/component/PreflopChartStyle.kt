package com.hand.log.preflop.chart.component

import androidx.compose.ui.graphics.Color
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
import org.jetbrains.compose.resources.StringResource

/**
 * 차트 셀 전용 고정 컬러 — 다크/라이트 모드 모두 동일 (GTO Wizard 스타일).
 *
 * 계열별 색상(hue)으로 베이스 액션을, 명도/변형으로 대응 계획을 구분한다.
 * - 오픈(RFI) 계열: 레드~로즈 (밸류 레드 / 블러프 오렌지 / 콜·4벳·잼은 명도 변형)
 * - 3벳 계열: 퍼플 (밸류·블러프·스택오프·폴드·콜·잼)
 * - 4벳 계열: 브릭 레드, 올인: 다크 네이비, 콜: 그린, 림프: 딥그린, 체크: 스틸블루, 폴드: 그레이
 */
private val ChartRed = Color(0xFFD63A3A) // 오픈(밸류)
private val ChartOrange = Color(0xFFE08A2E) // 오픈(블러프)
private val ChartRose = Color(0xFFB56A6A) // 오픈/폴드 — 3벳 맞으면 포기, 가장 약한 오픈
private val ChartPink = Color(0xFFD65A8E) // 오픈/콜 — 3벳 콜
private val ChartRedDark = Color(0xFFA82626) // 오픈/4벳 — 강하게 되받음
private val ChartMaroon = Color(0xFF7E1E1E) // 오픈/잼(open-shove)
private val ChartPurple = Color(0xFF8E44AD) // 3벳(밸류)
private val ChartViolet = Color(0xFFB07CC6) // 3벳(블러프)
private val ChartPurpleDeep = Color(0xFF6C3483) // 3벳/스택오프
private val ChartPurpleMuted = Color(0xFF9B84B0) // 3벳/폴드
private val ChartIndigo = Color(0xFF5B6BBF) // 3벳/콜
private val ChartPurpleDark = Color(0xFF4A235A) // 3벳/잼
private val ChartBrick = Color(0xFFC0392B) // 4벳(밸류)
private val ChartOrangeDark = Color(0xFFB56718) // 4벳(블러프)
private val ChartNavy = Color(0xFF2C3E50) // 올인 — 다크 네이비
private val ChartGreen = Color(0xFF0FB67F) // 콜 — 테마 primary(다크) 동일값
private val ChartGreenDark = Color(0xFF2E9E6B) // 림프 — 콜보다 살짝 짙은 밝은 그린
private val ChartBlue = Color(0xFF3D7EA6) // 체크(림프 대응) — 폴드/콜과 구분되는 스틸 블루
private val ChartGray = Color(0xFF607D8B) // 폴드 — 슬레이트 그레이
private val ChartText = Color(0xFFFFFFFF) // 텍스트 (모든 셀 공통 흰색)

/** 액션별 (배경색, 글자색). 다크/라이트 모드 무관하게 동일한 고정 컬러를 반환한다. */
internal fun actionColors(action: PreflopAction): Pair<Color, Color> = when (action) {
	PreflopAction.RAISE -> ChartRed to ChartText
	PreflopAction.RAISE_BLUFF -> ChartOrange to ChartText
	PreflopAction.RAISE_FOLD -> ChartRose to ChartText
	PreflopAction.RAISE_CALL -> ChartPink to ChartText
	PreflopAction.RAISE_4BET -> ChartRedDark to ChartText
	PreflopAction.RAISE_JAM -> ChartMaroon to ChartText
	PreflopAction.THREE_BET -> ChartPurple to ChartText
	PreflopAction.THREE_BET_BLUFF -> ChartViolet to ChartText
	PreflopAction.THREE_BET_STACKOFF -> ChartPurpleDeep to ChartText
	PreflopAction.THREE_BET_FOLD -> ChartPurpleMuted to ChartText
	PreflopAction.THREE_BET_CALL -> ChartIndigo to ChartText
	PreflopAction.THREE_BET_JAM -> ChartPurpleDark to ChartText
	PreflopAction.FOUR_BET -> ChartBrick to ChartText
	PreflopAction.FOUR_BET_BLUFF -> ChartOrangeDark to ChartText
	PreflopAction.ALL_IN -> ChartNavy to ChartText
	PreflopAction.CALL -> ChartGreen to ChartText
	PreflopAction.LIMP -> ChartGreenDark to ChartText
	PreflopAction.CHECK -> ChartBlue to ChartText
	PreflopAction.FOLD -> ChartGray to ChartText
}

internal fun actionLabelRes(action: PreflopAction): StringResource = when (action) {
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
