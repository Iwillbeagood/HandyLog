package com.hand.log.record.quick.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hand.log.designsystem.etc.ThemePreview
import com.hand.log.designsystem.etc.ThemePreviews
import com.hand.log.designsystem.theme.HandyTheme
import com.hand.log.domain.model.ActionType
import com.hand.log.domain.model.Suit
import com.hand.log.record.quick.contract.StreetCloseTarget
import com.hand.log.record.quick.model.HandKey
import com.hand.log.ui.poker.iconRes
import com.hand.log.record.quick.model.HandKeyboardLayout
import com.hand.log.record.quick.model.label
import com.hand.log.record.quick.model.notation
import com.hand.log.record.quick.model.span
import handylog.core.res.generated.resources.Res
import handylog.core.res.generated.resources.backspace
import handylog.core.res.generated.resources.btn_undo
import handylog.core.res.generated.resources.corner_down_left
import handylog.core.res.generated.resources.crown
import handylog.core.res.generated.resources.grid_3x3
import handylog.core.res.generated.resources.quick_record_key_card_picker
import handylog.core.res.generated.resources.quick_record_close_hero
import handylog.core.res.generated.resources.quick_record_key_next_line
import handylog.core.res.generated.resources.quick_record_key_unit_chip
import handylog.core.res.generated.resources.street_flop
import handylog.core.res.generated.resources.street_river
import handylog.core.res.generated.resources.street_turn
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun HandKeyboard(
	positionRows: List<List<HandKey>>,
	enabledKeys: Set<HandKey>,
	actorKey: HandKey?,
	heroKey: HandKey?,
	amountInChips: Boolean,
	onKeyClick: (HandKey) -> Unit,
	modifier: Modifier = Modifier,
	streetCloseTarget: StreetCloseTarget? = null,
	sizedAsMultiple: Boolean = false,
) {
	Column(
		modifier = modifier
			.fillMaxWidth()
			.background(HandyTheme.colorScheme.card)
			.padding(6.dp),
		verticalArrangement = Arrangement.spacedBy(6.dp),
	) {
		KeyPanel {
			positionRows.forEach { row ->
				KeyRow(
					keys = row,
					slots = HandKeyboardLayout.POSITION_ROW_SIZE,
					enabledKeys = enabledKeys,
					highlightedKey = actorKey,
					heroKey = heroKey,
					onKeyClick = onKeyClick,
				)
			}
		}
		KeyPanel {
			Row(
				modifier = Modifier.height(IntrinsicSize.Min),
				horizontalArrangement = Arrangement.spacedBy(8.dp),
			) {
				KeyGrid(
					rows = HandKeyboardLayout.actionRows,
					enabledKeys = enabledKeys,
					amountInChips = amountInChips,
					sizedAsMultiple = sizedAsMultiple,
					onKeyClick = onKeyClick,
					modifier = Modifier.weight(2f),
				)
				KeyGrid(
					rows = HandKeyboardLayout.padRows,
					enabledKeys = enabledKeys,
					streetCloseTarget = streetCloseTarget,
					onKeyClick = onKeyClick,
					modifier = Modifier.weight(3f),
				)
			}
		}
	}
}

@Composable
private fun KeyGrid(
	rows: List<List<HandKey>>,
	enabledKeys: Set<HandKey>,
	onKeyClick: (HandKey) -> Unit,
	modifier: Modifier = Modifier,
	amountInChips: Boolean = false,
	streetCloseTarget: StreetCloseTarget? = null,
	sizedAsMultiple: Boolean = false,
) {
	Column(
		modifier = modifier.fillMaxHeight(),
		verticalArrangement = Arrangement.spacedBy(4.dp),
	) {
		rows.forEach { row ->
			KeyRow(
				keys = row,
				slots = row.sumOf { it.span },
				enabledKeys = enabledKeys,
				amountInChips = amountInChips,
				streetCloseTarget = streetCloseTarget,
				sizedAsMultiple = sizedAsMultiple,
				onKeyClick = onKeyClick,
				modifier = Modifier.weight(1f),
			)
		}
	}
}

@Composable
private fun KeyPanel(content: @Composable ColumnScope.() -> Unit) {
	Column(
		modifier = Modifier
			.fillMaxWidth()
			.clip(RoundedCornerShape(10.dp))
			.background(HandyTheme.colorScheme.background)
			.padding(4.dp),
		verticalArrangement = Arrangement.spacedBy(4.dp),
		content = content,
	)
}

@Composable
private fun KeyRow(
	keys: List<HandKey>,
	slots: Int,
	enabledKeys: Set<HandKey>,
	onKeyClick: (HandKey) -> Unit,
	modifier: Modifier = Modifier,
	highlightedKey: HandKey? = null,
	heroKey: HandKey? = null,
	amountInChips: Boolean = false,
	streetCloseTarget: StreetCloseTarget? = null,
	sizedAsMultiple: Boolean = false,
) {
	Row(
		modifier = modifier
			.fillMaxWidth()
			.height(IntrinsicSize.Min),
		horizontalArrangement = Arrangement.spacedBy(4.dp),
	) {
		keys.forEach { key ->
			val keyModifier = Modifier
				.weight(key.span.toFloat())
				.fillMaxHeight()
			if (key == HandKey.NextLine && streetCloseTarget != null) {
				StreetCloseKey(
					target = streetCloseTarget,
					onClick = { onKeyClick(key) },
					modifier = keyModifier,
				)
			} else if (key == HandKey.AmountUnit) {
				AmountUnitSwitch(
					amountInChips = amountInChips,
					enabled = key in enabledKeys,
					onClick = { onKeyClick(key) },
					modifier = keyModifier,
				)
			} else {
				KeyButton(
					key = key,
					enabled = key in enabledKeys,
					highlighted = key == highlightedKey,
					isHero = key == heroKey,
					sizedAsMultiple = sizedAsMultiple,
					onClick = { onKeyClick(key) },
					modifier = keyModifier,
				)
			}
		}
		repeat(slots - keys.sumOf { it.span }) {
			Spacer(modifier = Modifier.weight(1f))
		}
	}
}

@Composable
private fun KeyButton(
	key: HandKey,
	enabled: Boolean,
	highlighted: Boolean,
	onClick: () -> Unit,
	modifier: Modifier = Modifier,
	isHero: Boolean = false,
	sizedAsMultiple: Boolean = false,
) {
	val colors = HandyTheme.colorScheme
	val shape = RoundedCornerShape(8.dp)
	Box(
		modifier = modifier
			.alpha(if (enabled) 1f else 0.3f)
			.clip(shape)
			.background(colors.secondary)
			.then(if (highlighted) Modifier.border(1.5.dp, colors.primary, shape) else Modifier)
			.clickable(enabled = enabled, onClick = onClick)
			.padding(vertical = 7.dp),
		contentAlignment = Alignment.Center,
	) {
		if (isHero) {
			Icon(
				painter = painterResource(Res.drawable.crown),
				contentDescription = null,
				tint = colors.gold,
				modifier = Modifier
					.align(Alignment.TopEnd)
					.offset(x = (-4).dp, y = (-4).dp)
					.size(8.dp),
			)
		}
		when (key) {
			HandKey.NextLine -> Icon(
				painter = painterResource(Res.drawable.corner_down_left),
				contentDescription = stringResource(Res.string.quick_record_key_next_line),
				tint = colors.textPrimary,
				modifier = Modifier.size(18.dp),
			)
			HandKey.CardPicker -> Icon(
				painter = painterResource(Res.drawable.grid_3x3),
				contentDescription = stringResource(Res.string.quick_record_key_card_picker),
				tint = colors.textPrimary,
				modifier = Modifier.size(18.dp),
			)
			HandKey.Backspace -> Icon(
				painter = painterResource(Res.drawable.backspace),
				contentDescription = stringResource(Res.string.btn_undo),
				tint = colors.textPrimary,
				modifier = Modifier.size(18.dp),
			)
			is HandKey.SuitKey -> Icon(
				painter = painterResource(key.suit.iconRes()),
				contentDescription = key.suit.symbol,
				tint = if (key.suit.isRed) colors.suitRed else colors.suitBlack,
				modifier = Modifier.size(16.dp),
			)
			else -> Text(
				text = key.label(sizedAsMultiple),
				style = HandyTheme.typography.medium14,
				color = when {
					isHero -> colors.gold
					highlighted -> colors.primary
					key is HandKey.Act && key.type.isAggressive -> colors.accent
					key is HandKey.Act && key.type == ActionType.FOLD -> colors.textSecondary
					else -> colors.textPrimary
				},
				textAlign = TextAlign.Center,
				maxLines = 1,
			)
		}
	}
}

private val StreetCloseTarget.labelRes: StringResource
	get() = when (this) {
		StreetCloseTarget.FLOP -> Res.string.street_flop
		StreetCloseTarget.TURN -> Res.string.street_turn
		StreetCloseTarget.RIVER -> Res.string.street_river
		StreetCloseTarget.HERO -> Res.string.quick_record_close_hero
	}

@Composable
private fun StreetCloseKey(
	target: StreetCloseTarget,
	onClick: () -> Unit,
	modifier: Modifier = Modifier,
) {
	val colors = HandyTheme.colorScheme
	Row(
		modifier = modifier
			.clip(RoundedCornerShape(8.dp))
			.background(colors.primary)
			.clickable(onClick = onClick)
			.padding(vertical = 7.dp),
		horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
		verticalAlignment = Alignment.CenterVertically,
	) {
		Icon(
			painter = painterResource(Res.drawable.corner_down_left),
			contentDescription = null,
			tint = colors.onPrimary,
			modifier = Modifier.size(18.dp),
		)
		Text(
			text = stringResource(target.labelRes),
			style = HandyTheme.typography.medium14,
			color = colors.onPrimary,
			maxLines = 1,
		)
	}
}

@Composable
private fun AmountUnitSwitch(
	amountInChips: Boolean,
	enabled: Boolean,
	onClick: () -> Unit,
	modifier: Modifier = Modifier,
) {
	Row(
		modifier = modifier
			.alpha(if (enabled) 1f else 0.3f)
			.clip(RoundedCornerShape(8.dp))
			.background(HandyTheme.colorScheme.secondary)
			.clickable(enabled = enabled, onClick = onClick)
			.padding(3.dp),
	) {
		AmountUnitSegment(
			text = HandKey.AmountUnit.notation,
			selected = !amountInChips,
			modifier = Modifier.weight(1f),
		)
		AmountUnitSegment(
			text = stringResource(Res.string.quick_record_key_unit_chip),
			selected = amountInChips,
			modifier = Modifier.weight(1f),
		)
	}
}

@Composable
private fun AmountUnitSegment(
	text: String,
	selected: Boolean,
	modifier: Modifier = Modifier,
) {
	val colors = HandyTheme.colorScheme
	Text(
		text = text,
		style = HandyTheme.typography.medium14,
		color = if (selected) colors.onPrimary else colors.textSecondary,
		textAlign = TextAlign.Center,
		maxLines = 1,
		modifier = modifier
			.clip(RoundedCornerShape(6.dp))
			.then(if (selected) Modifier.background(colors.primary) else Modifier)
			.padding(vertical = 4.dp),
	)
}

internal val Suit.isRed: Boolean
	get() = this == Suit.HEARTS || this == Suit.DIAMONDS

private val ActionType.isAggressive: Boolean
	get() = this == ActionType.BET || this == ActionType.RAISE || this == ActionType.ALL_IN

@ThemePreviews
@Composable
private fun HandKeyboardActionPreview() {
	val state = QuickRecordPreviewData.state(QuickRecordPreviewData.actionInput)
	ThemePreview {
		HandKeyboard(
			positionRows = state.positionRows,
			enabledKeys = state.enabledKeys,
			actorKey = state.actorKey,
			heroKey = state.heroKey,
			amountInChips = state.amountInChips,
			onKeyClick = {},
		)
	}
}

@ThemePreviews
@Composable
private fun HandKeyboardStreetClosePreview() {
	val state = QuickRecordPreviewData.streetCloseState
	ThemePreview {
		HandKeyboard(
			positionRows = state.positionRows,
			enabledKeys = state.enabledKeys,
			actorKey = state.actorKey,
			heroKey = state.heroKey,
			amountInChips = state.amountInChips,
			streetCloseTarget = state.streetCloseTarget,
			onKeyClick = {},
		)
	}
}

@ThemePreviews
@Composable
private fun HandKeyboardCardPreview() {
	val state = QuickRecordPreviewData.state(QuickRecordPreviewData.boardInput)
	ThemePreview {
		HandKeyboard(
			positionRows = state.positionRows,
			enabledKeys = state.enabledKeys,
			actorKey = state.actorKey,
			heroKey = state.heroKey,
			amountInChips = state.amountInChips,
			onKeyClick = {},
		)
	}
}

@ThemePreviews
@Composable
private fun HandKeyboardReviewPreview() {
	val state = QuickRecordPreviewData.state(QuickRecordPreviewData.reviewInput)
	ThemePreview {
		HandKeyboard(
			positionRows = state.positionRows,
			enabledKeys = state.enabledKeys,
			actorKey = state.actorKey,
			heroKey = state.heroKey,
			amountInChips = state.amountInChips,
			onKeyClick = {},
		)
	}
}

@ThemePreviews
@Composable
private fun AmountUnitSwitchBbPreview() {
	ThemePreview {
		AmountUnitSwitch(amountInChips = false, enabled = true, onClick = {})
	}
}

@ThemePreviews
@Composable
private fun AmountUnitSwitchChipPreview() {
	ThemePreview {
		AmountUnitSwitch(amountInChips = true, enabled = true, onClick = {})
	}
}
