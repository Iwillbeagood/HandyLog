package com.hand.log.table.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.hand.log.designsystem.component.HandySectionLabel
import com.hand.log.designsystem.component.HandyTextField
import com.hand.log.designsystem.component.RegularButton
import com.hand.log.designsystem.component.VerticalSpacer
import com.hand.log.designsystem.theme.HandyTheme
import com.hand.log.domain.model.Player
import com.hand.log.domain.model.PlayerTendency
import com.hand.log.ui.poker.SheetDragBlocker
import com.hand.log.designsystem.etc.ThemePreview
import com.hand.log.designsystem.etc.ThemePreviews

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PlayerSetupSheet(
	initialSeat: Int,
	isHero: Boolean,
	playerCount: Int,
	startingStack: Double,
	players: List<Player>,
	onSave: (List<Player>) -> Unit,
	onDismiss: () -> Unit,
) {
	val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
	val colors = HandyTheme.colorScheme

	var editingPlayers by remember {
		mutableStateOf(
			(1..playerCount).map { seat ->
				players.find { it.seat == seat } ?: Player(seat = seat, stack = 0.0)
			},
		)
	}

	val currentPlayer = editingPlayers.find { it.seat == initialSeat }
		?: Player(seat = initialSeat, stack = 0.0)

	ModalBottomSheet(
		onDismissRequest = onDismiss,
		sheetState = sheetState,
		containerColor = colors.card,
		contentColor = colors.textPrimary,
	) {
		val onReset = {
			val resetPlayers = editingPlayers.map {
				if (it.seat == initialSeat) {
					Player(
						seat = initialSeat,
						stack = startingStack,
					)
				} else {
					it
				}
			}
			onSave(resetPlayers)
		}

		if (isHero) {
			HeroSetupContent(
				playerStack = if (currentPlayer.stack > 0) currentPlayer.stack.toLong().toString() else "",
				onStackChange = { stack ->
					val stackValue = stack.toDoubleOrNull() ?: 0.0
					editingPlayers = editingPlayers.map {
						if (it.seat == initialSeat) it.copy(stack = stackValue) else it
					}
				},
				onResetClick = onReset,
				onSaveClick = { onSave(editingPlayers) },
			)
		} else {
			PlayerSetupContent(
				seatNumber = initialSeat,
				playerName = currentPlayer.name ?: "",
				onNameChange = { name ->
					editingPlayers = editingPlayers.map {
						if (it.seat == initialSeat) it.copy(name = name.ifBlank { null }) else it
					}
				},
				playerStack = if (currentPlayer.stack > 0) currentPlayer.stack.toLong().toString() else "",
				onStackChange = { stack ->
					val stackValue = stack.toDoubleOrNull() ?: 0.0
					editingPlayers = editingPlayers.map {
						if (it.seat == initialSeat) it.copy(stack = stackValue) else it
					}
				},
				selectedTendency = currentPlayer.tendency,
				onTendencyChange = { tendency ->
					editingPlayers = editingPlayers.map {
						if (it.seat == initialSeat) it.copy(tendency = tendency) else it
					}
				},
				playerMemo = currentPlayer.memo ?: "",
				onMemoChange = { memo ->
					editingPlayers = editingPlayers.map {
						if (it.seat == initialSeat) it.copy(memo = memo.ifBlank { null }) else it
					}
				},
				onResetClick = onReset,
				onSaveClick = { onSave(editingPlayers) },
			)
		}
	}
}

@Composable
private fun HeroSetupContent(
	playerStack: String,
	onStackChange: (String) -> Unit,
	onResetClick: () -> Unit,
	onSaveClick: () -> Unit,
) {
	val colors = HandyTheme.colorScheme
	val typography = HandyTheme.typography

	Column(
		modifier = Modifier
			.fillMaxWidth()
			.nestedScroll(SheetDragBlocker)
			.verticalScroll(rememberScrollState())
			.padding(horizontal = 20.dp)
			.padding(bottom = 32.dp),
	) {
		Text(
			text = "Hero",
			style = typography.bold20,
			color = colors.gold,
		)

		VerticalSpacer(16.dp)
		HandyTextField(
			value = playerStack,
			onValueChange = onStackChange,
			label = "스택",
			keyboardType = KeyboardType.Number,
		)

		VerticalSpacer(20.dp)
		Row(
			modifier = Modifier.fillMaxWidth(),
			horizontalArrangement = Arrangement.spacedBy(8.dp),
		) {
			RegularButton(
				text = "초기화",
				onClick = onResetClick,
				containerColor = colors.muted,
				contentColor = colors.textSecondary,
				modifier = Modifier.weight(1f),
			)
			RegularButton(
				text = "저장",
				onClick = onSaveClick,
				modifier = Modifier.weight(1f),
			)
		}
	}
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun PlayerSetupContent(
	seatNumber: Int,
	playerName: String,
	onNameChange: (String) -> Unit,
	playerStack: String,
	onStackChange: (String) -> Unit,
	selectedTendency: PlayerTendency?,
	onTendencyChange: (PlayerTendency?) -> Unit,
	playerMemo: String,
	onMemoChange: (String) -> Unit,
	onResetClick: () -> Unit,
	onSaveClick: () -> Unit,
) {
	val colors = HandyTheme.colorScheme
	val typography = HandyTheme.typography

	Column(
		modifier = Modifier
			.fillMaxWidth()
			.nestedScroll(SheetDragBlocker)
			.verticalScroll(rememberScrollState())
			.padding(horizontal = 20.dp)
			.padding(bottom = 32.dp),
	) {
		Text(
			text = "Seat $seatNumber",
			style = typography.bold20,
			color = colors.textPrimary,
		)

		VerticalSpacer(16.dp)
		HandyTextField(
			value = playerName,
			onValueChange = onNameChange,
			label = "이름",
		)

		VerticalSpacer(12.dp)
		HandyTextField(
			value = playerStack,
			onValueChange = onStackChange,
			label = "스택",
			keyboardType = KeyboardType.Number,
		)

		VerticalSpacer(12.dp)
		HandySectionLabel("성향")
		FlowRow(
			horizontalArrangement = Arrangement.spacedBy(6.dp),
			verticalArrangement = Arrangement.spacedBy(6.dp),
		) {
			val tendencyOptions = listOf<PlayerTendency?>(null) + PlayerTendency.entries
			tendencyOptions.forEach { tendency ->
				val isSelected = tendency == selectedTendency
				val label = tendency?.label ?: "없음"

				HandyToggleChip(
					text = label,
					isSelected = isSelected,
					onClick = { onTendencyChange(tendency) },
				)
			}
		}

		VerticalSpacer(12.dp)
		HandyTextField(
			value = playerMemo,
			onValueChange = onMemoChange,
			label = "메모",
		)

		VerticalSpacer(20.dp)
		Row(
			modifier = Modifier.fillMaxWidth(),
			horizontalArrangement = Arrangement.spacedBy(8.dp),
		) {
			RegularButton(
				text = "초기화",
				onClick = onResetClick,
				containerColor = colors.muted,
				contentColor = colors.textSecondary,
				modifier = Modifier.weight(1f),
			)
			RegularButton(
				text = "저장",
				onClick = onSaveClick,
				modifier = Modifier.weight(1f),
			)
		}
	}
}

@Composable
private fun HandyToggleChip(
	text: String,
	isSelected: Boolean,
	onClick: () -> Unit,
) {
	val colors = HandyTheme.colorScheme
	val bgColor = if (isSelected) colors.primary else colors.muted
	val fgColor = if (isSelected) colors.onPrimary else colors.textSecondary

	Box(
		modifier = Modifier
			.clip(RoundedCornerShape(8.dp))
			.background(bgColor, RoundedCornerShape(8.dp))
			.clickable(onClick = onClick)
			.padding(horizontal = 12.dp, vertical = 6.dp),
	) {
		Text(
			text = text,
			style = HandyTheme.typography.medium12,
			color = fgColor,
		)
	}
}

@ThemePreviews
@Composable
private fun PlayerSetupContentPreview() {
	ThemePreview {
		PlayerSetupContent(
			seatNumber = 3,
			playerName = "Hero",
			onNameChange = {},
			playerStack = "62000",
			onStackChange = {},
			selectedTendency = PlayerTendency.NIT,
			onTendencyChange = {},
			playerMemo = "타이트하게 플레이",
			onMemoChange = {},
			onResetClick = {},
			onSaveClick = {},
		)
	}
}

@ThemePreviews
@Composable
private fun PlayerSetupContentEmptyPreview() {
	ThemePreview {
		PlayerSetupContent(
			seatNumber = 1,
			playerName = "",
			onNameChange = {},
			playerStack = "",
			onStackChange = {},
			selectedTendency = null,
			onTendencyChange = {},
			playerMemo = "",
			onMemoChange = {},
			onResetClick = {},
			onSaveClick = {},
		)
	}
}
