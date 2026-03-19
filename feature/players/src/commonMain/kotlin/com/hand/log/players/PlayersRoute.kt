package com.hand.log.players

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hand.log.domain.model.SavedPlayer
import com.hand.log.players.component.PlayerEditSheet
import com.hand.log.players.contract.PlayersModalEffect
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun PlayersRoute() {
	val viewModel: PlayersViewModel = koinViewModel()
	val state by viewModel.state.collectAsStateWithLifecycle()
	val modalEffect by viewModel.modalEffect.collectAsStateWithLifecycle()

	PlayersScreen(
		state = state,
		onPlayerClick = viewModel::showPlayerEdit,
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

		PlayersModalEffect.ShowAddPlayer -> {
			PlayerEditSheet(
				player = null,
				onSave = onSavePlayer,
				onDismiss = onDismiss,
			)
		}
	}
}
