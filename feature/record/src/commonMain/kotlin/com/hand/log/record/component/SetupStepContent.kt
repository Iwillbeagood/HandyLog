package com.hand.log.record.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
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
import com.hand.log.domain.model.PocketCards
import com.hand.log.domain.model.PokerTable
import com.hand.log.domain.model.Rank
import com.hand.log.domain.model.Suit
import com.hand.log.record.contract.RecordHandState
import com.hand.log.record.model.RecordPlayers
import com.hand.log.ui.poker.BoardCardsPreview
import com.hand.log.ui.poker.CardSize
import com.hand.log.ui.poker.PlayingCard
import kotlinx.datetime.LocalDate
import com.hand.log.designsystem.etc.ThemePreview
import com.hand.log.designsystem.etc.ThemePreviews
import org.jetbrains.compose.resources.stringResource
import handylog.core.res.generated.resources.Res
import handylog.core.res.generated.resources.*
@Composable
internal fun SetupStepContent(
	state: RecordHandState.Recording,
	onSelectHeroCard: () -> Unit,
	onSelectBoardCards: () -> Unit,
	onUpdateHeroStack: (String) -> Unit,
	onUpdateButtonSeat: (Int) -> Unit,
	onUpdateBlinds: (String, String) -> Unit,
	heroStackFocusRequester: FocusRequester = FocusRequester(),
) {
	val colors = HandyTheme.colorScheme

	Column {
		HandySectionLabel(stringResource(Res.string.record_hero_card))
		Row(
			horizontalArrangement = Arrangement.spacedBy(8.dp),
			verticalAlignment = Alignment.CenterVertically,
			modifier = Modifier.clickable { onSelectHeroCard() },
		) {
			HeroCardAnimated(
				card = state.heroHand?.card1,
				index = 0,
			)
			HeroCardAnimated(
				card = state.heroHand?.card2,
				index = 1,
			)
		}

		VerticalSpacer(16.dp)
		HandySectionLabel(stringResource(Res.string.board_cards))
		BoardCardsPreview(
			pickedCards = state.streets.boardCards,
			modifier = Modifier.clickable { onSelectBoardCards() },
		)

		VerticalSpacer(16.dp)
		HandyTextField(
			value = if (state.heroInitialStack == 0.0) "" else state.heroInitialStack.toLong().toString(),
			onValueChange = onUpdateHeroStack,
			label = stringResource(Res.string.record_hero_stack),
			keyboardType = KeyboardType.Number,
			modifier = Modifier.focusRequester(heroStackFocusRequester),
		)

		VerticalSpacer(16.dp)
		HandySectionLabel(stringResource(Res.string.record_hero_position))
		HandySelector(
			options = state.allPositionNames,
			selected = state.heroPositionName,
			onSelect = { position ->
				onUpdateButtonSeat(state.buttonSeatForHeroPosition(position))
			},
			selectedColor = colors.gold,
			selectedContentColor = colors.card,
		)

		if (state.table?.gameType is GameType.Tournament) {
			VerticalSpacer(16.dp)
			HandySectionLabel(stringResource(Res.string.table_form_blinds))
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
					gameType = GameType.Cash(sb = 500.0, bb = 1000.0),
					heroSeat = 3,
					createdAt = 0L,
				),
				players = RecordPlayers.create(playerCount = 9, defaultStack = 50000.0),
				blinds = Blinds(sb = 500.0, bb = 1000.0),
			),
			onSelectHeroCard = {},
			onSelectBoardCards = {},
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
					gameType = GameType.Cash(sb = 500.0, bb = 1000.0),
					heroSeat = 3,
					createdAt = 0L,
				),
				players = RecordPlayers.create(playerCount = 9, defaultStack = 50000.0)
					.update(
						3,
					) { copy(cards = PocketCards(Card(Rank.ACE, Suit.SPADES), Card(Rank.KING, Suit.HEARTS))) },
				buttonSeat = 3,
				blinds = Blinds(sb = 500.0, bb = 1000.0),
			),
			onSelectHeroCard = {},
			onSelectBoardCards = {},
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
					gameType = GameType.Tournament(isBigBlindAnte = true),
					heroSeat = 5,
					createdAt = 0L,
				),
				players = RecordPlayers.create(playerCount = 9, defaultStack = 10000.0)
					.update(5) {
						copy(cards = PocketCards(Card(Rank.QUEEN, Suit.HEARTS), Card(Rank.JACK, Suit.HEARTS)))
					},
				buttonSeat = 1,
				blinds = Blinds(sb = 50.0, bb = 100.0, isBigBlindAnte = true),
			),
			onSelectHeroCard = {},
			onSelectBoardCards = {},
			onUpdateHeroStack = {},
			onUpdateButtonSeat = {},
			onUpdateBlinds = { _, _ -> },
		)
	}
}
