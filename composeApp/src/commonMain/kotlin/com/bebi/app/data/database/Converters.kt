package com.bebi.app.data.database

import androidx.room.TypeConverter
import kotlinx.serialization.json.Json

/**
 * Converters for Room database
 */
class Converters {
    
    /**
     * Converts a List of Strings to a JSON string for storage
     */
    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return Json.encodeToString(value)
    }

    /**
     * Converts a JSON string back to a List of Strings
     */
    @TypeConverter
    fun toStringList(value: String): List<String> {
        if (value.isBlank()) return emptyList()
        return try {
            Json.decodeFromString(value)
        } catch (e: Exception) {
            emptyList()
        }
    }
}
