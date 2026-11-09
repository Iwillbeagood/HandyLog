package com.hand.log.record.quick.component

import androidx.compose.runtime.Composable
import com.hand.log.domain.model.Card
import com.hand.log.domain.model.quick.CardOwner
import com.hand.log.record.quick.contract.QuickRecordModalEffect
import com.hand.log.ui.poker.BoardCardSelectorSheet
import com.hand.log.ui.poker.CardSelectorSheet
import handylog.core.res.generated.resources.Res
import handylog.core.res.generated.resources.board_cards
import handylog.core.res.generated.resources.card_selector_hero
import handylog.core.res.generated.resources.card_selector_showdown
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun QuickCardPicker(
	picker: QuickRecordModalEffect.ShowCardPicker,
	onCardsSelected: (List<Card>) -> Unit,
	onUnknownSelected: () -> Unit,
	onDismiss: () -> Unit,
) {
	when (picker.owner) {
		CardOwner.Board -> BoardCardSelectorSheet(
			title = stringResource(Res.string.board_cards),
			selectedCards = picker.usedCards,
			onCardsSelected = onCardsSelected,
			onDismiss = onDismiss,
			initialCards = picker.initialCards,
			minCards = picker.cardCount,
			maxCards = picker.cardCount,
		)
		CardOwner.Hero, is CardOwner.Opponent -> CardSelectorSheet(
			title = if (picker.owner == CardOwner.Hero) {
				stringResource(Res.string.card_selector_hero)
			} else {
				stringResource(Res.string.card_selector_showdown, picker.positionLabel)
			},
			maxCards = picker.cardCount,
			selectedCards = picker.usedCards,
			onCardsSelected = onCardsSelected,
			onDismiss = onDismiss,
			onUnknownSelected = onUnknownSelected.takeIf { picker.allowsUnknown },
			initialCards = picker.initialCards,
		)
	}
}
