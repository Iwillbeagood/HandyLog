package com.hand.log.preflop.drill

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hand.log.domain.model.Position
import com.hand.log.domain.model.ProFeature
import com.hand.log.domain.model.preflop.PreflopAction
import com.hand.log.domain.model.preflop.PreflopChart
import com.hand.log.domain.model.preflop.PreflopHand
import com.hand.log.domain.model.preflop.PreflopScenario
import com.hand.log.domain.model.preflop.PreflopStack
import com.hand.log.domain.model.preflop.gradeChartDrill
import com.hand.log.domain.repository.ProEntitlementRepository
import com.hand.log.preflop.chart.data.PreflopCatalog
import com.hand.log.preflop.chart.data.PreflopChartRepository
import com.hand.log.preflop.drill.contract.DrillBrush
import com.hand.log.preflop.drill.contract.DrillPhase
import com.hand.log.preflop.drill.contract.DrillSpot
import com.hand.log.preflop.drill.contract.PreflopChartDrillModalEffect
import com.hand.log.preflop.drill.contract.PreflopChartDrillState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class PreflopChartDrillViewModel(
	private val chartRepository: PreflopChartRepository,
	proEntitlementRepository: ProEntitlementRepository,
) : ViewModel() {

	private var catalog: PreflopCatalog = PreflopCatalog(emptyMap(), emptyMap())

	private var strokeMode: DrillBrush? = null

	private val _modalEffect =
		MutableStateFlow<PreflopChartDrillModalEffect>(PreflopChartDrillModalEffect.Idle)
	val modalEffect: StateFlow<PreflopChartDrillModalEffect> get() = _modalEffect

	private val _state = MutableStateFlow(PreflopChartDrillState())
	val state: StateFlow<PreflopChartDrillState> get() = _state

	init {
		viewModelScope.launch {
			catalog = chartRepository.load()
			_state.update { it.copy(isLoading = false) }
		}
		viewModelScope.launch {
			proEntitlementRepository.observeIsPro().collect { pro ->
				_state.update {
					it.copy(isPro = pro, selectedStack = if (pro) it.selectedStack else PreflopStack.FREE)
				}
			}
		}
	}

	fun selectStack(stack: PreflopStack) {
		if (stack in _state.value.lockedStacks) {
			_modalEffect.update { PreflopChartDrillModalEffect.ShowPaywall(ProFeature.PREFLOP_STACKS) }
			return
		}
		_state.update { it.copy(selectedStack = stack) }
	}

	fun dismissModal() {
		_modalEffect.update { PreflopChartDrillModalEffect.Idle }
	}

	fun start() {
		val spot = randomSpot(_state.value.selectedStack, exclude = null) ?: return
		paint(spot)
	}

	fun nextSpot() {
		val current = _state.value.spot ?: return
		val spot = randomSpot(current.stack, exclude = current.hero) ?: return
		paint(spot)
	}

	fun retry() {
		val spot = _state.value.spot ?: return
		paint(spot)
	}

	fun backToSetup() {
		strokeMode = null
		_state.update {
			it.copy(phase = DrillPhase.Setup, painted = emptyMap(), brush = null, showAnswer = false)
		}
	}

	fun selectBrush(brush: DrillBrush) {
		_state.update { it.copy(brush = brush) }
	}

	fun strokeStart(hand: PreflopHand) {
		val s = _state.value
		if (s.phase !is DrillPhase.Painting) return
		val brush = s.brush ?: return
		strokeMode = if (brush is DrillBrush.Paint && s.painted[hand.notation] == brush.action) {
			DrillBrush.Erase
		} else {
			brush
		}
		applyStroke(hand)
	}

	fun strokeMove(hand: PreflopHand) = applyStroke(hand)

	fun strokeEnd() {
		strokeMode = null
	}

	fun reset() {
		_state.update { it.copy(painted = emptyMap()) }
	}

	fun grade() {
		_state.update { s ->
			val phase = s.phase as? DrillPhase.Painting ?: return@update s
			val result = gradeChartDrill(phase.spot.answer.actions, s.painted)
			s.copy(phase = DrillPhase.Graded(phase.spot, result), showAnswer = false)
		}
	}

	fun showAnswer(show: Boolean) {
		_state.update { it.copy(showAnswer = show) }
	}

	private fun applyStroke(hand: PreflopHand) {
		val mode = strokeMode ?: return
		_state.update {
			val painted = when (mode) {
				is DrillBrush.Paint -> it.painted + (hand.notation to mode.action)
				DrillBrush.Erase -> it.painted - hand.notation
			}
			it.copy(painted = painted)
		}
	}

	private fun paint(spot: DrillSpot) {
		strokeMode = null
		_state.update {
			it.copy(
				phase = DrillPhase.Painting(spot),
				painted = emptyMap(),
				brush = spot.answer.presentActions.firstOrNull()?.let(DrillBrush::Paint) ?: DrillBrush.Erase,
				showAnswer = false,
			)
		}
	}

	private fun randomSpot(stack: PreflopStack, exclude: Position?): DrillSpot? {
		val spots = catalog.charts
			.filterKeys { it.stack == stack && it.scenario == PreflopScenario.RFI }
			.map { (query, chart) -> DrillSpot(stack, query.hero, chart.withoutFold()) }
			.filterNot { it.answer.isEmpty }
			.distinctBy { it.hero }
		val candidates = spots.filter { it.hero != exclude }.ifEmpty { spots }
		return candidates.randomOrNull()
	}

	private fun PreflopChart.withoutFold(): PreflopChart =
		PreflopChart(actions.filterValues { it != PreflopAction.FOLD })
}
