package com.hand.log.preflop.home.contract

import com.hand.log.domain.model.ProFeature

internal data class PreflopHomeState(
	val quizLocked: Boolean = false,
)

internal sealed interface PreflopHomeModalEffect {
	data object Idle : PreflopHomeModalEffect
	data class ShowPaywall(val feature: ProFeature) : PreflopHomeModalEffect
}

internal sealed interface PreflopHomeEffect {
	data object NavigateToQuiz : PreflopHomeEffect
}
