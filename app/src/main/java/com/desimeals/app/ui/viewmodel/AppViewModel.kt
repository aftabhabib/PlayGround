package com.desimeals.app.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.desimeals.app.data.Repository
import com.desimeals.app.data.db.*
import com.desimeals.app.util.Dates
import com.desimeals.app.util.prefsFlow
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class DashboardState(
    val received: Double = 0.0,
    val paidOut: Double = 0.0,
    val outstanding: Double = 0.0,
    val expenses: Double = 0.0,
    val advance: Double = 0.0,
    val todayIncome: Double = 0.0,
    val todayExpense: Double = 0.0,
    val monthIncome: Double = 0.0,
    val monthExpense: Double = 0.0,
    val totalCustomers: Int = 0,
    val activeCustomers: Int = 0,
    val inactiveCustomers: Int = 0,
    val dueCustomers: Int = 0,
    val advanceCustomers: Int = 0
)

class AppViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = Repository.get(app)
    val prefs = prefsFlow(app)

    val customers: StateFlow<List<CustomerEntity>> =
        repo.customers.observeAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val expenses: StateFlow<List<ExpenseEntity>> =
        repo.expenses.observeAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val cashbook: StateFlow<List<CashbookEntity>> =
        repo.cash.observeAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val invoices: StateFlow<List<InvoiceEntity>> =
        repo.invoices.observeAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val suppliers: StateFlow<List<SupplierEntity>> =
        repo.suppliers.observeAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val accounts: StateFlow<List<AccountEntity>> =
        repo.cash.observeAccounts().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val categories: StateFlow<List<ExpenseCategoryEntity>> =
        repo.expenses.observeCategories().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val plans: StateFlow<List<MealPlanEntity>> =
        repo.meals.observePlans().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _balances = MutableStateFlow<Map<Long, Double>>(emptyMap())
    val balances: StateFlow<Map<Long, Double>> = _balances

    private val _dash = MutableStateFlow(DashboardState())
    val dash: StateFlow<DashboardState> = _dash

    var rangeFrom: Long = Dates.startOfMonth()
    var rangeTo: Long = System.currentTimeMillis()

    init {
        viewModelScope.launch { refresh() }
    }

    suspend fun refresh(from: Long = rangeFrom, to: Long = rangeTo) {
        rangeFrom = from; rangeTo = to
        val bals = repo.allCustomerBalances()
        _balances.value = bals
        val allCustomers = repo.customers.getAllOnce()
        val outstanding = bals.values.filter { it > 0.005 }.sum()
        val advance = -bals.values.filter { it < -0.005 }.sum()
        val t0 = Dates.startOfDay(); val t1 = Dates.endOfDay()
        val m0 = Dates.startOfMonth(); val m1 = Dates.endOfMonth()
        val todayPay = repo.txs.sumPayments(t0, t1)
        val monthPay = repo.txs.sumPayments(m0, m1)
        val rangePay = repo.txs.sumPayments(from, to)
        val todayExp = repo.expenses.sumPaid(t0, t1)
        val monthExp = repo.expenses.sumPaid(m0, m1)
        val rangeExp = repo.expenses.sumPaid(from, to)
        val cashBetween = repo.cash.between(from, to)
        val otherIncome = cashBetween.filter { it.kind == "IN" && (it.category == "cash_sale" || it.category == "other_income") }.sumOf { it.amount }
        val paidOutCash = cashBetween.filter { it.kind == "OUT" }.sumOf { it.amount }
        _dash.value = DashboardState(
            received = rangePay + otherIncome,
            paidOut = paidOutCash + rangeExp - rangeExp * 0 + (if (paidOutCash == 0.0) 0.0 else 0.0),
            outstanding = outstanding,
            expenses = rangeExp,
            advance = advance,
            todayIncome = todayPay,
            todayExpense = todayExp,
            monthIncome = monthPay,
            monthExpense = monthExp,
            totalCustomers = allCustomers.size,
            activeCustomers = allCustomers.count { it.status == "Active" },
            inactiveCustomers = allCustomers.count { it.status != "Active" },
            dueCustomers = bals.values.count { it > 0.005 },
            advanceCustomers = bals.values.count { it < -0.005 }
        )
    }

    fun ledger(cid: Long): Flow<List<CustomerTransactionEntity>> = repo.txs.observeLedger(cid)
    fun observeBills(sid: Long) = repo.suppliers.observeBills(sid)
    fun observePayments(sid: Long) = repo.suppliers.observePayments(sid)

    // ---- writes ----
    suspend fun saveCustomer(e: CustomerEntity): Long = repo.customers.upsert(e)
    suspend fun setCustomerStatus(id: Long, s: String) { repo.customers.setStatus(id, s); refresh() }
    suspend fun addTx(e: CustomerTransactionEntity): Long {
        val id = repo.safeInsertCustomerTx(e)
        // mirror payment into cashbook as IN (receivable collection, not new income double count handled in reports)
        if (e.type == "CREDIT" && (e.category == "payment" || e.category == "advance")) {
            val accs = repo.cash.accountsOnce()
            val accId = accs.firstOrNull { it.type == "CASH" }?.id ?: accs.firstOrNull()?.id ?: 0L
            if (accId != 0L) repo.cash.insert(
                CashbookEntity(accountId = accId, kind = "IN", category = "customer_payment",
                    title = "Payment: ${e.description}", amount = e.credit, method = e.paymentMethod,
                    linkedCustomerId = e.customerId, notes = e.notes, date = e.timestamp)
            )
        }
        if (e.type == "DEBIT") {
            // meal charges are income; no cashbook movement until paid
        }
        refresh()
        return id
    }
    suspend fun saveExpense(e: ExpenseEntity): Long {
        val id = repo.expenses.upsert(e)
        if (e.isPaid) {
            val accs = repo.cash.accountsOnce()
            val accId = accs.firstOrNull { it.type == "CASH" }?.id ?: accs.firstOrNull()?.id ?: 0L
            if (accId != 0L) repo.cash.insert(
                CashbookEntity(accountId = accId, kind = "OUT", category = "expense",
                    title = "Expense: ${e.title}", amount = e.amount, method = e.paymentMethod,
                    linkedExpenseId = id, notes = e.description, date = e.date)
            )
        }
        refresh()
        return id
    }
    suspend fun deleteExpense(id: Long) { repo.expenses.delete(id); refresh() }
    suspend fun saveSupplier(s: SupplierEntity) = repo.suppliers.upsert(s)
    suspend fun saveSupplierBill(b: SupplierBillEntity) = repo.suppliers.insertBill(b)
    suspend fun saveSupplierPayment(p: SupplierPaymentEntity) {
        repo.suppliers.insertPayment(p)
        p.billId?.let { repo.suppliers.addBillPayment(it, p.amount) }
        val accs = repo.cash.accountsOnce()
        val accId = accs.firstOrNull()?.id ?: 0L
        if (accId != 0L) repo.cash.insert(
            CashbookEntity(accountId = accId, kind = "OUT", category = "supplier_payment",
                title = "Supplier payment", amount = p.amount, method = p.method, linkedSupplierId = p.supplierId, date = p.date)
        )
        refresh()
    }
    suspend fun saveCash(e: CashbookEntity): Long { val id = repo.cash.insert(e); refresh(); return id }
    suspend fun saveAccount(a: AccountEntity) = repo.cash.upsertAccount(a)
    suspend fun saveTransfer(fromId: Long, toId: Long, amount: Double, notes: String) {
        val now = System.currentTimeMillis()
        repo.cash.insert(CashbookEntity(accountId = fromId, kind = "OUT", category = "transfer", title = "Transfer out", amount = amount, notes = notes, date = now))
        repo.cash.insert(CashbookEntity(accountId = toId, kind = "IN", category = "transfer", title = "Transfer in", amount = amount, notes = notes, date = now))
        repo.cash.insertTransfer(TransferEntity(fromAccountId = fromId, toAccountId = toId, amount = amount, notes = notes, date = now))
        refresh()
    }
    suspend fun saveMealEntry(e: MealEntryEntity) = repo.meals.upsertEntry(e)
    suspend fun savePlan(p: MealPlanEntity) = repo.meals.upsertPlan(p)

    suspend fun generateMonthlyBill(customerId: Long, ps: Long, pe: Long, discount: Double = 0.0, notes: String = ""): Long {
        val c = repo.customers.getById(customerId) ?: throw IllegalArgumentException("Customer not found")
        if (repo.invoices.countForPeriod(customerId, ps, pe) > 0) throw IllegalStateException("Bill already exists for this customer and period.")
        val entries = repo.meals.entriesFor(customerId, ps, pe)
        val unbilled = entries.filter { !it.billed && !it.cancelled }
        val planRate = c.mealRate
        var subtotal = 0.0
        val items = mutableListOf<Triple<String, Double, Double>>()
        if (unbilled.isNotEmpty()) {
            // charge per day present at plan daily equivalent (monthly/30) if mealRate is monthly, else per-day
            val perDay = if (c.billingCycle == "Monthly") planRate / 30.0 else planRate
            val days = unbilled.size.toDouble()
            subtotal += days * perDay
            items.add(Triple("Meals ($days days)", days, perDay))
            val extra = unbilled.sumOf { it.extraCharge }
            if (extra > 0) { subtotal += extra; items.add(Triple("Extra meals", 1.0, extra)) }
        } else {
            subtotal = planRate
            items.add(Triple("Monthly meal charges (${c.mealPlan})", 1.0, planRate))
        }
        val prevDue = repo.customerBalance(customerId).coerceAtLeast(0.0)
        val total = subtotal + prevDue - discount
        val invNo = "INV-${System.currentTimeMillis() % 1000000}"
        val invId = repo.invoices.insert(InvoiceEntity(invoiceNo = invNo, customerId = customerId, periodStart = ps, periodEnd = pe, subtotal = subtotal, discount = discount, previousDue = prevDue, total = total, status = if (total <= 0.005) "Paid" else "Unpaid", notes = notes))
        items.forEach { (t, q, r) -> repo.invoices.insertItem(InvoiceItemEntity(invoiceId = invId, title = t, qty = q, rate = r, amount = q * r)) }
        // post debit for the bill (excluding previousDue which is already in ledger)
        repo.safeInsertCustomerTx(CustomerTransactionEntity(customerId = customerId, type = "DEBIT", category = "meal_charge", description = "Monthly bill $invNo", debit = subtotal - discount, paymentMethod = "-", reference = invNo, notes = notes))
        refresh()
        return invId
    }
}
