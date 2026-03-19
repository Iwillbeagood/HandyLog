package com.hand.log.record.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.hand.log.designsystem.component.HandySectionLabel
import com.hand.log.designsystem.component.HandySelector
import com.hand.log.designsystem.component.HandyTextField
import com.hand.log.designsystem.component.VerticalSpacer
import com.hand.log.designsystem.theme.HandyTheme
import com.hand.log.domain.model.Blinds
import com.hand.log.domain.model.Card
import com.hand.log.domain.model.GameType
import com.hand.log.domain.model.HeroHand
import com.hand.log.domain.model.PokerTable
import com.hand.log.domain.model.Rank
import com.hand.log.domain.model.Suit
import com.hand.log.record.contract.RecordHandState
import com.hand.log.record.model.RecordPlayers
import com.hand.log.ui.poker.CardSize
import com.hand.log.ui.poker.PlayingCard
import kotlinx.datetime.LocalDate
import com.hand.log.designsystem.etc.ThemePreview
import com.hand.log.designsystem.etc.ThemePreviews
@Composable
internal fun SetupStepContent(
	state: RecordHandState.Recording,
	onSelectHeroCard: () -> Unit,
	onUpdateHeroStack: (String) -> Unit,
	onUpdateButtonSeat: (Int) -> Unit,
	onUpdateBlinds: (String, String) -> Unit,
	onShowTableEdit: () -> Unit = {},
) {
	val colors = HandyTheme.colorScheme

	Column {
		// 테이블 설정 버튼
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.clip(RoundedCornerShape(8.dp))
				.background(colors.muted)
				.clickable(onClick = onShowTableEdit)
				.padding(horizontal = 12.dp, vertical = 10.dp),
			verticalAlignment = Alignment.CenterVertically,
			horizontalArrangement = Arrangement.SpaceBetween,
		) {
			Column {
				Text(
					text = state.table?.location ?: "테이블",
					style = HandyTheme.typography.bold14,
					color = colors.textPrimary,
				)
				state.table?.let {
					Text(
						text = "${it.gameType.label} · ${it.playerCount}인 · 스택 ${it.startingStack.toLong()}",
						style = HandyTheme.typography.regular12,
						color = colors.textSecondary,
					)
				}
			}
			Text(
				text = "수정",
				style = HandyTheme.typography.bold12,
				color = colors.primary,
			)
		}

		VerticalSpacer(16.dp)
		HandySectionLabel("히어로 카드")
		Row(
			horizontalArrangement = Arrangement.spacedBy(8.dp),
			verticalAlignment = Alignment.CenterVertically,
			modifier = Modifier.clickable { onSelectHeroCard() },
		) {
			HeroCardAnimated(
				card = state.heroCards.getOrNull(0),
				index = 0,
			)
			HeroCardAnimated(
				card = state.heroCards.getOrNull(1),
				index = 1,
			)
		}

		VerticalSpacer(16.dp)
		HandyTextField(
			value = if (state.heroStack == 0.0) "" else state.heroStack.toLong().toString(),
			onValueChange = onUpdateHeroStack,
			label = "히어로 스택",
			keyboardType = KeyboardType.Number,
		)

		VerticalSpacer(16.dp)
		HandySectionLabel("내 포지션")
		HandySelector(
			options = state.allPositionNames,
			selected = state.heroPositionName,
			onSelect = { position ->
				onUpdateButtonSeat(state.buttonSeatForHeroPosition(position))
			},
			selectedColor = colors.gold,
			selectedContentColor = colors.card,
		)

		VerticalSpacer(16.dp)
		HandySectionLabel("버튼 좌석")
		HandySelector(
			range = 1..(state.table?.playerCount ?: 9),
			selected = state.buttonSeat,
			onSelect = onUpdateButtonSeat,
		)

		if (state.table?.gameType == GameType.TOURNAMENT) {
			VerticalSpacer(16.dp)
			HandySectionLabel("블라인드")
			Row(
				modifier = Modifier.fillMaxWidth(),
				horizontalArrangement = Arrangement.spacedBy(8.dp),
			) {
				HandyTextField(
					value = state.bbText,
					onValueChange = { newBb ->
						val newSb = (newBb.toLongOrNull() ?: 0L) / 2
						onUpdateBlinds(newSb.toString(), newBb)
					},
					label = "BB",
					modifier = Modifier.weight(1f),
					keyboardType = KeyboardType.Number,
				)
				HandyTextField(
					value = state.sbText,
					onValueChange = { newSb ->
						onUpdateBlinds(newSb, state.bbText)
					},
					label = "SB",
					modifier = Modifier.weight(1f),
					keyboardType = KeyboardType.Number,
				)
			}
		}
	}
}

@Composable
private fun HeroCardAnimated(
	card: Card?,
	index: Int,
) {
	val isSet = card != null
	val rotation by animateFloatAsState(
		targetValue = if (isSet) 0f else 180f,
		animationSpec = tween(
			durationMillis = 400,
			delayMillis = index * 100,
		),
	)

	Box(
		modifier = Modifier.graphicsLayer {
			rotationY = rotation
			cameraDistance = 12f * density
		},
	) {
		if (rotation <= 90f) {
			PlayingCard(card = card, size = CardSize.LG)
		} else {
			PlayingCard(card = null, size = CardSize.LG, faceDown = true)
		}
	}
}

@ThemePreviews
@Composable
private fun SetupStepContentEmptyPreview() {
	ThemePreview {
		SetupStepContent(
			state = RecordHandState.Recording(
				tableId = "test",
				table = PokerTable(
					id = "test",
					date = LocalDate(2026, 3, 14),
					gameType = GameType.CASH,
					startingStack = 50000.0,
					blinds = Blinds(sb = 500.0, bb = 1000.0),
					playerCount = 9,
					heroSeat = 3,
					createdAt = 0L,
				),
				players = RecordPlayers.create(playerCount = 9, defaultStack = 50000.0),
				blinds = Blinds(sb = 500.0, bb = 1000.0),
			),
			onSelectHeroCard = {},
			onUpdateHeroStack = {},
			onUpdateButtonSeat = {},
			onUpdateBlinds = { _, _ -> },
		)
	}
}

@ThemePreviews
@Composable
private fun SetupStepContentFilledPreview() {
	ThemePreview {
		SetupStepContent(
			state = RecordHandState.Recording(
				tableId = "test",
				table = PokerTable(
					id = "test",
					date = LocalDate(2026, 3, 14),
					gameType = GameType.CASH,
					startingStack = 50000.0,
					blinds = Blinds(sb = 500.0, bb = 1000.0),
					playerCount = 9,
					heroSeat = 3,
					createdAt = 0L,
				),
				heroHand = HeroHand(Card(Rank.ACE, Suit.SPADES), Card(Rank.KING, Suit.HEARTS)),
				players = RecordPlayers.create(playerCount = 9, defaultStack = 50000.0),
				buttonSeat = 3,
				blinds = Blinds(sb = 500.0, bb = 1000.0),
			),
			onSelectHeroCard = {},
			onUpdateHeroStack = {},
			onUpdateButtonSeat = {},
			onUpdateBlinds = { _, _ -> },
		)
	}
}

@ThemePreviews
@Composable
private fun SetupStepContentTournamentPreview() {
	ThemePreview {
		SetupStepContent(
			state = RecordHandState.Recording(
				tableId = "test",
				table = PokerTable(
					id = "test",
					date = LocalDate(2026, 3, 14),
					gameType = GameType.TOURNAMENT,
					startingStack = 10000.0,
					blinds = Blinds(sb = 50.0, bb = 100.0, isBigBlindAnte = true),
					playerCount = 9,
					heroSeat = 5,
					createdAt = 0L,
				),
				heroHand = HeroHand(Card(Rank.QUEEN, Suit.HEARTS), Card(Rank.JACK, Suit.HEARTS)),
				players = RecordPlayers.create(playerCount = 9, defaultStack = 10000.0),
				buttonSeat = 1,
				blinds = Blinds(sb = 50.0, bb = 100.0, isBigBlindAnte = true),
			),
			onSelectHeroCard = {},
			onUpdateHeroStack = {},
			onUpdateButtonSeat = {},
			onUpdateBlinds = { _, _ -> },
		)
	}
}
