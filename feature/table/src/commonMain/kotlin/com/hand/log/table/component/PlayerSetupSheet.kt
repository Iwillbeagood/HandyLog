package com.hand.log.table.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.hand.log.designsystem.theme.HandyTheme
import com.hand.log.domain.model.Player
import com.hand.log.domain.model.PlayerTendency

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
internal fun PlayerSetupSheet(
	playerCount: Int,
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
	var selectedSeat by remember { mutableStateOf(1) }

	ModalBottomSheet(
		onDismissRequest = onDismiss,
		sheetState = sheetState,
		containerColor = colors.modalBackground,
	) {
		Column(
			modifier = Modifier
				.fillMaxWidth()
				.padding(horizontal = 16.dp)
				.verticalScroll(rememberScrollState()),
		) {
			Text(
				text = "플레이어 설정",
				style = HandyTheme.typography.bold18,
				color = colors.textPrimary,
				modifier = Modifier.padding(bottom = 16.dp),
			)

			// Seat selector
			Text(
				text = "좌석 선택",
				style = HandyTheme.typography.medium14,
				color = colors.textSecondary,
				modifier = Modifier.padding(bottom = 8.dp),
			)

			FlowRow(
				horizontalArrangement = Arrangement.spacedBy(8.dp),
				verticalArrangement = Arrangement.spacedBy(8.dp),
				modifier = Modifier.padding(bottom = 16.dp),
			) {
				(1..playerCount).forEach { seat ->
					val isSelected = seat == selectedSeat
					val hasPlayer = editingPlayers.find { it.seat == seat }?.name?.isNotBlank() == true

					Box(
						modifier = Modifier
							.size(40.dp)
							.clip(CircleShape)
							.background(
								when {
									isSelected -> colors.primary
									hasPlayer -> colors.accent.copy(alpha = 0.2f)
									else -> colors.muted
								},
							)
							.clickable { selectedSeat = seat },
						contentAlignment = Alignment.Center,
					) {
						Text(
							text = "$seat",
							color = if (isSelected) colors.onPrimary else colors.textPrimary,
							style = HandyTheme.typography.bold14,
						)
					}
				}
			}

			// Player form for selected seat
			val currentPlayer = editingPlayers.find { it.seat == selectedSeat }
				?: Player(seat = selectedSeat, stack = 0.0)

			Text(
				text = "Seat $selectedSeat",
				style = HandyTheme.typography.bold16,
				color = colors.textPrimary,
				modifier = Modifier.padding(bottom = 12.dp),
			)

			// Name
			OutlinedTextField(
				value = currentPlayer.name ?: "",
				onValueChange = { name ->
					editingPlayers = editingPlayers.map {
						if (it.seat == selectedSeat) it.copy(name = name.ifBlank { null }) else it
					}
				},
				label = { Text("이름", color = colors.textSecondary) },
				placeholder = { Text("플레이어 이름", color = colors.textSecondary) },
				modifier = Modifier.fillMaxWidth(),
				singleLine = true,
				colors = OutlinedTextFieldDefaults.colors(
					focusedBorderColor = colors.primary,
					unfocusedBorderColor = colors.inputBorder,
					cursorColor = colors.primary,
					focusedTextColor = colors.textPrimary,
					unfocusedTextColor = colors.textPrimary,
				),
			)

			Spacer(modifier = Modifier.height(8.dp))

			// Stack
			OutlinedTextField(
				value = if (currentPlayer.stack > 0) currentPlayer.stack.toLong().toString() else "",
				onValueChange = { stack ->
					val stackValue = stack.toDoubleOrNull() ?: 0.0
					editingPlayers = editingPlayers.map {
						if (it.seat == selectedSeat) it.copy(stack = stackValue) else it
					}
				},
				label = { Text("스택", color = colors.textSecondary) },
				placeholder = { Text("스택 금액", color = colors.textSecondary) },
				keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
				modifier = Modifier.fillMaxWidth(),
				singleLine = true,
				colors = OutlinedTextFieldDefaults.colors(
					focusedBorderColor = colors.primary,
					unfocusedBorderColor = colors.inputBorder,
					cursorColor = colors.primary,
					focusedTextColor = colors.textPrimary,
					unfocusedTextColor = colors.textPrimary,
				),
			)

			Spacer(modifier = Modifier.height(8.dp))

			// Tendency
			TendencySelector(
				selectedTendency = currentPlayer.tendency,
				onTendencySelected = { tendency ->
					editingPlayers = editingPlayers.map {
						if (it.seat == selectedSeat) it.copy(tendency = tendency) else it
					}
				},
			)

			Spacer(modifier = Modifier.height(8.dp))

			// Memo
			OutlinedTextField(
				value = currentPlayer.memo ?: "",
				onValueChange = { memo ->
					editingPlayers = editingPlayers.map {
						if (it.seat == selectedSeat) it.copy(memo = memo.ifBlank { null }) else it
					}
				},
				label = { Text("메모", color = colors.textSecondary) },
				placeholder = { Text("플레이어 메모", color = colors.textSecondary) },
				modifier = Modifier.fillMaxWidth(),
				minLines = 2,
				colors = OutlinedTextFieldDefaults.colors(
					focusedBorderColor = colors.primary,
					unfocusedBorderColor = colors.inputBorder,
					cursorColor = colors.primary,
					focusedTextColor = colors.textPrimary,
					unfocusedTextColor = colors.textPrimary,
				),
			)

			Spacer(modifier = Modifier.height(20.dp))

			// Save button
			Button(
				onClick = { onSave(editingPlayers) },
				modifier = Modifier
					.fillMaxWidth()
					.height(52.dp),
				colors = ButtonDefaults.buttonColors(
					containerColor = colors.primary,
					contentColor = colors.onPrimary,
				),
				shape = RoundedCornerShape(12.dp),
			) {
				Text(
					text = "저장",
					style = HandyTheme.typography.bold16,
				)
			}

			Spacer(modifier = Modifier.height(24.dp))
		}
	}
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TendencySelector(
	selectedTendency: PlayerTendency?,
	onTendencySelected: (PlayerTendency?) -> Unit,
) {
	val colors = HandyTheme.colorScheme

	Text(
		text = "성향",
		style = HandyTheme.typography.medium14,
		color = colors.textSecondary,
		modifier = Modifier.padding(bottom = 4.dp),
	)

	FlowRow(
		horizontalArrangement = Arrangement.spacedBy(6.dp),
		verticalArrangement = Arrangement.spacedBy(6.dp),
	) {
		PlayerTendency.entries.forEach { tendency ->
			val isSelected = tendency == selectedTendency
			Box(
				modifier = Modifier
					.clip(RoundedCornerShape(8.dp))
					.background(if (isSelected) colors.primary else colors.muted)
					.clickable {
						onTendencySelected(if (isSelected) null else tendency)
					}
					.padding(horizontal = 12.dp, vertical = 6.dp),
			) {
				Text(
					text = tendency.label,
					style = HandyTheme.typography.medium12,
					color = if (isSelected) colors.onPrimary else colors.textSecondary,
				)
			}
		}
	}
}
