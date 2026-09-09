package com.hand.log.preflop.chart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hand.log.domain.model.Position
import com.hand.log.domain.model.ProFeature
import com.hand.log.domain.model.preflop.PreflopAction
import com.hand.log.domain.model.preflop.PreflopChart
import com.hand.log.domain.model.preflop.PreflopChartQuery
import com.hand.log.domain.model.preflop.PreflopPositions
import com.hand.log.domain.model.preflop.PreflopScenario
import com.hand.log.domain.model.preflop.PreflopSelection
import com.hand.log.domain.model.preflop.PreflopStack
import com.hand.log.domain.repository.AppSettingsRepository
import com.hand.log.domain.usecase.CheckFeatureLimitUseCase
import com.hand.log.preflop.chart.contract.PreflopChartModalEffect
import com.hand.log.preflop.chart.contract.PreflopChartState
import com.hand.log.preflop.chart.data.PreflopCatalog
import com.hand.log.preflop.chart.data.PreflopChartRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class PreflopChartViewModel(
	private val repository: PreflopChartRepository,
	private val appSettingsRepository: AppSettingsRepository,
	private val checkFeatureLimit: CheckFeatureLimitUseCase,
) : ViewModel() {

	private var catalog: PreflopCatalog = PreflopCatalog(emptyMap(), emptyMap())

	private var isPro: Boolean = false

	private val _state = MutableStateFlow(
		resolve(PreflopStack.BB100, PreflopScenario.RFI, Position.BTN, null, loading = true),
	)
	val state: StateFlow<PreflopChartState> = _state

	private val _modalEffect = MutableStateFlow<PreflopChartModalEffect>(PreflopChartModalEffect.Idle)
	val modalEffect: StateFlow<PreflopChartModalEffect> get() = _modalEffect

	private var pendingSpot: PreflopSelection? = null
	private var catalogLoaded = false

	init {
		viewModelScope.launch {
			catalog = repository.load()
			isPro = checkFeatureLimit.canUseAllPreflopStacks()
			catalogLoaded = true
			// 저장된 선택을 먼저 읽은(suspend) 뒤 pendingSpot 을 확인한다. 로드 중 openSpot(리뷰의
			// 차트 보기)이 들어오면 pendingSpot 이 반영되어, 저장된 선택이 스팟을 덮어쓰지 않는다.
			val saved = appSettingsRepository.observePreflopSelection().first()
			val sel = pendingSpot ?: saved
			_state.update { resolve(sel.stack, sel.scenario, sel.hero, sel.villain) }
		}
	}

	/** 특정 스팟(퀴즈 리뷰 등)으로 차트를 연다. 사용자의 저장된 선택은 덮어쓰지 않는다. */
	fun openSpot(selection: PreflopSelection) {
		pendingSpot = selection
		if (catalogLoaded) {
			_state.update { resolve(selection.stack, selection.scenario, selection.hero, selection.villain) }
		}
	}

	fun selectStack(stack: PreflopStack) {
		// 무료 사용자가 100BB 외 스택을 고르면 전환 대신 Paywall 을 띄운다.
		if (!isPro && stack != PreflopStack.FREE) {
			_modalEffect.update { PreflopChartModalEffect.ShowPaywall(ProFeature.PREFLOP_STACKS) }
			return
		}
		_state.update { resolve(stack, it.scenario, it.hero, it.villain) }
		persistSelection()
	}

	fun dismissModal() {
		_modalEffect.update { PreflopChartModalEffect.Idle }
	}

	fun selectScenario(scenario: PreflopScenario) {
		_state.update { resolve(it.stack, scenario, it.hero, it.villain) }
		persistSelection()
	}

	fun selectHero(hero: Position) {
		// 히어로를 바꾸면 상대는 항상 첫 옵션(가장 앞 포지션)으로 초기화한다.
		_state.update { resolve(it.stack, it.scenario, hero, null) }
		persistSelection()
	}

	fun selectVillain(villain: Position) {
		_state.update { resolve(it.stack, it.scenario, it.hero, villain) }
		persistSelection()
	}

	fun selectSeat(position: Position) {
		val s = _state.value
		if (position == s.hero) return
		if (position == s.villain) {
			_state.update { resolve(it.stack, PreflopScenario.RFI, it.hero, null) }
			persistSelection()
			return
		}
		val order = s.tableSeatOrder
		val heroIdx = order.indexOf(s.hero)
		val posIdx = order.indexOf(position)
		if (heroIdx < 0 || posIdx < 0) return
		val scenario = if (posIdx < heroIdx) PreflopScenario.FACING_RFI else PreflopScenario.VS_3BET
		_state.update { resolve(it.stack, scenario, it.hero, position) }
		persistSelection()
	}

	fun nextHero() = moveHero(-1)

	fun prevHero() = moveHero(1)

	private fun moveHero(direction: Int) {
		val s = _state.value
		val order = s.tableSeatOrder
		if (order.size <= 1) return
		val currentIndex = order.indexOf(s.hero).coerceAtLeast(0)
		for (step in 1..order.size) {
			val idx = ((currentIndex + direction * step) % order.size + order.size) % order.size
			val candidate = order[idx]
			val spot = spotFor(candidate, s.stack) ?: continue
			_state.update { resolve(it.stack, spot.first, candidate, spot.second) }
			persistSelection()
			return
		}
	}

	private fun spotFor(pos: Position, stack: PreflopStack): Pair<PreflopScenario, Position?>? {
		if (hasHeroData(stack, PreflopScenario.RFI, pos)) {
			return PreflopScenario.RFI to null
		}
		val opener = PreflopPositions.raisersBefore(pos).firstOrNull { v ->
			catalog.charts.containsKey(PreflopChartQuery(stack, PreflopScenario.FACING_RFI, pos, v))
		}
		return opener?.let { PreflopScenario.FACING_RFI to it }
	}

	private fun persistSelection() {
		val s = _state.value
		viewModelScope.launch {
			appSettingsRepository.setPreflopSelection(
				PreflopSelection(s.stack, s.scenario, s.hero, s.villain),
			)
		}
	}

	/** 선택값을 유효 범위로 정규화한 뒤 로드된 카탈로그에서 차트를 찾아 State 를 구성한다. */
	private fun resolve(
		requestedStack: PreflopStack,
		scenario: PreflopScenario,
		hero: Position,
		desiredVillain: Position?,
		loading: Boolean = false,
	): PreflopChartState {
		// 무료 사용자는 어떤 입력이 와도 항상 100BB 차트만 본다.
		val stack = if (isPro) requestedStack else PreflopStack.FREE
		// 데이터(차트)가 없는 시나리오는 옵션에서 제외하고, 현재 시나리오가 없으면 첫 옵션으로 대체한다.
		val scenarioOptions = PreflopScenario.entries.filter { s -> hasScenarioData(stack, s) }
		val resolvedScenario = if (scenario in scenarioOptions) {
			scenario
		} else {
			scenarioOptions.firstOrNull() ?: scenario
		}

		// vs 오픈(FACING_RFI)은 앞에 레이저가 있어야 하므로 UTG 를 히어로에서 제외한다.
		// 데이터(차트)가 없는 포지션은 옵션에서 제외한다.
		val candidateHeroes = when (resolvedScenario) {
			PreflopScenario.FACING_RFI -> PreflopPositions.facingRfiHeroes
			else -> PreflopPositions.order
		}.filter { h -> hasHeroData(stack, resolvedScenario, h) }
		val resolvedHero = if (hero in candidateHeroes) hero else candidateHeroes.firstOrNull() ?: hero
		// 모든 (상대→원본그룹) 매핑이 완전히 동일한 히어로끼리 하나의 칩으로 병합한다(데이터 손실 없음).
		// VS_LIMP 는 BB 고정 매치업이라 포지션 선택 칩을 숨긴다.
		val heroOptions = if (resolvedScenario == PreflopScenario.VS_LIMP) {
			emptyList()
		} else {
			mergeBy(candidateHeroes) { h -> heroSignature(stack, resolvedScenario, h) }
		}

		val candidateVillains = when (resolvedScenario) {
			PreflopScenario.RFI, PreflopScenario.VS_LIMP -> emptyList()
			PreflopScenario.FACING_RFI -> PreflopPositions.raisersBefore(resolvedHero)
			PreflopScenario.VS_3BET -> PreflopPositions.threeBettorsAfter(resolvedHero)
		}.filter { v ->
			catalog.charts.containsKey(
				PreflopChartQuery(stack, resolvedScenario, resolvedHero, v),
			)
		}
		val resolvedVillain = when {
			// SB 림프에 대응하는 BB — 상대는 항상 SB 로 고정.
			resolvedScenario == PreflopScenario.VS_LIMP -> Position.SB
			candidateVillains.isEmpty() -> null
			desiredVillain != null && desiredVillain in candidateVillains -> desiredVillain
			else -> candidateVillains.first()
		}
		// 같은 원본 그룹(예: "vs UTG/UTG+1")의 상대끼리 하나의 칩으로 병합한다.
		val villainOptions = mergeBy(candidateVillains) { v ->
			catalog.groupKeys[PreflopChartQuery(stack, resolvedScenario, resolvedHero, v)] ?: v.name
		}

		val rawChart = catalog.charts[PreflopChartQuery(stack, resolvedScenario, resolvedHero, resolvedVillain)]
			?: PreflopChart.EMPTY

		// 원본 차트를 그대로 표시한다. vs 3벳은 폴드까지 노출, 그 외 시나리오는 폴드 셀만 숨긴다.
		val filteredActions = if (resolvedScenario == PreflopScenario.VS_3BET) {
			rawChart.actions
		} else {
			rawChart.actions.filterValues { it != PreflopAction.FOLD }
		}

		val chart = PreflopChart(filteredActions)

		val rotatableHeroes = PreflopPositions.rfiHeroes.filter { h ->
			hasHeroData(
				stack,
				PreflopScenario.RFI,
				h,
			)
		}
		val openRaiserSeats = PreflopPositions.raisersBefore(resolvedHero).filter { v ->
			catalog.charts.containsKey(PreflopChartQuery(stack, PreflopScenario.FACING_RFI, resolvedHero, v))
		}
		val threeBettorSeats = PreflopPositions.threeBettorsAfter(resolvedHero).filter { v ->
			catalog.charts.containsKey(PreflopChartQuery(stack, PreflopScenario.VS_3BET, resolvedHero, v))
		}

		return PreflopChartState(
			stack = stack,
			scenario = resolvedScenario,
			hero = resolvedHero,
			villain = resolvedVillain,
			scenarioOptions = scenarioOptions,
			heroOptions = heroOptions,
			villainOptions = villainOptions,
			rotatableHeroes = rotatableHeroes,
			openRaiserSeats = openRaiserSeats,
			threeBettorSeats = threeBettorSeats,
			chart = chart,
			isLoading = loading,
			isPro = isPro,
		)
	}

	/** 해당 스택에서 [scenario] 로 조회 가능한 차트가 하나라도 있는지. */
	private fun hasScenarioData(stack: PreflopStack, scenario: PreflopScenario): Boolean =
		catalog.charts.keys.any { it.stack == stack && it.scenario == scenario }

	/** 해당 스택·시나리오에서 [hero] 로 조회 가능한 차트가 하나라도 있는지. */
	private fun hasHeroData(stack: PreflopStack, scenario: PreflopScenario, hero: Position): Boolean =
		catalog.charts.keys.any { it.stack == stack && it.scenario == scenario && it.hero == hero }

	/** [hero] 의 (상대 → 원본그룹) 매핑. 이 매핑이 같은 히어로끼리는 완전히 호환되므로 병합 대상이 된다. */
	private fun heroSignature(stack: PreflopStack, scenario: PreflopScenario, hero: Position): Map<Position?, String> =
		catalog.charts.keys
			.filter { it.stack == stack && it.scenario == scenario && it.hero == hero }
			.associate { it.villain to (catalog.groupKeys[it] ?: "") }

	/** [items] 를 [key] 값이 같은 것끼리 원래 순서를 유지하며 그룹으로 묶는다. */
	private fun <T, K> mergeBy(items: List<T>, key: (T) -> K): List<List<T>> {
		val groups = LinkedHashMap<K, MutableList<T>>()
		items.forEach { item -> groups.getOrPut(key(item)) { mutableListOf() }.add(item) }
		return groups.values.map { it.toList() }
	}
}
