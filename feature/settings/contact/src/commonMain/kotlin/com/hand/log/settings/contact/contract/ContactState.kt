package com.hand.log.settings.contact.contract

import androidx.compose.runtime.Immutable

@Immutable
internal data class ContactState(
	val title: String = "",
	val content: String = "",
	val email: String = "",
	val isSubmitting: Boolean = false,
) {
	val canSubmit: Boolean
		get() = title.isNotBlank() && content.isNotBlank() && !isSubmitting
}
