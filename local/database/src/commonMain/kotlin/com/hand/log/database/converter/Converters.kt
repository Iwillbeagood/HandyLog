package com.hand.log.database.converter

import androidx.room.TypeConverter
import com.hand.log.domain.model.ActionType
import com.hand.log.domain.model.Blinds
import com.hand.log.domain.model.GameType
import com.hand.log.domain.model.HandStreets
import com.hand.log.domain.model.HeroHand
import com.hand.log.domain.model.ShowdownEntry
import com.hand.log.domain.model.PlayerTendency
import com.hand.log.domain.model.Street
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.Json

private val json = Json { ignoreUnknownKeys = true }

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

	@TypeConverter
	fun fromBlinds(value: Blinds?): String? = value?.let { json.encodeToString(Blinds.serializer(), it) }

	@TypeConverter
	fun toBlinds(value: String?): Blinds? = value?.let { json.decodeFromString(Blinds.serializer(), it) }

	@TypeConverter
	fun fromHeroHand(value: HeroHand?): String? = value?.let { json.encodeToString(HeroHand.serializer(), it) }

	@TypeConverter
	fun toHeroHand(value: String?): HeroHand? = value?.let { json.decodeFromString(HeroHand.serializer(), it) }

	@TypeConverter
	fun fromHandStreets(value: HandStreets): String = json.encodeToString(HandStreets.serializer(), value)

	@TypeConverter
	fun toHandStreets(value: String): HandStreets = json.decodeFromString(HandStreets.serializer(), value)

	@TypeConverter
	fun fromShowdown(value: List<ShowdownEntry>): String = json.encodeToString(value)

	@TypeConverter
	fun toShowdown(value: String): List<ShowdownEntry> = json.decodeFromString(value)
}
