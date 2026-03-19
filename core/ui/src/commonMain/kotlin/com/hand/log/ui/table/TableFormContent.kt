package com.hand.log.ui.table

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
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
import com.hand.log.designsystem.component.HandySelector
import com.hand.log.designsystem.component.HandySectionLabel
import com.hand.log.designsystem.component.HandySwitch
import com.hand.log.designsystem.component.HandyTextField
import com.hand.log.designsystem.component.HandyToggleGroup
import com.hand.log.designsystem.component.VerticalSpacer
import com.hand.log.designsystem.theme.HandyTheme
import com.hand.log.domain.model.GameType
import handylog.core.res.generated.resources.Res
import handylog.core.res.generated.resources.calendar
import handylog.core.res.generated.resources.map_pin
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.painterResource
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class)
@Composable
fun TableFormFields(
	date: String,
	onDateChange: (String) -> Unit,
	location: String,
	onLocationChange: (String) -> Unit,
	gameType: GameType,
	onGameTypeChange: (GameType) -> Unit,
	startingStack: String,
	onStartingStackChange: (String) -> Unit,
	sbText: String,
	onSbChange: (String) -> Unit,
	bbText: String,
	onBbChange: (String) -> Unit,
	straddleEnabled: Boolean,
	onStraddleEnabledChange: (Boolean) -> Unit,
	straddleText: String,
	onStraddleChange: (String) -> Unit,
	bigBlindAnteEnabled: Boolean,
	onBigBlindAnteChange: (Boolean) -> Unit,
	playerCount: Int,
	onPlayerCountChange: (Int) -> Unit,
	heroSeat: Int,
	onHeroSeatChange: (Int) -> Unit,
) {
	val colors = HandyTheme.colorScheme
	val typography = HandyTheme.typography

	var showDatePicker by remember { mutableStateOf(false) }
	val datePickerState = rememberDatePickerState()

	// Date selector
	HandySectionLabel("날짜")
	Box(
		modifier = Modifier
			.fillMaxWidth()
			.clip(RoundedCornerShape(8.dp))
			.background(colors.muted, RoundedCornerShape(8.dp))
			.border(1.dp, colors.inputBorder, RoundedCornerShape(8.dp))
			.clickable { showDatePicker = true }
			.padding(horizontal = 12.dp, vertical = 10.dp),
	) {
		Row(verticalAlignment = Alignment.CenterVertically) {
			Icon(
				painter = painterResource(Res.drawable.calendar),
				contentDescription = null,
				modifier = Modifier.size(12.dp),
				tint = colors.textSecondary,
			)
			Spacer(modifier = Modifier.width(8.dp))
			Text(
				text = date.ifEmpty { "날짜를 선택하세요" },
				style = typography.regular14,
				color = if (date.isEmpty()) colors.textSecondary.copy(alpha = 0.5f) else colors.textPrimary,
			)
		}
	}

	if (showDatePicker) {
		DatePickerDialog(
			onDismissRequest = { showDatePicker = false },
			confirmButton = {
				TextButton(onClick = {
					datePickerState.selectedDateMillis?.let { millis ->
						val instant = kotlin.time.Instant.fromEpochMilliseconds(millis)
						val localDate = LocalDate.fromEpochDays(
							(instant.epochSeconds / 86400).toInt(),
						)
						onDateChange(localDate.toString())
					}
					showDatePicker = false
				}) {
					Text("확인")
				}
			},
			dismissButton = {
				TextButton(onClick = { showDatePicker = false }) {
					Text("취소")
				}
			},
		) {
			DatePicker(state = datePickerState)
		}
	}

	VerticalSpacer(16.dp)
	HandyTextField(
		value = location,
		onValueChange = onLocationChange,
		label = "장소 (선택)",
		leadingIcon = Res.drawable.map_pin,
	)

	VerticalSpacer(16.dp)
	HandySectionLabel("게임 유형")
	HandyToggleGroup(
		options = GameType.entries.toList(),
		selected = gameType,
		onSelect = onGameTypeChange,
		label = { it.label },
	)

	VerticalSpacer(16.dp)
	HandyTextField(
		value = startingStack,
		onValueChange = onStartingStackChange,
		label = "시작 스택",
		keyboardType = KeyboardType.Number,
	)

	VerticalSpacer(16.dp)
	if (gameType == GameType.CASH) {
		CashBlindsSection(
			sbText = sbText,
			onSbChange = onSbChange,
			bbText = bbText,
			onBbChange = onBbChange,
			straddleEnabled = straddleEnabled,
			onStraddleEnabledChange = onStraddleEnabledChange,
			straddleText = straddleText,
			onStraddleChange = onStraddleChange,
		)
	} else {
		TournamentBlindsSection(
			bigBlindAnteEnabled = bigBlindAnteEnabled,
			onBigBlindAnteChange = onBigBlindAnteChange,
		)
	}

	VerticalSpacer(16.dp)
	HandySectionLabel("플레이어 수")
	HandySelector(
		range = 2..10,
		selected = playerCount,
		onSelect = onPlayerCountChange,
	)

	VerticalSpacer(16.dp)
	HandySectionLabel("내 좌석 번호")
	HandySelector(
		range = 1..playerCount,
		selected = heroSeat,
		onSelect = onHeroSeatChange,
		selectedColor = colors.gold,
		selectedContentColor = colors.card,
	)
}

@Composable
private fun CashBlindsSection(
	sbText: String,
	onSbChange: (String) -> Unit,
	bbText: String,
	onBbChange: (String) -> Unit,
	straddleEnabled: Boolean,
	onStraddleEnabledChange: (Boolean) -> Unit,
	straddleText: String,
	onStraddleChange: (String) -> Unit,
) {
	val colors = HandyTheme.colorScheme
	val typography = HandyTheme.typography

	Column(
		modifier = Modifier
			.fillMaxWidth()
			.background(colors.muted, RoundedCornerShape(12.dp))
			.padding(12.dp),
		verticalArrangement = Arrangement.spacedBy(12.dp),
	) {
		HandySectionLabel("블라인드")
		Row(
			modifier = Modifier.fillMaxWidth(),
			horizontalArrangement = Arrangement.spacedBy(8.dp),
		) {
			HandyTextField(
				value = sbText,
				onValueChange = onSbChange,
				label = "SB",
				modifier = Modifier.weight(1f),
				keyboardType = KeyboardType.Number,
			)
			HandyTextField(
				value = bbText,
				onValueChange = onBbChange,
				label = "BB",
				modifier = Modifier.weight(1f),
				keyboardType = KeyboardType.Number,
			)
		}

		Row(
			modifier = Modifier.fillMaxWidth(),
			verticalAlignment = Alignment.CenterVertically,
		) {
			Text(
				text = "스트래들",
				style = typography.medium10,
				color = colors.textSecondary,
			)
			Spacer(modifier = Modifier.weight(1f))
			HandySwitch(
				checked = straddleEnabled,
				onCheckedChange = onStraddleEnabledChange,
			)
		}

		if (straddleEnabled) {
			HandyTextField(
				value = straddleText,
				onValueChange = onStraddleChange,
				label = "스트래들 금액",
				keyboardType = KeyboardType.Number,
			)
		}
	}
}

@Composable
private fun TournamentBlindsSection(
	bigBlindAnteEnabled: Boolean,
	onBigBlindAnteChange: (Boolean) -> Unit,
) {
	val colors = HandyTheme.colorScheme
	val typography = HandyTheme.typography

	Row(
		modifier = Modifier
			.fillMaxWidth()
			.background(colors.muted, RoundedCornerShape(12.dp))
			.padding(12.dp),
		verticalAlignment = Alignment.CenterVertically,
	) {
		Text(
			text = "빅블라인드 엔티",
			style = typography.medium10,
			color = colors.textSecondary,
		)
		Spacer(modifier = Modifier.weight(1f))
		HandySwitch(
			checked = bigBlindAnteEnabled,
			onCheckedChange = onBigBlindAnteChange,
		)
	}
}
