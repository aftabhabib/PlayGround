package com.desimeals.app.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "meal_entries",
    foreignKeys = [
        ForeignKey(
            entity = CustomerEntity::class,
            parentColumns = ["id"],
            childColumns = ["customerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("customerId"), Index("date")]
)
data class MealEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val customerId: Long,
    /** Start-of-day epoch millis for the meal day. */
    val date: Long,
    val breakfast: Boolean = false,
    val lunch: Boolean = false,
    val dinner: Boolean = false,
    val notes: String = ""
) {
    val mealCount: Int get() = listOf(breakfast, lunch, dinner).count { it }
}
