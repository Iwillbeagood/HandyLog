package com.hand.log.handdetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.unit.dp
import com.hand.log.designsystem.component.VerticalSpacer
import com.hand.log.designsystem.theme.HandyTheme
import com.hand.log.domain.model.HandRecord
import androidx.compose.ui.graphics.rememberGraphicsLayer
import com.hand.log.designsystem.etc.ThemePreview
import com.hand.log.designsystem.etc.ThemePreviews
import com.hand.log.domain.model.Action
import com.hand.log.domain.model.ActionType
import com.hand.log.domain.model.Blinds
import com.hand.log.domain.model.Card
import com.hand.log.domain.model.FlopStreet
import com.hand.log.domain.model.HandStreets
import com.hand.log.domain.model.PocketCards
import com.hand.log.domain.model.PreflopStreet
import com.hand.log.domain.model.Rank
import com.hand.log.domain.model.RiverStreet
import com.hand.log.domain.model.Suit
import com.hand.log.domain.model.TurnStreet
import com.hand.log.handdetail.component.ActionGridSection
import com.hand.log.handdetail.component.HandDetailTableView
import com.hand.log.handdetail.component.ResultSection

@Composable
internal fun HandDetailContent(
	hand: HandRecord,
	useBbUnit: Boolean,
	graphicsLayer: GraphicsLayer,
	onMarkPlayer: (Int) -> Unit = {},
	modifier: Modifier = Modifier,
) {
	val colors = HandyTheme.colorScheme

	Column(
		modifier = modifier
			.verticalScroll(rememberScrollState())
			.background(colors.background)
			.drawWithContent {
				graphicsLayer.record {
					this@drawWithContent.drawContent()
				}
				drawLayer(graphicsLayer)
			}
			.padding(horizontal = 16.dp),
		verticalArrangement = Arrangement.spacedBy(12.dp),
	) {
		VerticalSpacer(4.dp)
		HandDetailTableView(
			hand = hand,
			useBbUnit = useBbUnit,
		)

		ActionGridSection(
			hand = hand,
			useBbUnit = useBbUnit,
		)

		ResultSection(hand = hand, onMarkPlayer = onMarkPlayer)
		VerticalSpacer(32.dp)
	}

}

@ThemePreviews
@Composable
private fun HandDetailContentPreview() {
	val hand = HandRecord(
		id = "h1",
		tableId = "t1",
		createdAt = 0L,
		blinds = Blinds(sb = 500.0, bb = 1000.0),
		heroHand = PocketCards(Card(Rank.ACE, Suit.SPADES), Card(Rank.KING, Suit.SPADES)),
		heroSeat = 3,
		heroStack = 50000.0,
		buttonSeat = 1,
		streets = HandStreets(
			preflop = PreflopStreet(
				actions = listOf(
					Action(playerSeat = 4, type = ActionType.FOLD, stackBefore = 50000.0),
					Action(playerSeat = 5, type = ActionType.FOLD, stackBefore = 50000.0),
					Action(
						playerSeat = 6,
						type = ActionType.RAISE,
						amount = 2500.0,
						stackBefore = 50000.0,
						betLevel = 2,
					),
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
					Action(
						playerSeat = 6,
						type = ActionType.BET,
						amount = 5000.0,
						stackBefore = 47500.0,
						betLevel = 1,
					),
					Action(playerSeat = 3, type = ActionType.CALL, amount = 5000.0, stackBefore = 46500.0),
				),
			),
			turn = TurnStreet(
				card = Card(Rank.KING, Suit.HEARTS),
				actions = listOf(
					Action(playerSeat = 3, type = ActionType.CHECK, stackBefore = 36500.0),
					Action(
						playerSeat = 6,
						type = ActionType.BET,
						amount = 12000.0,
						stackBefore = 37500.0,
						betLevel = 1,
					),
					Action(
						playerSeat = 3,
						type = ActionType.RAISE,
						amount = 30000.0,
						stackBefore = 36500.0,
						betLevel = 2,
					),
					Action(playerSeat = 6, type = ActionType.ALL_IN, amount = 37500.0, stackBefore = 25500.0),
					Action(playerSeat = 3, type = ActionType.CALL, amount = 37500.0, stackBefore = 6500.0),
				),
			),
			river = RiverStreet(card = Card(Rank.TWO, Suit.CLUBS)),
		),
		result = 49000.0,
		memo = "탑투페어로 체크레이즈 → 올인 콜",
	)
	ThemePreview {
		HandDetailContent(
			hand = hand,
			useBbUnit = false,
			graphicsLayer = rememberGraphicsLayer(),
		)
	}
}
