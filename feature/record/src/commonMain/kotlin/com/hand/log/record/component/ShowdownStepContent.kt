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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import com.hand.log.designsystem.component.HandySectionLabel
import com.hand.log.designsystem.component.HandyTextField
import com.hand.log.designsystem.component.VerticalSpacer
import com.hand.log.designsystem.etc.ThemePreview
import com.hand.log.designsystem.etc.ThemePreviews
import com.hand.log.designsystem.theme.HandyTheme
import com.hand.log.domain.model.Blinds
import com.hand.log.domain.model.Card
import com.hand.log.domain.model.GameType
import com.hand.log.domain.model.HeroHand
import com.hand.log.domain.model.PokerTable
import com.hand.log.domain.model.Rank
import com.hand.log.domain.model.Street
import com.hand.log.domain.model.Suit
import com.hand.log.record.contract.RecordHandState
import com.hand.log.record.contract.RecordStep
import com.hand.log.domain.model.FlopStreet
import com.hand.log.domain.model.PreflopStreet
import com.hand.log.domain.model.RiverStreet
import com.hand.log.domain.model.ShowdownResult
import com.hand.log.domain.model.TurnStreet
import com.hand.log.record.model.RecordPlayer
import com.hand.log.record.model.RecordPlayers
import com.hand.log.record.model.RecordShowdown
import com.hand.log.domain.model.HandStreets
import com.hand.log.record.model.PlayerStatus
import com.hand.log.ui.poker.CardSize
import com.hand.log.ui.poker.PlayingCard
import kotlinx.datetime.LocalDate

@Composable
internal fun ShowdownStepContent(
	state: RecordHandState.Recording,
	onSelectSingleBoardCard: (Street, Int) -> Unit,
	onSelectShowdownCard: (Int) -> Unit,
	onUpdateMemo: (String) -> Unit,
) {
	val boardCards = state.streets.boardCards
	val results = state.showdownResults
	val hasResults = results.isNotEmpty()

	Column {
		if (boardCards.isNotEmpty()) {
			HandySectionLabel("보드 (탭하여 수정)")
			VerticalSpacer(4.dp)
			Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
				// 플랍
				state.streets.flop?.cards?.forEachIndexed { index, card ->
					PlayingCard(
						card = card,
						size = CardSize.MD,
						onClick = { onSelectSingleBoardCard(Street.FLOP, index) },
					)
				}
				// 턴
				state.streets.turn?.cards?.forEachIndexed { index, card ->
					PlayingCard(
						card = card,
						size = CardSize.MD,
						onClick = { onSelectSingleBoardCard(Street.TURN, index) },
					)
				}
				// 리버
				state.streets.river?.cards?.forEachIndexed { index, card ->
					PlayingCard(
						card = card,
						size = CardSize.MD,
						onClick = { onSelectSingleBoardCard(Street.RIVER, index) },
					)
				}
			}
			VerticalSpacer(16.dp)
		}

		HandySectionLabel("플레이어 핸드")
		VerticalSpacer(8.dp)

		// 히어로가 폴드했더라도 쇼다운에 표시
		val heroSeat = state.table?.heroSeat
		val heroFolded = heroSeat != null && heroSeat !in state.remainingSeats.toSet()
		if (heroFolded) {
			ShowdownPlayerCard(
				state = state,
				seat = heroSeat,
				result = null,
				hasResults = hasResults,
				isFolded = true,
				onSelectShowdownCard = onSelectShowdownCard,
			)
			VerticalSpacer(8.dp)
		}

		state.remainingSeats.forEach { seat ->
			ShowdownPlayerCard(
				state = state,
				seat = seat,
				result = results.find { it.seat == seat },
				hasResults = hasResults,
				onSelectShowdownCard = onSelectShowdownCard,
			)
			VerticalSpacer(8.dp)
		}

		// 결과 (자동 계산)
		if (hasResults) {
			val heroResult = state.heroResult
			val isPositive = heroResult >= 0
			val resultText = if (isPositive) "+${heroResult.toLong()}" else "${heroResult.toLong()}"
			val colors = HandyTheme.colorScheme

			VerticalSpacer(8.dp)
			HandySectionLabel("결과")
			Text(
				text = resultText,
				style = HandyTheme.typography.bold20,
				color = if (isPositive) colors.primary else colors.error,
			)
		}

		VerticalSpacer(12.dp)
		HandyTextField(
			value = state.memo,
			onValueChange = onUpdateMemo,
			label = "메모",
		)

		VerticalSpacer(16.dp)
	}
}

@Composable
private fun ShowdownPlayerCard(
	state: RecordHandState.Recording,
	seat: Int,
	result: ShowdownResult?,
	hasResults: Boolean,
	isFolded: Boolean = false,
	onSelectShowdownCard: (Int) -> Unit,
) {
	val colors = HandyTheme.colorScheme
	val posName = state.positionName(seat)
	val isHero = seat == state.table?.heroSeat
	val hand = if (isHero) state.heroHand else state.showdown[seat]
	val player = state.players[seat]
	val isWinner = result?.isWinner == true
	val currentStack = state.getPlayerStack(seat)
	val initialStack = player?.initialStack ?: 0.0
	val stackChange = currentStack - initialStack
	val isEliminated = result != null && currentStack <= 0.0

	Box {
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.clip(RoundedCornerShape(8.dp))
				.then(
					if (isWinner) {
						Modifier.border(2.dp, colors.gold, RoundedCornerShape(8.dp))
					} else {
						Modifier
					},
				)
				.background(
					when {
						isFolded -> colors.muted.copy(alpha = 0.5f)
						isWinner -> colors.gold.copy(alpha = 0.15f)
						isEliminated -> colors.error.copy(alpha = 0.05f)
						isHero -> colors.gold.copy(alpha = 0.05f)
						else -> colors.muted
					},
				)
				.clickable(enabled = !isHero && !isFolded) { onSelectShowdownCard(seat) }
				.padding(12.dp),
			verticalAlignment = Alignment.CenterVertically,
			horizontalArrangement = Arrangement.spacedBy(8.dp),
		) {
			// 좌석 아이콘
			Box(
				modifier = Modifier
					.size(32.dp)
					.clip(CircleShape)
					.background(
						if (isHero) {
							colors.gold.copy(alpha = 0.15f)
						} else {
							colors.primary.copy(alpha = 0.15f)
						},
					),
				contentAlignment = Alignment.Center,
			) {
				Text(
					text = "$seat",
					style = HandyTheme.typography.bold12,
					color = if (isHero) colors.gold else colors.primary,
				)
			}

			// 포지션 + 족보 + 스택
			Column(modifier = Modifier.weight(1f)) {
				Row(
					verticalAlignment = Alignment.CenterVertically,
					horizontalArrangement = Arrangement.spacedBy(6.dp),
				) {
					Text(
						text = posName,
						style = HandyTheme.typography.bold14,
						color = if (isFolded) colors.textSecondary.copy(alpha = 0.5f)
						else if (isHero) colors.gold
						else colors.textPrimary,
					)
					if (isWinner) {
						Text(
							text = "WIN",
							style = HandyTheme.typography.bold10,
							color = colors.gold,
							modifier = Modifier
								.clip(RoundedCornerShape(4.dp))
								.background(colors.gold.copy(alpha = 0.2f))
								.padding(horizontal = 6.dp, vertical = 2.dp),
						)
					}
					if (isFolded) {
						Text(
							text = "FOLD",
							style = HandyTheme.typography.bold10,
							color = colors.textSecondary,
							modifier = Modifier
								.clip(RoundedCornerShape(4.dp))
								.background(colors.muted)
								.padding(horizontal = 6.dp, vertical = 2.dp),
						)
					}
				}

				if (result != null) {
					Text(
						text = result.ranking.label,
						style = HandyTheme.typography.regular12,
						color = if (isWinner) colors.gold else colors.textSecondary,
					)
				} else if (isHero) {
					Text(
						text = "Hero",
						style = HandyTheme.typography.regular10,
						color = if (isFolded) colors.textSecondary.copy(alpha = 0.5f) else colors.gold,
					)
				}

				Row(
					horizontalArrangement = Arrangement.spacedBy(8.dp),
					verticalAlignment = Alignment.CenterVertically,
				) {
					Text(
						text = "스택: ${currentStack.toLong()}",
						style = HandyTheme.typography.regular10,
						color = colors.textSecondary,
					)
					if (hasResults && stackChange != 0.0) {
						val isPositive = stackChange > 0
						Text(
							text = if (isPositive) "+${stackChange.toLong()}" else "${stackChange.toLong()}",
							style = HandyTheme.typography.bold10,
							color = if (isPositive) colors.primary else colors.error,
						)
					}
				}
			}

			// 카드 2장
			val isUnknown = !isHero && state.showdown.isUnknown(seat)
			Row(
				horizontalArrangement = Arrangement.spacedBy(4.dp),
				verticalAlignment = Alignment.CenterVertically,
			) {
				if (isUnknown) {
					PlayingCard(card = null, size = CardSize.SM)
					PlayingCard(card = null, size = CardSize.SM)
				} else if (hand != null) {
					PlayingCard(card = hand.card1, size = CardSize.SM)
					PlayingCard(card = hand.card2, size = CardSize.SM)
				} else {
					PlayingCard(card = null, size = CardSize.SM, faceDown = true)
					PlayingCard(card = null, size = CardSize.SM, faceDown = true)
				}
			}
		}

		if (isEliminated) {
			Box(
				modifier = Modifier
					.matchParentSize()
					.clip(RoundedCornerShape(8.dp)),
				contentAlignment = Alignment.Center,
			) {
				Text(
					text = "ELIMINATED",
					style = HandyTheme.typography.bold18,
					color = colors.error.copy(alpha = 0.4f),
					modifier = Modifier.rotate(-15f),
				)
			}
		}
	}
}

@ThemePreviews
@Composable
private fun ShowdownStepContentPreview() {
	ThemePreview {
		ShowdownStepContent(
			state = RecordHandState.Recording(
				tableId = "test",
				table = PokerTable(
					id = "test",
					date = LocalDate(2026, 3, 17),
					gameType = GameType.CASH,
					startingStack = 50000.0,
					blinds = Blinds(sb = 500.0, bb = 1000.0),
					playerCount = 6,
					heroSeat = 3,
					createdAt = 0L,
				),
				heroHand = HeroHand(Card(Rank.ACE, Suit.SPADES), Card(Rank.KING, Suit.SPADES)),
				players = RecordPlayers.create(playerCount = 6, defaultStack = 50000.0),
				currentStep = RecordStep.SHOWDOWN,
				showdown = RecordShowdown(
					seat1 = HeroHand(Card(Rank.QUEEN, Suit.HEARTS), Card(Rank.JACK, Suit.HEARTS)),
				),
			),
			onSelectSingleBoardCard = { _, _ -> },
			onSelectShowdownCard = {},
			onUpdateMemo = {},
		)
	}
}

@ThemePreviews
@Composable
private fun ShowdownStepContentResultPreview() {
	ThemePreview {
		// 6인 테이블, Hero=3(BB) AKs vs Seat1(BTN) QJhh
		// 보드: A♥ T♦ 7♣ K♥ 2♣
		// Hero: 탑투페어 (AK), Seat1: 노페어 (QJ)
		// Seat 4,5,6 폴드, Seat2 폴드
		ShowdownStepContent(
			state = RecordHandState.Recording(
				tableId = "test",
				table = PokerTable(
					id = "test",
					date = LocalDate(2026, 3, 17),
					gameType = GameType.CASH,
					startingStack = 50000.0,
					blinds = Blinds(sb = 500.0, bb = 1000.0),
					playerCount = 6,
					heroSeat = 3,
					createdAt = 0L,
				),
				heroHand = HeroHand(Card(Rank.ACE, Suit.SPADES), Card(Rank.KING, Suit.SPADES)),
				players = RecordPlayers(
					player1 = RecordPlayer(
						seat = 1,
						stack = 0.0,
						initialStack = 50000.0,
						status = PlayerStatus.ALL_IN,

					),
					player2 = RecordPlayer(
						seat = 2,
						stack = 49500.0,
						initialStack = 50000.0,
						status = PlayerStatus.FOLDED,

					),
					player3 = RecordPlayer(
						seat = 3,
						stack = 0.0,
						initialStack = 50000.0,
						status = PlayerStatus.ALL_IN,

					),
					player4 = RecordPlayer(
						seat = 4,
						stack = 50000.0,
						initialStack = 50000.0,
						status = PlayerStatus.FOLDED,
					),
					player5 = RecordPlayer(
						seat = 5,
						stack = 50000.0,
						initialStack = 50000.0,
						status = PlayerStatus.FOLDED,
					),
					player6 = RecordPlayer(
						seat = 6,
						stack = 50000.0,
						initialStack = 50000.0,
						status = PlayerStatus.FOLDED,
					),
				),
				currentStep = RecordStep.SHOWDOWN,
				streets = HandStreets(
					preflop = PreflopStreet(),
					flop = FlopStreet(
						card1 = Card(Rank.ACE, Suit.HEARTS),
						card2 = Card(Rank.TEN, Suit.DIAMONDS),
						card3 = Card(Rank.SEVEN, Suit.CLUBS),
					),
					turn = TurnStreet(card = Card(Rank.KING, Suit.HEARTS)),
					river = RiverStreet(card = Card(Rank.TWO, Suit.CLUBS)),
				),
				showdown = RecordShowdown(
					seat1 = HeroHand(Card(Rank.QUEEN, Suit.HEARTS), Card(Rank.JACK, Suit.HEARTS)),
				),
				memo = "탑투페어로 올인 콜",
			),
			onSelectSingleBoardCard = { _, _ -> },
			onSelectShowdownCard = {},
			onUpdateMemo = {},
		)
	}
}
