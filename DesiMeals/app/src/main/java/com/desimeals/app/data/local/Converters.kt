package com.desimeals.app.data.local

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromTransactionType(value: TransactionType): String = value.name

    @TypeConverter
    fun toTransactionType(value: String): TransactionType =
        runCatching { TransactionType.valueOf(value) }.getOrDefault(TransactionType.CHARGE)

    @TypeConverter
    fun fromMealType(value: MealType): String = value.name

    @TypeConverter
    fun toMealType(value: String): MealType =
        runCatching { MealType.valueOf(value) }.getOrDefault(MealType.OTHER)
}
