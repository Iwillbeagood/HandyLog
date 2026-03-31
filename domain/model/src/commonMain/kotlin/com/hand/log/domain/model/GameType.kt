package com.hand.log.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

enum class GameTypeEnum {
	TOURNAMENT,
	CASH,
}

@Serializable
sealed class GameType {

	abstract val gameTypeEnum: GameTypeEnum

	@Serializable
	@SerialName("TOURNAMENT")
	data class Tournament(
		val isBigBlindAnte: Boolean = false,
	) : GameType() {
		@Transient
		override val gameTypeEnum: GameTypeEnum = GameTypeEnum.TOURNAMENT
	}

	@Serializable
	@SerialName("CASH")
	data class Cash(
		val sb: Double,
		val bb: Double,
		val straddle: Double? = null,
	) : GameType() {
		@Transient
		override val gameTypeEnum: GameTypeEnum = GameTypeEnum.CASH
	}
}
