package com.hand.log.record.util

import androidx.compose.runtime.Composable
import com.hand.log.record.contract.CardSelectorTarget
import com.hand.log.ui.stringRes
import handylog.core.res.generated.resources.Res
import handylog.core.res.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun CardSelectorTarget.title(): String = when (this) {
	is CardSelectorTarget.HeroCard -> stringResource(Res.string.card_selector_hero)
	is CardSelectorTarget.AllBoardCards -> stringResource(Res.string.board_cards)
	is CardSelectorTarget.BoardCard -> stringResource(
		Res.string.card_selector_board,
		stringResource(street.stringRes()),
	)
	is CardSelectorTarget.SingleBoardCard -> stringResource(
		Res.string.card_selector_board_change,
		stringResource(street.stringRes()),
	)
	is CardSelectorTarget.ShowdownCard -> stringResource(
		Res.string.card_selector_showdown,
		positionName,
	)
}
