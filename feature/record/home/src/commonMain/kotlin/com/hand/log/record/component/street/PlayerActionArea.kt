package com.hand.log.record.component.street

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.hand.log.designsystem.component.HandySectionLabel
import com.hand.log.designsystem.component.VerticalSpacer
import com.hand.log.designsystem.etc.ThemePreview
import com.hand.log.designsystem.etc.ThemePreviews
import com.hand.log.designsystem.theme.HandyTheme
import com.hand.log.domain.model.Action
import com.hand.log.domain.model.ActionType
import com.hand.log.domain.model.Blinds
import com.hand.log.domain.model.Card
import com.hand.log.domain.model.GameType
import com.hand.log.domain.model.HandStreets
import com.hand.log.domain.model.Player
import com.hand.log.domain.model.PocketCards
import com.hand.log.domain.model.PokerTable
import com.hand.log.domain.model.PreflopStreet
import com.hand.log.domain.model.Rank
import com.hand.log.domain.model.Street
import com.hand.log.domain.model.Suit
import com.hand.log.record.component.ActionSelector
import com.hand.log.record.contract.RecordHandState
import com.hand.log.record.contract.RecordStep
import com.hand.log.record.model.RecordPlayers
import com.hand.log.record.model.calculateLastRaiseTo
import handylog.core.res.generated.resources.Res
import handylog.core.res.generated.resources.record_action
import handylog.core.res.generated.resources.record_all_actions_complete
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun PlayerActionArea(
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
			val initialStack = state.players[state.currentActionSeat]?.initialStack
			val blindCost = state.getBlindCost(state.currentActionSeat)

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
				if (state.currentStreet != Street.PREFLOP && currentStack != null) {
					Text(
						text = state.formatAmount(currentStack),
						style = HandyTheme.typography.bold14,
						color = colors.textSecondary,
					)
				}
			}

			if (state.currentStreet == Street.PREFLOP) {
				VerticalSpacer(8.dp)
				PlayerStackInput(
					initialStack = initialStack,
					blindCost = blindCost,
					bbAmount = state.blinds?.bb ?: 0.0,
					posName = posName,
					onValueChange = { onUpdatePlayerStack(state.currentActionSeat, it) },
				)
			}

			VerticalSpacer(12.dp)
			HandySectionLabel(stringResource(Res.string.record_action))
			val streetActions = state.streets.getActions(state.currentStreet)
			val lastBet = calculateLastRaiseTo(
				streetActions = streetActions,
				currentStreet = state.currentStreet,
				bb = state.blinds?.bb ?: 0.0,
			)

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
				lastBetAmount = lastBet,
				useBbUnit = state.useBbUnit,
				playerStack = currentStack,
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

@ThemePreviews
@Composable
private fun PlayerActionAreaPreview() {
	ThemePreview {
		PlayerActionArea(
			state = RecordHandState.Recording(
				tableId = "test",
				table = PokerTable(
					id = "test",
					date = LocalDate(2026, 3, 14),
					gameType = GameType.Cash(sb = 500.0, bb = 1000.0),
					heroSeat = 3,
					maxPlayers = 6,
					players = (1..6).map { Player(seat = it) },
					createdAt = 0L,
				),
				currentStep = RecordStep.PREFLOP,
				buttonSeat = 1,
				blinds = Blinds(sb = 500.0, bb = 1000.0),
				players = RecordPlayers.create(playerCount = 6, defaultStack = 50000.0)
					.update(
						3,
					) { copy(cards = PocketCards(Card(Rank.ACE, Suit.SPADES), Card(Rank.KING, Suit.SPADES))) },
				currentActionSeat = 4,
				streets = HandStreets(
					preflop = PreflopStreet(
						actions = listOf(
							Action(playerSeat = 4, type = ActionType.RAISE, amount = 2500.0),
						),
					),
				),
			),
			onSelectActionType = {},
			onUpdateActionAmount = {},
			onUpdatePlayerStack = { _, _ -> },
			onConfirmAction = {},
			preflopPresets = listOf(2.0, 2.5, 3.0, 4.0, 5.0),
			postflopPresets = listOf(33, 50, 75, 100),
		)
	}
}

@ThemePreviews
@Composable
private fun PlayerActionAreaCompletePreview() {
	ThemePreview {
		PlayerActionArea(
			state = RecordHandState.Recording(
				tableId = "test",
				table = PokerTable(
					id = "test",
					date = LocalDate(2026, 3, 14),
					gameType = GameType.Cash(sb = 500.0, bb = 1000.0),
					heroSeat = 3,
					maxPlayers = 6,
					players = (1..6).map { Player(seat = it) },
					createdAt = 0L,
				),
				currentStep = RecordStep.PREFLOP,
				buttonSeat = 1,
				blinds = Blinds(sb = 500.0, bb = 1000.0),
				currentActionSeat = null,
				streets = HandStreets(preflop = PreflopStreet()),
			),
			onSelectActionType = {},
			onUpdateActionAmount = {},
			onUpdatePlayerStack = { _, _ -> },
			onConfirmAction = {},
			preflopPresets = listOf(2.0, 2.5, 3.0, 4.0, 5.0),
			postflopPresets = listOf(33, 50, 75, 100),
		)
	}
}
