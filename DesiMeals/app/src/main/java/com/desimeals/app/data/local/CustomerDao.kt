package com.desimeals.app.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerDao {
    @Query("SELECT * FROM customers ORDER BY isActive DESC, name ASC")
    fun observeAll(): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers WHERE isActive = 1 ORDER BY name ASC")
    fun observeActive(): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers WHERE id = :id")
    fun observeById(id: Long): Flow<CustomerEntity?>

    @Query("SELECT * FROM customers WHERE id = :id")
    suspend fun getById(id: Long): CustomerEntity?

    @Query("SELECT COUNT(*) FROM customers WHERE isActive = 1")
    fun observeActiveCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(customer: CustomerEntity): Long

    @Update
    suspend fun update(customer: CustomerEntity)

    @Delete
    suspend fun delete(customer: CustomerEntity)

    @Query("UPDATE customers SET isActive = :active WHERE id = :id")
    suspend fun setActive(id: Long, active: Boolean)
}
