package com.hand.log.record.quick.contract

import androidx.compose.runtime.Immutable
import com.hand.log.domain.model.Card
import com.hand.log.domain.model.quick.CardOwner

@Immutable
internal sealed interface QuickRecordModalEffect {
	data object Idle : QuickRecordModalEffect
	data object ShowGuide : QuickRecordModalEffect
	data class ShowSetup(
		val setup: QuickSetup,
		val switchToChipOnConfirm: Boolean = false,
	) : QuickRecordModalEffect
	data object ConfirmExit : QuickRecordModalEffect
	data class ShowHeroSelect(
		val positionOptions: List<String>,
		val heroPosition: String?,
	) : QuickRecordModalEffect
	data class ShowCardPicker(
		val owner: CardOwner,
		val positionLabel: String,
		val cardCount: Int,
		val initialCards: List<Card>,
		val usedCards: Set<Card>,
		val origin: CardPickerOrigin = CardPickerOrigin.SCREEN,
	) : QuickRecordModalEffect {
		val allowsUnknown: Boolean
			get() = origin != CardPickerOrigin.KEYBOARD
	}
}

internal enum class CardPickerOrigin {
	SCREEN,
	KEYBOARD,
}

@Immutable
internal sealed interface QuickRecordEffect {
	data object SaveSuccess : QuickRecordEffect
	data object SaveError : QuickRecordEffect
	data object NavigateBack : QuickRecordEffect
	data object InvalidLine : QuickRecordEffect
	data object CardConflict : QuickRecordEffect
	data object SetupConflict : QuickRecordEffect
}
