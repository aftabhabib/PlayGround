package com.desimeals.app.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY date DESC, id DESC")
    fun observeAll(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE customerId = :customerId ORDER BY date DESC, id DESC")
    fun observeForCustomer(customerId: Long): Flow<List<TransactionEntity>>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE type = :type")
    fun observeTotalByType(type: TransactionType): Flow<Double>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE type = :type AND date BETWEEN :from AND :to")
    fun observeTotalByTypeInRange(type: TransactionType, from: Long, to: Long): Flow<Double>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE customerId = :customerId AND type = 'CHARGE'")
    fun observeChargesForCustomer(customerId: Long): Flow<Double>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE customerId = :customerId AND type = 'PAYMENT'")
    fun observePaymentsForCustomer(customerId: Long): Flow<Double>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: TransactionEntity): Long

    @Delete
    suspend fun delete(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteById(id: Long)
}
