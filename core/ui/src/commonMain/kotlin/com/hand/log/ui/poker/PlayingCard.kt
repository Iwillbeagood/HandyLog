package com.hand.log.ui.poker

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hand.log.designsystem.theme.HandLogTheme
import com.hand.log.designsystem.theme.HandyTheme
import com.hand.log.domain.model.Card
import com.hand.log.domain.model.Rank
import com.hand.log.domain.model.Suit
import org.jetbrains.compose.ui.tooling.preview.Preview

enum class CardSize(val width: Dp, val height: Dp, val fontSize: TextUnit, val suitSize: TextUnit) {
	XS(24.dp, 32.dp, 10.sp, 8.sp),
	SM(32.dp, 44.dp, 12.sp, 10.sp),
	MD(40.dp, 56.dp, 14.sp, 12.sp),
	LG(56.dp, 80.dp, 18.sp, 16.sp),
}

@Composable
fun PlayingCard(
	card: Card?,
	modifier: Modifier = Modifier,
	size: CardSize = CardSize.MD,
	faceDown: Boolean = false,
	selected: Boolean = false,
	onClick: (() -> Unit)? = null,
) {
	val colors = HandyTheme.colorScheme
	val shape = RoundedCornerShape(4.dp)

	Box(
		modifier = modifier
			.size(size.width, size.height)
			.clip(shape)
			.then(
				if (selected) {
					Modifier.border(2.dp, colors.primary, shape)
				} else {
					Modifier.border(1.dp, colors.border, shape)
				},
			)
			.then(
				if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier,
			),
		contentAlignment = Alignment.Center,
	) {
		when {
			faceDown -> CardBack(size)
			card != null -> CardFace(card, size)
			else -> CardEmpty(size)
		}
	}
}

@Composable
private fun CardFace(card: Card, size: CardSize) {
	val colors = HandyTheme.colorScheme
	val suitColor = when (card.suit) {
		Suit.HEARTS, Suit.DIAMONDS -> colors.suitRed
		Suit.CLUBS, Suit.SPADES -> colors.textPrimary
	}

	Column(
		modifier = Modifier
			.size(size.width, size.height)
			.background(colors.card),
		horizontalAlignment = Alignment.CenterHorizontally,
		verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically),
	) {
		Text(
			text = card.rank.symbol,
			color = suitColor,
			fontSize = size.fontSize,
			fontWeight = FontWeight.Bold,
			lineHeight = size.fontSize,
		)
		Text(
			text = card.suit.symbol,
			color = suitColor,
			fontSize = size.suitSize,
			lineHeight = size.suitSize,
		)
	}
}

@Composable
private fun CardBack(size: CardSize) {
	Box(
		modifier = Modifier
			.size(size.width, size.height)
			.background(
				Brush.linearGradient(
					colors = listOf(
						Color(0xFF1E3A5F),
						Color(0xFF2C5282),
					),
				),
			),
		contentAlignment = Alignment.Center,
	) {
		Box(
			modifier = Modifier
				.size(size.width - 6.dp, size.height - 6.dp)
				.border(1.dp, Color(0xFF4A90D9), RoundedCornerShape(2.dp)),
		)
	}
}

@Composable
private fun CardEmpty(size: CardSize) {
	val colors = HandyTheme.colorScheme
	Box(
		modifier = Modifier
			.size(size.width, size.height)
			.background(colors.muted),
		contentAlignment = Alignment.Center,
	) {
		Text(
			text = "?",
			color = colors.textSecondary,
			fontSize = size.fontSize,
		)
	}
}

@Preview
@Composable
private fun PlayingCardFaceUpPreview() {
	HandLogTheme {
		Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
			PlayingCard(card = Card(Rank.ACE, Suit.SPADES), size = CardSize.LG)
			PlayingCard(card = Card(Rank.KING, Suit.HEARTS), size = CardSize.LG)
			PlayingCard(card = Card(Rank.QUEEN, Suit.DIAMONDS), size = CardSize.LG)
			PlayingCard(card = Card(Rank.JACK, Suit.CLUBS), size = CardSize.LG)
		}
	}
}

@Preview
@Composable
private fun PlayingCardSizesPreview() {
	HandLogTheme {
		Row(
			horizontalArrangement = Arrangement.spacedBy(8.dp),
			verticalAlignment = Alignment.Bottom,
		) {
			PlayingCard(card = Card(Rank.ACE, Suit.SPADES), size = CardSize.XS)
			PlayingCard(card = Card(Rank.ACE, Suit.SPADES), size = CardSize.SM)
			PlayingCard(card = Card(Rank.ACE, Suit.SPADES), size = CardSize.MD)
			PlayingCard(card = Card(Rank.ACE, Suit.SPADES), size = CardSize.LG)
		}
	}
}

@Preview
@Composable
private fun PlayingCardStatesPreview() {
	HandLogTheme {
		Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
			PlayingCard(card = Card(Rank.TEN, Suit.HEARTS), size = CardSize.LG)
			PlayingCard(card = Card(Rank.TEN, Suit.HEARTS), size = CardSize.LG, selected = true)
			PlayingCard(card = null, size = CardSize.LG, faceDown = true)
			PlayingCard(card = null, size = CardSize.LG)
		}
	}
}
