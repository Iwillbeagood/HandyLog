package com.hand.log.preflop.quiz.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hand.log.domain.model.preflop.PreflopChart
import com.hand.log.domain.model.preflop.PreflopChartQuery
import com.hand.log.domain.model.preflop.PreflopStack
import com.hand.log.domain.repository.QuizRecordRepository
import com.hand.log.preflop.chart.data.PreflopChartRepository
import com.hand.log.preflop.quiz.common.PreflopQuizType
import com.hand.log.preflop.quiz.common.QuizQuestionGenerator
import com.hand.log.preflop.quiz.home.contract.PreflopQuizHomeState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class PreflopQuizViewModel(
	quizRecordRepository: QuizRecordRepository,
	private val chartRepository: PreflopChartRepository,
	private val generator: QuizQuestionGenerator,
) : ViewModel() {

	private val selectedStack = MutableStateFlow<PreflopStack?>(null)
	private val charts = MutableStateFlow<Map<PreflopChartQuery, PreflopChart>>(emptyMap())

	val state: StateFlow<PreflopQuizHomeState> = combine(
		quizRecordRepository.observeRecent(RECENT_LIMIT),
		selectedStack,
		charts,
	) { records, stack, loaded ->
		PreflopQuizHomeState(
			selectedStack = stack,
			availableTypes = if (loaded.isEmpty()) {
				PreflopQuizType.entries.toSet()
			} else {
				generator.availableTypes(loaded, stack)
			},
			records = records,
		)
	}.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PreflopQuizHomeState())

	init {
		viewModelScope.launch {
			val loaded = chartRepository.load().charts
			charts.update { loaded }
		}
	}

	fun onStackSelect(stack: PreflopStack?) = selectedStack.update { stack }

	companion object {
		const val RECENT_LIMIT = 5
	}
}
