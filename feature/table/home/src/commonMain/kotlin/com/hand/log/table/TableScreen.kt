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
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hand.log.designsystem.component.BaseScaffold
import com.hand.log.designsystem.component.FadeAnimatedVisibility
import com.hand.log.designsystem.component.TopAppbarIcon
import com.hand.log.designsystem.component.HandyFab
import com.hand.log.designsystem.component.HandyTopAppbar
import com.hand.log.designsystem.etc.ThemePreview
import com.hand.log.designsystem.etc.ThemePreviews
import com.hand.log.designsystem.theme.HandyTheme
import com.hand.log.domain.model.Blinds
import com.hand.log.domain.model.Card
import com.hand.log.domain.model.FlopStreet
import com.hand.log.domain.model.GameType
import com.hand.log.domain.model.HandPlayer
import com.hand.log.domain.model.HandRecord
import com.hand.log.domain.model.HandStreets
import com.hand.log.domain.model.PocketCards
import com.hand.log.domain.model.Player
import com.hand.log.domain.model.PokerTable
import com.hand.log.domain.model.Rank
import com.hand.log.domain.model.Suit
import com.hand.log.table.component.HandRecordCard
import com.hand.log.table.component.PokerTableView
import com.hand.log.table.contract.TableState
import handylog.core.res.generated.resources.Res
import handylog.core.res.generated.resources.*
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun TableScreen(
	state: TableState,
	onBack: () -> Unit,
	onNavigateToRecordHand: () -> Unit,
	onNavigateToHandDetail: (String) -> Unit,
	onShowDeleteConfirm: () -> Unit,
	onSeatClick: (Int) -> Unit,
	onShowTableEdit: () -> Unit,
	onBalanceClick: () -> Unit,
) {
	val colors = HandyTheme.colorScheme
	val typography = HandyTheme.typography
	val tableData = state as? TableState.TableData

	BaseScaffold(
		topBar = {
			if (tableData != null) {
				HandyTopAppbar(
					title = if (tableData.table.gameType is GameType.Tournament) {
						stringResource(Res.string.table_detail_tournament)
					} else {
						stringResource(Res.string.table_detail_cash)
					},
					onBackEvent = onBack,
					endContent = {
						Row {
							TopAppbarIcon(
								icon = Res.drawable.delete,
								onClick = onShowDeleteConfirm,
							)
							TopAppbarIcon(
								icon = Res.drawable.pencil,
								onClick = onShowTableEdit,
							)
						}
					},
					subContent = {
						Row(
							modifier = Modifier
								.fillMaxWidth()
								.padding(bottom = 8.dp),
							horizontalArrangement = Arrangement.Center,
						) {
							tableData.table.location?.let {
								Icon(
									painter = painterResource(Res.drawable.map_pin),
									contentDescription = null,
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
								text = tableData.table.date.toString(),
								style = typography.regular12,
								color = colors.textSecondary,
							)
						}
					},
				)
			} else {
				HandyTopAppbar(
					onBackEvent = onBack,
				)
			}
		},
		floatingActionButton = {
			HandyFab(
				onClick = onNavigateToRecordHand,
				contentDescription = stringResource(Res.string.table_detail_record_hand),
			)
		},
	) {
		FadeAnimatedVisibility(tableData != null) {
			val data = tableData ?: return@FadeAnimatedVisibility
			TableContent(
				state = data,
				onSeatClick = onSeatClick,
				onBalanceClick = onBalanceClick,
				onNavigateToHandDetail = onNavigateToHandDetail,
			)
		}
	}

}

@Composable
private fun TableContent(
	state: TableState.TableData,
	onSeatClick: (Int) -> Unit,
	onBalanceClick: () -> Unit,
	onNavigateToHandDetail: (String) -> Unit,
) {
	val colors = HandyTheme.colorScheme

	LazyColumn(
		modifier = Modifier.fillMaxSize(),
		contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
		verticalArrangement = Arrangement.spacedBy(12.dp),
	) {
		item {
			PokerTableView(
				table = state.table,
				onSeatClick = onSeatClick,
				onBalanceClick = onBalanceClick,
				modifier = Modifier
					.fillMaxWidth()
					.padding(vertical = 8.dp),
			)
		}

		item {
			Row(
				modifier = Modifier
					.fillMaxWidth()
					.padding(top = 8.dp),
				horizontalArrangement = Arrangement.SpaceBetween,
				verticalAlignment = Alignment.CenterVertically,
			) {
				Text(
					text = stringResource(Res.string.table_detail_record_hand),
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

		if (state.hands.isEmpty()) {
			item {
				Column(
					modifier = Modifier
						.fillMaxWidth()
						.padding(vertical = 32.dp),
					horizontalAlignment = Alignment.CenterHorizontally,
				) {
					Text(
						text = stringResource(Res.string.table_detail_empty_title),
						style = HandyTheme.typography.medium14,
						color = colors.textSecondary,
					)
					Spacer(modifier = Modifier.height(4.dp))
					Text(
						text = stringResource(Res.string.table_detail_empty_desc),
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
					tableDate = state.table.date,
					index = handIndex,
					onClick = { onNavigateToHandDetail(hand.id) },
				)
			}
		}

		item {
			Spacer(modifier = Modifier.height(80.dp))
		}
	}
}

@ThemePreviews
@Composable
private fun TableScreenPreview() {
	ThemePreview {
		TableScreen(
			state = TableState.TableData(
				table = PokerTable(
					id = "1",
					date = LocalDate(2026, 3, 12),
					location = "강남 홀덤펍",
					gameType = GameType.Cash(sb = 500.0, bb = 1000.0),
					heroSeat = 3,
					players = listOf(
						Player(seat = 1, name = "Player 1"),
						Player(seat = 3, name = "Hero"),
						Player(seat = 5, name = "Player 5"),
					),
					createdAt = 1710000000000L,
				),
				hands = listOf(
					HandRecord(
						id = "h1",
						tableId = "1",
						createdAt = 1710000000000L,
						blinds = Blinds(sb = 500.0, bb = 1000.0),
						heroSeat = 3,
						players = listOf(
							HandPlayer(
								seat = 3,
								cards = PocketCards(Card(Rank.ACE, Suit.SPADES), Card(Rank.KING, Suit.SPADES)),
								initialStack = 62000.0,
								isHero = true,
							),
						),
						buttonSeat = 1,
						streets = HandStreets(
							flop = FlopStreet(
								card1 = Card(Rank.ACE, Suit.HEARTS),
								card2 = Card(Rank.KING, Suit.DIAMONDS),
								card3 = Card(Rank.QUEEN, Suit.CLUBS),
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
						heroSeat = 3,
						players = listOf(
							HandPlayer(
								seat = 3,
								cards = PocketCards(Card(Rank.JACK, Suit.HEARTS), Card(Rank.TEN, Suit.HEARTS)),
								initialStack = 50000.0,
								isHero = true,
							),
						),
						buttonSeat = 3,
						result = -8500.0,
					),
				),
			),
			onBack = {},
			onNavigateToRecordHand = {},
			onNavigateToHandDetail = {},
			onSeatClick = {},
			onShowTableEdit = {},
			onShowDeleteConfirm = {},
			onBalanceClick = {},
		)
	}
}

@ThemePreviews
@Composable
private fun TableScreenEmptyPreview() {
	ThemePreview {
		TableScreen(
			state = TableState.TableData(
				table = PokerTable(
					id = "1",
					date = LocalDate(2026, 3, 12),
					location = "강남",
					gameType = GameType.Cash(sb = 500.0, bb = 1000.0),
					heroSeat = 1,
					players = emptyList(),
					createdAt = 1710000000000L,
				),
			),
			onBack = {},
			onNavigateToRecordHand = {},
			onNavigateToHandDetail = {},
			onSeatClick = {},
			onShowTableEdit = {},
			onShowDeleteConfirm = {},
			onBalanceClick = {},
		)
	}
}
