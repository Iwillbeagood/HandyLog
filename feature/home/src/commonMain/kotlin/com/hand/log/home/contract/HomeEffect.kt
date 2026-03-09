package com.hand.log.home.contract

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.hand.log.domain.model.etc.error.MessageType

@Stable
internal sealed interface HomeModalEffect {

	@Immutable
	data object Idle : HomeModalEffect

}

@Stable
internal sealed interface HomeEffect {

	@Immutable
	data class ShowSnackBar(val messageType: MessageType) : HomeEffect
}
