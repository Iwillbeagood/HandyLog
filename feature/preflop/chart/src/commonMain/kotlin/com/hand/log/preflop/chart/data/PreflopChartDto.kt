package com.hand.log.preflop.chart.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** *_preflop_charts.json 의 개별 차트 항목. page/matrix 등 나머지 필드는 무시한다. */
@Serializable
internal data class PreflopChartDto(
	@SerialName("stack_size") val stackSize: String,
	val category: String,
	@SerialName("chart_name") val chartName: String,
	val actions: Map<String, String>,
)
