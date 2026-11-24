package com.hand.log.preflop.drill

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hand.log.domain.model.ProFeature
import com.hand.log.domain.model.preflop.PreflopAction
import com.hand.log.domain.model.preflop.PreflopChart
import com.hand.log.domain.model.preflop.PreflopHand
import com.hand.log.domain.model.preflop.PreflopPositions
import com.hand.log.domain.model.preflop.PreflopScenario
import com.hand.log.domain.model.preflop.PreflopStack
import com.hand.log.domain.model.preflop.gradeChartDrill
import com.hand.log.domain.repository.ProEntitlementRepository
import com.hand.log.preflop.chart.data.PreflopCatalog
import com.hand.log.preflop.chart.data.PreflopChartRepository
import com.hand.log.preflop.drill.contract.DrillBrush
import com.hand.log.preflop.drill.contract.DrillLine
import com.hand.log.preflop.drill.contract.DrillMode
import com.hand.log.preflop.drill.contract.DrillPhase
import com.hand.log.preflop.drill.contract.DrillSpot
import com.hand.log.preflop.drill.contract.DrillSummaryItem
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

	fun selectMode(mode: DrillMode) {
		_state.update { it.copy(selectedMode = mode) }
	}

	fun start() {
		val s = _state.value
		when (s.selectedMode) {
			DrillMode.RANDOM -> {
				val spot = spots(s.selectedStack).randomOrNull() ?: return
				_state.update {
					it.copy(
						sequence = emptyList(),
						sequenceIndex = 0,
						sequenceResults = emptyMap(),
					)
				}
				paint(spot)
			}
			DrillMode.SEQUENTIAL -> startSequence(s.selectedStack)
		}
	}

	fun nextSpot() {
		val s = _state.value
		val current = s.spot ?: return
		if (s.sequence.isEmpty()) {
			val all = spots(current.stack)
			val spot = all.filter { it.hero != current.hero }.ifEmpty { all }.randomOrNull() ?: return
			paint(spot)
			return
		}
		if (s.isLastSpot) {
			showSummary()
			return
		}
		val index = s.sequenceIndex + 1
		_state.update { it.copy(sequenceIndex = index) }
		paint(s.sequence[index], painted = s.painted, checkedLines = s.checkedLines)
	}

	fun restartSequence() {
		val stack = (_state.value.phase as? DrillPhase.Summary)?.stack ?: return
		startSequence(stack)
	}

	fun retry() {
		val s = _state.value
		val spot = s.spot ?: return
		paint(spot, painted = s.painted, checkedLines = s.checkedLines)
	}

	fun backToSetup() {
		strokeMode = null
		_state.update {
			it.copy(
				phase = DrillPhase.Setup,
				sequence = emptyList(),
				sequenceIndex = 0,
				sequenceResults = emptyMap(),
				painted = emptyMap(),
				checkedLines = emptySet(),
				brush = null,
				strokeHand = null,
				showAnswer = false,
			)
		}
	}

	fun openBrushPicker() {
		val s = _state.value
		if (!s.canPickBrush) return
		_modalEffect.update { PreflopChartDrillModalEffect.ShowBrushPicker(s.palette, s.brush) }
	}

	fun selectBrush(action: PreflopAction) {
		_state.update { it.copy(brush = action) }
		dismissModal()
	}

	fun toggleLine(line: DrillLine) {
		_state.update { s ->
			if (s.phase !is DrillPhase.Painting) return@update s
			if (line in s.checkedLines) {
				val remaining = s.checkedLines - line
				val kept = remaining.flatMapTo(mutableSetOf()) { it.hands }
				val erased = line.hands.filterNot { it in kept }.map(PreflopHand::notation)
				s.copy(painted = s.painted - erased.toSet(), checkedLines = remaining)
			} else {
				val brush = s.brush ?: return@update s
				s.copy(
					painted = s.painted + line.hands.associate { it.notation to brush },
					checkedLines = s.checkedLines + line,
				)
			}
		}
	}

	fun strokeStart(hand: PreflopHand) {
		val s = _state.value
		if (s.phase !is DrillPhase.Painting) return
		val brush = s.brush ?: return
		strokeMode = if (s.painted[hand.notation] == brush) DrillBrush.Erase else DrillBrush.Paint(brush)
		applyStroke(hand)
	}

	fun strokeMove(hand: PreflopHand) = applyStroke(hand)

	fun strokeEnd() {
		strokeMode = null
		_state.update { it.copy(strokeHand = null) }
	}

	fun reset() {
		_state.update { it.copy(painted = emptyMap(), checkedLines = emptySet()) }
	}

	fun grade() {
		_state.update { s ->
			val phase = s.phase as? DrillPhase.Painting ?: return@update s
			val result = gradeChartDrill(phase.spot.answer.actions, s.painted)
			s.copy(
				phase = DrillPhase.Graded(phase.spot, result),
				showAnswer = false,
				sequenceResults = if (s.sequence.isEmpty()) s.sequenceResults else s.sequenceResults + (phase.spot.hero to result),
			)
		}
	}

	fun showAnswer(show: Boolean) {
		_state.update { it.copy(showAnswer = show) }
	}

	private fun applyStroke(hand: PreflopHand) {
		val mode = strokeMode ?: return
		_state.update {
			when (mode) {
				is DrillBrush.Paint -> it.copy(
					painted = it.painted + (hand.notation to mode.action),
					strokeHand = hand,
				)
				DrillBrush.Erase -> it.copy(
					painted = it.painted - hand.notation,
					checkedLines = it.checkedLines.filterNot { line -> hand in line.hands }.toSet(),
					strokeHand = hand,
				)
			}
		}
	}

	private fun paint(
		spot: DrillSpot,
		painted: Map<String, PreflopAction> = emptyMap(),
		checkedLines: Set<DrillLine> = emptySet(),
	) {
		strokeMode = null
		_state.update {
			val palette = spot.answer.presentActions
			it.copy(
				phase = DrillPhase.Painting(spot),
				painted = painted,
				checkedLines = checkedLines,
				strokeHand = null,
				brush = it.brush.takeIf { brush -> painted.isNotEmpty() && brush in palette } ?: palette.firstOrNull(),
				showAnswer = false,
			)
		}
	}

	private fun startSequence(stack: PreflopStack) {
		val sequence = spots(stack).sortedBy { PreflopPositions.order.indexOf(it.hero) }
		val first = sequence.firstOrNull() ?: return
		_state.update { it.copy(sequence = sequence, sequenceIndex = 0, sequenceResults = emptyMap()) }
		paint(first)
	}

	private fun showSummary() {
		strokeMode = null
		_state.update { s ->
			val stack = s.spot?.stack ?: return@update s
			val items = s.sequence.mapNotNull { spot ->
				s.sequenceResults[spot.hero]?.let { DrillSummaryItem(spot.hero, it.accuracyPct) }
			}
			s.copy(
				phase = DrillPhase.Summary(stack, items),
				painted = emptyMap(),
				checkedLines = emptySet(),
				strokeHand = null,
				showAnswer = false,
			)
		}
	}

	private fun spots(stack: PreflopStack): List<DrillSpot> = catalog.charts
		.filterKeys { it.stack == stack && it.scenario == PreflopScenario.RFI }
		.map { (query, chart) -> DrillSpot(stack, query.hero, chart.withoutFold()) }
		.filterNot { it.answer.isEmpty }
		.distinctBy { it.hero }

	private fun PreflopChart.withoutFold(): PreflopChart =
		PreflopChart(actions.filterValues { it != PreflopAction.FOLD })
}
