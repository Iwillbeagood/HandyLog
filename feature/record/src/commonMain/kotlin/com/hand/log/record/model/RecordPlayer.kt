package com.hand.log.record.model

import androidx.compose.runtime.Immutable
import com.hand.log.domain.model.Card
import com.hand.log.domain.model.PocketCards
import com.hand.log.domain.model.ShowdownEntry

enum class PlayerStatus {
	ACTIVE,
	FOLDED,
	ALL_IN,
}

@Immutable
data class RecordPlayer(
	val seat: Int,
	val cards: PocketCards? = null,
	val stack: Double = 0.0,
	val initialStack: Double? = null,
	val status: PlayerStatus = PlayerStatus.ACTIVE,
	val currentBet: Double = 0.0,
	val isCardsUnknown: Boolean = false,
)

@Immutable
data class RecordPlayers(
	private val map: Map<Int, RecordPlayer> = emptyMap(),
) {
	private val active: List<RecordPlayer>
		get() = map.values.toList()

	operator fun get(seat: Int): RecordPlayer? = map[seat]

	/** 스택 반환. initialStack이 null이면(미입력) null 반환 = 무제한 */
	fun getStack(seat: Int): Double? {
		val player = get(seat) ?: return null
		return if (player.initialStack == null) null else player.stack
	}

	fun getInitialStack(seat: Int): Double? = get(seat)?.initialStack

	val foldedSeats: Set<Int>
		get() = active.filter { it.status == PlayerStatus.FOLDED }.map { it.seat }.toSet()

	val allInSeats: Set<Int>
		get() = active.filter { it.status == PlayerStatus.ALL_IN }.map { it.seat }.toSet()

	val inactiveSeats: Set<Int>
		get() = foldedSeats + allInSeats

	val seats: List<Int>
		get() = map.keys.sorted()

	fun update(seat: Int, block: RecordPlayer.() -> RecordPlayer): RecordPlayers {
		val player = map[seat] ?: return this
		return RecordPlayers(map + (seat to player.block()))
	}

	fun resetCurrentBets(): RecordPlayers =
		RecordPlayers(map.mapValues { (_, p) -> p.copy(currentBet = 0.0) })

	/** 모든 플레이어의 카드 (히어로 + 쇼다운) */
	val allCards: Set<Card>
		get() = map.values.mapNotNull { it.cards }
			.flatMap { listOf(it.card1, it.card2) }
			.toSet()

	/** 쇼다운 엔트리 변환 (히어로 제외, 카드 있는 플레이어만) */
	fun toShowdownEntries(heroSeat: Int): List<ShowdownEntry> = buildList {
		map.values.filter { it.seat != heroSeat && it.cards != null }.forEach { p ->
			add(ShowdownEntry(seat = p.seat, cards = p.cards!!))
		}
	}

	companion object {
		fun create(playerCount: Int, defaultStack: Double, stacks: Map<Int, Double> = emptyMap()): RecordPlayers {
			val map = (1..playerCount).associate { seat ->
				val s = stacks[seat] ?: defaultStack
				val initial: Double? = if (s == 0.0) null else s
				seat to RecordPlayer(seat = seat, stack = s, initialStack = initial)
			}
			return RecordPlayers(map)
		}
	}
}
