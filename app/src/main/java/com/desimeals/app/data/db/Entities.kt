package com.desimeals.app.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "customers")
data class CustomerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val mobile: String,
    val whatsapp: String = "",
    val email: String = "",
    val address: String = "",
    val photoUri: String = "",
    val joiningDate: Long = System.currentTimeMillis(),
    val mealPlan: String = "Monthly",
    val mealRate: Double = 0.0,
    val billingCycle: String = "Monthly",
    val paymentDueDay: Int = 5,
    val openingBalance: Double = 0.0,
    val status: String = "Active", // Active, Inactive, Suspended
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "customer_transactions")
data class CustomerTransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val customerId: Long,
    val timestamp: Long = System.currentTimeMillis(),
    val type: String, // DEBIT or CREDIT
    val category: String = "", // meal_charge, extra_meal, payment, discount, adjustment, opening
    val description: String = "",
    val debit: Double = 0.0,
    val credit: Double = 0.0,
    val paymentMethod: String = "Cash",
    val reference: String = "",
    val notes: String = "",
    val createdBy: String = "Owner",
    val correctionOf: Long? = null,
    val correctionReason: String = ""
)

@Entity(tableName = "meal_plans")
data class MealPlanEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val breakfastRate: Double = 0.0,
    val lunchRate: Double = 0.0,
    val dinnerRate: Double = 0.0,
    val fullDayRate: Double = 0.0,
    val monthlyRate: Double = 0.0
)

@Entity(tableName = "meal_entries")
data class MealEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val customerId: Long,
    val date: Long,
    val breakfast: Boolean = false,
    val lunch: Boolean = false,
    val dinner: Boolean = false,
    val extraMeals: Int = 0,
    val extraCharge: Double = 0.0,
    val cancelled: Boolean = false,
    val billed: Boolean = false,
    val notes: String = ""
)

@Entity(tableName = "invoices")
data class InvoiceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val invoiceNo: String,
    val customerId: Long,
    val periodStart: Long,
    val periodEnd: Long,
    val issuedAt: Long = System.currentTimeMillis(),
    val subtotal: Double = 0.0,
    val discount: Double = 0.0,
    val previousDue: Double = 0.0,
    val total: Double = 0.0,
    val status: String = "Unpaid", // Draft, Unpaid, Partially Paid, Paid, Overdue, Cancelled
    val notes: String = ""
)

@Entity(tableName = "invoice_items")
data class InvoiceItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val invoiceId: Long,
    val title: String,
    val qty: Double = 1.0,
    val rate: Double = 0.0,
    val amount: Double = 0.0
)

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: Long = System.currentTimeMillis(),
    val category: String,
    val title: String,
    val description: String = "",
    val amount: Double,
    val paymentMethod: String = "Cash",
    val vendor: String = "",
    val invoiceNo: String = "",
    val receiptUri: String = "",
    val paidBy: String = "Owner",
    val isPaid: Boolean = true,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "expense_categories")
data class ExpenseCategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val isDefault: Boolean = false
)

@Entity(tableName = "suppliers")
data class SupplierEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val contact: String = "",
    val address: String = "",
    val category: String = "General",
    val openingBalance: Double = 0.0,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "supplier_bills")
data class SupplierBillEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val supplierId: Long,
    val date: Long = System.currentTimeMillis(),
    val billNo: String = "",
    val amount: Double,
    val paidAmount: Double = 0.0,
    val notes: String = ""
)

@Entity(tableName = "supplier_payments")
data class SupplierPaymentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val supplierId: Long,
    val billId: Long? = null,
    val date: Long = System.currentTimeMillis(),
    val amount: Double,
    val method: String = "Cash",
    val reference: String = "",
    val notes: String = ""
)

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String, // Cash, Bank
    val type: String, // CASH, BANK
    val openingBalance: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "cashbook")
data class CashbookEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: Long = System.currentTimeMillis(),
    val accountId: Long,
    val kind: String, // IN or OUT or TRANSFER_IN / TRANSFER_OUT
    val category: String = "", // customer_payment, cash_sale, supplier_payment, expense, capital_in, withdrawal, bank_deposit, bank_withdrawal, transfer, other_income, refund, adjustment
    val title: String,
    val amount: Double,
    val method: String = "Cash",
    val reference: String = "",
    val linkedCustomerId: Long? = null,
    val linkedExpenseId: Long? = null,
    val linkedSupplierId: Long? = null,
    val notes: String = ""
)

@Entity(tableName = "transfers")
data class TransferEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: Long = System.currentTimeMillis(),
    val fromAccountId: Long,
    val toAccountId: Long,
    val amount: Double,
    val notes: String = ""
)
