package com.hand.log.playersetup

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
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
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt
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
	player: Player?,
	occupiedSeats: Set<Int>,
	maxSeat: Int,
	onSaved: (isEditMode: Boolean) -> Unit,
	onDeleted: () -> Unit,
	onDismiss: () -> Unit,
	viewModel: PlayerSetupViewModel = koinViewModel(),
) {
	val state by viewModel.state.collectAsStateWithLifecycle()
	val savedPlayers by viewModel.savedPlayers.collectAsStateWithLifecycle()

	LaunchedEffect(initialSeat) {
		viewModel.initialize(tableId, initialSeat, player, occupiedSeats)
	}

	var warningMessage by remember { mutableStateOf<String?>(null) }
	val nameRequiredMessage = stringResource(Res.string.player_setup_name_required)

	LaunchedEffect(Unit) {
		viewModel.effect.collect { effect ->
			when (effect) {
				is PlayerSetupEffect.SaveComplete -> {
					onSaved(effect.isEditMode)
					onDismiss()
				}
				PlayerSetupEffect.DeleteComplete -> {
					onDeleted()
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

	PlayerSetupContent(
		state = state,
		savedPlayers = savedPlayers,
		maxSeat = maxSeat,
		warningMessage = warningMessage,
		onNameChange = viewModel::updateName,
		onTendencySelect = viewModel::selectTendency,
		onMemoChange = viewModel::updateMemo,
		onSeatChange = viewModel::updateSeat,
		onQuickLoadSavedPlayer = viewModel::loadSavedPlayerAndSave,
		onSaveToMarkingChange = viewModel::toggleSaveToMarking,
		onClearSeatClick = viewModel::clearSeatAndSave,
		onSaveClick = viewModel::save,
		onDismiss = onDismiss,
	)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PlayerSetupContent(
	state: PlayerSetupState,
	savedPlayers: List<SavedPlayer>,
	maxSeat: Int,
	warningMessage: String? = null,
	onNameChange: (String) -> Unit,
	onTendencySelect: (PlayerTendency) -> Unit,
	onMemoChange: (String) -> Unit,
	onSeatChange: (Int) -> Unit,
	onQuickLoadSavedPlayer: (SavedPlayer) -> Unit,
	onSaveToMarkingChange: () -> Unit,
	onClearSeatClick: () -> Unit,
	onSaveClick: () -> Unit,
	onDismiss: () -> Unit,
) {
	val colors = HandyTheme.colorScheme

	val isEdit = state.isEditMode

	HandyBottomSheet(
		onDismissRequest = onDismiss,
		title = if (isEdit) {
			stringResource(Res.string.player_setup_edit_title, state.player.seat)
		} else {
			stringResource(Res.string.player_setup_add_title, state.player.seat)
		},
		confirmText = if (isEdit) {
			stringResource(
				Res.string.btn_edit,
			)
		} else {
			stringResource(Res.string.btn_add)
		},
		onConfirm = onSaveClick,
		subText = if (isEdit) stringResource(Res.string.player_setup_clear_seat) else null,
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
							.clickable { onTendencySelect(tendency) }
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
			innerModifier = Modifier.heightIn(min = 120.dp),
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
			val shakeOffset = remember { Animatable(0f) }
			LaunchedEffect(warningMessage) {
				repeat(3) {
					shakeOffset.animateTo(8f, tween(50))
					shakeOffset.animateTo(-8f, tween(50))
				}
				shakeOffset.animateTo(0f, tween(50))
			}
			Text(
				text = warningMessage,
				style = HandyTheme.typography.medium12,
				color = HandyTheme.colorScheme.error,
				modifier = Modifier.offset { IntOffset(shakeOffset.value.roundToInt(), 0) },
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
				player = Player(
					seat = 3,
					name = "John",
					tendency = PlayerTendency.TIGHT_AGGRESSIVE,
					memo = "타이트",
				),
				occupiedSeats = setOf(1, 2, 5),
			),
			savedPlayers = listOf(
				SavedPlayer(id = "1", name = "Mike", tendency = PlayerTendency.LOOSE_AGGRESSIVE),
			),
			maxSeat = 9,
			onNameChange = {}, onTendencySelect = {}, onMemoChange = {},
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
			onNameChange = {}, onTendencySelect = {}, onMemoChange = {},
			onSeatChange = {}, onQuickLoadSavedPlayer = {}, onSaveToMarkingChange = {},
			onClearSeatClick = {}, onSaveClick = {}, onDismiss = {},
		)
	}
}
