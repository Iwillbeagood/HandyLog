package {{packageName}}.{{featureName}}

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable

@Stable
internal sealed interface {{className}}State {

    @Immutable
    data object Loading : {{className}}State

    @Immutable
    data class {{className}}Data(
        // TODO: Data를 정의해야 합니다.
    ) : {{className}}State
}