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
import androidx.compose.material3.Icon
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
import com.hand.log.designsystem.theme.HandyTheme
import com.hand.log.domain.model.Card
import com.hand.log.domain.model.Rank
import com.hand.log.domain.model.Suit
import handylog.core.res.generated.resources.Res
import handylog.core.res.generated.resources.clover_card
import handylog.core.res.generated.resources.diamond_card
import handylog.core.res.generated.resources.heart_card
import handylog.core.res.generated.resources.spade_card
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import com.hand.log.designsystem.etc.ThemePreview
import com.hand.log.designsystem.etc.ThemePreviews

enum class CardSize(val width: Dp, val height: Dp, val fontSize: TextUnit, val suitIconSize: Dp, val spacing: Dp = 2.dp) {
	XXS(16.dp, 22.dp, 7.sp, 7.dp, 0.dp),
	XS(24.dp, 32.dp, 9.sp, 10.dp, 0.dp),
	SM(32.dp, 44.dp, 12.sp, 10.dp),
	MD(40.dp, 56.dp, 14.sp, 12.dp),
	LG(56.dp, 80.dp, 18.sp, 16.dp),
}

fun Suit.iconRes(): DrawableResource = when (this) {
	Suit.SPADES -> Res.drawable.spade_card
	Suit.HEARTS -> Res.drawable.heart_card
	Suit.DIAMONDS -> Res.drawable.diamond_card
	Suit.CLUBS -> Res.drawable.clover_card
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
			.background(colors.card)
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
		Suit.CLUBS, Suit.SPADES -> colors.suitBlack
	}

	Column(
		modifier = Modifier
			.size(size.width, size.height)
			.background(colors.card),
		horizontalAlignment = Alignment.CenterHorizontally,
		verticalArrangement = Arrangement.spacedBy(size.spacing, Alignment.CenterVertically),
	) {
		Text(
			text = card.rank.symbol,
			color = suitColor,
			fontSize = size.fontSize,
			fontWeight = FontWeight.Bold,
			lineHeight = size.fontSize,
		)
		Icon(
			painter = painterResource(card.suit.iconRes()),
			contentDescription = card.suit.symbol,
			tint = suitColor,
			modifier = Modifier.size(size.suitIconSize),
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

@ThemePreviews
@Composable
private fun PlayingCardFaceUpPreview() {
	ThemePreview {
		Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
			PlayingCard(card = Card(Rank.ACE, Suit.SPADES), size = CardSize.LG)
			PlayingCard(card = Card(Rank.KING, Suit.HEARTS), size = CardSize.LG)
			PlayingCard(card = Card(Rank.QUEEN, Suit.DIAMONDS), size = CardSize.LG)
			PlayingCard(card = Card(Rank.JACK, Suit.CLUBS), size = CardSize.LG)
		}
	}
}

@ThemePreviews
@Composable
private fun PlayingCardSizesPreview() {
	ThemePreview {
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

@ThemePreviews
@Composable
private fun PlayingCardStatesPreview() {
	ThemePreview {
		Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
			PlayingCard(card = Card(Rank.TEN, Suit.HEARTS), size = CardSize.LG)
			PlayingCard(card = Card(Rank.TEN, Suit.HEARTS), size = CardSize.LG, selected = true)
			PlayingCard(card = null, size = CardSize.LG, faceDown = true)
			PlayingCard(card = null, size = CardSize.LG)
		}
	}
}
