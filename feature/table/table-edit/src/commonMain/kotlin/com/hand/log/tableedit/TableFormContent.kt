package com.hand.log.tableedit

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
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalFocusManager
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
import com.hand.log.ui.localizedLabel
import handylog.core.res.generated.resources.Res
import handylog.core.res.generated.resources.*
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun TableFormFields(
	date: String,
	onDateClick: () -> Unit,
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
	val focusManager = LocalFocusManager.current

	// Date selector
	HandySectionLabel(stringResource(Res.string.table_form_date))
	Box(
		modifier = Modifier
			.fillMaxWidth()
			.clip(RoundedCornerShape(8.dp))
			.background(colors.muted, RoundedCornerShape(8.dp))
			.border(1.dp, colors.inputBorder, RoundedCornerShape(8.dp))
			.clickable {
				focusManager.clearFocus()
				onDateClick()
			}
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
				text = date.ifEmpty { stringResource(Res.string.table_form_date_placeholder) },
				style = typography.regular14,
				color = if (date.isEmpty()) colors.textSecondary.copy(alpha = 0.5f) else colors.textPrimary,
			)
		}
	}

	VerticalSpacer(16.dp)
	HandyTextField(
		value = location,
		onValueChange = onLocationChange,
		label = stringResource(Res.string.table_form_location),
		leadingIcon = Res.drawable.map_pin,
	)

	VerticalSpacer(16.dp)
	HandySectionLabel(stringResource(Res.string.table_form_game_type))
	HandyToggleGroup(
		options = GameType.entries.toList(),
		selected = gameType,
		onSelect = {
			focusManager.clearFocus()
			onGameTypeChange(it)
		},
		label = { it.localizedLabel() },
	)

	VerticalSpacer(16.dp)
	HandyTextField(
		value = startingStack,
		onValueChange = onStartingStackChange,
		label = stringResource(Res.string.table_form_starting_stack),
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
			onStraddleEnabledChange = {
				focusManager.clearFocus()
				onStraddleEnabledChange(it)
			},
			straddleText = straddleText,
			onStraddleChange = onStraddleChange,
		)
	} else {
		TournamentBlindsSection(
			bigBlindAnteEnabled = bigBlindAnteEnabled,
			onBigBlindAnteChange = {
				focusManager.clearFocus()
				onBigBlindAnteChange(it)
			},
		)
	}

	VerticalSpacer(16.dp)
	HandySectionLabel(stringResource(Res.string.table_form_player_count))
	HandySelector(
		range = 2..10,
		selected = playerCount,
		onSelect = {
			focusManager.clearFocus()
			onPlayerCountChange(it)
		},
	)

	VerticalSpacer(16.dp)
	HandySectionLabel(stringResource(Res.string.table_form_hero_seat))
	HandySelector(
		range = 1..maxOf(playerCount, 9),
		selected = heroSeat,
		onSelect = {
			focusManager.clearFocus()
			onHeroSeatChange(it)
		},
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
		HandySectionLabel(stringResource(Res.string.table_form_blinds))
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
				text = stringResource(Res.string.table_form_straddle),
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
				label = stringResource(Res.string.table_form_straddle_amount),
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
			text = stringResource(Res.string.table_form_bb_ante),
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
