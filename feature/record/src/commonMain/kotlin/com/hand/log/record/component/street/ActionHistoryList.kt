package com.hand.log.record.component.street

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hand.log.designsystem.etc.ThemePreview
import com.hand.log.designsystem.etc.ThemePreviews
import com.hand.log.designsystem.theme.HandyTheme
import com.hand.log.domain.model.Action
import com.hand.log.domain.model.ActionType
import com.hand.log.domain.model.Blinds
import com.hand.log.domain.model.Card
import com.hand.log.domain.model.FlopStreet
import com.hand.log.domain.model.GameType
import com.hand.log.domain.model.HandStreets
import com.hand.log.domain.model.Player
import com.hand.log.domain.model.PocketCards
import com.hand.log.domain.model.PokerTable
import com.hand.log.domain.model.PreflopStreet
import com.hand.log.domain.model.Rank
import com.hand.log.domain.model.Street
import com.hand.log.domain.model.Suit
import com.hand.log.record.contract.RecordHandState
import com.hand.log.record.contract.RecordStep
import com.hand.log.record.model.RecordPlayers
import com.hand.log.ui.poker.indicatorColor
import com.hand.log.ui.stringRes
import handylog.core.res.generated.resources.Res
import handylog.core.res.generated.resources.action_n_bet
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.stringResource

/**
 * 프리플랍부터 현재 스트릿까지 입력한 액션을 스트릿별로 묶어 순서대로 보여주는 기록 리스트.
 * 테이블 그래픽(공간 뷰)과 달리 액션 진행 순서를 한눈에 확인하는 용도.
 */
@Composable
internal fun ActionHistoryList(
	state: RecordHandState.Recording,
	modifier: Modifier = Modifier,
) {
	val colors = HandyTheme.colorScheme
	val streetsWithActions = Street.entries.mapNotNull { street ->
		val actions = state.streets.getActions(street).filter { it.type != ActionType.FOLD }
		if (actions.isEmpty()) null else street to actions
	}
	if (streetsWithActions.isEmpty()) return

	Column(
		modifier = modifier
			.fillMaxWidth()
			.clip(RoundedCornerShape(12.dp))
			.background(colors.card)
			.border(1.dp, colors.border, RoundedCornerShape(12.dp))
			.padding(horizontal = 12.dp, vertical = 10.dp),
		verticalArrangement = Arrangement.spacedBy(10.dp),
	) {
		streetsWithActions.forEach { (street, actions) ->
			Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
				Text(
					text = stringResource(street.stringRes()),
					style = HandyTheme.typography.bold10,
					color = colors.textSecondary,
					modifier = Modifier.padding(bottom = 2.dp),
				)
				actions.forEach { action ->
					ActionHistoryRow(
						state = state,
						action = action,
						isHero = action.playerSeat == state.table?.heroSeat,
					)
				}
			}
		}
	}
}

@Composable
private fun ActionHistoryRow(
	state: RecordHandState.Recording,
	action: Action,
	isHero: Boolean,
) {
	val colors = HandyTheme.colorScheme
	val amount = action.amount ?: 0.0
	val hasAmount = amount > 0 &&
		action.type != ActionType.FOLD &&
		action.type != ActionType.CHECK

	Row(
		modifier = Modifier.fillMaxWidth(),
		verticalAlignment = Alignment.CenterVertically,
	) {
		Text(
			text = state.positionName(action.playerSeat),
			style = HandyTheme.typography.bold12,
			color = if (isHero) colors.gold else colors.textPrimary,
			modifier = Modifier.width(44.dp),
		)
		Text(
			text = if (action.betLevel >= 3) {
				stringResource(Res.string.action_n_bet, action.betLevel)
			} else {
				stringResource(action.type.stringRes())
			},
			style = HandyTheme.typography.medium12,
			color = action.type.indicatorColor(),
			modifier = Modifier
				.weight(1f)
				.padding(start = 8.dp),
		)
		if (hasAmount) {
			Text(
				text = state.formatAmount(amount),
				style = HandyTheme.typography.bold12,
				color = colors.textPrimary,
				textAlign = TextAlign.End,
			)
		}
	}
}

@ThemePreviews
@Composable
private fun ActionHistoryListPreview() {
	ThemePreview {
		ActionHistoryList(
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
				currentStep = RecordStep.FLOP,
				buttonSeat = 1,
				blinds = Blinds(sb = 500.0, bb = 1000.0),
				players = RecordPlayers.create(playerCount = 6, defaultStack = 50000.0)
					.update(3) {
						copy(cards = PocketCards(Card(Rank.ACE, Suit.SPADES), Card(Rank.KING, Suit.SPADES)))
					},
				streets = HandStreets(
					preflop = PreflopStreet(
						actions = listOf(
							Action(playerSeat = 6, type = ActionType.RAISE, amount = 2500.0, betLevel = 2),
							Action(playerSeat = 3, type = ActionType.CALL, amount = 2500.0),
							Action(playerSeat = 4, type = ActionType.FOLD),
						),
					),
					flop = FlopStreet(
						card1 = Card(Rank.ACE, Suit.HEARTS),
						card2 = Card(Rank.KING, Suit.DIAMONDS),
						card3 = Card(Rank.QUEEN, Suit.CLUBS),
						actions = listOf(
							Action(playerSeat = 3, type = ActionType.BET, amount = 6000.0),
							Action(playerSeat = 6, type = ActionType.CALL, amount = 6000.0),
						),
					),
				),
			),
		)
	}
}
