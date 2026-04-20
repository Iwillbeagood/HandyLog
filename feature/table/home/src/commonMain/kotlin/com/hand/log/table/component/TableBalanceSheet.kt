package com.hand.log.table.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.hand.log.designsystem.component.HandySectionLabel
import com.hand.log.designsystem.component.HandySelector
import com.hand.log.designsystem.component.VerticalSpacer
import com.hand.log.designsystem.component.modal.HandyBottomSheet
import com.hand.log.designsystem.etc.ThemePreview
import com.hand.log.designsystem.etc.ThemePreviews
import com.hand.log.designsystem.theme.HandyTheme
import com.hand.log.designsystem.theme.nonScaledSp
import com.hand.log.domain.model.GameType
import com.hand.log.domain.model.Player
import com.hand.log.domain.model.PlayerTendency
import com.hand.log.domain.model.PokerTable
import kotlinx.datetime.LocalDate
import handylog.core.res.generated.resources.Res
import handylog.core.res.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun TableBalanceSheet(
	table: PokerTable,
	onConfirm: (heroSeat: Int, otherSeats: Set<Int>) -> Unit,
	onDismiss: () -> Unit,
) {
	val colors = HandyTheme.colorScheme
	val typography = HandyTheme.typography
	val maxPlayers = table.maxPlayers.takeIf { it > 0 } ?: table.playerCount
	val playerCount = remember { mutableStateOf(table.playerCount.coerceIn(2, maxPlayers)) }
	val heroSeat = remember { mutableStateOf(table.heroSeat) }
	val selectedSeats = remember { mutableStateOf(emptySet<Int>()) }
	val totalSelected = selectedSeats.value.size + 1 // +1 for HERO
	val isFull = totalSelected >= playerCount.value
	val isValid = totalSelected == playerCount.value

	HandyBottomSheet(
		onDismissRequest = onDismiss,
		title = stringResource(Res.string.table_balance),
		confirmText = stringResource(Res.string.btn_confirm),
		onConfirm = {
			onDismiss()
			onConfirm(heroSeat.value, selectedSeats.value)
		},
		confirmEnabled = isValid,
	) {
		// 플레이어 수 선택
		HandySectionLabel(stringResource(Res.string.table_balance_player_count))
		HandySelector(
			range = 2..maxPlayers,
			selected = playerCount.value,
			onSelect = { count ->
				playerCount.value = count
				selectedSeats.value = emptySet()
			},
		)

		VerticalSpacer(16.dp)

		// HERO 좌석 선택
		HandySectionLabel(stringResource(Res.string.table_form_hero_seat))
		HandySelector(
			range = 1..maxPlayers,
			selected = heroSeat.value,
			onSelect = { seat ->
				// HERO가 이동하면 이전 HERO 자리를 selectedSeats에서 제거
				selectedSeats.value = selectedSeats.value - seat
				heroSeat.value = seat
			},
			selectedColor = colors.gold,
			selectedContentColor = colors.card,
		)

		VerticalSpacer(16.dp)

		// 다른 플레이어 좌석 선택
		HandySectionLabel(
			stringResource(
				Res.string.table_balance_seat_select_desc,
				selectedSeats.value.size,
				playerCount.value - 1,
			),
		)

		VerticalSpacer(12.dp)

		FlowRow(
			horizontalArrangement = Arrangement.spacedBy(8.dp),
			verticalArrangement = Arrangement.spacedBy(8.dp),
		) {
			for (seat in 1..maxPlayers) {
				val isHero = seat == heroSeat.value
				val isSelected = seat in selectedSeats.value
				val isDisabled = !isSelected && isFull

				val bgColor = when {
					isHero -> colors.gold.copy(alpha = 0.15f)
					isSelected -> colors.primary
					isDisabled -> colors.muted.copy(alpha = 0.5f)
					else -> colors.muted
				}
				val borderColor = when {
					isHero -> colors.gold
					isSelected -> colors.primary
					isDisabled -> colors.inputBorder.copy(alpha = 0.3f)
					else -> colors.inputBorder
				}
				val textColor = when {
					isHero -> colors.gold
					isSelected -> colors.onPrimary
					isDisabled -> colors.textSecondary.copy(alpha = 0.3f)
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
							when {
								isHero -> Modifier
								isSelected -> Modifier.clickable {
									selectedSeats.value = selectedSeats.value - seat
								}
								!isDisabled -> Modifier.clickable {
									selectedSeats.value = selectedSeats.value + seat
								}
								else -> Modifier
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

@ThemePreviews
@Composable
private fun TableBalanceSheetPreview() {
	ThemePreview {
		TableBalanceSheet(
			table = PokerTable(
				id = "1",
				date = LocalDate(2026, 3, 12),
				location = "강남 홀덤펍",
				gameType = GameType.Cash(sb = 500.0, bb = 1000.0),
				maxPlayers = 9,
				heroSeat = 3,
				players = listOf(
					Player(seat = 1, name = "Fish", tendency = PlayerTendency.FISH),
					Player(seat = 3, name = "Hero"),
					Player(seat = 5, name = "Reg", tendency = PlayerTendency.REGULAR),
					Player(seat = 7, name = "Shark", tendency = PlayerTendency.SHARK),
				),
				createdAt = 1710000000000L,
			),
			onConfirm = { _, _ -> },
			onDismiss = {},
		)
	}
}
