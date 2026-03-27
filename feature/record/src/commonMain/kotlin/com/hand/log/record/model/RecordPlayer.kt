package com.hand.log.record.model

import androidx.compose.runtime.Immutable

enum class PlayerStatus {
	ACTIVE,
	FOLDED,
	ALL_IN,
}

@Immutable
data class RecordPlayer(
	val seat: Int,
	val stack: Double = 0.0,
	val initialStack: Double = 0.0,
	val status: PlayerStatus = PlayerStatus.ACTIVE,
	val currentBet: Double = 0.0,
)

@Immutable
data class RecordPlayers(
	val player1: RecordPlayer? = null,
	val player2: RecordPlayer? = null,
	val player3: RecordPlayer? = null,
	val player4: RecordPlayer? = null,
	val player5: RecordPlayer? = null,
	val player6: RecordPlayer? = null,
	val player7: RecordPlayer? = null,
	val player8: RecordPlayer? = null,
	val player9: RecordPlayer? = null,
	val player10: RecordPlayer? = null,
) {
	private val all: List<RecordPlayer?>
		get() = listOf(player1, player2, player3, player4, player5, player6, player7, player8, player9, player10)

	private val active: List<RecordPlayer>
		get() = all.filterNotNull()

	operator fun get(seat: Int): RecordPlayer? = when (seat) {
		1 -> player1
		2 -> player2
		3 -> player3
		4 -> player4
		5 -> player5
		6 -> player6
		7 -> player7
		8 -> player8
		9 -> player9
		10 -> player10
		else -> null
	}

	fun getStack(seat: Int): Double = get(seat)?.stack ?: 0.0

	val foldedSeats: Set<Int>
		get() = active.filter { it.status == PlayerStatus.FOLDED }.map { it.seat }.toSet()

	val allInSeats: Set<Int>
		get() = active.filter { it.status == PlayerStatus.ALL_IN }.map { it.seat }.toSet()

	val inactiveSeats: Set<Int>
		get() = foldedSeats + allInSeats

	fun update(seat: Int, block: RecordPlayer.() -> RecordPlayer): RecordPlayers {
		val player = get(seat) ?: return this
		val updated = player.block()
		return when (seat) {
			1 -> copy(player1 = updated)
			2 -> copy(player2 = updated)
			3 -> copy(player3 = updated)
			4 -> copy(player4 = updated)
			5 -> copy(player5 = updated)
			6 -> copy(player6 = updated)
			7 -> copy(player7 = updated)
			8 -> copy(player8 = updated)
			9 -> copy(player9 = updated)
			10 -> copy(player10 = updated)
			else -> this
		}
	}

	fun resetCurrentBets(): RecordPlayers = copy(
		player1 = player1?.copy(currentBet = 0.0),
		player2 = player2?.copy(currentBet = 0.0),
		player3 = player3?.copy(currentBet = 0.0),
		player4 = player4?.copy(currentBet = 0.0),
		player5 = player5?.copy(currentBet = 0.0),
		player6 = player6?.copy(currentBet = 0.0),
		player7 = player7?.copy(currentBet = 0.0),
		player8 = player8?.copy(currentBet = 0.0),
		player9 = player9?.copy(currentBet = 0.0),
		player10 = player10?.copy(currentBet = 0.0),
	)

	companion object {
		fun create(playerCount: Int, defaultStack: Double, stacks: Map<Int, Double> = emptyMap()): RecordPlayers {
			fun player(seat: Int): RecordPlayer? {
				val s = stacks[seat] ?: defaultStack
				return if (seat <= playerCount) {
					RecordPlayer(seat = seat, stack = s, initialStack = s)
				} else {
					null
				}
			}
			return RecordPlayers(
				player1 = player(1),
				player2 = player(2),
				player3 = player(3),
				player4 = player(4),
				player5 = player(5),
				player6 = player(6),
				player7 = player(7),
				player8 = player(8),
				player9 = player(9),
				player10 = player(10),
			)
		}
	}
}
