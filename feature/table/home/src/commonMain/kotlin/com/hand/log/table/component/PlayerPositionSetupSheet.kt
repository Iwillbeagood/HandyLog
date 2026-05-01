package com.hand.log.table.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.hand.log.designsystem.component.HandySectionLabel
import com.hand.log.designsystem.component.VerticalSpacer
import com.hand.log.designsystem.component.modal.HandyBottomSheet
import com.hand.log.designsystem.etc.ThemePreview
import com.hand.log.designsystem.etc.ThemePreviews
import com.hand.log.designsystem.theme.HandyTheme
import com.hand.log.designsystem.theme.nonScaledSp
import handylog.core.res.generated.resources.Res
import handylog.core.res.generated.resources.btn_confirm
import handylog.core.res.generated.resources.player_position_setup_count
import handylog.core.res.generated.resources.player_position_setup_desc
import handylog.core.res.generated.resources.player_position_setup_hero
import handylog.core.res.generated.resources.player_position_setup_skip
import handylog.core.res.generated.resources.player_position_setup_title
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun PlayerPositionSetupSheet(
	maxPlayers: Int,
	heroSeat: Int,
	playerCount: Int = 0,
	onConfirm: (Set<Int>) -> Unit,
	onDismiss: () -> Unit,
) {
	val selectedSeats = remember { mutableStateOf(emptySet<Int>()) }

	HandyBottomSheet(
		onDismissRequest = onDismiss,
		title = stringResource(Res.string.player_position_setup_title),
		confirmText = stringResource(Res.string.btn_confirm),
		onConfirm = { onConfirm(selectedSeats.value + heroSeat) },
		confirmEnabled = selectedSeats.value.isNotEmpty(),
		subText = stringResource(Res.string.player_position_setup_skip),
		onSub = onDismiss,
	) {
		PlayerPositionSetupContent(
			maxPlayers = maxPlayers,
			heroSeat = heroSeat,
			playerCount = playerCount,
			selectedSeats = selectedSeats,
		)
	}
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PlayerPositionSetupContent(
	maxPlayers: Int,
	heroSeat: Int,
	playerCount: Int,
	selectedSeats: MutableState<Set<Int>>,
) {
	val colors = HandyTheme.colorScheme
	val typography = HandyTheme.typography
	val totalSelected = selectedSeats.value.size + 1
	val targetCount = if (playerCount > 0) playerCount else maxPlayers

	HandySectionLabel(stringResource(Res.string.player_position_setup_desc))

	VerticalSpacer(8.dp)

	Text(
		text = stringResource(
			Res.string.player_position_setup_count,
			totalSelected,
			targetCount,
		),
		style = typography.medium12,
		color = if (totalSelected >= targetCount) colors.primary else colors.textSecondary,
	)

	VerticalSpacer(12.dp)

	FlowRow(
		horizontalArrangement = Arrangement.spacedBy(8.dp),
		verticalArrangement = Arrangement.spacedBy(8.dp),
	) {
		for (seat in 1..maxPlayers) {
			val isHero = seat == heroSeat
			val isSelected = seat in selectedSeats.value

			val bgColor = when {
				isHero -> colors.gold.copy(alpha = 0.15f)
				isSelected -> colors.primary
				else -> colors.muted
			}
			val borderColor = when {
				isHero -> colors.gold
				isSelected -> colors.primary
				else -> colors.inputBorder
			}
			val textColor = when {
				isHero -> colors.gold
				isSelected -> colors.onPrimary
				else -> colors.textPrimary
			}

			Box(
				modifier = Modifier
					.size(48.dp)
					.clip(RoundedCornerShape(8.dp))
					.background(bgColor)
					.border(
						width = if (isHero || isSelected) 2.dp else 1.dp,
						color = borderColor,
						shape = RoundedCornerShape(8.dp),
					)
					.then(
						if (!isHero) {
							val isFull = totalSelected >= targetCount
							Modifier.clickable(enabled = isSelected || !isFull) {
								selectedSeats.value = if (isSelected) {
									selectedSeats.value - seat
								} else {
									selectedSeats.value + seat
								}
							}
						} else {
							Modifier
						},
					),
				contentAlignment = Alignment.Center,
			) {
				Text(
					text = if (isHero) {
						stringResource(Res.string.player_position_setup_hero)
					} else {
						"$seat"
					},
					style = typography.bold12.nonScaledSp,
					color = textColor,
				)
			}
		}
	}
}

@ThemePreviews
@Composable
private fun PlayerPositionSetupContentPreview() {
	ThemePreview {
		Column(
			modifier = Modifier
				.fillMaxWidth()
				.background(HandyTheme.colorScheme.card),
		) {
			val selectedSeats = remember { mutableStateOf(setOf(1, 5, 7)) }
			PlayerPositionSetupContent(
				maxPlayers = 9,
				heroSeat = 3,
				playerCount = 6,
				selectedSeats = selectedSeats,
			)
		}
	}
}
