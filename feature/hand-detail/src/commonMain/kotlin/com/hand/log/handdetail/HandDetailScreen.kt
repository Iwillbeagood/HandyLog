package com.hand.log.handdetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import com.hand.log.designsystem.component.HandySwitch
import com.hand.log.designsystem.component.RegularButton
import com.hand.log.handdetail.model.HandHistoryFormatter
import com.hand.log.handdetail.model.formatWithComma
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.hand.log.designsystem.component.BaseScaffold
import com.hand.log.designsystem.component.HandyTopAppbar
import com.hand.log.designsystem.component.VerticalSpacer
import com.hand.log.designsystem.etc.ThemePreview
import com.hand.log.designsystem.etc.ThemePreviews
import com.hand.log.designsystem.theme.HandyTheme
import com.hand.log.domain.model.Action
import com.hand.log.domain.model.ActionType
import com.hand.log.domain.model.Blinds
import com.hand.log.domain.model.Card
import com.hand.log.domain.model.FlopStreet
import com.hand.log.domain.model.HandRecord
import com.hand.log.domain.model.HandStreets
import com.hand.log.domain.model.HeroHand
import com.hand.log.domain.model.PreflopStreet
import com.hand.log.domain.model.Rank
import com.hand.log.domain.model.RiverStreet
import com.hand.log.domain.model.Suit
import com.hand.log.domain.model.TurnStreet
import com.hand.log.handdetail.model.ActionRowUiModel
import com.hand.log.handdetail.model.HandDetailUiModel
import com.hand.log.handdetail.model.StreetSectionUiModel
import com.hand.log.ui.poker.CardSize
import com.hand.log.ui.poker.PlayingCard
import com.hand.log.ui.poker.indicatorColor

@Composable
internal fun HandDetailScreen(
	hand: HandRecord,
	onBack: () -> Unit,
) {
	var useBbUnit by remember { mutableStateOf(false) }
	val uiModel = remember(hand, useBbUnit) { HandDetailUiModel.from(hand, useBbUnit) }
	val colors = HandyTheme.colorScheme

	BaseScaffold(
		containerColor = colors.background,
		topBar = {
			HandyTopAppbar(
				title = "핸드 상세",
				onBackEvent = onBack,
				subContent = {
					HandySwitch(
						checked = useBbUnit,
						text = "BB",
						onCheckedChange = { useBbUnit = it },
					)
				},
				endContent = {

				},
			)
		},
	) {
		LazyColumn(
			modifier = Modifier
				.fillMaxSize()
				.padding(horizontal = 16.dp),
			verticalArrangement = Arrangement.spacedBy(12.dp),
		) {
			item {
				VerticalSpacer(8.dp)
				HandSummarySection(hand)
			}

			items(uiModel.streetSections) { section ->
				StreetSectionView(section)
			}

			item {
				ResultSection(hand)
				VerticalSpacer(16.dp)
				val clipboardManager = LocalClipboardManager.current
				RegularButton(
					text = "텍스트로 공유",
					onClick = {
						val text = HandHistoryFormatter.format(hand)
						clipboardManager.setText(AnnotatedString(text))
					},
				)
				VerticalSpacer(32.dp)
			}
		}
	}
}

@Composable
private fun HandSummarySection(hand: HandRecord) {
	val colors = HandyTheme.colorScheme

	Column(
		modifier = Modifier
			.fillMaxWidth()
			.clip(RoundedCornerShape(12.dp))
			.background(colors.card)
			.padding(16.dp),
	) {
		Row(
			verticalAlignment = Alignment.CenterVertically,
			horizontalArrangement = Arrangement.spacedBy(12.dp),
		) {
			Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
				hand.heroCards.forEach { card ->
					PlayingCard(card = card, size = CardSize.LG)
				}
				if (hand.heroCards.isEmpty()) {
					PlayingCard(card = null, size = CardSize.LG, faceDown = true)
					PlayingCard(card = null, size = CardSize.LG, faceDown = true)
				}
			}

			Column {
				hand.blinds?.let {
					Text(
						text = "SB ${it.sb.toLong()} / BB ${it.bb.toLong()}",
						style = HandyTheme.typography.regular12,
						color = colors.textSecondary,
					)
				}
				Text(
					text = "BTN: Seat ${hand.buttonSeat}",
					style = HandyTheme.typography.regular12,
					color = colors.textSecondary,
				)
				Text(
					text = "스택: ${formatWithComma(hand.heroStack.toLong())}",
					style = HandyTheme.typography.regular12,
					color = colors.textSecondary,
				)
			}
		}
	}
}

@Composable
private fun StreetSectionView(section: StreetSectionUiModel) {
	val colors = HandyTheme.colorScheme

	Column(
		modifier = Modifier
			.fillMaxWidth()
			.clip(RoundedCornerShape(12.dp))
			.background(colors.card)
			.padding(16.dp),
	) {
		Row(
			modifier = Modifier.fillMaxWidth(),
			verticalAlignment = Alignment.CenterVertically,
			horizontalArrangement = Arrangement.SpaceBetween,
		) {
			Text(
				text = section.label,
				style = HandyTheme.typography.bold16,
				color = colors.textPrimary,
			)
			Text(
				text = "POT: ${section.pot}",
				style = HandyTheme.typography.bold14,
				color = colors.gold,
			)
		}

		if (section.boardCards.isNotEmpty() || section.previousCards.isNotEmpty()) {
			VerticalSpacer(8.dp)
			Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
				section.previousCards.forEach { card ->
					PlayingCard(card = card, size = CardSize.SM)
				}
				section.boardCards.forEach { card ->
					PlayingCard(card = card, size = CardSize.MD)
				}
			}
		}

		if (section.actionRows.isNotEmpty()) {
			VerticalSpacer(12.dp)
			Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
				section.actionRows.forEach { row ->
					ActionRowView(row)
				}
				if (section.foldCount > 0) {
					Text(
						text = "외 ${section.foldCount}명 폴드",
						style = HandyTheme.typography.regular12,
						color = colors.textSecondary.copy(alpha = 0.6f),
					)
				}
			}
		}
	}
}

@Composable
private fun ActionRowView(row: ActionRowUiModel) {
	val colors = HandyTheme.colorScheme
	val actionColor = row.actionType.indicatorColor()

	Row(
		modifier = Modifier.fillMaxWidth(),
		verticalAlignment = Alignment.CenterVertically,
		horizontalArrangement = Arrangement.spacedBy(8.dp),
	) {
		// 포지션 인디케이터: 히어로만 gold 배경
		Box(
			modifier = Modifier
				.size(36.dp)
				.clip(CircleShape)
				.background(
					if (row.isHero) {
						colors.gold.copy(alpha = 0.15f)
					} else {
						actionColor.copy(alpha = 0.15f)
					},
				),
			contentAlignment = Alignment.Center,
		) {
			Text(
				text = row.positionName,
				style = HandyTheme.typography.bold10,
				color = if (row.isHero) colors.gold else actionColor,
				maxLines = 1,
			)
		}

		// 액션 라벨: 항상 액션 타입의 고유 색상 사용
		Column {
			Text(
				text = row.actionLabel,
				style = HandyTheme.typography.bold14,
				color = actionColor,
			)
			row.stackBefore?.let { stack ->
				Text(
					text = "스택 $stack",
					style = HandyTheme.typography.regular10,
					color = colors.textSecondary,
				)
			}
		}

		Spacer(modifier = Modifier.weight(1f))

		row.amount?.let { amount ->
			Text(
				text = "$amount",
				style = HandyTheme.typography.bold14,
				color = colors.gold,
			)
		}
	}
}

@Composable
private fun ResultSection(hand: HandRecord) {
	val colors = HandyTheme.colorScheme

	if (hand.result != null || hand.memo != null) {
		Column(
			modifier = Modifier
				.fillMaxWidth()
				.clip(RoundedCornerShape(12.dp))
				.background(colors.card)
				.padding(16.dp),
		) {
			Text(
				text = "결과",
				style = HandyTheme.typography.bold16,
				color = colors.textPrimary,
			)

			hand.result?.let { result ->
				VerticalSpacer(8.dp)
				val isPositive = result >= 0
				Text(
					text = if (isPositive) {
						"+${formatWithComma(
							result.toLong(),
						)}"
					} else {
						formatWithComma(result.toLong())
					},
					style = HandyTheme.typography.bold20,
					color = if (isPositive) colors.primary else colors.error,
				)
			}

			hand.memo?.let { memo ->
				VerticalSpacer(8.dp)
				Text(
					text = memo,
					style = HandyTheme.typography.regular14,
					color = colors.textSecondary,
				)
			}
		}
	}
}

@ThemePreviews
@Composable
private fun HandDetailScreenPreview() {
	ThemePreview {
		HandDetailScreen(
			hand = HandRecord(
				id = "h1",
				tableId = "t1",
				createdAt = 1710000000000L,
				blinds = Blinds(sb = 500.0, bb = 1000.0),
				heroHand = HeroHand(Card(Rank.ACE, Suit.SPADES), Card(Rank.KING, Suit.SPADES)),
				heroSeat = 3,
				heroStack = 50000.0,
				buttonSeat = 1,
				streets = HandStreets(
					preflop = PreflopStreet(
						actions = listOf(
							Action(playerSeat = 4, type = ActionType.RAISE, amount = 2500.0, stackBefore = 50000.0),
							Action(playerSeat = 5, type = ActionType.FOLD, stackBefore = 50000.0),
							Action(playerSeat = 6, type = ActionType.CALL, amount = 2500.0, stackBefore = 50000.0),
							Action(playerSeat = 1, type = ActionType.FOLD, stackBefore = 50000.0),
							Action(playerSeat = 2, type = ActionType.FOLD, stackBefore = 49500.0),
							Action(playerSeat = 3, type = ActionType.CALL, amount = 2500.0, stackBefore = 49000.0),
						),
					),
					flop = FlopStreet(
						card1 = Card(Rank.ACE, Suit.HEARTS),
						card2 = Card(Rank.TEN, Suit.DIAMONDS),
						card3 = Card(Rank.SEVEN, Suit.CLUBS),
						actions = listOf(
							Action(playerSeat = 3, type = ActionType.CHECK, stackBefore = 46500.0),
							Action(playerSeat = 4, type = ActionType.BET, amount = 5000.0, stackBefore = 47500.0),
							Action(playerSeat = 6, type = ActionType.FOLD, stackBefore = 47500.0),
							Action(playerSeat = 3, type = ActionType.CALL, amount = 5000.0, stackBefore = 46500.0),
						),
					),
					turn = TurnStreet(
						card = Card(Rank.KING, Suit.HEARTS),
						actions = listOf(
							Action(playerSeat = 3, type = ActionType.CHECK, stackBefore = 41500.0),
							Action(playerSeat = 4, type = ActionType.BET, amount = 12000.0, stackBefore = 42500.0),
							Action(playerSeat = 3, type = ActionType.RAISE, amount = 30000.0, stackBefore = 41500.0),
							Action(playerSeat = 4, type = ActionType.ALL_IN, amount = 30500.0, stackBefore = 30500.0),
							Action(playerSeat = 3, type = ActionType.CALL, amount = 30500.0, stackBefore = 11500.0),
						),
					),
					river = RiverStreet(
						card = Card(Rank.TWO, Suit.CLUBS),
					),
				),
				result = 49000.0,
				memo = "탑투페어로 체크레이즈 → 올인 콜, 상대 ATo",
			),
			onBack = {},
		)
	}
}
