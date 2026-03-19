package com.hand.log.players

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.hand.log.designsystem.component.BaseScaffold
import com.hand.log.designsystem.component.HandyTopAppbar
import com.hand.log.designsystem.theme.HandyTheme
import com.hand.log.domain.model.PlayerTendency
import com.hand.log.domain.model.SavedPlayer
import com.hand.log.players.contract.PlayersState
import handylog.core.res.generated.resources.Res
import handylog.core.res.generated.resources.plus
import org.jetbrains.compose.resources.painterResource

@Composable
internal fun PlayersScreen(
	state: PlayersState,
	onPlayerClick: (SavedPlayer) -> Unit,
	onAddPlayer: () -> Unit,
) {
	val colors = HandyTheme.colorScheme

	BaseScaffold(
		topBar = {
			HandyTopAppbar(title = "플레이어 마킹")
		},
		floatingActionButton = {
			FloatingActionButton(
				onClick = onAddPlayer,
				containerColor = colors.primary,
				contentColor = colors.onPrimary,
			) {
				Icon(
					painter = painterResource(Res.drawable.plus),
					contentDescription = "플레이어 추가",
				)
			}
		},
	) {
		when (state) {
			PlayersState.Loading -> {}
			is PlayersState.Success -> {
				if (state.players.isEmpty()) {
					Box(
						modifier = Modifier
							.fillMaxSize()
							.padding(16.dp),
						contentAlignment = Alignment.Center,
					) {
						Text(
							text = "자주 만나는 플레이어를 마킹하세요",
							style = HandyTheme.typography.medium14,
							color = colors.textSecondary,
						)
					}
				} else {
					LazyColumn(
						modifier = Modifier
							.fillMaxSize()
							.padding(horizontal = 16.dp),
						verticalArrangement = Arrangement.spacedBy(8.dp),
					) {
						items(state.players, key = { it.id }) { player ->
							PlayerCard(
								player = player,
								onClick = { onPlayerClick(player) },
							)
						}
					}
				}
			}
		}
	}
}

@Composable
private fun PlayerCard(
	player: SavedPlayer,
	onClick: () -> Unit,
) {
	val colors = HandyTheme.colorScheme

	Row(
		modifier = Modifier
			.fillMaxWidth()
			.clip(RoundedCornerShape(12.dp))
			.background(colors.card)
			.clickable(onClick = onClick)
			.padding(16.dp),
		verticalAlignment = Alignment.CenterVertically,
		horizontalArrangement = Arrangement.spacedBy(12.dp),
	) {
		Box(
			modifier = Modifier
				.size(40.dp)
				.clip(CircleShape)
				.background(colors.primary.copy(alpha = 0.15f)),
			contentAlignment = Alignment.Center,
		) {
			Text(
				text = player.name.first().uppercase(),
				style = HandyTheme.typography.bold16,
				color = colors.primary,
			)
		}

		Column(modifier = Modifier.weight(1f)) {
			Text(
				text = player.name,
				style = HandyTheme.typography.bold14,
				color = colors.textPrimary,
			)

			player.tendency?.let { tendency ->
				TendencyBadge(tendency)
			}

			player.memo?.let { memo ->
				Text(
					text = memo,
					style = HandyTheme.typography.regular12,
					color = colors.textSecondary,
					maxLines = 1,
				)
			}
		}
	}
}

@Composable
private fun TendencyBadge(tendency: PlayerTendency) {
	val colors = HandyTheme.colorScheme

	Text(
		text = tendency.label,
		style = HandyTheme.typography.bold10,
		color = colors.primary,
		modifier = Modifier
			.clip(RoundedCornerShape(4.dp))
			.background(colors.primary.copy(alpha = 0.15f))
			.padding(horizontal = 6.dp, vertical = 2.dp),
	)
}
