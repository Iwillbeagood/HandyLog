package com.hand.log.players.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.hand.log.designsystem.component.HandySectionLabel
import com.hand.log.designsystem.component.HandyTextField
import com.hand.log.designsystem.component.VerticalSpacer
import com.hand.log.designsystem.component.modal.HandyBottomSheet
import com.hand.log.designsystem.etc.ThemePreview
import com.hand.log.designsystem.etc.ThemePreviews
import com.hand.log.designsystem.theme.HandyTheme
import com.hand.log.domain.model.PlayerTendency
import com.hand.log.ui.localizedLabel
import com.hand.log.domain.model.SavedPlayer
import handylog.core.res.generated.resources.Res
import handylog.core.res.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun PlayerEditSheet(
	player: SavedPlayer?,
	onSave: (SavedPlayer) -> Unit,
	onDelete: ((String) -> Unit)? = null,
	onDismiss: () -> Unit,
) {
	val colors = HandyTheme.colorScheme
	val isNew = player == null || player.id.isBlank()

	var name by remember { mutableStateOf(player?.name ?: "") }
	var tendency by remember { mutableStateOf(player?.tendency) }
	var memo by remember { mutableStateOf(player?.memo ?: "") }

	val canDelete = !isNew && onDelete != null

	HandyBottomSheet(
		onDismissRequest = onDismiss,
		title = if (isNew) stringResource(Res.string.player_add) else stringResource(Res.string.player_edit),
		confirmText = if (isNew) stringResource(Res.string.btn_add) else stringResource(Res.string.btn_save),
		onConfirm = {
			if (name.isNotBlank()) {
				onSave(
					SavedPlayer(
						id = player?.id ?: "",
						name = name.trim(),
						tendency = tendency,
						memo = memo.ifBlank { null },
						createdAt = player?.createdAt ?: 0L,
					),
				)
			}
		},
		confirmEnabled = name.isNotBlank(),
		subText = if (canDelete) stringResource(Res.string.btn_delete) else null,
		onSub = { if (canDelete) onDelete!!(player!!.id) },
		subContainerColor = colors.error.copy(alpha = 0.15f),
		subContentColor = colors.error,
	) {
		PlayerEditFields(
			name = name,
			onNameChange = { name = it },
			tendency = tendency,
			onTendencyChange = { tendency = it },
			memo = memo,
			onMemoChange = { memo = it },
		)
	}
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PlayerEditFields(
	name: String,
	onNameChange: (String) -> Unit,
	tendency: PlayerTendency?,
	onTendencyChange: (PlayerTendency?) -> Unit,
	memo: String,
	onMemoChange: (String) -> Unit,
) {
	val colors = HandyTheme.colorScheme

	HandyTextField(value = name, onValueChange = onNameChange, label = stringResource(Res.string.player_name))

	VerticalSpacer(12.dp)
	HandySectionLabel(stringResource(Res.string.player_tendency))
	FlowRow(
		horizontalArrangement = Arrangement.spacedBy(6.dp),
		verticalArrangement = Arrangement.spacedBy(6.dp),
	) {
		val options = listOf<PlayerTendency?>(null) + PlayerTendency.entries
		options.forEach { option ->
			val isSelected = option == tendency
			Box(
				modifier = Modifier
					.clip(RoundedCornerShape(8.dp))
					.background(if (isSelected) colors.primary else colors.muted)
					.clickable { onTendencyChange(option) }
					.padding(horizontal = 12.dp, vertical = 6.dp),
			) {
				Text(
					text = option?.localizedLabel() ?: stringResource(Res.string.player_tendency_none),
					style = HandyTheme.typography.medium12,
					color = if (isSelected) colors.onPrimary else colors.textSecondary,
				)
			}
		}
	}

	VerticalSpacer(12.dp)
	HandyTextField(value = memo, onValueChange = onMemoChange, label = stringResource(Res.string.player_memo))
}

@ThemePreviews
@Composable
private fun PlayerEditSheetNewPreview() {
	ThemePreview {
		PlayerEditSheet(
			player = null,
			onSave = {},
			onDismiss = {},
		)
	}
}

@ThemePreviews
@Composable
private fun PlayerEditSheetEditPreview() {
	ThemePreview {
		PlayerEditSheet(
			player = SavedPlayer(
				id = "1",
				name = "John",
				tendency = PlayerTendency.TIGHT,
				memo = "프리플랍 타이트",
			),
			onSave = {},
			onDelete = {},
			onDismiss = {},
		)
	}
}
