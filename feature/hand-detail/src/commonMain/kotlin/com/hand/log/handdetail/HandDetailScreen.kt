package com.hand.log.handdetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.unit.dp
import com.hand.log.designsystem.component.BaseScaffold
import com.hand.log.designsystem.component.FadeAnimatedVisibility
import com.hand.log.designsystem.component.HandySwitch
import com.hand.log.designsystem.component.HandyTopAppbar
import com.hand.log.designsystem.etc.ThemePreview
import com.hand.log.designsystem.etc.ThemePreviews
import com.hand.log.handdetail.component.HandDetailTopBarEndContent
import org.jetbrains.compose.resources.stringResource
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
import com.hand.log.domain.model.HandPlayer
import com.hand.log.domain.model.Suit
import com.hand.log.domain.model.TurnStreet
import com.hand.log.handdetail.contract.HandDetailState
import handylog.core.res.generated.resources.*

@Composable
internal fun HandDetailScreen(
	state: HandDetailState,
	onToggleBbUnit: () -> Unit,
	onBack: () -> Unit,
	onShowDeleteConfirm: () -> Unit,
	onShareText: () -> Unit,
	onShareImage: () -> Unit,
	onDownloadImage: () -> Unit,
	onMarkPlayer: (Int) -> Unit = {},
	onEditHeroHand: () -> Unit = {},
	onEditShowdownHand: (Int) -> Unit = {},
	onMemoClick: () -> Unit = {},
	graphicsLayer: GraphicsLayer = rememberGraphicsLayer(),
) {
	val loaded = state as? HandDetailState.Detail

	BaseScaffold(
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
							checked = loaded?.useBbUnit ?: false,
							text = "BB",
							onCheckedChange = { onToggleBbUnit() },
						)
					}
				},
				endContent = {
					HandDetailTopBarEndContent(
						onShowDeleteConfirm = onShowDeleteConfirm,
						onShareText = onShareText,
						onShareImage = onShareImage,
						onDownloadImage = onDownloadImage,
					)
				},
			)
		},
	) {
		FadeAnimatedVisibility(loaded != null) {
			if (loaded != null) {
				HandDetailContent(
					hand = loaded.hand,
					useBbUnit = loaded.useBbUnit,
					onMemoClick = onMemoClick,
					graphicsLayer = graphicsLayer,
					onMarkPlayer = onMarkPlayer,
					onEditHeroHand = onEditHeroHand,
					onEditShowdownHand = onEditShowdownHand,
				)
			}
		}
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
		heroSeat = 3,
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
		players = listOf(
			HandPlayer(
				seat = 3,
				cards = PocketCards(Card(Rank.ACE, Suit.SPADES), Card(Rank.KING, Suit.SPADES)),
				initialStack = 50000.0,
				isHero = true,
			),
			HandPlayer(
				seat = 4,
				cards = PocketCards(Card(Rank.ACE, Suit.HEARTS), Card(Rank.TEN, Suit.HEARTS)),
			),
		),
		result = 49000.0,
		memo = "탑투페어로 체크레이즈 → 올인 콜, 상대 ATo",
	)
	ThemePreview {
		HandDetailScreen(
			state = HandDetailState.Detail(
				hand = hand,
			),
			onToggleBbUnit = {},
			onBack = {},
			onShowDeleteConfirm = {},
			onShareText = {},
			onShareImage = {},
			onDownloadImage = {},
		)
	}
}
