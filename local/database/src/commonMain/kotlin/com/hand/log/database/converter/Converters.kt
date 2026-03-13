package com.hand.log.database.converter

import androidx.room.TypeConverter
import com.hand.log.domain.model.ActionType
import com.hand.log.domain.model.GameType
import com.hand.log.domain.model.PlayerTendency
import com.hand.log.domain.model.Street
import kotlinx.datetime.LocalDate

class Converters {
	@TypeConverter
	fun fromGameType(value: GameType): String = value.name

	@TypeConverter
	fun toGameType(value: String): GameType = GameType.valueOf(value)

	@TypeConverter
	fun fromActionType(value: ActionType): String = value.name

	@TypeConverter
	fun toActionType(value: String): ActionType = ActionType.valueOf(value)

	@TypeConverter
	fun fromStreet(value: Street): String = value.name

	@TypeConverter
	fun toStreet(value: String): Street = Street.valueOf(value)

	@TypeConverter
	fun fromPlayerTendency(value: PlayerTendency?): String? = value?.name

	@TypeConverter
	fun toPlayerTendency(value: String?): PlayerTendency? = value?.let { PlayerTendency.valueOf(it) }

	@TypeConverter
	fun fromLocalDate(value: LocalDate): String = value.toString()

	@TypeConverter
	fun toLocalDate(value: String): LocalDate = LocalDate.parse(value)
}
