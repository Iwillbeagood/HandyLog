package com.hand.log.players.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import com.hand.log.designsystem.component.HandySectionLabel
import com.hand.log.designsystem.component.HandyTextField
import com.hand.log.designsystem.component.RegularButton
import com.hand.log.designsystem.component.VerticalSpacer
import com.hand.log.designsystem.theme.HandyTheme
import com.hand.log.domain.model.PlayerTendency
import com.hand.log.domain.model.SavedPlayer
import com.hand.log.ui.poker.SheetDragBlocker

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
internal fun PlayerEditSheet(
	player: SavedPlayer?,
	onSave: (SavedPlayer) -> Unit,
	onDelete: ((String) -> Unit)? = null,
	onDismiss: () -> Unit,
) {
	val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
	val colors = HandyTheme.colorScheme
	val isNew = player == null || player.id.isBlank()

	var name by remember { mutableStateOf(player?.name ?: "") }
	var tendency by remember { mutableStateOf(player?.tendency) }
	var memo by remember { mutableStateOf(player?.memo ?: "") }

	ModalBottomSheet(
		onDismissRequest = onDismiss,
		sheetState = sheetState,
		containerColor = colors.card,
		contentColor = colors.textPrimary,
	) {
		Column(
			modifier = Modifier
				.fillMaxWidth()
				.nestedScroll(SheetDragBlocker)
				.verticalScroll(rememberScrollState())
				.padding(horizontal = 20.dp)
				.padding(bottom = 32.dp),
		) {
			Text(
				text = if (isNew) "플레이어 추가" else "플레이어 수정",
				style = HandyTheme.typography.bold20,
				color = colors.textPrimary,
			)

			VerticalSpacer(16.dp)
			HandyTextField(
				value = name,
				onValueChange = { name = it },
				label = "이름",
			)

			VerticalSpacer(12.dp)
			HandySectionLabel("성향")
			FlowRow(
				horizontalArrangement = Arrangement.spacedBy(6.dp),
				verticalArrangement = Arrangement.spacedBy(6.dp),
			) {
				val options = listOf<PlayerTendency?>(null) + PlayerTendency.entries
				options.forEach { option ->
					val isSelected = option == tendency
					val label = option?.label ?: "없음"

					Box(
						modifier = Modifier
							.clip(RoundedCornerShape(8.dp))
							.background(if (isSelected) colors.primary else colors.muted)
							.clickable { tendency = option }
							.padding(horizontal = 12.dp, vertical = 6.dp),
					) {
						Text(
							text = label,
							style = HandyTheme.typography.medium12,
							color = if (isSelected) colors.onPrimary else colors.textSecondary,
						)
					}
				}
			}

			VerticalSpacer(12.dp)
			HandyTextField(
				value = memo,
				onValueChange = { memo = it },
				label = "메모",
			)

			VerticalSpacer(20.dp)
			RegularButton(
				text = if (isNew) "추가" else "저장",
				onClick = {
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
				enabled = name.isNotBlank(),
			)

			if (!isNew && onDelete != null && player != null) {
				VerticalSpacer(8.dp)
				RegularButton(
					text = "삭제",
					onClick = { onDelete(player.id) },
					containerColor = colors.error.copy(alpha = 0.15f),
					contentColor = colors.error,
				)
			}
		}
	}
}
