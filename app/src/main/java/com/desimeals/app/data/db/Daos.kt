package com.desimeals.app.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerDao {
    @Query("SELECT * FROM customers ORDER BY name ASC")
    fun observeAll(): Flow<List<CustomerEntity>>
    @Query("SELECT * FROM customers WHERE status = :status ORDER BY name ASC")
    fun observeByStatus(status: String): Flow<List<CustomerEntity>>
    @Query("SELECT * FROM customers WHERE id = :id")
    suspend fun getById(id: Long): CustomerEntity?
    @Query("SELECT * FROM customers WHERE name LIKE '%' || :q || '%' OR mobile LIKE '%' || :q || '%' OR CAST(id AS TEXT) LIKE '%' || :q || '%'")
    fun search(q: String): Flow<List<CustomerEntity>>
    @Query("SELECT COUNT(*) FROM customers")
    fun countAll(): Flow<Int>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(e: CustomerEntity): Long
    @Update
    suspend fun update(e: CustomerEntity)
    @Query("UPDATE customers SET status = :status WHERE id = :id")
    suspend fun setStatus(id: Long, status: String)
    @Query("SELECT * FROM customers")
    suspend fun getAllOnce(): List<CustomerEntity>
}

@Dao
interface CustomerTxDao {
    @Query("SELECT * FROM customer_transactions WHERE customerId = :cid ORDER BY timestamp ASC, id ASC")
    fun observeLedger(cid: Long): Flow<List<CustomerTransactionEntity>>
    @Query("SELECT * FROM customer_transactions WHERE customerId = :cid ORDER BY timestamp ASC, id ASC")
    suspend fun ledgerOnce(cid: Long): List<CustomerTransactionEntity>
    @Query("SELECT * FROM customer_transactions ORDER BY timestamp DESC, id DESC LIMIT :limit")
    fun observeRecent(limit: Int): Flow<List<CustomerTransactionEntity>>
    @Query("SELECT * FROM customer_transactions ORDER BY timestamp DESC LIMIT :limit")
    suspend fun recentOnce(limit: Int): List<CustomerTransactionEntity>
    @Query("SELECT * FROM customer_transactions WHERE timestamp BETWEEN :from AND :to ORDER BY timestamp ASC")
    suspend fun between(from: Long, to: Long): List<CustomerTransactionEntity>
    @Query("SELECT IFNULL(SUM(debit),0) FROM customer_transactions WHERE timestamp BETWEEN :from AND :to")
    suspend fun sumDebit(from: Long, to: Long): Double
    @Query("SELECT IFNULL(SUM(credit),0) FROM customer_transactions WHERE category IN ('payment','advance') AND timestamp BETWEEN :from AND :to")
    suspend fun sumPayments(from: Long, to: Long): Double
    @Insert
    suspend fun insert(e: CustomerTransactionEntity): Long
    @Query("SELECT COUNT(*) FROM customer_transactions WHERE customerId = :cid AND description = :desc AND ABS(debit - :debit) < 0.005 AND ABS(credit - :credit) < 0.005 AND timestamp BETWEEN :winStart AND :winEnd")
    suspend fun countDuplicates(cid: Long, desc: String, debit: Double, credit: Double, winStart: Long, winEnd: Long): Int
    @Query("DELETE FROM customer_transactions WHERE id = :id")
    suspend fun deleteById(id: Long)
}

@Dao
interface MealDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPlan(e: MealPlanEntity): Long
    @Query("SELECT * FROM meal_plans ORDER BY name ASC")
    fun observePlans(): Flow<List<MealPlanEntity>>
    @Query("SELECT * FROM meal_plans")
    suspend fun plansOnce(): List<MealPlanEntity>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertEntry(e: MealEntryEntity): Long
    @Query("SELECT * FROM meal_entries WHERE date BETWEEN :from AND :to ORDER BY date DESC")
    fun observeEntries(from: Long, to: Long): Flow<List<MealEntryEntity>>
    @Query("SELECT * FROM meal_entries WHERE customerId = :cid AND date BETWEEN :from AND :to")
    suspend fun entriesFor(cid: Long, from: Long, to: Long): List<MealEntryEntity>
    @Query("SELECT * FROM meal_entries WHERE date BETWEEN :from AND :to")
    suspend fun entriesBetween(from: Long, to: Long): List<MealEntryEntity>
}

@Dao
interface InvoiceDao {
    @Insert
    suspend fun insert(e: InvoiceEntity): Long
    @Insert
    suspend fun insertItem(e: InvoiceItemEntity): Long
    @Query("SELECT * FROM invoices ORDER BY issuedAt DESC")
    fun observeAll(): Flow<List<InvoiceEntity>>
    @Query("SELECT * FROM invoices WHERE customerId = :cid ORDER BY issuedAt DESC")
    fun observeForCustomer(cid: Long): Flow<List<InvoiceEntity>>
    @Query("SELECT * FROM invoices WHERE id = :id")
    suspend fun getById(id: Long): InvoiceEntity?
    @Query("SELECT * FROM invoice_items WHERE invoiceId = :iid")
    suspend fun itemsOf(iid: Long): List<InvoiceItemEntity>
    @Query("SELECT COUNT(*) FROM invoices WHERE customerId = :cid AND periodStart = :ps AND periodEnd = :pe AND status != 'Cancelled'")
    suspend fun countForPeriod(cid: Long, ps: Long, pe: Long): Int
    @Update
    suspend fun update(e: InvoiceEntity)
    @Query("SELECT * FROM invoices WHERE issuedAt BETWEEN :from AND :to ORDER BY issuedAt DESC")
    suspend fun between(from: Long, to: Long): List<InvoiceEntity>
}

@Dao
interface ExpenseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(e: ExpenseEntity): Long
    @Query("SELECT * FROM expenses ORDER BY date DESC, id DESC")
    fun observeAll(): Flow<List<ExpenseEntity>>
    @Query("SELECT * FROM expenses WHERE date BETWEEN :from AND :to ORDER BY date DESC")
    fun observeBetween(from: Long, to: Long): Flow<List<ExpenseEntity>>
    @Query("SELECT * FROM expenses WHERE date BETWEEN :from AND :to ORDER BY date DESC")
    suspend fun between(from: Long, to: Long): List<ExpenseEntity>
    @Query("SELECT IFNULL(SUM(amount),0) FROM expenses WHERE date BETWEEN :from AND :to AND isPaid = 1")
    suspend fun sumPaid(from: Long, to: Long): Double
    @Query("SELECT * FROM expenses ORDER BY date DESC LIMIT :limit")
    suspend fun recent(limit: Int): List<ExpenseEntity>
    @Query("DELETE FROM expenses WHERE id = :id")
    suspend fun delete(id: Long)
    @Query("SELECT * FROM expenses WHERE id = :id")
    suspend fun getById(id: Long): ExpenseEntity?
    @Query("SELECT * FROM expense_categories ORDER BY name ASC")
    fun observeCategories(): Flow<List<ExpenseCategoryEntity>>
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCategory(e: ExpenseCategoryEntity): Long
    @Query("SELECT * FROM expense_categories")
    suspend fun categoriesOnce(): List<ExpenseCategoryEntity>
}

@Dao
interface SupplierDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(s: SupplierEntity): Long
    @Query("SELECT * FROM suppliers ORDER BY name ASC")
    fun observeAll(): Flow<List<SupplierEntity>>
    @Query("SELECT * FROM suppliers")
    suspend fun allOnce(): List<SupplierEntity>
    @Query("SELECT * FROM suppliers WHERE id = :id")
    suspend fun getById(id: Long): SupplierEntity?
    @Insert
    suspend fun insertBill(b: SupplierBillEntity): Long
    @Query("SELECT * FROM supplier_bills WHERE supplierId = :sid ORDER BY date DESC")
    fun observeBills(sid: Long): Flow<List<SupplierBillEntity>>
    @Query("SELECT * FROM supplier_bills WHERE supplierId = :sid")
    suspend fun billsOnce(sid: Long): List<SupplierBillEntity>
    @Insert
    suspend fun insertPayment(p: SupplierPaymentEntity): Long
    @Query("SELECT * FROM supplier_payments WHERE supplierId = :sid ORDER BY date DESC")
    fun observePayments(sid: Long): Flow<List<SupplierPaymentEntity>>
    @Query("SELECT * FROM supplier_payments WHERE supplierId = :sid")
    suspend fun paymentsOnce(sid: Long): List<SupplierPaymentEntity>
    @Query("UPDATE supplier_bills SET paidAmount = paidAmount + :amt WHERE id = :billId")
    suspend fun addBillPayment(billId: Long, amt: Double)
}

@Dao
interface CashbookDao {
    @Insert
    suspend fun insert(e: CashbookEntity): Long
    @Query("SELECT * FROM cashbook ORDER BY date DESC, id DESC")
    fun observeAll(): Flow<List<CashbookEntity>>
    @Query("SELECT * FROM cashbook WHERE date BETWEEN :from AND :to ORDER BY date DESC, id DESC")
    suspend fun between(from: Long, to: Long): List<CashbookEntity>
    @Query("SELECT * FROM cashbook WHERE date BETWEEN :from AND :to ORDER BY date ASC")
    fun observeBetween(from: Long, to: Long): Flow<List<CashbookEntity>>
    @Query("SELECT * FROM cashbook ORDER BY date DESC LIMIT :limit")
    suspend fun recent(limit: Int): List<CashbookEntity>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAccount(a: AccountEntity): Long
    @Query("SELECT * FROM accounts")
    fun observeAccounts(): Flow<List<AccountEntity>>
    @Query("SELECT * FROM accounts")
    suspend fun accountsOnce(): List<AccountEntity>
    @Query("SELECT * FROM accounts WHERE id = :id")
    suspend fun accountById(id: Long): AccountEntity?
    @Insert
    suspend fun insertTransfer(t: TransferEntity): Long
}
