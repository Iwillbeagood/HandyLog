package com.hand.log.record.quick.contract

import androidx.compose.runtime.Immutable
import com.hand.log.domain.model.Card
import com.hand.log.domain.model.Position
import com.hand.log.domain.model.quick.QuickShowdownHand

@Immutable
internal data class QuickFinishForm(
	val heroPositionOptions: List<String>,
	val heroPosition: Position?,
	val heroCards: List<Card>,
	val showdown: List<QuickShowdownHand>,
	val board: List<Card>,
	val showCards: Boolean,
	val showBoard: Boolean,
	val canSave: Boolean,
)
