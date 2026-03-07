package com.hand.log.home

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable

@Stable
internal sealed interface HomeState {

    @Immutable
    data object Loading : HomeState

    @Immutable
    data class HomeData(
        // TODO: Data를 정의해야 합니다.
    ) : HomeState
}