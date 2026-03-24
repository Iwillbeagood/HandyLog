package com.hand.log.domain.usecase

import com.hand.log.domain.model.Player
import com.hand.log.domain.model.PokerTable
import com.hand.log.domain.repository.PokerTableRepository

class CreatePokerTableUseCase(
	private val pokerTableRepository: PokerTableRepository,
) {

	/**
	 * 새 테이블 생성 시 모든 플레이어의 스택을 startingStack으로 초기화하여 저장.
	 */
	suspend operator fun invoke(table: PokerTable): PokerTable {
		val players = (1..table.playerCount).map { seat ->
			Player(seat = seat, stack = table.startingStack)
		}
		return pokerTableRepository.saveTable(table.copy(players = players))
	}
}
