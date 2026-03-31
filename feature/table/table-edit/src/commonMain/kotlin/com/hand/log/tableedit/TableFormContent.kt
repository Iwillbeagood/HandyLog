package com.hand.log.tableedit

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.hand.log.designsystem.component.HandyCheckBox
import com.hand.log.designsystem.component.HandySelector
import com.hand.log.designsystem.etc.ThemePreview
import com.hand.log.designsystem.etc.ThemePreviews
import com.hand.log.designsystem.component.HandySectionLabel
import com.hand.log.designsystem.component.HandySwitch
import com.hand.log.designsystem.component.HandyTextField
import com.hand.log.designsystem.component.HandyToggleGroup
import com.hand.log.designsystem.component.VerticalSpacer
import com.hand.log.designsystem.theme.HandyTheme
import handylog.core.res.generated.resources.Res
import handylog.core.res.generated.resources.*
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun ColumnScope.TableFormFields(
	date: String,
	onDateClick: () -> Unit,
	location: String,
	onLocationChange: (String) -> Unit,
	isCash: Boolean,
	onIsCashChange: (Boolean) -> Unit,
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
	maxPlayers: Int,
	onMaxPlayersChange: (Int) -> Unit,
	playerCount: Int,
	onPlayerCountChange: (Int) -> Unit,
	heroSeat: Int,
	onHeroSeatChange: (Int) -> Unit,
	isEditMode: Boolean = false,
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
		options = listOf(false, true),
		selected = isCash,
		onSelect = {
			focusManager.clearFocus()
			onIsCashChange(it)
		},
		label = {
			if (it) {
				stringResource(Res.string.game_type_cash)
			} else {
				stringResource(Res.string.game_type_tournament)
			}
		},
	)

	if (isCash) {
		VerticalSpacer(16.dp)
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
		VerticalSpacer(8.dp)
		HandyCheckBox(
			text = stringResource(Res.string.table_form_bb_ante),
			checked = bigBlindAnteEnabled,
			onCheckedChange = {
				focusManager.clearFocus()
				onBigBlindAnteChange(it)

			},
			modifier = Modifier.align(Alignment.End),
		)
	}

	VerticalSpacer(16.dp)
	HandySectionLabel(stringResource(Res.string.table_form_max_players))
	HandySelector(
		range = 6..10,
		selected = maxPlayers,
		onSelect = {
			focusManager.clearFocus()
			onMaxPlayersChange(it)
		},
	)

	if (!isEditMode) {
		VerticalSpacer(16.dp)
		HandySectionLabel(stringResource(Res.string.table_form_player_count))
		HandySelector(
			range = 2..maxPlayers,
			selected = playerCount,
			onSelect = {
				focusManager.clearFocus()
				onPlayerCountChange(it)
			},
		)

		VerticalSpacer(16.dp)
		HandySectionLabel(stringResource(Res.string.table_form_hero_seat))
		HandySelector(
			range = 1..maxPlayers,
			selected = heroSeat,
			onSelect = {
				focusManager.clearFocus()
				onHeroSeatChange(it)
			},
			selectedColor = colors.gold,
			selectedContentColor = colors.card,
		)
	}
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

@ThemePreviews
@Composable
private fun TableFormFieldsCashPreview() {
	ThemePreview {
		Column(
			modifier = Modifier
				.verticalScroll(rememberScrollState())
				.padding(16.dp),
		) {
			TableFormFields(
				date = "2026-03-25",
				onDateClick = {},
				location = "강남 홀덤펍",
				onLocationChange = {},
				isCash = true,
				onIsCashChange = {},
				sbText = "500",
				onSbChange = {},
				bbText = "1000",
				onBbChange = {},
				straddleEnabled = true,
				onStraddleEnabledChange = {},
				straddleText = "2000",
				onStraddleChange = {},
				bigBlindAnteEnabled = false,
				onBigBlindAnteChange = {},
				maxPlayers = 9,
				onMaxPlayersChange = {},
				playerCount = 9,
				onPlayerCountChange = {},
				heroSeat = 3,
				onHeroSeatChange = {},
			)
		}
	}
}

@ThemePreviews
@Composable
private fun TableFormFieldsTournamentPreview() {
	ThemePreview {
		Column(
			modifier = Modifier
				.verticalScroll(rememberScrollState())
				.padding(16.dp),
		) {
			TableFormFields(
				date = "2026-03-25",
				onDateClick = {},
				location = "",
				onLocationChange = {},
				isCash = false,
				onIsCashChange = {},
				sbText = "",
				onSbChange = {},
				bbText = "",
				onBbChange = {},
				straddleEnabled = false,
				onStraddleEnabledChange = {},
				straddleText = "",
				onStraddleChange = {},
				bigBlindAnteEnabled = true,
				onBigBlindAnteChange = {},
				playerCount = 6,
				maxPlayers = 6,
				onMaxPlayersChange = {},
				onPlayerCountChange = {},
				heroSeat = 1,
				onHeroSeatChange = {},
			)
		}
	}
}
