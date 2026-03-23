package com.hand.log.record.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hand.log.designsystem.component.HandySectionLabel
import com.hand.log.designsystem.component.HandyTextField
import com.hand.log.designsystem.component.VerticalSpacer
import com.hand.log.designsystem.theme.HandyTheme
import com.hand.log.domain.model.ActionType
import com.hand.log.domain.model.Blinds
import com.hand.log.domain.model.Card
import com.hand.log.domain.model.PokerTable
import com.hand.log.domain.model.Rank
import com.hand.log.domain.model.Street
import com.hand.log.domain.model.PreflopStreet
import com.hand.log.domain.model.FlopStreet
import com.hand.log.domain.model.PocketCards
import com.hand.log.domain.model.HandStreets
import com.hand.log.domain.model.Suit
import com.hand.log.domain.model.Action
import com.hand.log.domain.model.GameType
import handylog.core.res.generated.resources.Res
import handylog.core.res.generated.resources.undo
import org.jetbrains.compose.resources.painterResource
import com.hand.log.record.contract.RecordHandState
import com.hand.log.record.contract.RecordStep
import com.hand.log.ui.poker.CardSize
import com.hand.log.ui.poker.PlayingCard
import kotlinx.datetime.LocalDate
import com.hand.log.designsystem.etc.ThemePreview
import com.hand.log.designsystem.etc.ThemePreviews
import org.jetbrains.compose.resources.stringResource
import handylog.core.res.generated.resources.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun StreetStepContent(
	state: RecordHandState.Recording,
	onSelectBoardCard: (Street) -> Unit,
	onSelectActionSeat: (Int) -> Unit,
	onSelectActionType: (ActionType) -> Unit,
	onUpdateActionAmount: (String) -> Unit,
	onUpdatePlayerStack: (Int, String) -> Unit,
	onConfirmAction: () -> Unit,
	onRemoveLastAction: () -> Unit,
	preflopPresets: List<Double> = listOf(2.0, 2.5, 3.0, 4.0, 5.0),
	postflopPresets: List<Int> = listOf(33, 50, 75, 100),
) {
	val colors = HandyTheme.colorScheme
	val currentStreet = state.currentStreet
	val streetCards = state.streets.getCards(currentStreet)
	val streetActions = state.streets.getActions(currentStreet)

	Column {
		// Board Cards (not for PREFLOP)
		if (currentStreet != Street.PREFLOP) {
			HandySectionLabel(stringResource(Res.string.board_cards))
			val cardCount = when (currentStreet) {
				Street.FLOP -> 3
				Street.TURN -> 1
				Street.RIVER -> 1
				else -> 0
			}
			Row(
				horizontalArrangement = Arrangement.spacedBy(8.dp),
				verticalAlignment = Alignment.CenterVertically,
			) {
				if (currentStreet == Street.TURN || currentStreet == Street.RIVER) {
					val flopCards = state.streets.getCards(Street.FLOP)
					Row(
						horizontalArrangement = Arrangement.spacedBy(4.dp),
						modifier = Modifier.clickable { onSelectBoardCard(Street.FLOP) },
					) {
						flopCards.forEach { card ->
							PlayingCard(card = card, size = CardSize.MD)
						}
					}
				}
				if (currentStreet == Street.RIVER) {
					val turnCards = state.streets.getCards(Street.TURN)
					Row(
						modifier = Modifier.clickable { onSelectBoardCard(Street.TURN) },
					) {
						turnCards.forEach { card ->
							PlayingCard(card = card, size = CardSize.MD)
						}
					}
				}
				Row(
					horizontalArrangement = Arrangement.spacedBy(4.dp),
					modifier = Modifier.clickable { onSelectBoardCard(currentStreet) },
				) {
					(0 until cardCount).forEach { index ->
						BoardCardAnimated(
							card = streetCards.getOrNull(index),
							index = index,
						)
					}
				}
			}
			VerticalSpacer(16.dp)
		}

		// 프리플랍 오프너 선택 모드: 액션 없고 currentActionSeat이 null
		val isOpenerSelection = currentStreet == Street.PREFLOP &&
			streetActions.isEmpty() &&
			state.currentActionSeat == null

		// 프리플랍에서는 좌석 클릭으로 해당 플레이어까지 건너뛰기 가능
		val isPreflopSeatClickable = currentStreet == Street.PREFLOP

		// Action Table View + Pot
		ActionTableView(
			state = state,
			modifier = Modifier.fillMaxWidth(),
			onSeatClick = if (isPreflopSeatClickable) onSelectActionSeat else null,
		)

		if (isOpenerSelection) {
			VerticalSpacer(8.dp)
			Text(
				text = stringResource(Res.string.record_select_opener),
				style = HandyTheme.typography.medium14,
				color = colors.textSecondary,
				modifier = Modifier.fillMaxWidth(),
				textAlign = TextAlign.Center,
			)
		}

		// Undo button
		val actions = streetActions
		if (actions.isNotEmpty()) {
			VerticalSpacer(8.dp)
			Row(
				modifier = Modifier.fillMaxWidth(),
				horizontalArrangement = Arrangement.End,
			) {
				Row(
					modifier = Modifier
						.clip(RoundedCornerShape(6.dp))
						.background(colors.muted)
						.clickable(onClick = onRemoveLastAction)
						.padding(horizontal = 12.dp, vertical = 6.dp),
					verticalAlignment = Alignment.CenterVertically,
					horizontalArrangement = Arrangement.spacedBy(4.dp),
				) {
					Icon(
						painter = painterResource(Res.drawable.undo),
						contentDescription = null,
						modifier = Modifier.size(14.dp),
						tint = colors.textSecondary,
					)
					Text(
						text = stringResource(Res.string.btn_undo),
						style = HandyTheme.typography.medium12,
						color = colors.textSecondary,
					)
				}
			}
		}

		// Action Input Area (오프너 선택 모드에서는 숨김)
		if (!isOpenerSelection) {
			VerticalSpacer(16.dp)

			val boardCardsReady = state.streets.isBoardReady(currentStreet)

			AnimatedVisibility(visible = boardCardsReady) {
				PlayerActionArea(
					state = state,
					onSelectActionType = onSelectActionType,
					onUpdateActionAmount = onUpdateActionAmount,
					onUpdatePlayerStack = onUpdatePlayerStack,
					onConfirmAction = onConfirmAction,
					preflopPresets = preflopPresets,
					postflopPresets = postflopPresets,
				)
			}
		}

		VerticalSpacer(16.dp)
	}
}

@Composable
private fun PlayerActionArea(
	state: RecordHandState.Recording,
	onSelectActionType: (ActionType) -> Unit,
	onUpdateActionAmount: (String) -> Unit,
	onUpdatePlayerStack: (Int, String) -> Unit,
	onConfirmAction: () -> Unit,
	preflopPresets: List<Double>,
	postflopPresets: List<Int>,
) {
	val colors = HandyTheme.colorScheme

	Column {
		if (state.currentActionSeat != null) {
			val posName = state.positionName(state.currentActionSeat)
			val isHero = state.currentActionSeat == state.table?.heroSeat
			val currentStack = state.getPlayerStack(state.currentActionSeat)

			Row(
				modifier = Modifier.fillMaxWidth(),
				verticalAlignment = Alignment.CenterVertically,
				horizontalArrangement = Arrangement.spacedBy(8.dp),
			) {
				Box(
					modifier = Modifier
						.size(36.dp)
						.clip(CircleShape)
						.background(
							if (isHero) colors.gold.copy(alpha = 0.15f) else colors.primary.copy(alpha = 0.15f),
						),
					contentAlignment = Alignment.Center,
				) {
					Text(
						text = "${state.currentActionSeat}",
						style = HandyTheme.typography.bold14,
						color = if (isHero) colors.gold else colors.primary,
					)
				}
				Column(modifier = Modifier.weight(1f)) {
					Text(
						text = posName,
						style = HandyTheme.typography.bold16,
						color = if (isHero) colors.gold else colors.textPrimary,
					)
					if (isHero) {
						Text(
							text = "Hero",
							style = HandyTheme.typography.regular12,
							color = colors.gold,
						)
					}
				}
				if (state.currentStreet == Street.PREFLOP) {
					HandyTextField(
						value = if (currentStack == 0.0) "" else currentStack.toLong().toString(),
						onValueChange = { onUpdatePlayerStack(state.currentActionSeat, it) },
						label = stringResource(Res.string.player_stack),
						modifier = Modifier.weight(1f),
						keyboardType = KeyboardType.Number,
					)
				} else {
					Text(
						text = state.formatAmount(currentStack),
						style = HandyTheme.typography.bold14,
						color = colors.textSecondary,
					)
				}
			}

			VerticalSpacer(12.dp)
			ActionSelector(
				availableActions = state.availableActions,
				selectedAction = state.currentActionType,
				onSelectAction = onSelectActionType,
				currentStreet = state.currentStreet,
				currentAmount = state.currentActionAmount,
				onUpdateAmount = onUpdateActionAmount,
				onConfirmAction = onConfirmAction,
				bbAmount = state.blinds?.bb ?: 0.0,
				currentPot = state.currentPot,
				preflopPresets = preflopPresets,
				postflopPresets = postflopPresets,
				minRaiseAmount = state.minRaiseAmount,
				maxAmount = currentStack + (state.players[state.currentActionSeat]?.currentBet ?: 0.0),
				showAmountWarning = state.showAmountWarning,
			)
		} else {
			Box(
				modifier = Modifier
					.fillMaxWidth()
					.clip(RoundedCornerShape(8.dp))
					.background(colors.muted)
					.padding(16.dp),
				contentAlignment = Alignment.Center,
			) {
				Text(
					text = stringResource(Res.string.record_all_actions_complete),
					style = HandyTheme.typography.medium14,
					color = colors.textSecondary,
				)
			}
		}
	}
}

@Composable
private fun BoardCardAnimated(
	card: Card?,
	index: Int,
) {
	val isSet = card != null
	val rotation by animateFloatAsState(
		targetValue = if (isSet) 0f else 180f,
		animationSpec = tween(
			durationMillis = 400,
			delayMillis = index * 120,
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
private fun StreetStepContentPreflopPreview() {
	ThemePreview {
		StreetStepContent(
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
				currentStep = RecordStep.PREFLOP,
				buttonSeat = 1,
				blinds = Blinds(sb = 500.0, bb = 1000.0),
				heroHand = PocketCards(Card(Rank.ACE, Suit.SPADES), Card(Rank.KING, Suit.SPADES)),
				currentActionSeat = 4, // UTG
				streets = HandStreets(
					preflop = PreflopStreet(
						actions = listOf(
							Action(playerSeat = 4, type = ActionType.RAISE, amount = 2500.0),
							Action(playerSeat = 5, type = ActionType.FOLD),
						),
					),
				),
			),
			onSelectBoardCard = {},
			onSelectActionSeat = {},
			onSelectActionType = {},
			onUpdateActionAmount = {},
			onUpdatePlayerStack = { _, _ -> },
			onConfirmAction = {},
			onRemoveLastAction = {},
		)
	}
}

@ThemePreviews
@Composable
private fun StreetStepContentFlopPreview() {
	ThemePreview {
		StreetStepContent(
			state = RecordHandState.Recording(
				tableId = "test",
				table = PokerTable(
					id = "test",
					date = LocalDate(2026, 3, 14),
					gameType = GameType.CASH,
					startingStack = 50000.0,
					blinds = Blinds(sb = 500.0, bb = 1000.0),
					playerCount = 6,
					heroSeat = 3,
					createdAt = 0L,
				),
				currentStep = RecordStep.FLOP,
				buttonSeat = 1,
				blinds = Blinds(sb = 500.0, bb = 1000.0),
				heroHand = PocketCards(Card(Rank.ACE, Suit.SPADES), Card(Rank.KING, Suit.SPADES)),
				currentActionSeat = 2, // SB
				streets = HandStreets(
					preflop = PreflopStreet(),
					flop = FlopStreet(
						card1 = Card(Rank.ACE, Suit.HEARTS),
						card2 = Card(Rank.KING, Suit.DIAMONDS),
						card3 = Card(Rank.QUEEN, Suit.CLUBS),
					),
				),
			),
			onSelectBoardCard = {},
			onSelectActionSeat = {},
			onSelectActionType = {},
			onUpdateActionAmount = {},
			onUpdatePlayerStack = { _, _ -> },
			onConfirmAction = {},
			onRemoveLastAction = {},
		)
	}
}

@ThemePreviews
@Composable
private fun StreetStepContentCompletePreview() {
	ThemePreview {
		StreetStepContent(
			state = RecordHandState.Recording(
				tableId = "test",
				table = PokerTable(
					id = "test",
					date = LocalDate(2026, 3, 14),
					gameType = GameType.CASH,
					startingStack = 50000.0,
					blinds = Blinds(sb = 500.0, bb = 1000.0),
					playerCount = 6,
					heroSeat = 3,
					createdAt = 0L,
				),
				currentStep = RecordStep.PREFLOP,
				buttonSeat = 1,
				blinds = Blinds(sb = 500.0, bb = 1000.0),
				currentActionSeat = null, // 모든 액션 완료
				streets = HandStreets(
					preflop = PreflopStreet(
						actions = listOf(
							Action(playerSeat = 4, type = ActionType.RAISE, amount = 2500.0),
							Action(playerSeat = 5, type = ActionType.CALL, amount = 2500.0),
							Action(playerSeat = 6, type = ActionType.FOLD),
							Action(playerSeat = 1, type = ActionType.FOLD),
							Action(playerSeat = 2, type = ActionType.FOLD),
							Action(playerSeat = 3, type = ActionType.CALL, amount = 2500.0),
						),
					),
				),
			),
			onSelectBoardCard = {},
			onSelectActionSeat = {},
			onSelectActionType = {},
			onUpdateActionAmount = {},
			onUpdatePlayerStack = { _, _ -> },
			onConfirmAction = {},
			onRemoveLastAction = {},
		)
	}
}

@ThemePreviews
@Composable
private fun StreetStepContentOpenerSelectionPreview() {
	ThemePreview {
		StreetStepContent(
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
				currentStep = RecordStep.PREFLOP,
				buttonSeat = 1,
				blinds = Blinds(sb = 500.0, bb = 1000.0),
				heroHand = PocketCards(Card(Rank.ACE, Suit.SPADES), Card(Rank.KING, Suit.SPADES)),
				currentActionSeat = null,
				streets = HandStreets(
					preflop = PreflopStreet(),
				),
			),
			onSelectBoardCard = {},
			onSelectActionSeat = {},
			onSelectActionType = {},
			onUpdateActionAmount = {},
			onUpdatePlayerStack = { _, _ -> },
			onConfirmAction = {},
			onRemoveLastAction = {},
		)
	}
}
