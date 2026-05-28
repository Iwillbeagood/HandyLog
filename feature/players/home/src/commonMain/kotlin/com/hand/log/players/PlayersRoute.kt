package com.hand.log.players

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hand.log.domain.model.SavedPlayer
import com.hand.log.navigation.interop.LocalNavigateActionInterop
import com.hand.log.players.component.PlayerEditSheet
import com.hand.log.players.contract.PlayersModalEffect
import com.hand.log.ui.ProPaywallSheet

@Composable
internal fun PlayersRoute(
	viewModel: PlayersViewModel,
	openAdd: Boolean = false,
) {
	val state by viewModel.state.collectAsStateWithLifecycle()
	val modalEffect by viewModel.modalEffect.collectAsStateWithLifecycle()
	val navAction = LocalNavigateActionInterop.current

	LaunchedEffect(openAdd) {
		if (openAdd) {
			viewModel.consumeOpenAdd()
		}
	}

	PlayersScreen(
		state = state,
		onPlayerClick = { player ->
			navAction.navigateToPlayerHands(player.id, player.name)
		},
		onEditPlayer = viewModel::showPlayerEdit,
		onDeletePlayer = viewModel::deletePlayer,
		onAddPlayer = viewModel::showAddPlayer,
	)

	PlayersModalContent(
		modalEffect = modalEffect,
		onSavePlayer = viewModel::savePlayer,
		onDeletePlayer = viewModel::deletePlayer,
		onDismiss = viewModel::dismissModal,
	)
}

@Composable
private fun PlayersModalContent(
	modalEffect: PlayersModalEffect,
	onSavePlayer: (SavedPlayer) -> Unit,
	onDeletePlayer: (String) -> Unit,
	onDismiss: () -> Unit,
) {
	when (modalEffect) {
		PlayersModalEffect.Idle -> {}

		is PlayersModalEffect.ShowPlayerEdit -> {
			PlayerEditSheet(
				player = modalEffect.player,
				onSave = onSavePlayer,
				onDelete = onDeletePlayer,
				onDismiss = onDismiss,
			)
		}

		is PlayersModalEffect.ShowAddPlayer -> {
			PlayerEditSheet(
				player = null,
				onSave = onSavePlayer,
				onDismiss = onDismiss,
			)
		}

		is PlayersModalEffect.ShowPaywall -> {
			ProPaywallSheet(
				feature = modalEffect.feature,
				onDismiss = onDismiss,
			)
		}
	}
}
