package com.hand.log.playersetup

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hand.log.designsystem.component.HandyCheckBox
import com.hand.log.designsystem.component.HandySectionLabel
import com.hand.log.designsystem.component.HandySelector
import com.hand.log.designsystem.component.HandyTextField
import com.hand.log.designsystem.component.VerticalSpacer
import com.hand.log.designsystem.component.modal.HandyBottomSheet
import com.hand.log.designsystem.etc.ThemePreview
import com.hand.log.designsystem.etc.ThemePreviews
import com.hand.log.designsystem.etc.clickableSingle
import com.hand.log.designsystem.theme.HandyTheme
import com.hand.log.domain.model.Player
import com.hand.log.domain.model.PlayerTendency
import com.hand.log.ui.localizedLabel
import com.hand.log.domain.model.SavedPlayer
import com.hand.log.playersetup.contract.PlayerSetupEffect
import com.hand.log.playersetup.contract.PlayerSetupState
import handylog.core.res.generated.resources.Res
import handylog.core.res.generated.resources.*
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun PlayerSetupSheet(
	tableId: String,
	initialSeat: Int,
	isHero: Boolean,
	player: Player?,
	occupiedSeats: Set<Int>,
	maxSeat: Int,
	onComplete: () -> Unit,
	onDismiss: () -> Unit,
	viewModel: PlayerSetupViewModel = koinViewModel(),
) {
	val colors = HandyTheme.colorScheme
	val state by viewModel.state.collectAsStateWithLifecycle()
	val savedPlayers by viewModel.savedPlayers.collectAsStateWithLifecycle()

	LaunchedEffect(initialSeat) {
		viewModel.initialize(tableId, initialSeat, isHero, player, occupiedSeats)
	}

	var warningMessage by remember { mutableStateOf<String?>(null) }
	val nameRequiredMessage = stringResource(Res.string.player_setup_name_required)

	LaunchedEffect(Unit) {
		viewModel.effect.collect { effect ->
			when (effect) {
				PlayerSetupEffect.SaveComplete -> {
					onComplete()
					onDismiss()
				}
				PlayerSetupEffect.NameRequired -> {
					warningMessage = nameRequiredMessage
				}
			}
		}
	}

	LaunchedEffect(warningMessage) {
		if (warningMessage != null) {
			kotlinx.coroutines.delay(2000)
			warningMessage = null
		}
	}

	if (isHero) {
		HandyBottomSheet(
			onDismissRequest = onDismiss,
			title = "Hero",
			titleColor = colors.gold,
			confirmText = stringResource(Res.string.btn_save),
			onConfirm = viewModel::save,
		) {
			HandyTextField(
				value = state.playerName,
				onValueChange = viewModel::updateName,
				label = stringResource(Res.string.player_name),
			)
		}
	} else {
		PlayerSetupContent(
			state = state,
			savedPlayers = savedPlayers,
			maxSeat = maxSeat,
			warningMessage = warningMessage,
			onNameChange = viewModel::updateName,
			onTendencyChange = viewModel::updateTendency,
			onMemoChange = viewModel::updateMemo,
			onSeatChange = viewModel::updateSeat,
			onQuickLoadSavedPlayer = viewModel::loadSavedPlayerAndSave,
			onSaveToMarkingChange = viewModel::toggleSaveToMarking,
			onClearSeatClick = viewModel::clearSeatAndSave,
			onSaveClick = viewModel::save,
			onDismiss = onDismiss,
		)
	}
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PlayerSetupContent(
	state: PlayerSetupState,
	savedPlayers: List<SavedPlayer>,
	maxSeat: Int,
	warningMessage: String? = null,
	onNameChange: (String) -> Unit,
	onTendencyChange: (PlayerTendency?) -> Unit,
	onMemoChange: (String) -> Unit,
	onSeatChange: (Int) -> Unit,
	onQuickLoadSavedPlayer: (SavedPlayer) -> Unit,
	onSaveToMarkingChange: () -> Unit,
	onClearSeatClick: () -> Unit,
	onSaveClick: () -> Unit,
	onDismiss: () -> Unit,
) {
	val colors = HandyTheme.colorScheme

	HandyBottomSheet(
		onDismissRequest = onDismiss,
		title = "Seat ${state.player.seat}",
		confirmText = stringResource(Res.string.btn_save),
		onConfirm = onSaveClick,
		subText = stringResource(Res.string.player_setup_clear_seat),
		onSub = onClearSeatClick,
	) {
		if (savedPlayers.isNotEmpty()) {
			HandySectionLabel(stringResource(Res.string.player_saved)) {
				FlowRow(
					horizontalArrangement = Arrangement.spacedBy(6.dp),
					verticalArrangement = Arrangement.spacedBy(6.dp),
				) {
					savedPlayers.forEach { saved ->
						Box(
							modifier = Modifier
								.clip(RoundedCornerShape(8.dp))
								.background(colors.muted)
								.clickableSingle(onClick = { onQuickLoadSavedPlayer(saved) })
								.padding(horizontal = 10.dp, vertical = 6.dp),
						) {
							Row(
								verticalAlignment = Alignment.CenterVertically,
								horizontalArrangement = Arrangement.spacedBy(4.dp),
							) {
								Icon(
									painter = painterResource(Res.drawable.user_round),
									contentDescription = null,
									modifier = Modifier.size(12.dp),
									tint = colors.textSecondary,
								)
								Text(text = saved.name, style = HandyTheme.typography.medium12, color = colors.textPrimary)
							}
						}
					}
				}
			}
			VerticalSpacer(12.dp)
		}

		HandyTextField(
			value = state.playerName,
			onValueChange = onNameChange,
			label = stringResource(Res.string.player_name),
		)

		VerticalSpacer(12.dp)
		HandySectionLabel(stringResource(Res.string.player_setup_seat))
		HandySelector(
			range = 1..maxSeat,
			selected = state.player.seat,
			onSelect = onSeatChange,
			disabledValues = state.occupiedSeats,
		)

		VerticalSpacer(12.dp)
		HandySectionLabel(stringResource(Res.string.player_tendency)) {
			FlowRow(
				horizontalArrangement = Arrangement.spacedBy(6.dp),
				verticalArrangement = Arrangement.spacedBy(6.dp),
			) {
				PlayerTendency.entries.forEach { tendency ->
					val isSelected = tendency == state.selectedTendency
					Box(
						modifier = Modifier
							.clip(RoundedCornerShape(8.dp))
							.background(if (isSelected) colors.primary else colors.muted)
							.clickable { onTendencyChange(tendency) }
							.padding(horizontal = 12.dp, vertical = 6.dp),
					) {
						Text(
							text = tendency.localizedLabel(),
							style = HandyTheme.typography.medium12,
							color = if (isSelected) colors.onPrimary else colors.textSecondary,
						)
					}
				}
			}
		}

		VerticalSpacer(12.dp)
		HandyTextField(
			value = state.playerMemo,
			onValueChange = onMemoChange,
			label = stringResource(Res.string.player_memo),
		)

		VerticalSpacer(4.dp)
		HandyCheckBox(
			text = stringResource(Res.string.player_save_to_marking),
			checked = state.saveToMarking,
			onCheckedChange = { onSaveToMarkingChange() },
			modifier = Modifier
				.padding(vertical = 4.dp)
				.align(Alignment.End),
		)

		if (warningMessage != null) {
			VerticalSpacer(8.dp)
			Text(
				text = warningMessage,
				style = HandyTheme.typography.medium12,
				color = HandyTheme.colorScheme.error,
			)
		}
	}
}

@ThemePreviews
@Composable
private fun PlayerSetupContentPreview() {
	ThemePreview {
		PlayerSetupContent(
			state = PlayerSetupState(
				player = Player(seat = 3, name = "John", tendency = PlayerTendency.NIT, memo = "타이트"),
				occupiedSeats = setOf(1, 2, 5),
			),
			savedPlayers = listOf(SavedPlayer(id = "1", name = "Mike", tendency = PlayerTendency.LOOSE)),
			maxSeat = 9,
			onNameChange = {}, onTendencyChange = {}, onMemoChange = {},
			onSeatChange = {}, onQuickLoadSavedPlayer = {}, onSaveToMarkingChange = {},
			onClearSeatClick = {}, onSaveClick = {}, onDismiss = {},
		)
	}
}

@ThemePreviews
@Composable
private fun PlayerSetupContentEmptyPreview() {
	ThemePreview {
		PlayerSetupContent(
			state = PlayerSetupState(player = Player(seat = 1)),
			savedPlayers = emptyList(),
			maxSeat = 9,
			onNameChange = {}, onTendencyChange = {}, onMemoChange = {},
			onSeatChange = {}, onQuickLoadSavedPlayer = {}, onSaveToMarkingChange = {},
			onClearSeatClick = {}, onSaveClick = {}, onDismiss = {},
		)
	}
}
