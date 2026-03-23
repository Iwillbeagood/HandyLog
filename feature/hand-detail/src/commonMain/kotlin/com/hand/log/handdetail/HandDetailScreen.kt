package com.hand.log.handdetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.unit.dp
import com.hand.log.designsystem.component.BaseScaffold
import com.hand.log.designsystem.component.HandyMenuItem
import com.hand.log.designsystem.component.HandyPopupMenu
import com.hand.log.designsystem.component.HandySwitch
import handylog.core.res.generated.resources.hand_detail_share_image
import handylog.core.res.generated.resources.hand_detail_share_text
import handylog.core.res.generated.resources.hand_detail_title
import org.jetbrains.compose.resources.stringResource
import com.hand.log.designsystem.component.HandyTopAppbar
import com.hand.log.designsystem.component.TopAppbarIcon
import com.hand.log.designsystem.etc.ThemePreview
import com.hand.log.designsystem.etc.ThemePreviews
import com.hand.log.designsystem.theme.HandyTheme
import com.hand.log.domain.model.Action
import com.hand.log.domain.model.ActionType
import com.hand.log.domain.model.Blinds
import com.hand.log.domain.model.Card
import com.hand.log.domain.model.FlopStreet
import com.hand.log.domain.model.HandRecord
import com.hand.log.domain.model.HandStreets
import com.hand.log.domain.model.PreflopStreet
import com.hand.log.domain.model.Rank
import com.hand.log.domain.model.RiverStreet
import com.hand.log.domain.model.PocketCards
import com.hand.log.domain.model.ShowdownEntry
import com.hand.log.domain.model.Suit
import com.hand.log.domain.model.TurnStreet
import com.hand.log.handdetail.contract.HandDetailState
import handylog.core.res.generated.resources.Res
import handylog.core.res.generated.resources.file_text
import handylog.core.res.generated.resources.image
import handylog.core.res.generated.resources.pencil
import handylog.core.res.generated.resources.share_2
import kotlinx.coroutines.launch

@Composable
internal fun HandDetailScreen(
	state: HandDetailState.Success,
	onToggleBbUnit: () -> Unit,
	onBack: () -> Unit,
	onEdit: () -> Unit,
	onShareText: () -> Unit,
	onShareImage: (ImageBitmap) -> Unit,
) {
	val hand = state.hand
	val colors = HandyTheme.colorScheme
	val graphicsLayer = rememberGraphicsLayer()
	val scope = rememberCoroutineScope()

	BaseScaffold(
		containerColor = colors.background,
		topBar = {
			HandyTopAppbar(
				title = stringResource(Res.string.hand_detail_title),
				onBackEvent = onBack,
				subContent = {
					Row(
						modifier = Modifier
							.fillMaxWidth()
							.padding(horizontal = 16.dp, vertical = 4.dp),
						verticalAlignment = Alignment.CenterVertically,
						horizontalArrangement = Arrangement.End,
					) {
						HandySwitch(
							checked = state.useBbUnit,
							text = "BB",
							onCheckedChange = { onToggleBbUnit() },
						)
					}
				},
				endContent = {
					Row {
						TopAppbarIcon(
							tint = colors.textPrimary,
							icon = Res.drawable.pencil,
							onClick = onEdit,
						)
						Box {
							var showShareMenu by remember { mutableStateOf(false) }

							TopAppbarIcon(
								tint = colors.textPrimary,
								icon = Res.drawable.share_2,
								onClick = { showShareMenu = true },
							)

							HandyPopupMenu(
								expanded = showShareMenu,
								onDismissRequest = { showShareMenu = false },
								items = listOf(
									HandyMenuItem(
										icon = Res.drawable.file_text,
										text = stringResource(Res.string.hand_detail_share_text),
										onClick = onShareText,
									),
									HandyMenuItem(
										icon = Res.drawable.image,
										text = stringResource(Res.string.hand_detail_share_image),
										onClick = {
											scope.launch {
												val bitmap = graphicsLayer.toImageBitmap()
												onShareImage(bitmap)
											}
										},
									),
								),
							)
						}
					}
				},
			)
		},
	) {
		HandDetailContent(
			hand = hand,
			useBbUnit = state.useBbUnit,
			graphicsLayer = graphicsLayer,
		)
	}
}

@ThemePreviews
@Composable
private fun HandDetailScreenPreview() {
	val hand = HandRecord(
		id = "h1",
		tableId = "t1",
		createdAt = 1710000000000L,
		blinds = Blinds(sb = 500.0, bb = 1000.0),
		heroHand = PocketCards(Card(Rank.ACE, Suit.SPADES), Card(Rank.KING, Suit.SPADES)),
		heroSeat = 3,
		heroStack = 50000.0,
		buttonSeat = 1,
		streets = HandStreets(
			preflop = PreflopStreet(
				actions = listOf(
					Action(playerSeat = 4, type = ActionType.RAISE, amount = 2500.0, stackBefore = 50000.0),
					Action(playerSeat = 5, type = ActionType.FOLD, stackBefore = 50000.0),
					Action(playerSeat = 6, type = ActionType.CALL, amount = 2500.0, stackBefore = 50000.0),
					Action(playerSeat = 1, type = ActionType.FOLD, stackBefore = 50000.0),
					Action(playerSeat = 2, type = ActionType.FOLD, stackBefore = 49500.0),
					Action(playerSeat = 3, type = ActionType.CALL, amount = 2500.0, stackBefore = 49000.0),
				),
			),
			flop = FlopStreet(
				card1 = Card(Rank.ACE, Suit.HEARTS),
				card2 = Card(Rank.TEN, Suit.DIAMONDS),
				card3 = Card(Rank.SEVEN, Suit.CLUBS),
				actions = listOf(
					Action(playerSeat = 3, type = ActionType.CHECK, stackBefore = 46500.0),
					Action(playerSeat = 4, type = ActionType.BET, amount = 5000.0, stackBefore = 47500.0),
					Action(playerSeat = 6, type = ActionType.FOLD, stackBefore = 47500.0),
					Action(playerSeat = 3, type = ActionType.CALL, amount = 5000.0, stackBefore = 46500.0),
				),
			),
			turn = TurnStreet(
				card = Card(Rank.KING, Suit.HEARTS),
				actions = listOf(
					Action(playerSeat = 3, type = ActionType.CHECK, stackBefore = 41500.0),
					Action(playerSeat = 4, type = ActionType.BET, amount = 12000.0, stackBefore = 42500.0),
					Action(playerSeat = 3, type = ActionType.RAISE, amount = 30000.0, stackBefore = 41500.0),
					Action(playerSeat = 4, type = ActionType.ALL_IN, amount = 30500.0, stackBefore = 30500.0),
					Action(playerSeat = 3, type = ActionType.CALL, amount = 30500.0, stackBefore = 11500.0),
				),
			),
			river = RiverStreet(
				card = Card(Rank.TWO, Suit.CLUBS),
			),
		),
		showdown = listOf(
			ShowdownEntry(
				seat = 3,
				cards = PocketCards(Card(Rank.ACE, Suit.SPADES), Card(Rank.KING, Suit.SPADES)),
			),
			ShowdownEntry(
				seat = 4,
				cards = PocketCards(Card(Rank.ACE, Suit.HEARTS), Card(Rank.TEN, Suit.HEARTS)),
			),
		),
		result = 49000.0,
		memo = "탑투페어로 체크레이즈 → 올인 콜, 상대 ATo",
	)
	ThemePreview {
		HandDetailScreen(
			state = HandDetailState.Success(
				hand = hand,
			),
			onToggleBbUnit = {},
			onBack = {},
			onEdit = {},
			onShareText = {},
			onShareImage = { _ -> },
		)
	}
}
