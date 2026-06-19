package com.hand.log.ui.poker

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import com.hand.log.designsystem.component.modal.ModalButtonRow
import com.hand.log.designsystem.component.modal.SheetDragBlocker
import com.hand.log.designsystem.etc.ThemePreview
import com.hand.log.designsystem.etc.ThemePreviews
import com.hand.log.designsystem.theme.HandyTheme
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import com.hand.log.domain.model.Card
import com.hand.log.domain.model.Rank
import com.hand.log.domain.model.Suit
import handylog.core.res.generated.resources.Res
import handylog.core.res.generated.resources.btn_complete

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardSelectorSheet(
	title: String,
	maxCards: Int,
	selectedCards: Set<Card>,
	onCardsSelected: (List<Card>) -> Unit,
	onDismiss: () -> Unit,
	modifier: Modifier = Modifier,
	onUnknownSelected: (() -> Unit)? = null,
	initialCards: List<Card> = emptyList(),
	minCards: Int = maxCards,
) {
	val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
	val colors = HandyTheme.colorScheme
	val pickedCards =
		remember(title, maxCards) { mutableStateListOf<Card>().apply { addAll(initialCards) } }
	val showConfirmButton = minCards < maxCards

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
					color = if (pickedCards.size >= minCards) colors.primary else colors.textSecondary,
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
							onClick = { pickedCards.remove(card) },
						)
					}
				}
			}

			// 카드 미공개 옵션
			if (onUnknownSelected != null) {
				Box(
					modifier = Modifier
						.fillMaxWidth()
						.padding(bottom = 12.dp)
						.clip(RoundedCornerShape(8.dp))
						.background(colors.muted)
						.clickable {
							onUnknownSelected()
							onDismiss()
						}
						.padding(12.dp),
					contentAlignment = Alignment.Center,
				) {
					Text(
						text = "? 카드 미공개",
						style = HandyTheme.typography.bold14,
						color = colors.textSecondary,
					)
				}
			}

			Suit.entries.forEach { suit ->
				SuitSection(
					suit = suit,
					disabledCards = selectedCards,
					pickedCards = pickedCards.toSet(),
					onCardSelected = { card ->
						if (pickedCards.size < maxCards) {
							pickedCards.add(card)
							if (pickedCards.size >= maxCards) {
								onCardsSelected(pickedCards.toList())
							}
						}
					},
					onCardDeselected = { card -> pickedCards.remove(card) },
				)
			}

			// 선택완료 버튼 (항상 하단에 표시, minCards 미만이면 disabled)
			if (showConfirmButton) {
				ModalButtonRow(
					confirmText = stringResource(Res.string.btn_complete),
					onConfirm = { onCardsSelected(pickedCards.toList()) },
					confirmEnabled = pickedCards.size >= minCards,
					modifier = Modifier.padding(top = 8.dp),
				)
			}
		}
	}
}

@Composable
fun BoardCardsPreview(
	pickedCards: List<Card>,
	onCardDeselected: ((Int) -> Unit)? = null,
	modifier: Modifier = Modifier,
) {
	val colors = HandyTheme.colorScheme
	Row(
		horizontalArrangement = Arrangement.spacedBy(4.dp),
		verticalAlignment = Alignment.CenterVertically,
		modifier = modifier,
	) {
		// Flop (3 slots)
		(0 until 3).forEach { index ->
			val card = pickedCards.getOrNull(index)
			PlayingCard(
				card = card,
				size = CardSize.MD,
				faceDown = card == null,
				onClick = if (card != null && onCardDeselected != null) {
					{ onCardDeselected(index) }
				} else {
					null
				},
			)
		}
		// Turn separator + slot
		Text(
			text = "|",
			style = HandyTheme.typography.regular14,
			color = colors.textSecondary,
			modifier = Modifier.padding(horizontal = 2.dp),
		)
		val turnCard = pickedCards.getOrNull(3)
		PlayingCard(
			card = turnCard,
			size = CardSize.MD,
			faceDown = turnCard == null,
			onClick = if (turnCard != null && onCardDeselected != null) {
				{ onCardDeselected(3) }
			} else {
				null
			},
		)
		// River separator + slot
		Text(
			text = "|",
			style = HandyTheme.typography.regular14,
			color = colors.textSecondary,
			modifier = Modifier.padding(horizontal = 2.dp),
		)
		val riverCard = pickedCards.getOrNull(4)
		PlayingCard(
			card = riverCard,
			size = CardSize.MD,
			faceDown = riverCard == null,
			onClick = if (riverCard != null && onCardDeselected != null) {
				{ onCardDeselected(4) }
			} else {
				null
			},
		)
	}
}

@Composable
internal fun SuitSection(
	suit: Suit,
	disabledCards: Set<Card>,
	pickedCards: Set<Card>,
	onCardSelected: (Card) -> Unit,
	onCardDeselected: (Card) -> Unit,
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
			val isPicked = card in pickedCards
			val isDisabled = card in disabledCards

			when {
				isPicked -> PlayingCard(
					card = card,
					size = CardSize.SM,
					selected = true,
					onClick = { onCardDeselected(card) },
				)
				isDisabled -> PlayingCard(
					card = card,
					size = CardSize.SM,
					modifier = Modifier.alpha(0.3f),
				)
				else -> PlayingCard(
					card = card,
					size = CardSize.SM,
					onClick = { onCardSelected(card) },
				)
			}
		}
	}
}

@ThemePreviews
@Composable
private fun SuitSectionPreview() {
	val disabledCards = setOf(
		Card(Rank.ACE, Suit.SPADES),
		Card(Rank.KING, Suit.SPADES),
	)
	val pickedCards = setOf(
		Card(Rank.QUEEN, Suit.SPADES),
		Card(Rank.JACK, Suit.SPADES),
	)

	ThemePreview {
		Column(
			modifier = Modifier
				.fillMaxWidth()
				.padding(16.dp),
		) {
			SuitSection(
				suit = Suit.SPADES,
				disabledCards = disabledCards,
				pickedCards = pickedCards,
				onCardSelected = {},
				onCardDeselected = {},
			)
		}
	}
}
