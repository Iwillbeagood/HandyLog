package com.hand.log.ui

import androidx.compose.runtime.Composable
import com.hand.log.domain.model.Action
import com.hand.log.domain.model.ActionType
import com.hand.log.domain.model.GameType
import com.hand.log.domain.model.HandRanking
import com.hand.log.domain.model.PlayerTendency
import com.hand.log.domain.model.Street
import com.hand.log.domain.model.ThemeMode
import handylog.core.res.generated.resources.Res
import handylog.core.res.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
fun ActionType.localizedLabel(): String = when (this) {
	ActionType.FOLD -> stringResource(Res.string.action_fold)
	ActionType.CHECK -> stringResource(Res.string.action_check)
	ActionType.CALL -> stringResource(Res.string.action_call)
	ActionType.BET -> stringResource(Res.string.action_bet)
	ActionType.RAISE -> stringResource(Res.string.action_raise)
	ActionType.ALL_IN -> stringResource(Res.string.action_all_in)
}

/** Action의 betLevel 기반 로컬라이즈된 라벨 */
@Composable
fun Action.localizedLabel(): String = when {
	type == ActionType.ALL_IN -> stringResource(Res.string.action_all_in)
	type == ActionType.FOLD -> stringResource(Res.string.action_fold)
	type == ActionType.CHECK -> stringResource(Res.string.action_check)
	type == ActionType.CALL -> stringResource(Res.string.action_call)
	betLevel <= 0 -> type.localizedLabel()
	betLevel == 1 -> stringResource(Res.string.action_bet)
	betLevel == 2 -> stringResource(Res.string.action_raise)
	else -> stringResource(Res.string.action_n_bet, betLevel)
}

@Composable
fun Street.localizedLabel(): String = when (this) {
	Street.PREFLOP -> stringResource(Res.string.step_preflop)
	Street.FLOP -> stringResource(Res.string.step_flop)
	Street.TURN -> stringResource(Res.string.step_turn)
	Street.RIVER -> stringResource(Res.string.step_river)
}

@Composable
fun GameType.localizedLabel(): String = when (this) {
	GameType.CASH -> stringResource(Res.string.game_type_cash)
	GameType.TOURNAMENT -> stringResource(Res.string.game_type_tournament)
}

@Composable
fun PlayerTendency.localizedLabel(): String = when (this) {
	PlayerTendency.TIGHT -> stringResource(Res.string.tendency_tight)
	PlayerTendency.LOOSE -> stringResource(Res.string.tendency_loose)
	PlayerTendency.AGGRESSIVE -> stringResource(Res.string.tendency_aggressive)
	PlayerTendency.PASSIVE -> stringResource(Res.string.tendency_passive)
	PlayerTendency.NIT -> stringResource(Res.string.tendency_nit)
	PlayerTendency.MANIAC -> stringResource(Res.string.tendency_maniac)
	PlayerTendency.UNKNOWN -> stringResource(Res.string.tendency_unknown)
}

@Composable
fun ThemeMode.localizedLabel(): String = when (this) {
	ThemeMode.AUTO -> stringResource(Res.string.theme_auto)
	ThemeMode.LIGHT -> stringResource(Res.string.theme_light)
	ThemeMode.DARK -> stringResource(Res.string.theme_dark)
}

@Composable
fun ThemeMode.localizedDesc(): String = when (this) {
	ThemeMode.AUTO -> stringResource(Res.string.theme_auto_desc)
	ThemeMode.LIGHT -> stringResource(Res.string.theme_light_desc)
	ThemeMode.DARK -> stringResource(Res.string.theme_dark_desc)
}

@Composable
fun HandRanking.localizedLabel(): String = when (this) {
	HandRanking.ROYAL_FLUSH -> stringResource(Res.string.ranking_royal_flush)
	HandRanking.STRAIGHT_FLUSH -> stringResource(Res.string.ranking_straight_flush)
	HandRanking.FOUR_OF_A_KIND -> stringResource(Res.string.ranking_four_of_a_kind)
	HandRanking.FULL_HOUSE -> stringResource(Res.string.ranking_full_house)
	HandRanking.FLUSH -> stringResource(Res.string.ranking_flush)
	HandRanking.STRAIGHT -> stringResource(Res.string.ranking_straight)
	HandRanking.THREE_OF_A_KIND -> stringResource(Res.string.ranking_three_of_a_kind)
	HandRanking.TWO_PAIR -> stringResource(Res.string.ranking_two_pair)
	HandRanking.ONE_PAIR -> stringResource(Res.string.ranking_one_pair)
	HandRanking.HIGH_CARD -> stringResource(Res.string.ranking_high_card)
}
