package com.hand.log.record.quick.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hand.log.designsystem.etc.ThemePreview
import com.hand.log.designsystem.etc.ThemePreviews
import com.hand.log.designsystem.theme.HandyTheme
import com.hand.log.domain.model.Card
import com.hand.log.domain.model.Rank
import com.hand.log.domain.model.Suit
import com.hand.log.ui.poker.CardSize
import com.hand.log.ui.poker.PlayingCard
import handylog.core.res.generated.resources.Res
import handylog.core.res.generated.resources.crown
import handylog.core.res.generated.resources.quick_record_hero
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun CardGroup(
	cards: List<Card>,
	slots: Int,
	onClick: () -> Unit,
	label: @Composable () -> Unit,
) {
	Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
		label()
		Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
			repeat(slots) { index ->
				PlayingCard(
					card = cards.getOrNull(index),
					size = CardSize.SM,
					onClick = onClick,
				)
			}
		}
	}
}

@Composable
internal fun HeroLabel(position: String) {
	Row(
		verticalAlignment = Alignment.CenterVertically,
		horizontalArrangement = Arrangement.spacedBy(4.dp),
	) {
		Icon(
			painter = painterResource(Res.drawable.crown),
			contentDescription = null,
			tint = HandyTheme.colorScheme.gold,
			modifier = Modifier.size(12.dp),
		)
		Text(
			text = stringResource(Res.string.quick_record_hero, position),
			style = HandyTheme.typography.medium12,
			color = HandyTheme.colorScheme.gold,
		)
	}
}

@Composable
internal fun FinishLabel(text: String) {
	Text(
		text = text,
		style = HandyTheme.typography.medium12,
		color = HandyTheme.colorScheme.textSecondary,
	)
}

internal const val HOLE_CARD_SLOTS = 2

@ThemePreviews
@Composable
private fun HeroCardGroupPreview() {
	ThemePreview {
		Row(
			modifier = Modifier.padding(16.dp),
			horizontalArrangement = Arrangement.spacedBy(16.dp),
		) {
			CardGroup(
				cards = listOf(Card(Rank.ACE, Suit.SPADES), Card(Rank.KING, Suit.HEARTS)),
				slots = HOLE_CARD_SLOTS,
				onClick = {},
			) {
				HeroLabel(position = "BTN")
			}
			CardGroup(cards = emptyList(), slots = HOLE_CARD_SLOTS, onClick = {}) {
				FinishLabel(text = "UTG")
			}
		}
	}
}
