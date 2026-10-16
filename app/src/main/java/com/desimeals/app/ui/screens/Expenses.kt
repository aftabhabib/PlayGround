package com.desimeals.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.desimeals.app.data.db.ExpenseEntity
import com.desimeals.app.data.db.SupplierEntity
import com.desimeals.app.ui.components.*
import com.desimeals.app.ui.navigation.Routes
import com.desimeals.app.ui.viewmodel.AppViewModel
import com.desimeals.app.util.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseListScreen(vm: AppViewModel, nav: NavController) {
    val expenses by vm.expenses.collectAsState()
    val categories by vm.categories.collectAsState()
    val prefs by vm.prefs.collectAsState(initial = AppPrefs())
    var q by remember { mutableStateOf("") }
    var cat by remember { mutableStateOf("All") }
    val shown = expenses.filter {
        (cat == "All" || it.category == cat) && (q.isBlank() || it.title.contains(q, true) || it.vendor.contains(q, true) || it.description.contains(q, true))
    }
    val topCats = expenses.groupBy { it.category }.mapValues { it.value.sumOf { e -> e.amount } }.toList().sortedByDescending { it.second }.take(3)
    Scaffold(topBar = { TopAppBar(title = { Text("Expenses") }, actions = {
        IconButton(onClick = { nav.navigate(Routes.SUPPLIERS) }) { Icon(Icons.Default.Store, null) }
    }) }, floatingActionButton = { FloatingActionButton(onClick = { nav.navigate(Routes.ADD_EXPENSE) }) { Icon(Icons.Default.Add, null) } }) { pad ->
        Column(Modifier.padding(pad).padding(12.dp)) {
            OutlinedTextField(q, { q = it }, Modifier.fillMaxWidth(), label = { Text("Search description / vendor") }, singleLine = true)
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FilterChip(cat == "All", { cat = "All" }, { Text("All") })
                categories.take(4).forEach { c -> FilterChip(cat == c.name, { cat = c.name }, { Text(c.name.take(12)) }) }
            }
            if (topCats.isNotEmpty()) Text("Top: " + topCats.joinToString(" • ") { "${it.first.take(12)} ${formatMoney(it.second, prefs.currency, 0)}" }, style = MaterialTheme.typography.labelSmall)
            Spacer(Modifier.height(6.dp))
            if (shown.isEmpty()) EmptyState("No expenses yet.")
            LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items(shown.take(300)) { e ->
                    Card(onClick = { nav.navigate(Routes.expenseDetail(e.id)) }) {
                        Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column(Modifier.weight(1f)) { Text(e.title, fontWeight = FontWeight.SemiBold); Text("${e.category} • ${Dates.fmt(e.date, prefs.dateFmt)}${if (!e.isPaid) " • UNPAID" else ""}", style = MaterialTheme.typography.bodySmall) }
                            Text(formatMoney(e.amount, prefs.currency, prefs.decimals), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(vm: AppViewModel, nav: NavController, id: Long?) {
    val categories by vm.categories.collectAsState()
    val prefs by vm.prefs.collectAsState(initial = AppPrefs())
    val scope = rememberCoroutineScope()
    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(categories.firstOrNull()?.name ?: "Miscellaneous") }
    var vendor by remember { mutableStateOf("") }
    var method by remember { mutableStateOf(prefs.defaultPay.ifBlank { "Cash" }) }
    var paid by remember { mutableStateOf(true) }
    var desc by remember { mutableStateOf("") }
    var err by remember { mutableStateOf("") }
    LaunchedEffect(categories) { if (category.isBlank()) category = categories.firstOrNull()?.name ?: "Miscellaneous" }
    Scaffold(topBar = { TopAppBar(title = { Text("Add Expense") }, navigationIcon = {
        IconButton(onClick = { nav.popBackStack() }) { Icon(Icons.Default.ArrowBack, null) }
    }) }) { pad ->
        LazyColumn(Modifier.padding(pad).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item {
                if (err.isNotBlank()) Text(err, color = MaterialTheme.colorScheme.error)
                OutlinedTextField(title, { title = it }, label = { Text("Title *") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(amount, { amount = it }, label = { Text("Amount *") }, modifier = Modifier.fillMaxWidth())
                Text("Category", style = MaterialTheme.typography.labelMedium)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    (categories.map { it.name }.ifEmpty { listOf("Miscellaneous") }).chunked(3).forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) { row.forEach { c -> FilterChip(category == c, { category = c }, { Text(c.take(14)) }) } }
                    }
                }
                OutlinedTextField(vendor, { vendor = it }, label = { Text("Vendor / supplier") }, modifier = Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("Cash", "Bank transfer", "Card", "Mobile wallet").forEach { m -> FilterChip(method == m, { method = m }, { Text(m.take(10)) }) }
                }
                Row { FilterChip(paid, { paid = true }, { Text("Paid") }); Spacer(Modifier.width(8.dp)); FilterChip(!paid, { paid = false }, { Text("Unpaid") }) }
                OutlinedTextField(desc, { desc = it }, label = { Text("Description / notes") }, modifier = Modifier.fillMaxWidth())
                Button(onClick = {
                    val a = amount.toDoubleOrNull()
                    if (title.isBlank() || a == null || a <= 0) { err = "Title and valid amount required."; return@Button }
                    scope.launch { vm.saveExpense(ExpenseEntity(title = title.trim(), amount = a, category = category, vendor = vendor.trim(), paymentMethod = method, isPaid = paid, description = desc.trim())); nav.popBackStack() }
                }, modifier = Modifier.fillMaxWidth()) { Text("Save Expense") }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseDetailScreen(vm: AppViewModel, nav: NavController, id: Long) {
    val expenses by vm.expenses.collectAsState()
    val prefs by vm.prefs.collectAsState(initial = AppPrefs())
    val scope = rememberCoroutineScope()
    var confirm by remember { mutableStateOf(false) }
    val e = expenses.find { it.id == id }
    if (confirm) ConfirmDialog("Delete expense", "Delete this expense record?", { scope.launch { vm.deleteExpense(id); confirm = false; nav.popBackStack() } }, { confirm = false })
    Scaffold(topBar = { TopAppBar(title = { Text("Expense Details") }, navigationIcon = {
        IconButton(onClick = { nav.popBackStack() }) { Icon(Icons.Default.ArrowBack, null) }
    }, actions = { IconButton(onClick = { confirm = true }) { Icon(Icons.Default.Delete, null) } }) }) { pad ->
        Column(Modifier.padding(pad).padding(16.dp)) {
            if (e == null) { Text("Not found"); return@Column }
            Text(e.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("${e.category} • ${Dates.fmt(e.date, prefs.dateFmt)}")
            Text(formatMoney(e.amount, prefs.currency, prefs.decimals), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            if (e.vendor.isNotBlank()) Text("Vendor: ${e.vendor}")
            Text("Method: ${e.paymentMethod} • ${if (e.isPaid) "Paid" else "Unpaid"}")
            if (e.description.isNotBlank()) Text(e.description)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupplierListScreen(vm: AppViewModel, nav: NavController) {
    val suppliers by vm.suppliers.collectAsState()
    val scope = rememberCoroutineScope()
    var name by remember { mutableStateOf("") }
    var show by remember { mutableStateOf(false) }
    Scaffold(topBar = { TopAppBar(title = { Text("Suppliers") }, navigationIcon = {
        IconButton(onClick = { nav.popBackStack() }) { Icon(Icons.Default.ArrowBack, null) }
    }) }, floatingActionButton = { FloatingActionButton(onClick = { show = true }) { Icon(Icons.Default.Add, null) } }) { pad ->
        LazyColumn(Modifier.padding(pad).padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (suppliers.isEmpty()) item { EmptyState("No suppliers yet.") }
            items(suppliers) { s ->
                Card(onClick = { nav.navigate(Routes.supplierDetail(s.id)) }) {
                    Row(Modifier.fillMaxWidth().padding(12.dp)) { Column { Text(s.name, fontWeight = FontWeight.Bold); Text(s.category, style = MaterialTheme.typography.bodySmall) } }
                }
            }
        }
        if (show) AlertDialog(onDismissRequest = { show = false }, title = { Text("Add Supplier") }, text = {
            OutlinedTextField(name, { name = it }, label = { Text("Supplier name") })
        }, confirmButton = { TextButton(onClick = { if (name.isNotBlank()) scope.launch { vm.saveSupplier(SupplierEntity(name = name.trim())); show = false; name = "" } }) { Text("Save") } },
            dismissButton = { TextButton(onClick = { show = false }) { Text("Cancel") } })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupplierDetailScreen(vm: AppViewModel, nav: NavController, id: Long) {
    val suppliers by vm.suppliers.collectAsState()
    val scope = rememberCoroutineScope()
    val s = suppliers.find { it.id == id } ?: run { Box(Modifier.fillMaxSize()) { Text("Not found") }; return }
    val bills by vm.observeBills(id).collectAsState(initial = emptyList())
    val pays by vm.observePayments(id).collectAsState(initial = emptyList())
    var billAmt by remember { mutableStateOf("") }
    var payAmt by remember { mutableStateOf("") }
    Scaffold(topBar = { TopAppBar(title = { Text(s.name) }, navigationIcon = {
        IconButton(onClick = { nav.popBackStack() }) { Icon(Icons.Default.ArrowBack, null) }
    }) }) { pad ->
        LazyColumn(Modifier.padding(pad).padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                val billed = bills.sumOf { it.amount }
                val paid = pays.sumOf { it.amount }
                val out = Accounting.supplierOutstanding(billed, s.openingBalance, paid)
                Card { Column(Modifier.padding(12.dp)) {
                    Text("Billed: $billed • Paid: $paid", style = MaterialTheme.typography.bodyMedium)
                    Text("Outstanding: $out", fontWeight = FontWeight.Bold)
                } }
                SectionTitle("Add Bill / Payment (bills ≠ cash until paid)")
                OutlinedTextField(billAmt, { billAmt = it }, label = { Text("Bill amount") }, modifier = Modifier.fillMaxWidth())
                Button(onClick = { val a = billAmt.toDoubleOrNull() ?: 0.0; if (a > 0) scope.launch {
                    vm.saveSupplierBill(com.desimeals.app.data.db.SupplierBillEntity(supplierId = id, amount = a)); billAmt = ""
                } }) { Text("Add Bill") }
                OutlinedTextField(payAmt, { payAmt = it }, label = { Text("Payment amount") }, modifier = Modifier.fillMaxWidth())
                Button(onClick = { val a = payAmt.toDoubleOrNull() ?: 0.0; if (a > 0) scope.launch {
                    vm.saveSupplierPayment(com.desimeals.app.data.db.SupplierPaymentEntity(supplierId = id, amount = a)); payAmt = ""
                } }) { Text("Record Payment") }
                SectionTitle("Bills")
            }
            items(bills) { b -> ListItem(headlineContent = { Text("Bill ${b.billNo.ifBlank { "#${b.id}" }} — ${b.amount}") }, supportingContent = { Text(Dates.fmt(b.date)) }) ; HorizontalDivider() }
            item { SectionTitle("Payments") }
            items(pays) { p -> ListItem(headlineContent = { Text("Paid ${p.amount} • ${p.method}") }, supportingContent = { Text(Dates.fmt(p.date)) }); HorizontalDivider() }
        }
    }
}
