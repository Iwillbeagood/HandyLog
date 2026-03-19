package com.hand.log.ui.table

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.hand.log.designsystem.component.modal.HandyBottomSheet
import com.hand.log.domain.model.Blinds
import com.hand.log.domain.model.GameType
import com.hand.log.domain.model.PokerTable

@Composable
fun TableFormSheet(
	onDismissRequest: () -> Unit,
	onSubmit: (
		date: String,
		location: String?,
		gameType: GameType,
		startingStack: Double,
		blinds: Blinds?,
		playerCount: Int,
		heroSeat: Int,
	) -> Unit,
	table: PokerTable? = null,
) {
	val presenter = rememberTableFormPresenter(table)
	val state by presenter.state.collectAsState()

	HandyBottomSheet(
		onDismissRequest = onDismissRequest,
		title = state.title,
		confirmText = state.buttonText,
		onConfirm = {
			val loc = state.location.takeIf { l -> l.isNotBlank() }
			onSubmit(
				state.date,
				loc,
				state.gameType,
				state.startingStack.toDoubleOrNull() ?: 0.0,
				presenter.buildBlinds(),
				state.playerCount,
				state.heroSeat,
			)
			onDismissRequest()
		},
		confirmEnabled = state.isSubmitEnabled,
	) {
		TableFormFields(
			date = state.date,
			onDateChange = presenter::updateDate,
			location = state.location,
			onLocationChange = presenter::updateLocation,
			gameType = state.gameType,
			onGameTypeChange = presenter::updateGameType,
			startingStack = state.startingStack,
			onStartingStackChange = presenter::updateStartingStack,
			sbText = state.sbText,
			onSbChange = presenter::updateSb,
			bbText = state.bbText,
			onBbChange = presenter::updateBb,
			straddleEnabled = state.straddleEnabled,
			onStraddleEnabledChange = presenter::updateStraddleEnabled,
			straddleText = state.straddleText,
			onStraddleChange = presenter::updateStraddle,
			bigBlindAnteEnabled = state.bigBlindAnteEnabled,
			onBigBlindAnteChange = presenter::updateBigBlindAnte,
			playerCount = state.playerCount,
			onPlayerCountChange = presenter::updatePlayerCount,
			heroSeat = state.heroSeat,
			onHeroSeatChange = presenter::updateHeroSeat,
		)
	}
}
