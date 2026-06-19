package com.hand.log.settings.contact.contract

import androidx.compose.runtime.Immutable
import com.hand.log.domain.model.FeedbackCategory

@Immutable
internal data class ContactState(
	val category: FeedbackCategory = FeedbackCategory.FEATURE,
	val title: String = "",
	val content: String = "",
	val email: String = "",
	val isSubmitting: Boolean = false,
) {
	val canSubmit: Boolean
		get() = title.isNotBlank() && content.isNotBlank() && !isSubmitting
}
