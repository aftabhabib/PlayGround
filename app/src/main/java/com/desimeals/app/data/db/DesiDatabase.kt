package com.desimeals.app.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        CustomerEntity::class,
        CustomerTransactionEntity::class,
        MealPlanEntity::class,
        MealEntryEntity::class,
        InvoiceEntity::class,
        InvoiceItemEntity::class,
        ExpenseEntity::class,
        ExpenseCategoryEntity::class,
        SupplierEntity::class,
        SupplierBillEntity::class,
        SupplierPaymentEntity::class,
        AccountEntity::class,
        CashbookEntity::class,
        TransferEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class DesiDatabase : RoomDatabase() {
    abstract fun customerDao(): CustomerDao
    abstract fun customerTxDao(): CustomerTxDao
    abstract fun mealDao(): MealDao
    abstract fun invoiceDao(): InvoiceDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun supplierDao(): SupplierDao
    abstract fun cashbookDao(): CashbookDao
}
