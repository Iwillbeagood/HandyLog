package com.hand.log.preflop.chart.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.hand.log.designsystem.theme.HandyTheme
import com.hand.log.domain.model.preflop.PreflopAction
import handylog.core.res.generated.resources.Res
import handylog.core.res.generated.resources.preflop_action_3bet
import handylog.core.res.generated.resources.preflop_action_3bet_bluff
import handylog.core.res.generated.resources.preflop_action_4bet
import handylog.core.res.generated.resources.preflop_action_4bet_bluff
import handylog.core.res.generated.resources.preflop_action_call
import handylog.core.res.generated.resources.preflop_action_fold
import handylog.core.res.generated.resources.preflop_action_limp
import handylog.core.res.generated.resources.preflop_action_raise
import handylog.core.res.generated.resources.preflop_action_raise_bluff
import org.jetbrains.compose.resources.StringResource

/** 액션별 (배경색, 글자색). 벳 종류(오픈/3벳/4벳)와 밸류·블러프를 색으로 구분한다. */
@Composable
internal fun actionColors(action: PreflopAction): Pair<Color, Color> {
	val colors = HandyTheme.colorScheme
	return when (action) {
		PreflopAction.RAISE -> colors.primary to colors.onPrimary
		PreflopAction.RAISE_BLUFF -> colors.accent to colors.onAccent
		PreflopAction.THREE_BET -> colors.error to colors.onSecondary
		PreflopAction.THREE_BET_BLUFF -> colors.split to colors.onPrimary
		PreflopAction.FOUR_BET -> colors.gold to colors.onPrimary
		PreflopAction.FOUR_BET_BLUFF -> colors.goldMuted to colors.onPrimary
		PreflopAction.CALL -> colors.secondary to colors.onSecondary
		PreflopAction.LIMP -> colors.feltLight to colors.onPrimary
		PreflopAction.FOLD -> colors.muted to colors.textSecondary
	}
}

internal fun actionLabelRes(action: PreflopAction): StringResource = when (action) {
	PreflopAction.RAISE -> Res.string.preflop_action_raise
	PreflopAction.RAISE_BLUFF -> Res.string.preflop_action_raise_bluff
	PreflopAction.THREE_BET -> Res.string.preflop_action_3bet
	PreflopAction.THREE_BET_BLUFF -> Res.string.preflop_action_3bet_bluff
	PreflopAction.FOUR_BET -> Res.string.preflop_action_4bet
	PreflopAction.FOUR_BET_BLUFF -> Res.string.preflop_action_4bet_bluff
	PreflopAction.CALL -> Res.string.preflop_action_call
	PreflopAction.LIMP -> Res.string.preflop_action_limp
	PreflopAction.FOLD -> Res.string.preflop_action_fold
}
