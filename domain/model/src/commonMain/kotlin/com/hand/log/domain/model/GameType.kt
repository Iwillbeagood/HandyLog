package com.hand.log.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

enum class GameTypeEnum {
	TOURNAMENT,
	CASH,
}

@Serializable
sealed class GameType(val type: GameTypeEnum) {

	@Serializable
	@SerialName("TOURNAMENT")
	data class Tournament(
		val isBigBlindAnte: Boolean = false,
	) : GameType(GameTypeEnum.TOURNAMENT)

	@Serializable
	@SerialName("CASH")
	data class Cash(
		val sb: Double,
		val bb: Double,
		val straddle: Double? = null,
	) : GameType(GameTypeEnum.CASH)
}
