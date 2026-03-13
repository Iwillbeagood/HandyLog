package com.hand.log.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hand.log.designsystem.component.BaseScaffold
import com.hand.log.designsystem.theme.HandLogTheme
import com.hand.log.designsystem.theme.HandyTheme
import com.hand.log.domain.model.Blinds
import com.hand.log.domain.model.GameType
import com.hand.log.domain.model.PokerTable
import com.hand.log.home.component.EmptyState
import com.hand.log.home.component.TableCard
import com.hand.log.home.contract.HomeState
import com.hand.log.home.contract.TableListItem
import handylog.core.res.generated.resources.Res
import handylog.core.res.generated.resources.plus
import org.jetbrains.compose.resources.painterResource
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
internal fun HomeScreen(
	homeState: HomeState.HomeData,
	onNavigateToTableDetail: (String) -> Unit,
	onFabClick: () -> Unit,
) {
	val colors = HandyTheme.colorScheme

	BaseScaffold(
		floatingActionButton = {
			FloatingActionButton(
				onClick = onFabClick,
				containerColor = colors.primary,
				contentColor = colors.onPrimary,
				shape = CircleShape,
			) {
				Icon(
					painter = painterResource(Res.drawable.plus),
					contentDescription = "새 테이블",
				)
			}
		},
	) {
		Column(
			modifier = Modifier.fillMaxSize(),
		) {
			// Header
			Column(
				modifier = Modifier
					.fillMaxWidth()
					.padding(horizontal = 16.dp, vertical = 16.dp),
			) {
				Text(
					text = "Poker Tracker",
					style = HandyTheme.typography.bold20,
					color = colors.textPrimary,
				)
				Text(
					text = "핸드 히스토리 기록",
					style = HandyTheme.typography.regular12,
					color = colors.textSecondary,
				)
			}

			if (homeState.tables.isEmpty()) {
				EmptyState(modifier = Modifier.fillMaxSize())
			} else {
				LazyColumn(
					modifier = Modifier.fillMaxSize(),
					verticalArrangement = Arrangement.spacedBy(12.dp),
					contentPadding = PaddingValues(
						horizontal = 16.dp,
						vertical = 8.dp,
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

@Preview
@Composable
private fun HomeScreenEmptyPreview() {
	HandLogTheme {
		HomeScreen(
			homeState = HomeState.HomeData(tables = emptyList()),
			onNavigateToTableDetail = {},
			onFabClick = {},
		)
	}
}

@Preview
@Composable
private fun HomeScreenWithDataPreview() {
	HandLogTheme {
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
			onFabClick = {},
		)
	}
}
