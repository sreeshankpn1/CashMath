package com.cashmath.app.data

import androidx.room.TypeConverter
import com.cashmath.app.data.model.AccessScope
import com.cashmath.app.data.model.MemberRole
import org.json.JSONArray

class Converters {
    @TypeConverter
    fun fromTransactionType(value: TransactionType): String {
        return value.name
    }

    @TypeConverter
    fun toTransactionType(value: String): TransactionType {
        return try {
            TransactionType.valueOf(value)
        } catch (_: Exception) {
            TransactionType.EXPENSE
        }
    }

    @TypeConverter
    fun fromMemberRole(value: MemberRole?): String {
        return value?.name ?: MemberRole.STAFF.name
    }

    @TypeConverter
    fun toMemberRole(value: String?): MemberRole {
        return if (value.isNullOrBlank()) MemberRole.STAFF else try {
            MemberRole.valueOf(value)
        } catch (_: Exception) {
            MemberRole.STAFF
        }
    }

    @TypeConverter
    fun fromAccessScope(value: AccessScope?): String {
        return value?.name ?: AccessScope.ALL_BOOKS.name
    }

    @TypeConverter
    fun toAccessScope(value: String?): AccessScope {
        return if (value.isNullOrBlank()) AccessScope.ALL_BOOKS else try {
            AccessScope.valueOf(value)
        } catch (_: Exception) {
            AccessScope.ALL_BOOKS
        }
    }

    @TypeConverter
    fun fromStringList(paths: List<String>?): String {
        if (paths.isNullOrEmpty()) return "[]"
        val jsonArray = JSONArray()
        paths.forEach { jsonArray.put(it) }
        return jsonArray.toString()
    }

    @TypeConverter
    fun toStringList(data: String?): List<String> {
        if (data.isNullOrBlank() || data == "[]") return emptyList()
        val list = mutableListOf<String>()
        try {
            val jsonArray = JSONArray(data)
            for (i in 0 until jsonArray.length()) {
                list.add(jsonArray.getString(i))
            }
        } catch (_: Exception) {
            list.add(data)
        }
        return list
    }

    @TypeConverter
    fun fromLongList(list: List<Long>?): String {
        if (list.isNullOrEmpty()) return "[]"
        val jsonArray = JSONArray()
        list.forEach { jsonArray.put(it) }
        return jsonArray.toString()
    }

    @TypeConverter
    fun toLongList(data: String?): List<Long> {
        if (data.isNullOrBlank() || data == "[]") return emptyList()
        val list = mutableListOf<Long>()
        try {
            val jsonArray = JSONArray(data)
            for (i in 0 until jsonArray.length()) {
                list.add(jsonArray.getLong(i))
            }
        } catch (_: Exception) {
            data.toLongOrNull()?.let { list.add(it) }
        }
        return list
    }
}
