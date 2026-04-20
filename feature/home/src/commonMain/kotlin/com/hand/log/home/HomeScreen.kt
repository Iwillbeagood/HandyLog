package com.hand.log.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.hand.log.designsystem.etc.ThemePreview
import com.hand.log.designsystem.etc.ThemePreviews
import androidx.compose.ui.unit.dp
import com.hand.log.designsystem.component.BaseScaffold
import com.hand.log.designsystem.component.FadeAnimatedVisibility
import com.hand.log.designsystem.component.HandyHorizontalDivider
import com.hand.log.designsystem.component.HandySegmentedTab
import com.hand.log.designsystem.component.HandyTopAppbar
import com.hand.log.designsystem.component.IconButton
import com.hand.log.designsystem.component.TopAppbarType
import com.hand.log.domain.model.Blinds
import com.hand.log.domain.model.Card
import com.hand.log.domain.model.GameType
import com.hand.log.domain.model.HandRecord
import com.hand.log.domain.model.HandWithTable
import com.hand.log.domain.model.PocketCards
import com.hand.log.domain.model.PokerTable
import com.hand.log.domain.model.Rank
import com.hand.log.domain.model.Suit
import com.hand.log.domain.model.TableListItem
import com.hand.log.home.component.EmptyState
import com.hand.log.home.component.HomeHandCard
import com.hand.log.home.component.TableCard
import com.hand.log.home.contract.HomeState
import com.hand.log.home.contract.HomeTab
import handylog.core.res.generated.resources.Res
import handylog.core.res.generated.resources.*
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun HomeScreen(
	homeState: HomeState,
	selectedTab: HomeTab,
	onTabSelect: (HomeTab) -> Unit,
	onNavigateToTableDetail: (String) -> Unit,
	onNavigateToHandDetail: (String) -> Unit,
	onTableAdd: () -> Unit,
) {
	BaseScaffold {
		Column(
			modifier = Modifier.fillMaxSize(),
		) {
			HandyTopAppbar(
				navigationType = TopAppbarType.Main,
				iconButton = IconButton(
					text = stringResource(Res.string.home_new_table),
					icon = Res.drawable.plus,
					onClick = onTableAdd,
				),
			)
			HandyHorizontalDivider()

			// Segment Tab
			HandySegmentedTab(
				options = HomeTab.entries,
				selected = selectedTab,
				onSelect = onTabSelect,
				label = { tab ->
					when (tab) {
						HomeTab.TABLE -> stringResource(Res.string.home_tab_table)
						HomeTab.HAND -> stringResource(Res.string.home_tab_hand)
					}
				},
				modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
			)

			HomeContent(
				homeState = homeState,
				selectedTab = selectedTab,
				onNavigateToTableDetail = onNavigateToTableDetail,
				onNavigateToHandDetail = onNavigateToHandDetail,
			)
		}
	}
}

@Composable
private fun HomeContent(
	homeState: HomeState,
	selectedTab: HomeTab,
	onNavigateToTableDetail: (String) -> Unit,
	onNavigateToHandDetail: (String) -> Unit,
) {
	FadeAnimatedVisibility(homeState is HomeState.HomeData) {
		val data = homeState as? HomeState.HomeData ?: return@FadeAnimatedVisibility

		when (selectedTab) {
			HomeTab.TABLE -> {
				if (data.tables.isEmpty()) {
					EmptyState(modifier = Modifier.fillMaxSize())
				} else {
					LazyColumn(
						modifier = Modifier.fillMaxSize(),
						verticalArrangement = Arrangement.spacedBy(12.dp),
						contentPadding = PaddingValues(
							start = 16.dp,
							end = 16.dp,
							top = 12.dp,
							bottom = 12.dp,
						),
					) {
						items(data.tables, key = { it.table.id }) { item ->
							TableCard(
								item = item,
								onClick = { onNavigateToTableDetail(item.table.id) },
								modifier = Modifier.animateItem(),
							)
						}
					}
				}
			}
			HomeTab.HAND -> {
				if (data.hands.isEmpty()) {
					EmptyState(modifier = Modifier.fillMaxSize())
				} else {
					LazyColumn(
						modifier = Modifier.fillMaxSize(),
						verticalArrangement = Arrangement.spacedBy(12.dp),
						contentPadding = PaddingValues(
							start = 16.dp,
							end = 16.dp,
							top = 12.dp,
							bottom = 12.dp,
						),
					) {
						items(data.hands, key = { it.hand.id }) { item ->
							HomeHandCard(
								item = item,
								onClick = { onNavigateToHandDetail(item.hand.id) },
								modifier = Modifier.animateItem(),
							)
						}
					}
				}
			}
		}
	}
}

@ThemePreviews
@Composable
private fun HomeScreenEmptyPreview() {
	ThemePreview {
		HomeScreen(
			homeState = HomeState.HomeData(tables = emptyList()),
			selectedTab = HomeTab.TABLE,
			onTabSelect = {},
			onNavigateToTableDetail = {},
			onNavigateToHandDetail = {},
			onTableAdd = {},
		)
	}
}

@ThemePreviews
@Composable
private fun HomeScreenWithDataPreview() {
	ThemePreview {
		HomeScreen(
			homeState = HomeState.HomeData(
				tables = listOf(
					TableListItem(
						table = PokerTable(
							id = "1",
							date = LocalDate(2025, 3, 10),
							location = "강남 홀덤펍",
							gameType = GameType.Cash(sb = 1000.0, bb = 2000.0),
							heroSeat = 5,
							createdAt = 1710000000000L,
						),
						handCount = 12,
					),
					TableListItem(
						table = PokerTable(
							id = "2",
							date = LocalDate(2025, 3, 9),
							location = "WPT Korea",
							gameType = GameType.Tournament(),
							heroSeat = 3,
							createdAt = 1709900000000L,
						),
						handCount = 5,
					),
				),
				hands = listOf(
					HandWithTable(
						hand = HandRecord(
							id = "h1",
							tableId = "1",
							createdAt = 1710000000000L,
							blinds = Blinds(sb = 1000.0, bb = 2000.0),
							heroHand = PocketCards(Card(Rank.ACE, Suit.SPADES), Card(Rank.KING, Suit.SPADES)),
							heroStack = 62000.0,
							buttonSeat = 1,
						),
						table = PokerTable(
							id = "1",
							date = LocalDate(2025, 3, 10),
							location = "강남 홀덤펍",
							gameType = GameType.Cash(sb = 1000.0, bb = 2000.0),
							heroSeat = 5,
							createdAt = 1710000000000L,
						),
					),
				),
			),
			selectedTab = HomeTab.TABLE,
			onTabSelect = {},
			onNavigateToTableDetail = {},
			onNavigateToHandDetail = {},
			onTableAdd = {},
		)
	}
}
