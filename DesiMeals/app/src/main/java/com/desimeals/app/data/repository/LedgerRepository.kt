package com.desimeals.app.data.repository

import com.desimeals.app.data.local.MealDao
import com.desimeals.app.data.local.MealEntryEntity
import com.desimeals.app.data.local.TransactionDao
import com.desimeals.app.data.local.TransactionEntity
import com.desimeals.app.data.local.TransactionType
import com.desimeals.app.util.DateUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

data class DashboardStats(
    val activeMembers: Int = 0,
    val totalBilled: Double = 0.0,
    val totalReceived: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val monthBilled: Double = 0.0,
    val monthReceived: Double = 0.0,
    val monthExpenses: Double = 0.0
) {
    val totalReceivable: Double get() = totalBilled - totalReceived
    val monthNet: Double get() = monthReceived - monthExpenses
}

data class CustomerBalance(
    val billed: Double = 0.0,
    val paid: Double = 0.0
) {
    val due: Double get() = billed - paid
}

class LedgerRepository(
    private val transactionDao: TransactionDao,
    private val customerDao: com.desimeals.app.data.local.CustomerDao
) {
    fun observeAll(): Flow<List<TransactionEntity>> = transactionDao.observeAll()
    fun observeForCustomer(id: Long): Flow<List<TransactionEntity>> =
        transactionDao.observeForCustomer(id)
    fun observeBalance(customerId: Long): Flow<CustomerBalance> =
        combine(
            transactionDao.observeChargesForCustomer(customerId),
            transactionDao.observePaymentsForCustomer(customerId)
        ) { billed, paid -> CustomerBalance(billed, paid) }

    fun observeDashboard(): Flow<DashboardStats> {
        val (from, to) = DateUtils.monthRange(System.currentTimeMillis())
        val totals = combine(
            transactionDao.observeTotalByType(TransactionType.CHARGE),
            transactionDao.observeTotalByType(TransactionType.PAYMENT),
            transactionDao.observeTotalByType(TransactionType.EXPENSE)
        ) { billed: Double, received: Double, expenses: Double ->
            Triple(billed, received, expenses)
        }
        val monthTotals = combine(
            transactionDao.observeTotalByTypeInRange(TransactionType.CHARGE, from, to),
            transactionDao.observeTotalByTypeInRange(TransactionType.PAYMENT, from, to),
            transactionDao.observeTotalByTypeInRange(TransactionType.EXPENSE, from, to)
        ) { billed: Double, received: Double, expenses: Double ->
            Triple(billed, received, expenses)
        }
        return combine(
            customerDao.observeActiveCount(),
            totals,
            monthTotals
        ) { active: Int, all: Triple<Double, Double, Double>, month: Triple<Double, Double, Double> ->
            DashboardStats(
                activeMembers = active,
                totalBilled = all.first,
                totalReceived = all.second,
                totalExpenses = all.third,
                monthBilled = month.first,
                monthReceived = month.second,
                monthExpenses = month.third
            )
        }
    }

    suspend fun addTransaction(tx: TransactionEntity): Long {
        require(tx.amount > 0) { "Amount must be greater than zero" }
        return transactionDao.insert(tx)
    }

    suspend fun deleteTransaction(id: Long) = transactionDao.deleteById(id)
}

class MealRepository(private val mealDao: MealDao) {
    fun observeForDay(dayStart: Long): Flow<List<MealEntryEntity>> = mealDao.observeForDay(dayStart)
    fun observeForCustomer(id: Long): Flow<List<MealEntryEntity>> = mealDao.observeForCustomer(id)

    suspend fun toggleMeal(customerId: Long, dayStart: Long, meal: String) {
        val existing = mealDao.getForCustomerDay(customerId, dayStart)
            ?: MealEntryEntity(customerId = customerId, date = dayStart)
        val updated = when (meal) {
            "breakfast" -> existing.copy(breakfast = !existing.breakfast)
            "lunch" -> existing.copy(lunch = !existing.lunch)
            else -> existing.copy(dinner = !existing.dinner)
        }
        if (!updated.breakfast && !updated.lunch && !updated.dinner) {
            mealDao.deleteForCustomerDay(customerId, dayStart)
        } else {
            mealDao.upsert(updated)
        }
    }
}
