package com.hand.log.table

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hand.log.designsystem.component.BaseScaffold
import com.hand.log.designsystem.component.HandyTopAppbar
import com.hand.log.designsystem.component.IconButton
import com.hand.log.designsystem.theme.HandLogTheme
import com.hand.log.designsystem.theme.HandyTheme
import com.hand.log.domain.model.Blinds
import com.hand.log.domain.model.Card
import com.hand.log.domain.model.GameType
import com.hand.log.domain.model.HandRecord
import com.hand.log.domain.model.Player
import com.hand.log.domain.model.PokerTable
import com.hand.log.domain.model.Rank
import com.hand.log.domain.model.Street
import com.hand.log.domain.model.StreetData
import com.hand.log.domain.model.Suit
import com.hand.log.table.component.HandRecordCard
import com.hand.log.table.component.PokerTableView
import com.hand.log.table.contract.TableDetailState
import handylog.core.res.generated.resources.Res
import handylog.core.res.generated.resources.map_pin
import handylog.core.res.generated.resources.plus
import handylog.core.res.generated.resources.settings
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
internal fun TableDetailScreen(
	state: TableDetailState.TableData,
	onBack: () -> Unit,
	onNavigateToRecordHand: () -> Unit,
	onDeleteHand: (String) -> Unit,
	onShowPlayerSetup: () -> Unit,
) {
	val colors = HandyTheme.colorScheme
	val typography = HandyTheme.typography

	BaseScaffold(
		topBar = {
			HandyTopAppbar(
				title = if (state.table.gameType == GameType.TOURNAMENT) "토너먼트" else "캐시",
				onBackEvent = onBack,
				iconButton = IconButton(
					text = "설정",
					icon = Res.drawable.settings,
					onClick = onShowPlayerSetup,
				),
				subContent = {
					Row(
						modifier = Modifier
							.fillMaxWidth()
							.padding(bottom = 8.dp),
						horizontalArrangement = Arrangement.Center,
					) {
						state.table.location?.let {
							Icon(
								painter = painterResource(Res.drawable.map_pin),
								contentDescription = "위치 아이콘",
								tint = colors.textSecondary,
								modifier = Modifier
									.size(14.dp)
									.padding(end = 2.dp),
							)
							Text(
								text = "$it · ",
								style = typography.regular12,
								color = colors.textSecondary,
							)
						}
						Text(
							text = state.table.date.toString(),
							style = typography.regular12,
							color = colors.textSecondary,
						)
					}
				},
			)
		},
		floatingActionButton = {
			FloatingActionButton(
				onClick = onNavigateToRecordHand,
				containerColor = colors.primary,
				contentColor = colors.onPrimary,
				shape = CircleShape,
			) {
				Icon(
					painter = painterResource(Res.drawable.plus),
					contentDescription = "새 핸드 기록",
				)
			}
		},
	) {
		LazyColumn(
			modifier = Modifier.fillMaxSize(),
			contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
			verticalArrangement = Arrangement.spacedBy(12.dp),
		) {
			// Poker Table Visualization
			item {
				PokerTableView(
					table = state.table,
					modifier = Modifier
						.fillMaxWidth()
						.padding(vertical = 8.dp),
				)
			}

			// Hand History Header
			item {
				Row(
					modifier = Modifier
						.fillMaxWidth()
						.padding(top = 8.dp),
					horizontalArrangement = Arrangement.SpaceBetween,
					verticalAlignment = Alignment.CenterVertically,
				) {
					Text(
						text = "핸드 기록",
						style = HandyTheme.typography.bold14,
						color = colors.textSecondary,
					)
					Text(
						text = "${state.hands.size}개",
						style = HandyTheme.typography.regular12,
						color = colors.textSecondary,
					)
				}
			}

			// Hand History List
			if (state.hands.isEmpty()) {
				item {
					Column(
						modifier = Modifier
							.fillMaxWidth()
							.padding(vertical = 32.dp),
						horizontalAlignment = Alignment.CenterHorizontally,
					) {
						Text(
							text = "아직 기록된 핸드가 없습니다",
							style = HandyTheme.typography.medium14,
							color = colors.textSecondary,
						)
						Spacer(modifier = Modifier.height(4.dp))
						Text(
							text = "+ 버튼을 눌러 첫 핸드를 기록하세요",
							style = HandyTheme.typography.regular12,
							color = colors.textSecondary,
						)
					}
				}
			} else {
				items(state.hands, key = { it.id }) { hand ->
					val handIndex = state.hands.size - state.hands.indexOf(hand)
					HandRecordCard(
						hand = hand,
						index = handIndex,
						onDelete = { onDeleteHand(hand.id) },
					)
				}
			}

			// Bottom spacing
			item {
				Spacer(modifier = Modifier.height(80.dp))
			}
		}
	}
}

@Preview
@Composable
private fun TableDetailScreenPreview() {
	HandLogTheme {
		TableDetailScreen(
			state = TableDetailState.TableData(
				table = PokerTable(
					id = "1",
					date = LocalDate(2026, 3, 12),
					location = "강남 홀덤펍",
					gameType = GameType.CASH,
					startingStack = 50000.0,
					blinds = Blinds(sb = 500.0, bb = 1000.0),
					playerCount = 9,
					heroSeat = 3,
					players = listOf(
						Player(seat = 1, stack = 45000.0, name = "Player 1"),
						Player(seat = 3, stack = 62000.0, name = "Hero"),
						Player(seat = 5, stack = 38000.0, name = "Player 5"),
					),
					createdAt = 1710000000000L,
				),
				hands = listOf(
					HandRecord(
						id = "h1",
						tableId = "1",
						createdAt = 1710000000000L,
						blinds = Blinds(sb = 500.0, bb = 1000.0),
						heroCards = listOf(
							Card(Rank.ACE, Suit.SPADES),
							Card(Rank.KING, Suit.SPADES),
						),
						heroStack = 62000.0,
						buttonSeat = 1,
						streets = mapOf(
							Street.FLOP to StreetData(
								cards = listOf(
									Card(Rank.ACE, Suit.HEARTS),
									Card(Rank.KING, Suit.DIAMONDS),
									Card(Rank.QUEEN, Suit.CLUBS),
								),
							),
						),
						result = 15000.0,
						memo = "탑투페어로 올인 콜",
					),
					HandRecord(
						id = "h2",
						tableId = "1",
						createdAt = 1709900000000L,
						blinds = Blinds(sb = 500.0, bb = 1000.0),
						heroCards = listOf(
							Card(Rank.JACK, Suit.HEARTS),
							Card(Rank.TEN, Suit.HEARTS),
						),
						heroStack = 50000.0,
						buttonSeat = 3,
						result = -8500.0,
					),
				),
			),
			onBack = {},
			onNavigateToRecordHand = {},
			onDeleteHand = {},
			onShowPlayerSetup = {},
		)
	}
}

@Preview
@Composable
private fun TableDetailScreenEmptyPreview() {
	HandLogTheme {
		TableDetailScreen(
			state = TableDetailState.TableData(
				table = PokerTable(
					id = "1",
					date = LocalDate(2026, 3, 12),
					location = "강남",
					gameType = GameType.CASH,
					startingStack = 50000.0,
					blinds = Blinds(sb = 500.0, bb = 1000.0),
					playerCount = 6,
					heroSeat = 1,
					players = emptyList(),
					createdAt = 1710000000000L,
				),
			),
			onBack = {},
			onNavigateToRecordHand = {},
			onDeleteHand = {},
			onShowPlayerSetup = {},
		)
	}
}
