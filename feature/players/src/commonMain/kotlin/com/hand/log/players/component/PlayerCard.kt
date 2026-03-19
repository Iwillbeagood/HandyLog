package com.hand.log.players.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.CircleShape
import com.hand.log.designsystem.component.HandyIconButton
import com.hand.log.designsystem.etc.clickableSingle
import com.hand.log.designsystem.etc.ThemePreview
import com.hand.log.designsystem.etc.ThemePreviews
import com.hand.log.designsystem.theme.HandyTheme
import com.hand.log.domain.model.PlayerTendency
import com.hand.log.domain.model.SavedPlayer
import com.hand.log.ui.color.tendencyColor
import handylog.core.res.generated.resources.Res
import handylog.core.res.generated.resources.delete
import handylog.core.res.generated.resources.pencil
import handylog.core.res.generated.resources.user_round
import org.jetbrains.compose.resources.painterResource

@Composable
internal fun PlayerCard(
	player: SavedPlayer,
	onClick: () -> Unit,
	onEdit: () -> Unit,
	onDelete: () -> Unit,
	modifier: Modifier = Modifier,
) {
	val colors = HandyTheme.colorScheme

	Row(
		modifier = modifier
			.fillMaxWidth()
			.clip(RoundedCornerShape(12.dp))
			.background(colors.card)
			.clickableSingle(onClick = onClick)
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
			Icon(
				painter = painterResource(Res.drawable.user_round),
				contentDescription = null,
				modifier = Modifier.size(20.dp),
				tint = colors.primary,
			)
		}

		Column(
			verticalArrangement = Arrangement.spacedBy(2.dp),
			modifier = Modifier.weight(1f),
		) {
			Row(
				horizontalArrangement = Arrangement.spacedBy(4.dp),
				verticalAlignment = Alignment.CenterVertically,
				modifier = Modifier.fillMaxWidth(),
			) {
				Text(
					text = player.name,
					style = HandyTheme.typography.bold14,
					color = colors.textPrimary,
				)
				player.tendency?.let { tendency ->
					TendencyBadge(tendency)
				}
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

		// 수정/삭제 아이콘
		Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
			HandyIconButton(
				icon = Res.drawable.pencil,
				onClick = onEdit,
				contentDescription = "수정",
				size = 28.dp,
				iconSize = 16.dp,
			)
			HandyIconButton(
				icon = Res.drawable.delete,
				onClick = onDelete,
				contentDescription = "삭제",
				tint = colors.textSecondary,
				size = 28.dp,
				iconSize = 16.dp,
			)
		}
	}
}

@Composable
private fun TendencyBadge(tendency: PlayerTendency) {
	val color = tendency.tendencyColor()

	Text(
		text = tendency.label,
		style = HandyTheme.typography.bold10,
		color = color,
		modifier = Modifier
			.clip(RoundedCornerShape(4.dp))
			.background(color.copy(alpha = 0.15f))
			.padding(horizontal = 6.dp, vertical = 2.dp),
	)
}

@ThemePreviews
@Composable
private fun PlayerCardPreview() {
	ThemePreview {
		PlayerCard(
			player = SavedPlayer(
				id = "1",
				name = "John",
				tendency = PlayerTendency.TIGHT,
				memo = "프리플랍 타이트, 포스트플랍 어그레시브",
			),
			onClick = {},
			onEdit = {},
			onDelete = {},
			modifier = Modifier.padding(16.dp),
		)
	}
}

@ThemePreviews
@Composable
private fun PlayerCardNoTendencyPreview() {
	ThemePreview {
		PlayerCard(
			player = SavedPlayer(
				id = "2",
				name = "Mike",
			),
			onClick = {},
			onEdit = {},
			onDelete = {},
			modifier = Modifier.padding(16.dp),
		)
	}
}

@ThemePreviews
@Composable
private fun PlayerCardManiacPreview() {
	ThemePreview {
		PlayerCard(
			player = SavedPlayer(
				id = "3",
				name = "Phil",
				tendency = PlayerTendency.MANIAC,
				memo = "3벳 빈도 높음, 블러프 많이 함",
			),
			onClick = {},
			onEdit = {},
			onDelete = {},
			modifier = Modifier.padding(16.dp),
		)
	}
}
