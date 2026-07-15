package com.hand.log.domain.model

import kotlinx.serialization.Serializable

/**
 * 핸드의 파생 결과를 저장 시점에 한 번 계산해 담아두는 값 객체.
 *
 * 입력의 단일 진실은 여전히 Action 로그이며, 이 값들은 그로부터 계산된 캐시다
 * (ADR-0002 참고). 읽기 경로는 매번 재계산하지 않고 이 필드를 읽는다.
 */
@Serializable
data class HandResults(
	val potByStreet: Map<Street, Double> = emptyMap(),
	val finalStacks: Map<Int, Double> = emptyMap(),
	val winnerSeats: Set<Int> = emptySet(),
	val seatInvestments: Map<Int, Double> = emptyMap(),
)
