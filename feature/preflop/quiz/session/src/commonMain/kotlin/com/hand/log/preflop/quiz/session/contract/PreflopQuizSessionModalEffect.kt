package com.hand.log.preflop.quiz.session.contract

internal sealed interface PreflopQuizSessionModalEffect {
	data object Idle : PreflopQuizSessionModalEffect
	data object ConfirmExit : PreflopQuizSessionModalEffect
}
