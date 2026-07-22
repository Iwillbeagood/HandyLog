package com.hand.log.settings.contact.contract

import androidx.compose.runtime.Immutable
import com.hand.log.platform.image.PickedImage

@Immutable
internal data class ContactState(
	val title: String = "",
	val content: String = "",
	val email: String = "",
	val images: List<PickedImage> = emptyList(),
	val isSubmitting: Boolean = false,
) {
	val canSubmit: Boolean
		get() = title.isNotBlank() && content.isNotBlank() && !isSubmitting

	val canAddImage: Boolean
		get() = images.size < MAX_IMAGES

	companion object {
		const val MAX_IMAGES = 3
	}
}
