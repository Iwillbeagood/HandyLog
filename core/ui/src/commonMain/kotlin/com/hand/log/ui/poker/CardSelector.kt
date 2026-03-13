package com.hand.log.ui.poker

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hand.log.designsystem.theme.HandyTheme
import com.hand.log.domain.model.Card
import com.hand.log.domain.model.Rank
import com.hand.log.domain.model.Suit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardSelectorSheet(
	title: String,
	selectedCards: Set<Card>,
	onCardSelected: (Card) -> Unit,
	onDismiss: () -> Unit,
	modifier: Modifier = Modifier,
) {
	val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
	val colors = HandyTheme.colorScheme

	ModalBottomSheet(
		onDismissRequest = onDismiss,
		sheetState = sheetState,
		containerColor = colors.modalBackground,
		modifier = modifier,
	) {
		Column(
			modifier = Modifier
				.fillMaxWidth()
				.padding(horizontal = 16.dp, vertical = 8.dp),
		) {
			Text(
				text = title,
				style = HandyTheme.typography.bold18,
				color = colors.textPrimary,
				modifier = Modifier.padding(bottom = 16.dp),
			)

			Suit.entries.forEach { suit ->
				SuitSection(
					suit = suit,
					selectedCards = selectedCards,
					onCardSelected = onCardSelected,
				)
			}
		}
	}
}

@Composable
private fun SuitSection(
	suit: Suit,
	selectedCards: Set<Card>,
	onCardSelected: (Card) -> Unit,
) {
	val colors = HandyTheme.colorScheme
	val suitColor = when (suit) {
		Suit.HEARTS, Suit.DIAMONDS -> colors.suitRed
		Suit.CLUBS, Suit.SPADES -> colors.textPrimary
	}

	Text(
		text = suit.symbol,
		color = suitColor,
		fontSize = 18.sp,
		fontWeight = FontWeight.Bold,
		modifier = Modifier.padding(vertical = 4.dp),
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
			val isSelected = card in selectedCards

			PlayingCard(
				card = card,
				size = CardSize.SM,
				selected = isSelected,
				onClick = if (!isSelected) {
					{ onCardSelected(card) }
				} else {
					null
				},
			)
		}
	}
}
