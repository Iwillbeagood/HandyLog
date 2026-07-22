package com.hand.log.settings.contact

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hand.log.domain.model.Feedback
import com.hand.log.domain.model.FeedbackImage
import com.hand.log.domain.usecase.SubmitFeedbackUseCase
import com.hand.log.settings.contact.contract.ContactEffect
import com.hand.log.settings.contact.contract.ContactState
import com.hand.log.platform.image.PickedImage
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class ContactViewModel(
	private val submitFeedback: SubmitFeedbackUseCase,
) : ViewModel() {

	private val _state = MutableStateFlow(ContactState())
	val state: StateFlow<ContactState> = _state.asStateFlow()

	private val _effect = MutableSharedFlow<ContactEffect>()
	val effect: SharedFlow<ContactEffect> get() = _effect.asSharedFlow()

	fun updateTitle(value: String) {
		_state.update { it.copy(title = value) }
	}

	fun updateContent(value: String) {
		_state.update { it.copy(content = value) }
	}

	fun updateEmail(value: String) {
		_state.update { it.copy(email = value) }
	}

	fun addImage(image: PickedImage) {
		_state.update {
			if (!it.canAddImage) it else it.copy(images = it.images + image)
		}
	}

	fun removeImage(index: Int) {
		_state.update {
			if (index !in it.images.indices) {
				it
			} else {
				it.copy(
					images = it.images.filterIndexed { i, _ -> i != index },
				)
			}
		}
	}

	fun submit() {
		val current = _state.value
		if (!current.canSubmit) return

		_state.update { it.copy(isSubmitting = true) }
		viewModelScope.launch {
			val result = submitFeedback(
				Feedback(
					title = current.title,
					content = current.content,
					email = current.email,
					images = current.images.map { FeedbackImage(it.bytes, it.mimeType) },
				),
			)
			_state.update { it.copy(isSubmitting = false) }
			_effect.emit(
				if (result.isSuccess) ContactEffect.SubmitSuccess else ContactEffect.SubmitError,
			)
		}
	}
}
