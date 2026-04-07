package com.hand.log.record.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.hand.log.designsystem.component.VerticalSpacer
import com.hand.log.designsystem.theme.HandyTheme
import com.hand.log.designsystem.theme.nonScaledSp
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
import com.hand.log.designsystem.etc.ThemePreview
import com.hand.log.designsystem.etc.ThemePreviews
import com.hand.log.record.contract.RecordHandState
import com.hand.log.record.contract.RecordStep
import com.hand.log.ui.localizedLabel
import com.hand.log.ui.poker.indicatorColor
import handylog.core.res.generated.resources.Res
import handylog.core.res.generated.resources.pencil
import handylog.core.res.generated.resources.play
import handylog.core.res.generated.resources.record_edit_action_log
import handylog.core.res.generated.resources.record_resume_recording
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * 수정 모드 전용 StreetStep 화면.
 * 기록된 액션을 카드 리스트로 표시하고, 아이템 클릭 시 바텀시트(ActionSelector)를 띄워 수정.
 */
@Composable
internal fun StreetStepEditContent(
	state: RecordHandState.Recording,
	editingActionIndex: Int? = null,
	onSelectBoardCard: (Street) -> Unit,
	onEditAction: (Int) -> Unit,
	onResumeRecording: () -> Unit = {},
) {
	val colors = HandyTheme.colorScheme
	val currentStreet = state.currentStreet
	val streetActions = state.streets.getActions(currentStreet)

	Column {
		BoardCardsSection(
			currentStreet = currentStreet,
			streets = state.streets,
			onSelectBoardCard = onSelectBoardCard,
		)

		// 기록된 액션 리스트
		if (streetActions.isNotEmpty()) {
			VerticalSpacer(12.dp)
			Row(
				modifier = Modifier.fillMaxWidth(),
				horizontalArrangement = Arrangement.SpaceBetween,
				verticalAlignment = Alignment.CenterVertically,
			) {
				Text(
					text = stringResource(Res.string.record_edit_action_log),
					style = HandyTheme.typography.bold14,
					color = colors.textPrimary,
				)
				Text(
					text = "${streetActions.size}개",
					style = HandyTheme.typography.regular12,
					color = colors.textSecondary,
				)
			}
			VerticalSpacer(8.dp)

			Column(
				modifier = Modifier.fillMaxWidth(),
				verticalArrangement = Arrangement.spacedBy(8.dp),
			) {
				streetActions.forEachIndexed { index, action ->
					ActionItemCard(
						index = index,
						action = action,
						state = state,
						isFocused = index == editingActionIndex,
						onClick = { onEditAction(index) },
					)
				}
			}
		}

		// 플레이(기록 재개) 버튼
		VerticalSpacer(12.dp)
		ResumeRecordingButton(onClick = onResumeRecording)

		VerticalSpacer(16.dp)
	}
}

@Composable
private fun ResumeRecordingButton(
	onClick: () -> Unit,
) {
	val colors = HandyTheme.colorScheme

	Row(
		modifier = Modifier
			.fillMaxWidth()
			.clip(RoundedCornerShape(8.dp))
			.background(colors.primary)
			.clickable(onClick = onClick)
			.padding(horizontal = 14.dp, vertical = 12.dp),
		verticalAlignment = Alignment.CenterVertically,
		horizontalArrangement = Arrangement.Center,
	) {
		Icon(
			painter = painterResource(Res.drawable.play),
			contentDescription = null,
			modifier = Modifier.size(16.dp),
			tint = colors.onPrimary,
		)
		Text(
			text = stringResource(Res.string.record_resume_recording),
			style = HandyTheme.typography.bold14,
			color = colors.onPrimary,
			modifier = Modifier.padding(start = 8.dp),
		)
	}
}

@Composable
private fun ActionItemCard(
	index: Int,
	action: Action,
	state: RecordHandState.Recording,
	isFocused: Boolean = false,
	onClick: () -> Unit,
) {
	val colors = HandyTheme.colorScheme
	val posName = state.positionName(action.playerSeat)
	val isHero = action.playerSeat == state.table?.heroSeat
	val actionLabel = action.localizedLabel()
	val amountText = action.amount?.let { " ${state.formatAmount(it)}" } ?: ""
	val indicatorColor = action.type.indicatorColor()

	val bgColor = if (isFocused) colors.primary.copy(alpha = 0.08f) else colors.card
	val borderColor = if (isFocused) colors.primary else colors.border
	val borderWidth = if (isFocused) 1.5.dp else 1.dp

	Row(
		modifier = Modifier
			.fillMaxWidth()
			.clip(RoundedCornerShape(8.dp))
			.background(bgColor)
			.border(borderWidth, borderColor, RoundedCornerShape(8.dp))
			.clickable(onClick = onClick)
			.padding(horizontal = 14.dp, vertical = 12.dp),
		verticalAlignment = Alignment.CenterVertically,
		horizontalArrangement = Arrangement.spacedBy(10.dp),
	) {
		// 번호 원
		Box(
			modifier = Modifier
				.size(24.dp)
				.clip(RoundedCornerShape(12.dp))
				.background(if (isFocused) colors.primary else colors.muted),
			contentAlignment = Alignment.Center,
		) {
			Text(
				text = "${index + 1}",
				style = HandyTheme.typography.bold12.nonScaledSp,
				color = if (isFocused) colors.onPrimary else colors.textSecondary,
			)
		}

		// 포지션 + 액션
		Column(
			modifier = Modifier.weight(1f),
			verticalArrangement = Arrangement.spacedBy(2.dp),
		) {
			Text(
				text = posName,
				style = HandyTheme.typography.bold14,
				color = when {
					isFocused -> colors.primary
					isHero -> colors.gold
					else -> colors.textPrimary
				},
			)
			Text(
				text = "$actionLabel$amountText",
				style = HandyTheme.typography.medium12,
				color = if (isFocused) colors.primary else indicatorColor,
			)
		}

		// 연필 아이콘
		Icon(
			painter = painterResource(Res.drawable.pencil),
			contentDescription = null,
			modifier = Modifier.size(16.dp),
			tint = if (isFocused) colors.primary else colors.textSecondary,
		)
	}
}

@ThemePreviews
@Composable
private fun StreetStepEditPreflopPreview() {
	ThemePreview {
		StreetStepEditContent(
			state = RecordHandState.Recording(
				tableId = "test",
				table = PokerTable(
					id = "test",
					date = LocalDate(2026, 3, 14),
					gameType = GameType.Cash(sb = 500.0, bb = 1000.0),
					heroSeat = 3,
					players = (1..6).map { Player(seat = it) },
					createdAt = 0L,
				),
				currentStep = RecordStep.PREFLOP,
				buttonSeat = 1,
				blinds = Blinds(sb = 500.0, bb = 1000.0),
				heroHand = PocketCards(Card(Rank.ACE, Suit.SPADES), Card(Rank.KING, Suit.SPADES)),
				currentActionSeat = 4,
				isEditing = true,
				streets = HandStreets(
					preflop = PreflopStreet(
						actions = listOf(
							Action(playerSeat = 4, type = ActionType.RAISE, amount = 2500.0, betLevel = 2),
							Action(playerSeat = 5, type = ActionType.FOLD),
							Action(playerSeat = 6, type = ActionType.CALL, amount = 2500.0),
							Action(playerSeat = 1, type = ActionType.FOLD),
							Action(playerSeat = 2, type = ActionType.FOLD),
							Action(playerSeat = 3, type = ActionType.CALL, amount = 2500.0),
						),
					),
				),
			),
			onSelectBoardCard = {},
			onEditAction = {},
		)
	}
}

@ThemePreviews
@Composable
private fun StreetStepEditFlopPreview() {
	ThemePreview {
		StreetStepEditContent(
			state = RecordHandState.Recording(
				tableId = "test",
				table = PokerTable(
					id = "test",
					date = LocalDate(2026, 3, 14),
					gameType = GameType.Cash(sb = 500.0, bb = 1000.0),
					heroSeat = 3,
					players = (1..6).map { Player(seat = it) },
					createdAt = 0L,
				),
				currentStep = RecordStep.FLOP,
				buttonSeat = 1,
				blinds = Blinds(sb = 500.0, bb = 1000.0),
				heroHand = PocketCards(Card(Rank.ACE, Suit.SPADES), Card(Rank.KING, Suit.SPADES)),
				currentActionSeat = null,
				isEditing = true,
				streets = HandStreets(
					preflop = PreflopStreet(
						actions = listOf(
							Action(playerSeat = 4, type = ActionType.RAISE, amount = 2500.0, betLevel = 2),
							Action(playerSeat = 5, type = ActionType.FOLD),
							Action(playerSeat = 3, type = ActionType.CALL, amount = 2500.0),
						),
					),
					flop = FlopStreet(
						card1 = Card(Rank.ACE, Suit.HEARTS),
						card2 = Card(Rank.KING, Suit.DIAMONDS),
						card3 = Card(Rank.QUEEN, Suit.CLUBS),
						actions = listOf(
							Action(playerSeat = 3, type = ActionType.CHECK),
							Action(playerSeat = 4, type = ActionType.BET, amount = 3000.0, betLevel = 1),
							Action(playerSeat = 3, type = ActionType.CALL, amount = 3000.0),
						),
					),
				),
			),
			onSelectBoardCard = {},
			onEditAction = {},
		)
	}
}
