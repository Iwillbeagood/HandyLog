package com.hand.log.table.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.hand.log.designsystem.component.HandySectionLabel
import com.hand.log.designsystem.component.VerticalSpacer
import com.hand.log.designsystem.component.modal.HandyBottomSheet
import com.hand.log.designsystem.etc.clickableSingle
import com.hand.log.designsystem.theme.HandyTheme
import com.hand.log.designsystem.theme.nonScaledSp
import handylog.core.res.generated.resources.Res
import handylog.core.res.generated.resources.hero_seat_swap_title
import handylog.core.res.generated.resources.hero_seat_swap_desc
import handylog.core.res.generated.resources.player_position_setup_hero
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun HeroSeatSwapSheet(
	maxPlayers: Int,
	heroSeat: Int,
	onSeatSelected: (Int) -> Unit,
	onDismiss: () -> Unit,
) {
	val colors = HandyTheme.colorScheme
	val typography = HandyTheme.typography

	HandyBottomSheet(
		onDismissRequest = onDismiss,
		title = stringResource(Res.string.hero_seat_swap_title),
	) {
		HandySectionLabel(stringResource(Res.string.hero_seat_swap_desc))

		VerticalSpacer(12.dp)

		FlowRow(
			horizontalArrangement = Arrangement.spacedBy(8.dp),
			verticalArrangement = Arrangement.spacedBy(8.dp),
		) {
			for (seat in 1..maxPlayers) {
				val isHero = seat == heroSeat

				val bgColor = if (isHero) colors.gold.copy(alpha = 0.15f) else colors.muted
				val borderColor = if (isHero) colors.gold else colors.inputBorder
				val textColor = if (isHero) colors.gold else colors.textPrimary

				Box(
					modifier = Modifier
						.size(48.dp)
						.clip(RoundedCornerShape(8.dp))
						.background(bgColor)
						.border(
							width = if (isHero) 2.dp else 1.dp,
							color = borderColor,
							shape = RoundedCornerShape(8.dp),
						)
						.then(
							if (!isHero) {
								Modifier.clickableSingle(onClick = { onSeatSelected(seat) })
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
}
