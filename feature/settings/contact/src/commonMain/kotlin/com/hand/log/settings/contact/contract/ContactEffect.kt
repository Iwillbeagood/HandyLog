package com.hand.log.settings.contact.contract

import androidx.compose.runtime.Immutable

@Immutable
internal sealed interface ContactEffect {
	data object SubmitSuccess : ContactEffect
	data object SubmitError : ContactEffect
}
