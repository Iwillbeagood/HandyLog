package {{packageName}}.{{featureName}}

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import {{packageName}}.model.error.MessageType

@Stable
internal sealed interface {{className}}ModalEffect {

    @Immutable
    data object Idle : {{className}}ModalEffect

}

@Stable
internal sealed interface {{className}}Effect {

    @Immutable
    data class ShowSnackBar(val messageType: MessageType) : {{className}}Effect
}