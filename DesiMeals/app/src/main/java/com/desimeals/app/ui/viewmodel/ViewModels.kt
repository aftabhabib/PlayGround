package com.desimeals.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.desimeals.app.data.local.CustomerEntity
import com.desimeals.app.data.local.MealEntryEntity
import com.desimeals.app.data.local.TransactionEntity
import com.desimeals.app.data.local.TransactionType
import com.desimeals.app.data.repository.CustomerBalance
import com.desimeals.app.data.repository.CustomerRepository
import com.desimeals.app.data.repository.DashboardStats
import com.desimeals.app.data.repository.LedgerRepository
import com.desimeals.app.data.repository.MealRepository
import com.desimeals.app.di.AppContainer
import com.desimeals.app.util.DateUtils
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// ---------- Dashboard ----------
class DashboardViewModel(container: AppContainer) : ViewModel() {
    val stats: StateFlow<DashboardStats> =
        container.ledgerRepository.observeDashboard()
            .catch { emit(DashboardStats()) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardStats())

    val recent: StateFlow<List<TransactionEntity>> =
        container.ledgerRepository.observeAll()
            .catch { emit(emptyList()) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}

// ---------- Customers ----------
class CustomersViewModel(private val repo: CustomerRepository) : ViewModel() {
    private val _query = MutableStateFlow("")
    val query = _query.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val customers: StateFlow<List<CustomerEntity>> =
        repo.observeAll().catch { emit(emptyList()) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val filtered: StateFlow<List<CustomerEntity>> =
        combine(customers, _query) { list, q ->
            if (q.isBlank()) list
            else list.filter {
                it.name.contains(q, true) || it.phone.contains(q) || it.address.contains(q, true)
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setQuery(q: String) { _query.value = q }

    fun addCustomer(name: String, phone: String, address: String, rate: Double, onDone: (String?) -> Unit) {
        if (name.isBlank()) { onDone("Name is required"); return }
        if (rate < 0) { onDone("Monthly rate cannot be negative"); return }
        viewModelScope.launch {
            try {
                repo.upsert(CustomerEntity(name = name.trim(), phone = phone.trim(), address = address.trim(), monthlyRate = rate))
                onDone(null)
            } catch (e: Exception) { onDone(e.message ?: "Could not save member") }
        }
    }

    fun toggleActive(customer: CustomerEntity) {
        viewModelScope.launch { repo.setActive(customer.id, !customer.isActive) }
    }

    fun delete(customer: CustomerEntity) {
        viewModelScope.launch { runCatching { repo.delete(customer) } }
    }
}

// ---------- Customer detail ----------
class CustomerDetailViewModel(container: AppContainer, val customerId: Long) : ViewModel() {
    val customer: StateFlow<CustomerEntity?> =
        container.customerRepository.observeById(customerId)
            .catch { emit(null) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val balance: StateFlow<CustomerBalance> =
        container.ledgerRepository.observeBalance(customerId)
            .catch { emit(CustomerBalance()) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CustomerBalance())

    val transactions: StateFlow<List<TransactionEntity>> =
        container.ledgerRepository.observeForCustomer(customerId)
            .catch { emit(emptyList()) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val meals: StateFlow<List<MealEntryEntity>> =
        container.mealRepository.observeForCustomer(customerId)
            .catch { emit(emptyList()) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    fun addTransaction(type: TransactionType, amount: Double, desc: String, onDone: () -> Unit) {
        viewModelScope.launch {
            try {
                (containerRef ?: return@launch).let { c ->
                    c.ledgerRepository.addTransaction(
                        TransactionEntity(customerId = customerId, type = type, amount = amount, description = desc.ifBlank { type.name })
                    )
                }
                _error.value = null
                onDone()
            } catch (e: Exception) { _error.value = e.message }
        }
    }

    fun clearError() { _error.value = null }

    // Keep a ref without leaking context beyond repos (repos hold DB only)
    private var containerRef: AppContainer? = container
    override fun onCleared() { containerRef = null }
}

// ---------- Ledger ----------
class LedgerViewModel(container: AppContainer) : ViewModel() {
    private val _filter = MutableStateFlow<TransactionType?>(null)
    val filter = _filter.asStateFlow()

    private val all: StateFlow<List<TransactionEntity>> =
        container.ledgerRepository.observeAll().catch { emit(emptyList()) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val transactions: StateFlow<List<TransactionEntity>> =
        combine(all, _filter) { list, f -> if (f == null) list else list.filter { it.type == f } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val repo = container.ledgerRepository

    fun setFilter(f: TransactionType?) { _filter.value = f }

    fun addMessTransaction(type: TransactionType, amount: Double, desc: String, customerId: Long?, onResult: (String?) -> Unit) {
        if (amount <= 0) { onResult("Amount must be greater than zero"); return }
        viewModelScope.launch {
            try {
                repo.addTransaction(TransactionEntity(customerId = customerId, type = type, amount = amount, description = desc.ifBlank { type.name }))
                onResult(null)
            } catch (e: Exception) { onResult(e.message) }
        }
    }

    fun delete(id: Long) {
        viewModelScope.launch { runCatching { repo.deleteTransaction(id) } }
    }
}

// ---------- Meals ----------
class MealsViewModel(container: AppContainer) : ViewModel() {
    private val _day = MutableStateFlow(DateUtils.startOfDay())
    val day = _day.asStateFlow()

    private val mealRepo = container.mealRepository
    private val customerRepo = container.customerRepository

    val members: StateFlow<List<CustomerEntity>> =
        customerRepo.observeActive().catch { emit(emptyList()) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val entries: StateFlow<List<MealEntryEntity>> =
        _day.flatMapLatest { d -> mealRepo.observeForDay(d).catch { emit(emptyList()) } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun shiftDay(delta: Int) {
        _day.value = _day.value + delta * 24L * 60L * 60L * 1000L
    }

    fun toggle(customerId: Long, meal: String) {
        viewModelScope.launch { runCatching { mealRepo.toggleMeal(customerId, _day.value, meal) } }
    }
}

// ---------- Factory ----------
@Suppress("UNCHECKED_CAST")
class DesiViewModelFactory(private val container: AppContainer) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(DashboardViewModel::class.java) -> DashboardViewModel(container)
            modelClass.isAssignableFrom(CustomersViewModel::class.java) -> CustomersViewModel(container.customerRepository)
            modelClass.isAssignableFrom(LedgerViewModel::class.java) -> LedgerViewModel(container)
            modelClass.isAssignableFrom(MealsViewModel::class.java) -> MealsViewModel(container)
            else -> throw IllegalArgumentException("Unknown ViewModel ${modelClass.name}")
        } as T
    }

    fun customerDetail(customerId: Long): ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return CustomerDetailViewModel(container, customerId) as T
            }
        }
}
