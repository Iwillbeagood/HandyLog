package com.hand.log.ui.poker

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import com.hand.log.designsystem.theme.HandyTheme
import org.jetbrains.compose.resources.painterResource
import com.hand.log.domain.model.Card
import com.hand.log.domain.model.Rank
import com.hand.log.domain.model.Suit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardSelectorSheet(
	title: String,
	maxCards: Int,
	selectedCards: Set<Card>,
	onCardsSelected: (List<Card>) -> Unit,
	onDismiss: () -> Unit,
	modifier: Modifier = Modifier,
) {
	val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
	val colors = HandyTheme.colorScheme
	val pickedCards = remember { mutableStateListOf<Card>() }
	val allUsedCards = selectedCards + pickedCards

	ModalBottomSheet(
		onDismissRequest = onDismiss,
		sheetState = sheetState,
		containerColor = colors.card,
		modifier = modifier,
	) {
		Column(
			modifier = Modifier
				.fillMaxWidth()
				.nestedScroll(SheetDragBlocker)
				.padding(horizontal = 16.dp, vertical = 8.dp),
		) {
			Row(
				modifier = Modifier
					.fillMaxWidth()
					.padding(bottom = 12.dp),
				horizontalArrangement = Arrangement.SpaceBetween,
				verticalAlignment = Alignment.CenterVertically,
			) {
				Text(
					text = title,
					style = HandyTheme.typography.bold18,
					color = colors.textPrimary,
				)
				Text(
					text = "${pickedCards.size} / $maxCards",
					style = HandyTheme.typography.medium14,
					color = if (pickedCards.size == maxCards) colors.primary else colors.textSecondary,
				)
			}

			// Selected cards preview
			if (pickedCards.isNotEmpty()) {
				Row(
					horizontalArrangement = Arrangement.spacedBy(8.dp),
					modifier = Modifier.padding(bottom = 12.dp),
				) {
					pickedCards.forEach { card ->
						PlayingCard(
							card = card,
							size = CardSize.MD,
						)
					}
				}
			}

			Suit.entries.forEach { suit ->
				SuitSection(
					suit = suit,
					usedCards = allUsedCards,
					onCardSelected = { card ->
						pickedCards.add(card)
						if (pickedCards.size >= maxCards) {
							onCardsSelected(pickedCards.toList())
						}
					},
				)
			}
		}
	}
}

@Composable
private fun SuitSection(
	suit: Suit,
	usedCards: Set<Card>,
	onCardSelected: (Card) -> Unit,
) {
	val colors = HandyTheme.colorScheme
	val suitColor = when (suit) {
		Suit.HEARTS, Suit.DIAMONDS -> colors.suitRed
		Suit.CLUBS, Suit.SPADES -> colors.suitBlack
	}

	Icon(
		painter = painterResource(suit.iconRes()),
		contentDescription = suit.symbol,
		tint = suitColor,
		modifier = Modifier
			.size(20.dp)
			.padding(vertical = 4.dp),
	)

	LazyVerticalGrid(
		columns = GridCells.Fixed(13),
		contentPadding = PaddingValues(bottom = 12.dp),
		horizontalArrangement = Arrangement.spacedBy(4.dp),
		verticalArrangement = Arrangement.spacedBy(4.dp),
		modifier = Modifier.fillMaxWidth(),
	) {
		items(Rank.entries) { rank ->
			val card = Card(rank, suit)
			val isUsed = card in usedCards

			if (isUsed) {
				PlayingCard(
					card = card,
					size = CardSize.SM,
					modifier = Modifier.alpha(0.3f),
				)
			} else {
				PlayingCard(
					card = card,
					size = CardSize.SM,
					onClick = { onCardSelected(card) },
				)
			}
		}
	}
}
