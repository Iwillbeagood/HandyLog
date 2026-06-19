package com.hand.log.settings.contact

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hand.log.navigation.interop.LocalMainActionInterop
import com.hand.log.navigation.interop.LocalNavigateActionInterop
import com.hand.log.settings.contact.contract.ContactEffect
import handylog.core.res.generated.resources.Res
import handylog.core.res.generated.resources.contact_error
import handylog.core.res.generated.resources.contact_success

@Composable
internal fun ContactRoute(
	viewModel: ContactViewModel,
) {
	val state by viewModel.state.collectAsStateWithLifecycle()
	val navAction = LocalNavigateActionInterop.current
	val mainAction = LocalMainActionInterop.current

	ContactScreen(
		state = state,
		onCategorySelect = viewModel::selectCategory,
		onTitleChange = viewModel::updateTitle,
		onContentChange = viewModel::updateContent,
		onEmailChange = viewModel::updateEmail,
		onSubmit = viewModel::submit,
		onBack = navAction::popBackStack,
	)

	LaunchedEffect(Unit) {
		viewModel.effect.collect { effect ->
			when (effect) {
				ContactEffect.SubmitSuccess -> {
					mainAction.onShowToast(Res.string.contact_success)
					navAction.popBackStack()
				}
				ContactEffect.SubmitError -> {
					mainAction.onShowToast(Res.string.contact_error)
				}
			}
		}
	}
}
