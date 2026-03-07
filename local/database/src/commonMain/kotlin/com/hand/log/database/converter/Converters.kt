package com.hand.log.database.converter

import androidx.room.TypeConverter
import com.hand.log.domain.model.ActionType
import com.hand.log.domain.model.Position
import com.hand.log.domain.model.Street

class Converters {
    @TypeConverter
    fun fromPosition(value: Position?): String? {
        return value?.name
    }

    @TypeConverter
    fun toPosition(value: String?): Position? {
        return value?.let { Position.valueOf(it) }
    }

    @TypeConverter
    fun fromActionType(value: ActionType): String {
        return value.name
    }

    @TypeConverter
    fun toActionType(value: String): ActionType {
        return ActionType.valueOf(value)
    }

    @TypeConverter
    fun fromStreet(value: Street): String {
        return value.name
    }

    @TypeConverter
    fun toStreet(value: String): Street {
        return Street.valueOf(value)
    }
}

