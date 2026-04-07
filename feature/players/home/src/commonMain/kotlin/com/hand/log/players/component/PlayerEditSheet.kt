package com.hand.log.players.component

import androidx.compose.runtime.Composable
import com.hand.log.domain.model.SavedPlayer
import com.hand.log.playersedit.PlayerEditSheet as PlayerEditSheetImpl

@Composable
internal fun PlayerEditSheet(
	player: SavedPlayer?,
	onSave: (SavedPlayer) -> Unit,
	onDelete: ((String) -> Unit)? = null,
	onDismiss: () -> Unit,
) {
	PlayerEditSheetImpl(
		player = player,
		onSave = onSave,
		onDelete = onDelete,
		onDismiss = onDismiss,
	)
}
