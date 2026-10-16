package com.desimeals.app.data

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.desimeals.app.data.db.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class Repository(val db: DesiDatabase) {
    val customers = db.customerDao()
    val txs = db.customerTxDao()
    val meals = db.mealDao()
    val invoices = db.invoiceDao()
    val expenses = db.expenseDao()
    val suppliers = db.supplierDao()
    val cash = db.cashbookDao()

    suspend fun customerBalance(customerId: Long): Double {
        val c = customers.getById(customerId) ?: return 0.0
        val tx = txs.ledgerOnce(customerId)
        return c.openingBalance + tx.sumOf { it.debit - it.credit }
    }

    suspend fun allCustomerBalances(): Map<Long, Double> {
        val all = customers.getAllOnce()
        val out = HashMap<Long, Double>()
        for (c in all) {
            val tx = txs.ledgerOnce(c.id)
            out[c.id] = c.openingBalance + tx.sumOf { it.debit - it.credit }
        }
        return out
    }

    /** Guard against accidental duplicate payment within 2 minutes window. */
    suspend fun safeInsertCustomerTx(e: CustomerTransactionEntity): Long {
        val win = 2 * 60 * 1000L
        val dup = txs.countDuplicates(e.customerId, e.description, e.debit, e.credit, e.timestamp - win, e.timestamp + win)
        if (dup > 0) throw IllegalStateException("Possible duplicate entry detected. Please verify before saving again.")
        return txs.insert(e)
    }

    companion object {
        @Volatile private var INSTANCE: Repository? = null
        fun get(context: Context): Repository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Repository(
                    Room.databaseBuilder(context.applicationContext, DesiDatabase::class.java, "desi_meals.db")
                        .addCallback(object : RoomDatabase.Callback() {
                            override fun onCreate(db: SupportSQLiteDatabase) {
                                super.onCreate(db)
                                CoroutineScope(Dispatchers.IO).launch {
                                    seedDefaults(context.applicationContext)
                                }
                            }
                        })
                        .fallbackToDestructiveMigration()
                        .build()
                ).also { INSTANCE = it }
            }
        }

        suspend fun seedDefaults(context: Context) {
            try {
                val repo = get(context)
                if (repo.expenses.categoriesOnce().isEmpty()) {
                    listOf(
                        "Vegetables & Groceries", "Meat & Chicken", "Rice, Flour & Staples",
                        "Oil & Spices", "Gas & Fuel", "Electricity", "Water", "Rent",
                        "Staff Salaries", "Kitchen Equipment", "Repairs & Maintenance",
                        "Packaging", "Transportation", "Cleaning Supplies", "Internet & Telephone", "Miscellaneous"
                    ).forEach { repo.expenses.insertCategory(ExpenseCategoryEntity(name = it, isDefault = true)) }
                }
                if (repo.cash.accountsOnce().isEmpty()) {
                    repo.cash.upsertAccount(AccountEntity(name = "Cash", type = "CASH", openingBalance = 0.0))
                    repo.cash.upsertAccount(AccountEntity(name = "Bank", type = "BANK", openingBalance = 0.0))
                }
                if (repo.meals.plansOnce().isEmpty()) {
                    repo.meals.upsertPlan(MealPlanEntity(name = "Monthly", monthlyRate = 6000.0, fullDayRate = 250.0, breakfastRate = 60.0, lunchRate = 100.0, dinnerRate = 100.0))
                    repo.meals.upsertPlan(MealPlanEntity(name = "Full Day", fullDayRate = 300.0, breakfastRate = 70.0, lunchRate = 120.0, dinnerRate = 120.0))
                }
            } catch (_: Exception) { }
        }
    }
}
