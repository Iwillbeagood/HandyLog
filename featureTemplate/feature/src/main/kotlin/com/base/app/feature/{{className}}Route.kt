package {{packageName}}.{{featureName}}

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import {{packageName}}.designsystem.component.HmDefaultScaffold
import {{packageName}}.designsystem.component.HmFadeAnimatedVisibility
import {{packageName}}.designsystem.component.HmTopAppbar
import {{packageName}}.designsystem.theme.ChangeStatusBarColor
import {{packageName}}.model.error.MessageType

@Composable
internal fun {{className}}Route(
    onGoBack: () -> Unit,
    onShowSnackBar: (MessageType) -> Unit,
    viewModel: {{className}}ViewModel = hiltViewModel(),
) {
    ChangeStatusBarColor(MaterialTheme.colorScheme.surface)

    val {{lowerCamelClassName}}State by viewModel.{{lowerCamelClassName}}State.collectAsStateWithLifecycle()
    val {{lowerCamelClassName}}ModalEffect by viewModel.{{lowerCamelClassName}}ModalEffect.collectAsStateWithLifecycle()

    {{className}}Content(
        {{lowerCamelClassName}}State = {{lowerCamelClassName}}State,
        onGoBack = onGoBack
    )

    {{className}}ModalContent(
        {{lowerCamelClassName}}ModalEffect = {{lowerCamelClassName}}ModalEffect,
        onDismissRequest = viewModel::dismissDialog,
    )

	LaunchedEffect(true) {
		viewModel.{{lowerCamelClassName}}Effect.collect { effect ->
			when (effect) {
				is {{className}}Effect.ShowSnackBar -> onShowSnackBar(effect.messageType)
			}
		}
	}
}

@Composable
private fun {{className}}Content(
    {{lowerCamelClassName}}State: {{className}}State,
    onGoBack: () -> Unit,
) {
    HmDefaultScaffold(
        topBar = {
            HmTopAppbar(
                title = "TODO: 타이틀을 알맞게 설정해야 합니다.",
                onBackEvent = onGoBack,
            )
        }
    ) {
        HmFadeAnimatedVisibility({{lowerCamelClassName}}State is {{className}}State.{{className}}Data) {
            if ({{lowerCamelClassName}}State is {{className}}State.{{className}}Data) {
                {{className}}Screen(
                    {{lowerCamelClassName}}State = {{lowerCamelClassName}}State
                )
            }
        }
    }
}

@Composable
private fun {{className}}ModalContent(
    {{lowerCamelClassName}}ModalEffect: {{className}}ModalEffect,
    onDismissRequest: () -> Unit,
) {
    when ({{lowerCamelClassName}}ModalEffect) {
        {{className}}ModalEffect.Idle -> {}
    }
}
