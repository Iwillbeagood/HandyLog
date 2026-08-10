package com.hand.log.preflop.quiz.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hand.log.domain.model.preflop.QuizRecord
import com.hand.log.domain.repository.QuizRecordRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

internal class PreflopQuizViewModel(
	quizRecordRepository: QuizRecordRepository,
) : ViewModel() {

	val recentRecords: StateFlow<List<QuizRecord>> =
		quizRecordRepository.observeRecent(RECENT_LIMIT)
			.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

	companion object {
		const val RECENT_LIMIT = 5
	}
}
