package com.hand.log.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.hand.log.designsystem.etc.ThemePreview
import com.hand.log.designsystem.etc.ThemePreviews
import androidx.compose.ui.unit.dp
import com.hand.log.designsystem.component.BaseScaffold
import com.hand.log.designsystem.component.HandyHorizontalDivider
import com.hand.log.designsystem.component.HandyTopAppbar
import com.hand.log.designsystem.component.IconButton
import com.hand.log.designsystem.component.TopAppbarType
import com.hand.log.domain.model.Blinds
import com.hand.log.domain.model.GameType
import com.hand.log.domain.model.PokerTable
import com.hand.log.home.component.EmptyState
import com.hand.log.home.component.TableCard
import com.hand.log.home.contract.HomeState
import com.hand.log.home.contract.TableListItem
import handylog.core.res.generated.resources.Res
import handylog.core.res.generated.resources.plus
import kotlinx.datetime.LocalDate

@Composable
internal fun HomeScreen(
	homeState: HomeState.HomeData,
	onNavigateToTableDetail: (String) -> Unit,
	onTableAdd: () -> Unit,
) {
	BaseScaffold {
		Column(
			modifier = Modifier.fillMaxSize(),
		) {
			HandyTopAppbar(
				navigationType = TopAppbarType.Main,
				iconButton = IconButton(
					text = "새 테이블",
					icon = Res.drawable.plus,
					onClick = onTableAdd,
				),
			)
			HandyHorizontalDivider()

			if (homeState.tables.isEmpty()) {
				EmptyState(modifier = Modifier.fillMaxSize())
			} else {
				LazyColumn(
					modifier = Modifier.fillMaxSize(),
					verticalArrangement = Arrangement.spacedBy(12.dp),
					contentPadding = PaddingValues(
						start = 16.dp,
						end = 16.dp,
						top = 20.dp,
						bottom = 12.dp,
					),
				) {
					items(homeState.tables, key = { it.table.id }) { item ->
						TableCard(
							item = item,
							onClick = { onNavigateToTableDetail(item.table.id) },
						)
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
			onNavigateToTableDetail = {},
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
							gameType = GameType.CASH,
							startingStack = 200000.0,
							blinds = Blinds(sb = 1000.0, bb = 2000.0),
							playerCount = 9,
							heroSeat = 5,
							createdAt = 1710000000000L,
						),
						handCount = 12,
					),
					TableListItem(
						table = PokerTable(
							id = "2",
							date = LocalDate(2025, 3, 9),
							location = "토너먼트",
							gameType = GameType.TOURNAMENT,
							startingStack = 50000.0,
							playerCount = 6,
							heroSeat = 3,
							createdAt = 1709900000000L,
						),
						handCount = 5,
					),
				),
			),
			onNavigateToTableDetail = {},
			onTableAdd = {},
		)
	}
}
