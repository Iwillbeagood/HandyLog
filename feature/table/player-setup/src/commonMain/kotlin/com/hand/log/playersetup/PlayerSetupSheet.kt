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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hand.log.designsystem.component.HandyCheckBox
import com.hand.log.designsystem.component.HandySectionLabel
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
	initialSeat: Int,
	isHero: Boolean,
	startingStack: Double,
	players: List<Player>,
	onSave: (List<Player>) -> Unit,
	onDismiss: () -> Unit,
	viewModel: PlayerSetupViewModel = koinViewModel(),
) {
	val colors = HandyTheme.colorScheme
	val state by viewModel.state.collectAsStateWithLifecycle()
	val savedPlayers by viewModel.savedPlayers.collectAsStateWithLifecycle()

	LaunchedEffect(initialSeat) {
		viewModel.initialize(initialSeat, isHero, startingStack, players)
	}

	LaunchedEffect(Unit) {
		viewModel.effect.collect { effect ->
			when (effect) {
				is PlayerSetupEffect.SaveComplete -> {
					onSave(effect.players)
					onDismiss()
				}
			}
		}
	}

	if (isHero) {
		HandyBottomSheet(
			onDismissRequest = onDismiss,
			title = "Hero",
			titleColor = colors.gold,
			confirmText = stringResource(Res.string.btn_save),
			onConfirm = viewModel::save,
			subText = stringResource(Res.string.btn_reset),
			onSub = viewModel::resetAndSave,
		) {
			HandyTextField(
				value = state.playerStack,
				onValueChange = viewModel::updateStack,
				label = stringResource(Res.string.player_stack),
				keyboardType = KeyboardType.Number,
			)
		}
	} else {
		PlayerSetupContent(
			state = state,
			savedPlayers = savedPlayers,
			onNameChange = viewModel::updateName,
			onStackChange = viewModel::updateStack,
			onTendencyChange = viewModel::updateTendency,
			onMemoChange = viewModel::updateMemo,
			onQuickLoadSavedPlayer = viewModel::loadSavedPlayerAndSave,
			onSaveToMarkingChange = { viewModel.toggleSaveToMarking() },
			onResetClick = viewModel::resetAndSave,
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
	onNameChange: (String) -> Unit,
	onStackChange: (String) -> Unit,
	onTendencyChange: (PlayerTendency?) -> Unit,
	onMemoChange: (String) -> Unit,
	onQuickLoadSavedPlayer: (SavedPlayer) -> Unit,
	onSaveToMarkingChange: () -> Unit,
	onResetClick: () -> Unit,
	onSaveClick: () -> Unit,
	onDismiss: () -> Unit,
) {
	val colors = HandyTheme.colorScheme

	HandyBottomSheet(
		onDismissRequest = onDismiss,
		title = "Seat ${state.initialSeat}",
		confirmText = stringResource(Res.string.btn_save),
		onConfirm = onSaveClick,
		subText = stringResource(Res.string.btn_reset),
		onSub = onResetClick,
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

		HandyTextField(value = state.playerName, onValueChange = onNameChange, label = stringResource(Res.string.player_name))

		VerticalSpacer(12.dp)
		HandyTextField(
			value = state.playerStack,
			onValueChange = onStackChange,
			label = stringResource(Res.string.player_stack),
			keyboardType = KeyboardType.Number,
		)

		VerticalSpacer(12.dp)
		HandySectionLabel(stringResource(Res.string.player_tendency)) {
			FlowRow(
				horizontalArrangement = Arrangement.spacedBy(6.dp),
				verticalArrangement = Arrangement.spacedBy(6.dp),
			) {
				val options = listOf<PlayerTendency?>(null) + PlayerTendency.entries
				options.forEach { tendency ->
					val isSelected = tendency == state.selectedTendency
					Box(
						modifier = Modifier
							.clip(RoundedCornerShape(8.dp))
							.background(if (isSelected) colors.primary else colors.muted)
							.clickable { onTendencyChange(tendency) }
							.padding(horizontal = 12.dp, vertical = 6.dp),
					) {
						Text(
							text = tendency?.localizedLabel() ?: stringResource(Res.string.player_tendency_none),
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
	}
}

@ThemePreviews
@Composable
private fun PlayerSetupContentPreview() {
	ThemePreview {
		PlayerSetupContent(
			state = PlayerSetupState(
				initialSeat = 3,
				editingPlayers = listOf(
					Player(seat = 3, stack = 62000.0, name = "John", tendency = PlayerTendency.NIT, memo = "타이트"),
				),
			),
			savedPlayers = listOf(SavedPlayer(id = "1", name = "Mike", tendency = PlayerTendency.LOOSE)),
			onNameChange = {}, onStackChange = {}, onTendencyChange = {}, onMemoChange = {},
			onQuickLoadSavedPlayer = {}, onSaveToMarkingChange = {}, onResetClick = {}, onSaveClick = {},
			onDismiss = {},
		)
	}
}

@ThemePreviews
@Composable
private fun PlayerSetupContentEmptyPreview() {
	ThemePreview {
		PlayerSetupContent(
			state = PlayerSetupState(initialSeat = 1),
			savedPlayers = emptyList(),
			onNameChange = {}, onStackChange = {}, onTendencyChange = {}, onMemoChange = {},
			onQuickLoadSavedPlayer = {}, onSaveToMarkingChange = {}, onResetClick = {}, onSaveClick = {},
			onDismiss = {},
		)
	}
}
