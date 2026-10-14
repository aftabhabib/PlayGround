package com.desimeals.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MealDao {
    @Query("SELECT * FROM meal_entries WHERE date = :dayStart ORDER BY customerId ASC")
    fun observeForDay(dayStart: Long): Flow<List<MealEntryEntity>>

    @Query("SELECT * FROM meal_entries WHERE customerId = :customerId ORDER BY date DESC")
    fun observeForCustomer(customerId: Long): Flow<List<MealEntryEntity>>

    @Query("SELECT * FROM meal_entries WHERE customerId = :customerId AND date = :dayStart LIMIT 1")
    suspend fun getForCustomerDay(customerId: Long, dayStart: Long): MealEntryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entry: MealEntryEntity): Long

    @Query("DELETE FROM meal_entries WHERE customerId = :customerId AND date = :dayStart")
    suspend fun deleteForCustomerDay(customerId: Long, dayStart: Long)
}
