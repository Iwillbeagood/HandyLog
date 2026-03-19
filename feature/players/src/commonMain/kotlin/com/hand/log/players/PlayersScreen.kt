package com.hand.log.players

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hand.log.designsystem.component.BaseScaffold
import com.hand.log.designsystem.component.HandyHorizontalDivider
import com.hand.log.designsystem.component.HandyTopAppbar
import com.hand.log.designsystem.component.IconButton
import com.hand.log.designsystem.component.TopAppbarType
import com.hand.log.designsystem.etc.ThemePreview
import com.hand.log.designsystem.etc.ThemePreviews
import com.hand.log.domain.model.PlayerTendency
import com.hand.log.domain.model.SavedPlayer
import com.hand.log.players.component.PlayerCard
import com.hand.log.players.component.PlayersEmptyState
import com.hand.log.players.contract.PlayersState
import handylog.core.res.generated.resources.Res
import handylog.core.res.generated.resources.plus

@Composable
internal fun PlayersScreen(
	state: PlayersState,
	onPlayerClick: (SavedPlayer) -> Unit,
	onEditPlayer: (SavedPlayer) -> Unit,
	onDeletePlayer: (String) -> Unit,
	onAddPlayer: () -> Unit,
) {
	BaseScaffold {
		Column(
			modifier = Modifier.fillMaxSize(),
		) {
			HandyTopAppbar(
				title = "플레이어 마킹",
				navigationType = TopAppbarType.Main,
				iconButton = IconButton(
					text = "추가",
					icon = Res.drawable.plus,
					onClick = onAddPlayer,
				),
			)
			HandyHorizontalDivider()

			when (state) {
				PlayersState.Loading -> {}
				is PlayersState.Success -> {
					if (state.players.isEmpty()) {
						PlayersEmptyState(
							modifier = Modifier
								.fillMaxSize()
								.padding(16.dp),
						)
					} else {
						LazyColumn(
							modifier = Modifier.fillMaxSize(),
							verticalArrangement = Arrangement.spacedBy(8.dp),
							contentPadding = PaddingValues(
								start = 16.dp,
								end = 16.dp,
								top = 20.dp,
								bottom = 12.dp,
							),
						) {
							items(state.players, key = { it.id }) { player ->
								PlayerCard(
									player = player,
									onClick = { onPlayerClick(player) },
									onEdit = { onEditPlayer(player) },
									onDelete = { onDeletePlayer(player.id) },
								)
							}
						}
					}
				}
			}
		}
	}
}

@ThemePreviews
@Composable
private fun PlayersScreenEmptyPreview() {
	ThemePreview {
		PlayersScreen(
			state = PlayersState.Success(players = emptyList()),
			onPlayerClick = {},
			onEditPlayer = {},
			onDeletePlayer = {},
			onAddPlayer = {},
		)
	}
}

@ThemePreviews
@Composable
private fun PlayersScreenWithPlayersPreview() {
	ThemePreview {
		PlayersScreen(
			state = PlayersState.Success(
				players = listOf(
					SavedPlayer(id = "1", name = "John", tendency = PlayerTendency.TIGHT, memo = "프리플랍 타이트"),
					SavedPlayer(id = "2", name = "Mike", tendency = PlayerTendency.LOOSE),
					SavedPlayer(id = "3", name = "Phil", tendency = PlayerTendency.MANIAC, memo = "3벳 빈도 높음"),
					SavedPlayer(id = "4", name = "Dan"),
				),
			),
			onPlayerClick = {},
			onEditPlayer = {},
			onDeletePlayer = {},
			onAddPlayer = {},
		)
	}
}
