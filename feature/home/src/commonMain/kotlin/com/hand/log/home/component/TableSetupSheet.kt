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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.hand.log.ui.poker.SheetDragBlocker
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.painterResource
import com.hand.log.designsystem.component.HandyNumberSelector
import com.hand.log.designsystem.component.HandySectionLabel
import com.hand.log.designsystem.component.HandySwitch
import com.hand.log.designsystem.component.HandyTextField
import com.hand.log.designsystem.component.HandyToggleGroup
import com.hand.log.designsystem.component.RegularButton
import com.hand.log.designsystem.component.VerticalSpacer
import com.hand.log.designsystem.theme.HandLogTheme
import com.hand.log.designsystem.theme.HandyTheme
import com.hand.log.domain.model.Blinds
import com.hand.log.domain.model.GameType
import handylog.core.res.generated.resources.Res
import handylog.core.res.generated.resources.calendar
import handylog.core.res.generated.resources.map_pin
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.time.ExperimentalTime

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

	var date by remember { mutableStateOf("") }
	var location by remember { mutableStateOf("") }
	var gameType by remember { mutableStateOf(GameType.TOURNAMENT) }
	var startingStack by remember { mutableStateOf("") }
	var sbText by remember { mutableStateOf("") }
	var bbText by remember { mutableStateOf("") }
	var straddleEnabled by remember { mutableStateOf(false) }
	var straddleText by remember { mutableStateOf("") }
	var playerCount by remember { mutableIntStateOf(9) }
	var heroSeat by remember { mutableIntStateOf(1) }

	ModalBottomSheet(
		onDismissRequest = onDismissRequest,
		sheetState = sheetState,
		containerColor = colors.card,
		contentColor = colors.textPrimary,
	) {
		TableSetupContent(
			date = date,
			onDateChange = { date = it },
			location = location,
			onLocationChange = { location = it },
			gameType = gameType,
			onGameTypeChange = { gameType = it },
			startingStack = startingStack,
			onStartingStackChange = { startingStack = it },
			sbText = sbText,
			onSbChange = { sbText = it },
			bbText = bbText,
			onBbChange = { bbText = it },
			straddleEnabled = straddleEnabled,
			onStraddleEnabledChange = { straddleEnabled = it },
			straddleText = straddleText,
			onStraddleChange = { straddleText = it },
			playerCount = playerCount,
			onPlayerCountChange = { count ->
				playerCount = count
				if (heroSeat > count) heroSeat = 1
			},
			heroSeat = heroSeat,
			onHeroSeatChange = { heroSeat = it },
			onCreateClick = {
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
			isCreateEnabled = date.isNotBlank() && startingStack.isNotBlank(),
		)
	}
}

@OptIn(ExperimentalTime::class)
@Composable
internal fun TableSetupContent(
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
	playerCount: Int,
	onPlayerCountChange: (Int) -> Unit,
	heroSeat: Int,
	onHeroSeatChange: (Int) -> Unit,
	onCreateClick: () -> Unit,
	isCreateEnabled: Boolean,
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
		var showDatePicker by remember { mutableStateOf(false) }
		val datePickerState = rememberDatePickerState()

		Text(
			text = "새 테이블 생성",
			style = typography.bold20,
			color = colors.textPrimary,
		)
		VerticalSpacer(16.dp)

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
		if (gameType == GameType.CASH) {
			VerticalSpacer(16.dp)
			BlindsSection(
				sbText = sbText,
				onSbChange = onSbChange,
				bbText = bbText,
				onBbChange = onBbChange,
				straddleEnabled = straddleEnabled,
				onStraddleEnabledChange = onStraddleEnabledChange,
				straddleText = straddleText,
				onStraddleChange = onStraddleChange,
			)
		}

		VerticalSpacer(16.dp)
		HandySectionLabel("플레이어 수")
		HandyNumberSelector(
			range = 2..10,
			selected = playerCount,
			onSelect = onPlayerCountChange,
		)

		VerticalSpacer(16.dp)
		HandySectionLabel("내 좌석 번호")
		HandyNumberSelector(
			range = 1..playerCount,
			selected = heroSeat,
			onSelect = onHeroSeatChange,
			selectedColor = colors.gold,
			selectedContentColor = colors.card,
		)
		VerticalSpacer(16.dp)
		RegularButton(
			text = "테이블 생성",
			onClick = onCreateClick,
			enabled = isCreateEnabled,
			modifier = Modifier.fillMaxWidth(),
		)
	}
}

@Composable
private fun BlindsSection(
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

@Preview
@Composable
private fun BlindsSectionPreview() {
	HandLogTheme {
		BlindsSection(
			sbText = "1000",
			onSbChange = {},
			bbText = "2000",
			onBbChange = {},
			straddleEnabled = true,
			onStraddleEnabledChange = {},
			straddleText = "4000",
			onStraddleChange = {},
		)
	}
}

@Preview
@Composable
private fun TableSetupContentCashPreview() {
	HandLogTheme {
		TableSetupContent(
			date = "2026-03-13",
			onDateChange = {},
			location = "강남 홀덤펍",
			onLocationChange = {},
			gameType = GameType.CASH,
			onGameTypeChange = {},
			startingStack = "200000",
			onStartingStackChange = {},
			sbText = "1000",
			onSbChange = {},
			bbText = "2000",
			onBbChange = {},
			straddleEnabled = false,
			onStraddleEnabledChange = {},
			straddleText = "",
			onStraddleChange = {},
			playerCount = 9,
			onPlayerCountChange = {},
			heroSeat = 5,
			onHeroSeatChange = {},
			onCreateClick = {},
			isCreateEnabled = true,
		)
	}
}

@Preview
@Composable
private fun TableSetupContentTournamentPreview() {
	HandLogTheme {
		TableSetupContent(
			date = "2026-03-13",
			onDateChange = {},
			location = "WPT Korea",
			onLocationChange = {},
			gameType = GameType.TOURNAMENT,
			onGameTypeChange = {},
			startingStack = "50000",
			onStartingStackChange = {},
			sbText = "",
			onSbChange = {},
			bbText = "",
			onBbChange = {},
			straddleEnabled = false,
			onStraddleEnabledChange = {},
			straddleText = "",
			onStraddleChange = {},
			playerCount = 6,
			onPlayerCountChange = {},
			heroSeat = 3,
			onHeroSeatChange = {},
			onCreateClick = {},
			isCreateEnabled = true,
		)
	}
}

@Preview
@Composable
private fun TableSetupContentEmptyPreview() {
	HandLogTheme {
		TableSetupContent(
			date = "",
			onDateChange = {},
			location = "",
			onLocationChange = {},
			gameType = GameType.CASH,
			onGameTypeChange = {},
			startingStack = "",
			onStartingStackChange = {},
			sbText = "",
			onSbChange = {},
			bbText = "",
			onBbChange = {},
			straddleEnabled = false,
			onStraddleEnabledChange = {},
			straddleText = "",
			onStraddleChange = {},
			playerCount = 9,
			onPlayerCountChange = {},
			heroSeat = 1,
			onHeroSeatChange = {},
			onCreateClick = {},
			isCreateEnabled = false,
		)
	}
}
