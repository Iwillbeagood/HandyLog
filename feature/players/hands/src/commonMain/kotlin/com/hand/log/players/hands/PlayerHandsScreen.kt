package com.hand.log.players.hands

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.hand.log.designsystem.component.BaseScaffold
import com.hand.log.designsystem.component.HandyTopAppbar
import com.hand.log.designsystem.theme.HandyTheme
import com.hand.log.designsystem.theme.nonScaledSp
import com.hand.log.designsystem.etc.ThemePreview
import com.hand.log.designsystem.etc.ThemePreviews
import com.hand.log.domain.model.Action
import com.hand.log.domain.model.ActionType
import com.hand.log.domain.model.Blinds
import com.hand.log.domain.model.Card
import com.hand.log.domain.model.FlopStreet
import com.hand.log.domain.model.HandRecord
import com.hand.log.domain.model.HandStreets
import com.hand.log.domain.model.PocketCards
import com.hand.log.domain.model.PreflopStreet
import com.hand.log.domain.model.HandPlayer
import com.hand.log.domain.model.Rank
import com.hand.log.domain.model.RiverStreet
import com.hand.log.domain.model.Street
import com.hand.log.domain.model.Suit
import com.hand.log.domain.model.TurnStreet
import com.hand.log.ui.poker.CardSize
import com.hand.log.ui.poker.PlayingCard
import com.hand.log.ui.poker.formatWithComma
import handylog.core.res.generated.resources.Res
import handylog.core.res.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun PlayerHandsScreen(
	playerName: String,
	state: PlayerHandsState,
	onHandClick: (String) -> Unit,
	onBack: () -> Unit,
	paddingValues: PaddingValues = PaddingValues(),
) {
	val colors = HandyTheme.colorScheme

	BaseScaffold(
		containerColor = colors.background,
		topBar = {
			HandyTopAppbar(
				title = playerName,
				onBackEvent = onBack,
			)
		},
	) {
		when (state) {
			PlayerHandsState.Loading -> {}
			is PlayerHandsState.Success -> {
				if (state.hands.isEmpty()) {
					Box(
						modifier = Modifier.fillMaxSize().padding(16.dp),
						contentAlignment = Alignment.Center,
					) {
						Text(
							text = stringResource(Res.string.player_hands_empty),
							style = HandyTheme.typography.regular14,
							color = colors.textSecondary,
						)
					}
				} else {
					val wins = state.hands.count { (it.result ?: 0.0) > 0 }
					val losses = state.hands.count { (it.result ?: 0.0) < 0 }
					val draws = state.hands.size - wins - losses

					LazyColumn(
						modifier = Modifier.fillMaxSize(),
						contentPadding = PaddingValues(
							start = 16.dp,
							end = 16.dp,
							top = 16.dp,
							bottom = 16.dp + paddingValues.calculateBottomPadding(),
						),
						verticalArrangement = Arrangement.spacedBy(8.dp),
					) {
						item {
							RecordSummary(
								total = state.hands.size,
								wins = wins,
								losses = losses,
								draws = draws,
							)
						}
						items(state.hands, key = { it.id }) { hand ->
							HandItem(
								hand = hand,
								onClick = { onHandClick(hand.id) },
							)
						}
					}
				}
			}
		}
	}
}

@Composable
private fun HandItem(
	hand: HandRecord,
	onClick: () -> Unit,
) {
	val colors = HandyTheme.colorScheme
	val result = hand.result ?: 0.0
	val isWin = result > 0
	val isLose = result < 0

	Column(
		modifier = Modifier
			.fillMaxWidth()
			.clip(RoundedCornerShape(12.dp))
			.background(colors.card)
			.clickable(onClick = onClick)
			.padding(12.dp),
	) {
		Row(
			verticalAlignment = Alignment.CenterVertically,
			horizontalArrangement = Arrangement.spacedBy(10.dp),
		) {
			// 승패 뱃지
			Box(
				modifier = Modifier
					.size(28.dp)
					.clip(CircleShape)
					.background(
						when {
							isWin -> colors.primary.copy(alpha = 0.15f)
							isLose -> colors.error.copy(alpha = 0.15f)
							else -> colors.muted
						},
					),
				contentAlignment = Alignment.Center,
			) {
				Text(
					text = when {
						isWin -> "W"
						isLose -> "L"
						else -> "D"
					},
					style = HandyTheme.typography.bold10.nonScaledSp,
					color = when {
						isWin -> colors.primary
						isLose -> colors.error
						else -> colors.textSecondary
					},
				)
			}

			// 히어로 카드
			hand.heroHoleCards?.let { pocket ->
				Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
					PlayingCard(card = pocket.card1, size = CardSize.SM)
					PlayingCard(card = pocket.card2, size = CardSize.SM)
				}
			}

			// 정보
			Column(modifier = Modifier.weight(1f)) {
				Text(
					text = hand.heroPosition.label,
					style = HandyTheme.typography.bold12,
					color = colors.textPrimary,
				)
				hand.blinds?.let { blinds ->
					Text(
						text = "${formatWithComma(blinds.sb.toLong())}/${formatWithComma(blinds.bb.toLong())}",
						style = HandyTheme.typography.regular10,
						color = colors.textSecondary,
					)
				}
				hand.memo?.let { memo ->
					Text(
						text = memo,
						style = HandyTheme.typography.regular10,
						maxLines = 1,
					)
				}
				// 보드 카드
				val boardStreets = listOf(Street.FLOP, Street.TURN, Street.RIVER)
				val hasBoard = boardStreets.any { hand.streets.getCards(it).isNotEmpty() }
				if (hasBoard) {
					Row(
						horizontalArrangement = Arrangement.spacedBy(2.dp),
						modifier = Modifier.padding(top = 4.dp),
					) {
						boardStreets.forEach { street ->
							hand.streets.getCards(street).forEach { card ->
								PlayingCard(card = card, size = CardSize.XS)
							}
						}
					}
				}
			}

		}

		// 날짜 (왼쪽 하단)
		Text(
			text = formatDate(hand.createdAt),
			style = HandyTheme.typography.regular10,
			color = colors.textSecondary.copy(alpha = 0.5f),
			modifier = Modifier.padding(top = 4.dp),
		)
	}
}

@Composable
private fun RecordSummary(
	total: Int,
	wins: Int,
	losses: Int,
	draws: Int,
) {
	val colors = HandyTheme.colorScheme

	Row(
		modifier = Modifier
			.fillMaxWidth()
			.clip(RoundedCornerShape(12.dp))
			.background(colors.card)
			.padding(16.dp),
		horizontalArrangement = Arrangement.SpaceEvenly,
		verticalAlignment = Alignment.CenterVertically,
	) {
		SummaryItem(
			label = stringResource(Res.string.player_hands_total),
			value = "$total",
			color = colors.textPrimary,
		)
		SummaryItem(
			label = stringResource(Res.string.player_hands_win),
			value = "$wins",
			color = colors.primary,
		)
		SummaryItem(
			label = stringResource(Res.string.player_hands_lose),
			value = "$losses",
			color = colors.error,
		)
		SummaryItem(
			label = stringResource(Res.string.player_hands_draw),
			value = "$draws",
			color = colors.textSecondary,
		)
	}
}

@Composable
private fun SummaryItem(
	label: String,
	value: String,
	color: androidx.compose.ui.graphics.Color,
) {
	Column(horizontalAlignment = Alignment.CenterHorizontally) {
		Text(
			text = value,
			style = HandyTheme.typography.bold20,
			color = color,
		)
		Text(
			text = label,
			style = HandyTheme.typography.regular10,
			color = HandyTheme.colorScheme.textSecondary,
		)
	}
}

private fun formatDate(timestamp: Long): String {
	val days = timestamp / 86400000
	val date = kotlinx.datetime.LocalDate.fromEpochDays(days.toInt())
	@Suppress("DEPRECATION")
	return "${date.monthNumber}/${date.dayOfMonth}"
}

@ThemePreviews
@Composable
private fun PlayerHandsScreenPreview() {
	ThemePreview {
		PlayerHandsScreen(
			playerName = "Fish",
			state = PlayerHandsState.Success(
				hands = listOf(
					HandRecord(
						id = "h1",
						tableId = "t1",
						createdAt = 1710000000000L,
						blinds = Blinds(sb = 500.0, bb = 1000.0),
						heroSeat = 3,
						buttonSeat = 1,
						streets = HandStreets(
							preflop = PreflopStreet(
								actions = listOf(
									Action(playerSeat = 3, type = ActionType.RAISE, amount = 2500.0, savedPlayerId = "p1"),
									Action(playerSeat = 4, type = ActionType.CALL, amount = 2500.0, savedPlayerId = "p1"),
								),
							),
							flop = FlopStreet(
								card1 = Card(Rank.ACE, Suit.HEARTS),
								card2 = Card(Rank.TEN, Suit.DIAMONDS),
								card3 = Card(Rank.SEVEN, Suit.CLUBS),
							),
							turn = TurnStreet(card = Card(Rank.KING, Suit.HEARTS)),
							river = RiverStreet(card = Card(Rank.TWO, Suit.CLUBS)),
						),
						players = listOf(
							HandPlayer(
								seat = 3,
								cards = PocketCards(Card(Rank.ACE, Suit.SPADES), Card(Rank.KING, Suit.SPADES)),
								isHero = true,
							),
						),
						result = 15000.0,
						memo = "탑투페어로 올인 콜",
					),
					HandRecord(
						id = "h2",
						tableId = "t1",
						createdAt = 1709900000000L,
						blinds = Blinds(sb = 500.0, bb = 1000.0),
						heroSeat = 5,
						buttonSeat = 3,
						streets = HandStreets(
							preflop = PreflopStreet(
								actions = listOf(
									Action(playerSeat = 5, type = ActionType.RAISE, amount = 2500.0, savedPlayerId = "p1"),
									Action(playerSeat = 6, type = ActionType.CALL, amount = 2500.0, savedPlayerId = "p1"),
								),
							),
						),
						players = listOf(
							HandPlayer(
								seat = 5,
								cards = PocketCards(Card(Rank.QUEEN, Suit.HEARTS), Card(Rank.JACK, Suit.HEARTS)),
								isHero = true,
							),
						),
						result = -8500.0,
						memo = "블러프 캐치 실패",
					),
					HandRecord(
						id = "h3",
						tableId = "t2",
						createdAt = 1709800000000L,
						blinds = Blinds(sb = 1000.0, bb = 2000.0),
						heroSeat = 2,
						buttonSeat = 6,
						streets = HandStreets(
							preflop = PreflopStreet(
								actions = listOf(
									Action(playerSeat = 2, type = ActionType.CALL, amount = 2000.0, savedPlayerId = "p1"),
								),
							),
							flop = FlopStreet(
								card1 = Card(Rank.JACK, Suit.HEARTS),
								card2 = Card(Rank.NINE, Suit.DIAMONDS),
								card3 = Card(Rank.EIGHT, Suit.CLUBS),
							),
						),
						players = listOf(
							HandPlayer(
								seat = 2,
								cards = PocketCards(Card(Rank.JACK, Suit.SPADES), Card(Rank.TEN, Suit.SPADES)),
								isHero = true,
							),
						),
						result = 25000.0,
						memo = "스트레이트 드로 히트",
					),
				),
			),
			onHandClick = {},
			onBack = {},
		)
	}
}

@ThemePreviews
@Composable
private fun PlayerHandsScreenEmptyPreview() {
	ThemePreview {
		PlayerHandsScreen(
			playerName = "Unknown",
			state = PlayerHandsState.Success(hands = emptyList()),
			onHandClick = {},
			onBack = {},
		)
	}
}
