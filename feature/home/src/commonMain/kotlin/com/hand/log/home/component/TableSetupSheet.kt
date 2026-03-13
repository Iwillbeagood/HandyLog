package com.hand.log.home.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.hand.log.designsystem.theme.HandyTheme
import com.hand.log.domain.model.Blinds
import com.hand.log.domain.model.GameType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TableSetupSheet(
	sheetState: SheetState,
	onDismissRequest: () -> Unit,
	onCreateTable: (
		date: String,
		location: String?,
		gameType: GameType,
		startingStack: Double,
		blinds: Blinds?,
		playerCount: Int,
		heroSeat: Int,
	) -> Unit,
) {
	val colors = HandyTheme.colorScheme
	val typography = HandyTheme.typography

	var date by remember { mutableStateOf("") }
	var location by remember { mutableStateOf("") }
	var gameType by remember { mutableStateOf(GameType.CASH) }
	var startingStack by remember { mutableStateOf("") }
	var sbText by remember { mutableStateOf("") }
	var bbText by remember { mutableStateOf("") }
	var straddleEnabled by remember { mutableStateOf(false) }
	var straddleText by remember { mutableStateOf("") }
	var playerCount by remember { mutableIntStateOf(9) }
	var heroSeat by remember { mutableIntStateOf(1) }

	val textFieldColors = OutlinedTextFieldDefaults.colors(
		focusedBorderColor = colors.primary,
		unfocusedBorderColor = colors.inputBorder,
		focusedTextColor = colors.textPrimary,
		unfocusedTextColor = colors.textPrimary,
		cursorColor = colors.primary,
		focusedLabelColor = colors.primary,
		unfocusedLabelColor = colors.textSecondary,
	)

	ModalBottomSheet(
		onDismissRequest = onDismissRequest,
		sheetState = sheetState,
		containerColor = colors.card,
		contentColor = colors.textPrimary,
	) {
		Column(
			modifier = Modifier
				.fillMaxWidth()
				.verticalScroll(rememberScrollState())
				.padding(horizontal = 20.dp)
				.padding(bottom = 32.dp),
			verticalArrangement = Arrangement.spacedBy(16.dp),
		) {
			Text(
				text = "새 테이블 생성",
				style = typography.bold20,
				color = colors.textPrimary,
			)

			// Date input
			OutlinedTextField(
				value = date,
				onValueChange = { date = it },
				label = { Text("날짜 (YYYY-MM-DD)") },
				modifier = Modifier.fillMaxWidth(),
				singleLine = true,
				colors = textFieldColors,
			)

			// Location input
			OutlinedTextField(
				value = location,
				onValueChange = { location = it },
				label = { Text("장소 (선택)") },
				modifier = Modifier.fillMaxWidth(),
				singleLine = true,
				colors = textFieldColors,
			)

			// Game type toggle
			SectionLabel("게임 유형")
			Row(
				modifier = Modifier.fillMaxWidth(),
				horizontalArrangement = Arrangement.spacedBy(8.dp),
			) {
				GameType.entries.forEach { type ->
					val isSelected = gameType == type
					Box(
						modifier = Modifier
							.weight(1f)
							.clip(RoundedCornerShape(8.dp))
							.background(if (isSelected) colors.primary else colors.muted)
							.clickable { gameType = type }
							.padding(vertical = 12.dp),
						contentAlignment = Alignment.Center,
					) {
						Text(
							text = type.label,
							style = typography.medium14,
							color = if (isSelected) colors.onPrimary else colors.textSecondary,
						)
					}
				}
			}

			// Starting stack
			OutlinedTextField(
				value = startingStack,
				onValueChange = { startingStack = it },
				label = { Text("시작 스택") },
				modifier = Modifier.fillMaxWidth(),
				singleLine = true,
				keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
				colors = textFieldColors,
			)

			// Blinds (Cash only)
			if (gameType == GameType.CASH) {
				SectionLabel("블라인드")
				Row(
					modifier = Modifier.fillMaxWidth(),
					horizontalArrangement = Arrangement.spacedBy(8.dp),
				) {
					OutlinedTextField(
						value = sbText,
						onValueChange = { sbText = it },
						label = { Text("SB") },
						modifier = Modifier.weight(1f),
						singleLine = true,
						keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
						colors = textFieldColors,
					)
					OutlinedTextField(
						value = bbText,
						onValueChange = { bbText = it },
						label = { Text("BB") },
						modifier = Modifier.weight(1f),
						singleLine = true,
						keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
						colors = textFieldColors,
					)
				}

				// Straddle toggle
				Row(
					modifier = Modifier.fillMaxWidth(),
					verticalAlignment = Alignment.CenterVertically,
				) {
					Text(
						text = "스트래들",
						style = typography.medium14,
						color = colors.textPrimary,
					)
					Spacer(modifier = Modifier.weight(1f))
					Switch(
						checked = straddleEnabled,
						onCheckedChange = { straddleEnabled = it },
						colors = SwitchDefaults.colors(
							checkedTrackColor = colors.primary,
							checkedThumbColor = colors.onPrimary,
						),
					)
				}

				if (straddleEnabled) {
					OutlinedTextField(
						value = straddleText,
						onValueChange = { straddleText = it },
						label = { Text("스트래들 금액") },
						modifier = Modifier.fillMaxWidth(),
						singleLine = true,
						keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
						colors = textFieldColors,
					)
				}
			}

			// Player count selector (2-10)
			SectionLabel("플레이어 수")
			SeatSelector(
				range = 2..10,
				selected = playerCount,
				onSelect = { count ->
					playerCount = count
					if (heroSeat > count) heroSeat = 1
				},
			)

			// Hero seat selector (1 ~ playerCount)
			SectionLabel("내 좌석 번호")
			SeatSelector(
				range = 1..playerCount,
				selected = heroSeat,
				onSelect = { heroSeat = it },
				useGold = true,
			)

			Spacer(modifier = Modifier.height(8.dp))

			// Create button
			Button(
				onClick = {
					val stack = startingStack.toDoubleOrNull() ?: 0.0
					val blinds = if (gameType == GameType.CASH) {
						val sb = sbText.toDoubleOrNull() ?: 0.0
						val bb = bbText.toDoubleOrNull() ?: 0.0
						val straddle = if (straddleEnabled) straddleText.toDoubleOrNull() else null
						Blinds(sb = sb, bb = bb, straddle = straddle)
					} else {
						null
					}
					onCreateTable(
						date,
						location.takeIf { it.isNotBlank() },
						gameType,
						stack,
						blinds,
						playerCount,
						heroSeat,
					)
				},
				modifier = Modifier
					.fillMaxWidth()
					.height(52.dp),
				enabled = date.isNotBlank() && startingStack.isNotBlank(),
				shape = RoundedCornerShape(12.dp),
				colors = ButtonDefaults.buttonColors(
					containerColor = colors.primary,
					contentColor = colors.onPrimary,
					disabledContainerColor = colors.muted,
					disabledContentColor = colors.textSecondary,
				),
			) {
				Text(
					text = "테이블 생성",
					style = typography.bold16,
				)
			}
		}
	}
}

@Composable
private fun SectionLabel(text: String) {
	Text(
		text = text,
		style = HandyTheme.typography.regular12,
		color = HandyTheme.colorScheme.textSecondary,
	)
}

@Composable
private fun SeatSelector(
	range: IntRange,
	selected: Int,
	onSelect: (Int) -> Unit,
	useGold: Boolean = false,
) {
	val colors = HandyTheme.colorScheme
	val typography = HandyTheme.typography
	val selectedBg = if (useGold) colors.gold else colors.primary
	val selectedFg = if (useGold) colors.card else colors.onPrimary

	Row(
		modifier = Modifier.fillMaxWidth(),
		horizontalArrangement = Arrangement.spacedBy(4.dp),
	) {
		range.forEach { number ->
			val isSelected = number == selected
			Box(
				modifier = Modifier
					.size(40.dp)
					.clip(RoundedCornerShape(8.dp))
					.background(if (isSelected) selectedBg else colors.muted)
					.border(
						width = 1.dp,
						color = if (isSelected) selectedBg else colors.inputBorder,
						shape = RoundedCornerShape(8.dp),
					)
					.clickable { onSelect(number) },
				contentAlignment = Alignment.Center,
			) {
				Text(
					text = "$number",
					style = typography.medium14,
					color = if (isSelected) selectedFg else colors.textPrimary,
				)
			}
		}
	}
}
