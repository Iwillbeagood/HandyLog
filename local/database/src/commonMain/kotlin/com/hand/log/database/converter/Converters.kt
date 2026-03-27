package com.hand.log.database.converter

import androidx.room.TypeConverter
import com.hand.log.domain.model.ActionType
import com.hand.log.domain.model.Blinds
import com.hand.log.domain.model.GameType
import com.hand.log.domain.model.HandStreets
import com.hand.log.domain.model.PocketCards
import com.hand.log.domain.model.ShowdownEntry
import com.hand.log.domain.model.ShowdownResult
import com.hand.log.domain.model.PlayerTendency
import com.hand.log.domain.model.Street
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.Json

private val json = Json { ignoreUnknownKeys = true }

class Converters {
	@TypeConverter
	fun fromGameType(value: GameType): String = json.encodeToString(GameType.serializer(), value)

	@TypeConverter
	fun toGameType(value: String): GameType {
		// 이전 enum 형태 ("TOURNAMENT", "CASH") 호환
		return when (value) {
			"TOURNAMENT" -> GameType.Tournament()
			"CASH" -> GameType.Cash(sb = 0.0, bb = 0.0)
			else -> json.decodeFromString(GameType.serializer(), value)
		}
	}

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
	fun fromBlinds(value: Blinds?): String? = value?.let { json.encodeToString(Blinds.serializer(), it) }

	@TypeConverter
	fun toBlinds(value: String?): Blinds? = value?.let { json.decodeFromString(Blinds.serializer(), it) }

	@TypeConverter
	fun fromLocalDate(value: LocalDate): String = value.toString()

	@TypeConverter
	fun toLocalDate(value: String): LocalDate = LocalDate.parse(value)

	@TypeConverter
	fun fromPocketCards(value: PocketCards?): String? = value?.let { json.encodeToString(PocketCards.serializer(), it) }

	@TypeConverter
	fun toPocketCards(value: String?): PocketCards? = value?.let { json.decodeFromString(PocketCards.serializer(), it) }

	@TypeConverter
	fun fromHandStreets(value: HandStreets): String = json.encodeToString(HandStreets.serializer(), value)

	@TypeConverter
	fun toHandStreets(value: String): HandStreets = json.decodeFromString(HandStreets.serializer(), value)

	@TypeConverter
	fun fromShowdown(value: List<ShowdownEntry>): String = json.encodeToString(value)

	@TypeConverter
	fun toShowdown(value: String): List<ShowdownEntry> = json.decodeFromString(value)

	@TypeConverter
	fun fromShowdownResults(value: List<ShowdownResult>): String = json.encodeToString(value)

	@TypeConverter
	fun toShowdownResults(value: String): List<ShowdownResult> = json.decodeFromString(value)
}
