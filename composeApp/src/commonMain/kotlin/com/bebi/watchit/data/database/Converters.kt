package com.bebi.watchit.data.database

import androidx.room.TypeConverter
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
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
        return Json.encodeToString(ListSerializer(String.serializer()), value)
    }

    /**
     * Converts a JSON string back to a List of Strings
     */
    @TypeConverter
    fun toStringList(value: String): List<String> {
        if (value.isBlank()) return emptyList()
        return try {
            Json.decodeFromString(ListSerializer(String.serializer()), value)
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    /**
     * Converts a List of Integers to a JSON string for storage
     */
    @TypeConverter
    fun fromIntList(value: List<Int>): String {
        return Json.encodeToString(ListSerializer(Int.serializer()), value)
    }

    /**
     * Converts a JSON string back to a List of Integers
     */
    @TypeConverter
    fun toIntList(value: String): List<Int> {
        if (value.isBlank()) return emptyList()
        return try {
            Json.decodeFromString(ListSerializer(Int.serializer()), value)
        } catch (e: Exception) {
            emptyList()
        }
    }
}
