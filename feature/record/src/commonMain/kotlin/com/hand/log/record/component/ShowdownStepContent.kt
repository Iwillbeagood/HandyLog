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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import com.hand.log.designsystem.component.HandySectionLabel
import com.hand.log.designsystem.component.VerticalSpacer
import com.hand.log.designsystem.etc.ThemePreview
import com.hand.log.designsystem.etc.ThemePreviews
import com.hand.log.designsystem.theme.HandyTheme
import com.hand.log.designsystem.theme.nonScaledSp
import com.hand.log.domain.model.Card
import com.hand.log.domain.model.GameType
import com.hand.log.domain.model.PocketCards
import com.hand.log.domain.model.PokerTable
import com.hand.log.domain.model.Rank
import com.hand.log.domain.model.Street
import com.hand.log.domain.model.Suit
import com.hand.log.record.contract.RecordHandState
import com.hand.log.record.contract.RecordStep
import com.hand.log.domain.model.HandRanking
import com.hand.log.domain.model.HeroResultType
import com.hand.log.domain.model.FlopStreet
import com.hand.log.domain.model.PreflopStreet
import com.hand.log.domain.model.RiverStreet
import com.hand.log.domain.model.ShowdownResult
import com.hand.log.ui.localizedLabel
import com.hand.log.domain.model.TurnStreet
import com.hand.log.record.model.RecordPlayer
import com.hand.log.record.model.RecordPlayers
import com.hand.log.domain.model.HandStreets
import com.hand.log.record.model.PlayerStatus
import com.hand.log.ui.MemoField
import com.hand.log.ui.poker.CardSize
import com.hand.log.ui.poker.HoleCards
import com.hand.log.ui.poker.OutcomeBadge
import com.hand.log.ui.poker.PlayingCard
import com.hand.log.ui.poker.PositionBadge
import com.hand.log.ui.poker.outcomeColor
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.stringResource
import handylog.core.res.generated.resources.Res
import handylog.core.res.generated.resources.*

@Composable
internal fun ShowdownStepContent(
	state: RecordHandState.Recording,
	onSelectSingleBoardCard: (Street, Int) -> Unit,
	onSelectHeroCard: () -> Unit,
	onSelectShowdownCard: (Int) -> Unit,
	onUpdateMemo: (String) -> Unit,
) {
	val boardCards = state.streets.boardCards
	val results = state.showdownResults
	val hasResults = results.isNotEmpty()

	Column {
		if (boardCards.isNotEmpty()) {
			HandySectionLabel(stringResource(Res.string.showdown_board_edit))
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

		HandySectionLabel(stringResource(Res.string.showdown_player_hand))
		VerticalSpacer(8.dp)

		// 히어로가 폴드했더라도 쇼다운에 표시
		val heroSeat = state.table?.heroSeat
		val heroFolded = heroSeat != null && heroSeat !in state.remainingSeats.toSet()
		if (heroFolded) {
			ShowdownPlayerCard(
				state = state,
				seat = heroSeat,
				result = null,
				isFolded = true,
				onSelectHeroCard = onSelectHeroCard,
				onSelectShowdownCard = onSelectShowdownCard,
			)
			VerticalSpacer(8.dp)
		}

		state.remainingSeats.forEach { seat ->
			ShowdownPlayerCard(
				state = state,
				seat = seat,
				result = results.find { it.seat == seat },
				onSelectHeroCard = onSelectHeroCard,
				onSelectShowdownCard = onSelectShowdownCard,
			)
			VerticalSpacer(8.dp)
		}

		// 결과 (자동 계산) — HandRecord.resolvedHeroResultType과 동일 로직
		if (hasResults) {
			val heroSeat = state.table?.heroSeat
			val heroResult = state.heroResult
			val heroWonAnyPot = state.potResults.any { it.winnerSeat == heroSeat }
			val isWin = heroResult >= 0 || heroWonAnyPot
			val colors = HandyTheme.colorScheme
			val heroShowdownResult = results.find { it.seat == heroSeat }

			VerticalSpacer(8.dp)
			HandySectionLabel(stringResource(Res.string.showdown_result))

			val resultType = when {
				state.isFoldWin && isWin -> HeroResultType.FOLD_WIN
				state.isFoldWin -> HeroResultType.FOLD_LOSE
				heroShowdownResult?.isSplit == true -> HeroResultType.SHOWDOWN_SPLIT
				isWin -> HeroResultType.SHOWDOWN_WIN
				else -> HeroResultType.SHOWDOWN_LOSE
			}
			val ranking = heroShowdownResult?.ranking
				?.takeIf { it != HandRanking.WIN_BY_FOLD }
				?.localizedLabel() ?: ""
			Text(
				text = resultType.localizedLabel(ranking),
				style = HandyTheme.typography.bold20,
				color = when (resultType) {
					HeroResultType.SHOWDOWN_SPLIT -> colors.split
					HeroResultType.FOLD_WIN, HeroResultType.SHOWDOWN_WIN -> colors.primary
					HeroResultType.FOLD_LOSE, HeroResultType.SHOWDOWN_LOSE -> colors.error
				},
			)

			// 메인팟/사이드팟 분배 내역
			val potResults = state.potResults
			if (potResults.isNotEmpty()) {
				VerticalSpacer(8.dp)
				potResults.forEach { potResult ->
					val winnerPos = state.positionName(potResult.winnerSeat)
					val isHeroWin = potResult.winnerSeat == state.table?.heroSeat
					Row(
						modifier = Modifier
							.fillMaxWidth()
							.padding(vertical = 2.dp),
						horizontalArrangement = Arrangement.SpaceBetween,
					) {
						Text(
							text = "${potResult.label}: ${state.formatAmount(potResult.amount)}",
							style = HandyTheme.typography.regular12,
							color = colors.textSecondary,
						)
						Text(
							text = "→ $winnerPos${if (isHeroWin) " (Hero)" else ""}",
							style = HandyTheme.typography.bold12,
							color = if (isHeroWin) colors.primary else colors.error,
						)
					}
				}
			}
		}

		VerticalSpacer(12.dp)
		MemoField(
			value = state.memo,
			onValueChange = onUpdateMemo,
			label = stringResource(Res.string.showdown_memo),
		)

		VerticalSpacer(16.dp)
	}
}

@Composable
private fun ShowdownPlayerCard(
	state: RecordHandState.Recording,
	seat: Int,
	result: ShowdownResult?,
	isFolded: Boolean = false,
	onSelectHeroCard: () -> Unit = {},
	onSelectShowdownCard: (Int) -> Unit,
) {
	val colors = HandyTheme.colorScheme
	val posName = state.positionName(seat)
	val isHero = seat == state.table?.heroSeat
	val hand = if (isHero) state.heroHand else state.players[seat]?.cards
	val isUnknown = !isHero && state.players[seat]?.isCardsUnknown == true
	// 사이드팟에서 이긴 경우도 반영
	val wonAnyPot = state.potResults.any { it.winnerSeat == seat }
	val isSplit = result?.isSplit == true && !isUnknown
	val isWinner = (result?.isWinner == true || wonAnyPot) && !isUnknown
	val isLoser = (result != null && !isWinner && !isSplit && !isFolded) || isUnknown
	val player = state.players[seat]
	val currentStack = state.getPlayerStack(seat)
	val initialStack = player?.initialStack
	val isEliminated = result != null && initialStack != null && initialStack > 0 && currentStack != null && currentStack <= 0.0

	Box {
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.clip(RoundedCornerShape(8.dp))
				.then(
					when {
						isWinner -> Modifier.border(2.dp, colors.gold, RoundedCornerShape(8.dp))
						isSplit -> Modifier.border(1.dp, colors.split, RoundedCornerShape(8.dp))
						isLoser -> Modifier.border(1.dp, colors.error.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
						else -> Modifier
					},
				)
				.background(
					when {
						isFolded -> colors.muted.copy(alpha = 0.5f)
						isWinner -> colors.gold.copy(alpha = 0.15f)
						isSplit -> colors.split.copy(alpha = 0.1f)
						isLoser -> colors.error.copy(alpha = 0.05f)
						isEliminated -> colors.error.copy(alpha = 0.05f)
						else -> colors.muted
					},
				)
				.clickable(enabled = !isFolded) {
					if (isHero) onSelectHeroCard() else onSelectShowdownCard(seat)
				}
				.padding(12.dp),
			verticalAlignment = Alignment.CenterVertically,
			horizontalArrangement = Arrangement.spacedBy(8.dp),
		) {
			PositionBadge(
				positionName = posName,
				isHero = isHero,
				circleSize = 32.dp,
			)

			// 포지션 + 족보
			Column(modifier = Modifier.weight(1f)) {
				Row(
					verticalAlignment = Alignment.CenterVertically,
					horizontalArrangement = Arrangement.spacedBy(6.dp),
				) {
					Text(
						text = posName,
						style = HandyTheme.typography.bold14,
						color = when {
							isFolded -> colors.textSecondary.copy(alpha = 0.5f)
							isHero -> colors.gold
							else -> colors.textPrimary
						},
					)
					result?.outcome?.let { outcome ->
						OutcomeBadge(outcome = outcome)
					}
					if (isFolded) {
						Text(
							text = "FOLD",
							style = HandyTheme.typography.bold10.nonScaledSp,
							color = colors.textSecondary,
							modifier = Modifier
								.clip(RoundedCornerShape(4.dp))
								.background(colors.muted)
								.padding(horizontal = 6.dp, vertical = 2.dp),
						)
					}
				}

				if (result != null && !state.isFoldWin) {
					Text(
						text = result.ranking.localizedLabel(),
						style = HandyTheme.typography.regular12,
						color = outcomeColor(result?.outcome),
					)
				} else if (isHero) {
					Text(
						text = "Hero",
						style = HandyTheme.typography.regular10.nonScaledSp,
						color = if (isFolded) colors.textSecondary.copy(alpha = 0.5f) else colors.gold,
					)
				}

				if (result != null && currentStack != null && currentStack > 0) {
					Text(
						text = state.formatAmount(currentStack),
						style = HandyTheme.typography.regular10.nonScaledSp,
						color = colors.textSecondary,
					)
				}
			}

			HoleCards(
				cards = hand,
				isUnknown = isUnknown,
			)
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
					style = HandyTheme.typography.bold18.nonScaledSp,
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
					gameType = GameType.Cash(sb = 500.0, bb = 1000.0),
					heroSeat = 3,
					createdAt = 0L,
				),
				players = RecordPlayers.create(playerCount = 6, defaultStack = 50000.0)
					.update(
						3,
					) { copy(cards = PocketCards(Card(Rank.ACE, Suit.SPADES), Card(Rank.KING, Suit.SPADES))) }
					.update(1) {
						copy(cards = PocketCards(Card(Rank.QUEEN, Suit.HEARTS), Card(Rank.JACK, Suit.HEARTS)))
					},
				currentStep = RecordStep.SHOWDOWN,
			),
			onSelectSingleBoardCard = { _, _ -> },
			onSelectHeroCard = {},
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
					gameType = GameType.Cash(sb = 500.0, bb = 1000.0),
					heroSeat = 3,
					createdAt = 0L,
				),
				players = RecordPlayers(
					mapOf(
						1 to RecordPlayer(
							seat = 1,
							cards = PocketCards(Card(Rank.QUEEN, Suit.HEARTS), Card(Rank.JACK, Suit.HEARTS)),
							stack = 0.0,
							initialStack = 50000.0,
							status = PlayerStatus.ALL_IN,
						),
						2 to RecordPlayer(
							seat = 2,
							stack = 49500.0,
							initialStack = 50000.0,
							status = PlayerStatus.FOLDED,
						),
						3 to RecordPlayer(
							seat = 3,
							cards = PocketCards(Card(Rank.ACE, Suit.SPADES), Card(Rank.KING, Suit.SPADES)),
							stack = 0.0,
							initialStack = 50000.0,
							status = PlayerStatus.ALL_IN,
						),
						4 to RecordPlayer(
							seat = 4,
							stack = 50000.0,
							initialStack = 50000.0,
							status = PlayerStatus.FOLDED,
						),
						5 to RecordPlayer(
							seat = 5,
							stack = 50000.0,
							initialStack = 50000.0,
							status = PlayerStatus.FOLDED,
						),
						6 to RecordPlayer(
							seat = 6,
							stack = 50000.0,
							initialStack = 50000.0,
							status = PlayerStatus.FOLDED,
						),
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
				memo = "탑투페어로 올인 콜",
			),
			onSelectSingleBoardCard = { _, _ -> },
			onSelectHeroCard = {},
			onSelectShowdownCard = {},
			onUpdateMemo = {},
		)
	}
}
