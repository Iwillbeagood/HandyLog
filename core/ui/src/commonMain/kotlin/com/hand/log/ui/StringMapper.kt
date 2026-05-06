package com.hand.log.ui

import com.hand.log.domain.model.ActionType
import com.hand.log.domain.model.GameType
import com.hand.log.domain.model.HandRanking
import com.hand.log.domain.model.HeroResultType
import com.hand.log.domain.model.PlayerTendency
import com.hand.log.domain.model.Street
import com.hand.log.domain.model.ThemeMode
import handylog.core.res.generated.resources.Res
import handylog.core.res.generated.resources.*
import org.jetbrains.compose.resources.StringResource

fun ActionType.stringRes(): StringResource = when (this) {
	ActionType.FOLD -> Res.string.action_fold
	ActionType.CHECK -> Res.string.action_check
	ActionType.CALL -> Res.string.action_call
	ActionType.BET -> Res.string.action_bet
	ActionType.RAISE -> Res.string.action_raise
	ActionType.ALL_IN -> Res.string.action_all_in
}

fun Street.stringRes(): StringResource = when (this) {
	Street.PREFLOP -> Res.string.step_preflop
	Street.FLOP -> Res.string.step_flop
	Street.TURN -> Res.string.step_turn
	Street.RIVER -> Res.string.step_river
}

fun GameType.stringRes(): StringResource = when (this) {
	is GameType.Cash -> Res.string.game_type_cash
	is GameType.Tournament -> Res.string.game_type_tournament
}

fun PlayerTendency.stringRes(): StringResource = when (this) {
	PlayerTendency.TIGHT_AGGRESSIVE -> Res.string.tendency_tight_aggressive
	PlayerTendency.TIGHT_PASSIVE -> Res.string.tendency_tight_passive
	PlayerTendency.LOOSE_AGGRESSIVE -> Res.string.tendency_loose_aggressive
	PlayerTendency.LOOSE_PASSIVE -> Res.string.tendency_loose_passive
	PlayerTendency.SHARK -> Res.string.tendency_shark
	PlayerTendency.REGULAR -> Res.string.tendency_regular
	PlayerTendency.FISH -> Res.string.tendency_fish
	PlayerTendency.UNKNOWN -> Res.string.tendency_unknown
}

fun ThemeMode.stringRes(): StringResource = when (this) {
	ThemeMode.AUTO -> Res.string.theme_auto
	ThemeMode.LIGHT -> Res.string.theme_light
	ThemeMode.DARK -> Res.string.theme_dark
}

fun ThemeMode.descStringRes(): StringResource = when (this) {
	ThemeMode.AUTO -> Res.string.theme_auto_desc
	ThemeMode.LIGHT -> Res.string.theme_light_desc
	ThemeMode.DARK -> Res.string.theme_dark_desc
}

fun HandRanking.stringRes(): StringResource = when (this) {
	HandRanking.ROYAL_FLUSH -> Res.string.ranking_royal_flush
	HandRanking.STRAIGHT_FLUSH -> Res.string.ranking_straight_flush
	HandRanking.FOUR_OF_A_KIND -> Res.string.ranking_four_of_a_kind
	HandRanking.FULL_HOUSE -> Res.string.ranking_full_house
	HandRanking.FLUSH -> Res.string.ranking_flush
	HandRanking.STRAIGHT -> Res.string.ranking_straight
	HandRanking.THREE_OF_A_KIND -> Res.string.ranking_three_of_a_kind
	HandRanking.TWO_PAIR -> Res.string.ranking_two_pair
	HandRanking.ONE_PAIR -> Res.string.ranking_one_pair
	HandRanking.HIGH_CARD -> Res.string.ranking_high_card
	HandRanking.WIN_BY_FOLD -> Res.string.ranking_fold
}

fun HeroResultType.resultStringRes(hasRanking: Boolean): StringResource = when {
	this == HeroResultType.FOLD_WIN -> Res.string.showdown_result_fold_win
	this == HeroResultType.FOLD_LOSE -> Res.string.showdown_result_fold_lose
	!hasRanking && this == HeroResultType.SHOWDOWN_WIN -> Res.string.showdown_result_fold_win
	!hasRanking && this == HeroResultType.SHOWDOWN_LOSE -> Res.string.showdown_result_fold_lose
	this == HeroResultType.SHOWDOWN_SPLIT -> Res.string.showdown_result_split
	this == HeroResultType.SHOWDOWN_WIN -> Res.string.showdown_result_win
	else -> Res.string.showdown_result_lose
}
