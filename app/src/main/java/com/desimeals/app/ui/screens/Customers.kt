package com.desimeals.app.ui.screens

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import com.desimeals.app.data.db.CustomerEntity
import com.desimeals.app.data.db.CustomerTransactionEntity
import com.desimeals.app.ui.components.*
import com.desimeals.app.ui.navigation.Routes
import com.desimeals.app.ui.viewmodel.AppViewModel
import com.desimeals.app.util.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerListScreen(vm: AppViewModel, nav: NavController) {
    val customers by vm.customers.collectAsState()
    val balances by vm.balances.collectAsState()
    val prefs by vm.prefs.collectAsState(initial = AppPrefs())
    var q by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf("All") }
    val shown = customers.filter {
        (filter == "All" || it.status == filter) &&
        (q.isBlank() || it.name.contains(q, true) || it.mobile.contains(q) || it.id.toString() == q)
    }
    Scaffold(
        topBar = { TopAppBar(title = { Text("Customers") }, actions = {
            IconButton(onClick = { nav.navigate(Routes.SEARCH) }) { Icon(Icons.Default.Search, null) }
        }) },
        floatingActionButton = { FloatingActionButton(onClick = { nav.navigate(Routes.ADD_CUSTOMER) }) { Icon(Icons.Default.Add, null) } }
    ) { pad ->
        Column(Modifier.padding(pad).padding(12.dp)) {
            OutlinedTextField(q, { q = it }, Modifier.fillMaxWidth(), label = { Text("Search name, ID, mobile") }, leadingIcon = { Icon(Icons.Default.Search, null) }, singleLine = true)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("All", "Active", "Inactive", "Suspended").forEach { f ->
                    FilterChip(selected = filter == f, onClick = { filter = f }, label = { Text(f) })
                }
            }
            Spacer(Modifier.height(8.dp))
            if (shown.isEmpty()) EmptyState("No customers found.")
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(shown) { c ->
                    val b = balances[c.id] ?: 0.0
                    Card(onClick = { nav.navigate(Routes.customerDetail(c.id)) }) {
                        Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column(Modifier.weight(1f)) {
                                Text(c.name, fontWeight = FontWeight.SemiBold)
                                Text("${c.mobile} • ${c.status}", style = MaterialTheme.typography.bodySmall)
                                Text(Accounting.balanceLabel(b), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                            }
                            Text(formatMoney(b, prefs.currency, prefs.decimals),
                                color = if (b > 0.005) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditCustomerScreen(vm: AppViewModel, nav: NavController, id: Long?) {
    val scope = rememberCoroutineScope()
    var loaded by remember { mutableStateOf<CustomerEntity?>(null) }
    LaunchedEffect(id) {
        if (id != null) scope.launch {
            loaded = vm.customers.value.find { it.id == id }
        }
    }
    var name by remember(loaded) { mutableStateOf(loaded?.name ?: "") }
    var mobile by remember(loaded) { mutableStateOf(loaded?.mobile ?: "") }
    var whatsapp by remember(loaded) { mutableStateOf(loaded?.whatsapp ?: "") }
    var email by remember(loaded) { mutableStateOf(loaded?.email ?: "") }
    var address by remember(loaded) { mutableStateOf(loaded?.address ?: "") }
    var plan by remember(loaded) { mutableStateOf(loaded?.mealPlan ?: "Monthly") }
    var rate by remember(loaded) { mutableStateOf(loaded?.mealRate?.toString() ?: "6000") }
    var cycle by remember(loaded) { mutableStateOf(loaded?.billingCycle ?: "Monthly") }
    var opening by remember(loaded) { mutableStateOf(loaded?.openingBalance?.toString() ?: "0") }
    var status by remember(loaded) { mutableStateOf(loaded?.status ?: "Active") }
    var notes by remember(loaded) { mutableStateOf(loaded?.notes ?: "") }
    var err by remember { mutableStateOf("") }
    Scaffold(topBar = { TopAppBar(title = { Text(if (id == null) "Add Customer" else "Edit Customer") }, navigationIcon = {
        IconButton(onClick = { nav.popBackStack() }) { Icon(Icons.Default.ArrowBack, null) }
    }) }) { pad ->
        LazyColumn(Modifier.padding(pad).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item {
                if (err.isNotBlank()) Text(err, color = MaterialTheme.colorScheme.error)
                OutlinedTextField(name, { name = it }, label = { Text("Full name *") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(mobile, { mobile = it }, label = { Text("Mobile *") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(whatsapp, { whatsapp = it }, label = { Text("WhatsApp") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(email, { email = it }, label = { Text("Email (optional)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(address, { address = it }, label = { Text("Address") }, modifier = Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(plan, { plan = it }, label = { Text("Meal plan") }, modifier = Modifier.weight(1f), singleLine = true)
                    OutlinedTextField(rate, { rate = it }, label = { Text("Meal rate") }, modifier = Modifier.weight(1f), singleLine = true)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(cycle, { cycle = it }, label = { Text("Billing cycle") }, modifier = Modifier.weight(1f), singleLine = true)
                    OutlinedTextField(opening, { opening = it }, label = { Text("Opening balance") }, modifier = Modifier.weight(1f), singleLine = true)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Active", "Inactive", "Suspended").forEach { s ->
                        FilterChip(selected = status == s, onClick = { status = s }, label = { Text(s) })
                    }
                }
                OutlinedTextField(notes, { notes = it }, label = { Text("Notes") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                Button(onClick = {
                    if (name.isBlank() || mobile.isBlank()) { err = "Name and mobile are required."; return@Button }
                    val r = rate.toDoubleOrNull() ?: 0.0
                    val o = opening.toDoubleOrNull()
                    if (o == null) { err = "Invalid opening balance."; return@Button }
                    scope.launch {
                        vm.saveCustomer(CustomerEntity(
                            id = id ?: 0, name = name.trim(), mobile = mobile.trim(), whatsapp = whatsapp.trim(),
                            email = email.trim(), address = address.trim(), mealPlan = plan.trim(), mealRate = r,
                            billingCycle = cycle.trim(), openingBalance = o, status = status, notes = notes.trim(),
                            joiningDate = loaded?.joiningDate ?: System.currentTimeMillis()
                        ))
                        vm.refresh()
                        nav.popBackStack()
                    }
                }, modifier = Modifier.fillMaxWidth()) { Text("Save Customer") }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerDetailScreen(vm: AppViewModel, nav: NavController, id: Long) {
    val customers by vm.customers.collectAsState()
    val balances by vm.balances.collectAsState()
    val prefs by vm.prefs.collectAsState(initial = AppPrefs())
    val scope = rememberCoroutineScope()
    val ctx = LocalContext.current
    val c = customers.find { it.id == id }
    var showDeact by remember { mutableStateOf(false) }
    if (c == null) { Box(Modifier.fillMaxSize()) { Text("Customer not found") }; return }
    val b = balances[c.id] ?: 0.0
    if (showDeact) ConfirmDialog("Change status", "Deactivate this customer?", { scope.launch { vm.setCustomerStatus(id, "Inactive"); showDeact = false; nav.popBackStack() } }, { showDeact = false })
    Scaffold(topBar = { TopAppBar(title = { Text(c.name) }, navigationIcon = {
        IconButton(onClick = { nav.popBackStack() }) { Icon(Icons.Default.ArrowBack, null) }
    }, actions = {
        IconButton(onClick = { nav.navigate(Routes.editCustomer(id)) }) { Icon(Icons.Default.Edit, null) }
    }) }) { pad ->
        LazyColumn(Modifier.padding(pad).padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item {
                Card { Column(Modifier.padding(14.dp)) {
                    Text(c.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text("${c.mobile}  ${c.whatsapp}", style = MaterialTheme.typography.bodyMedium)
                    if (c.address.isNotBlank()) Text(c.address, style = MaterialTheme.typography.bodySmall)
                    Text("Plan: ${c.mealPlan} • Rate: ${formatMoney(c.mealRate, prefs.currency, prefs.decimals)} • ${c.status}", style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.height(8.dp))
                    Text("${Accounting.balanceLabel(b)}: ${formatMoney(b, prefs.currency, prefs.decimals)}", fontWeight = FontWeight.Bold,
                        color = if (b > 0.005) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
                } }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { nav.navigate(Routes.addDebit(id)) }, modifier = Modifier.weight(1f)) { Text("Charge") }
                    Button(onClick = { nav.navigate(Routes.addCredit(id)) }, modifier = Modifier.weight(1f)) { Text("Payment") }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { nav.navigate(Routes.ledger(id)) }, modifier = Modifier.weight(1f)) { Text("Ledger") }
                    OutlinedButton(onClick = {
                        scope.launch {
                            val txs = vm.ledger(id)
                            // share statement as CSV+PDF
                            nav.navigate(Routes.ledger(id))
                        }
                    }, modifier = Modifier.weight(1f)) { Text("Statement") }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = {
                        scope.launch {
                            try {
                                val ps = Dates.startOfMonth(); val pe = Dates.endOfMonth()
                                val invId = vm.generateMonthlyBill(id, ps, pe)
                                nav.navigate(Routes.invoiceDetail(invId))
                            } catch (e: Exception) { }
                        }
                    }, modifier = Modifier.weight(1f)) { Text("Monthly Bill") }
                    OutlinedButton(onClick = { showDeact = true }, modifier = Modifier.weight(1f)) { Text("Deactivate") }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LedgerScreen(vm: AppViewModel, nav: NavController, id: Long) {
    val prefs by vm.prefs.collectAsState(initial = AppPrefs())
    val customers by vm.customers.collectAsState()
    val scope = rememberCoroutineScope()
    val ctx = LocalContext.current
    var txs by remember { mutableStateOf<List<CustomerTransactionEntity>>(emptyList()) }
    LaunchedEffect(id) { vm.ledger(id).collect { txs = it } }
    val c = customers.find { it.id == id }
    val opening = c?.openingBalance ?: 0.0
    val bals = Accounting.runningBalances(opening, txs)
    val rows = txs.zip(bals.reversed().let { if (it.size == txs.size) it.reversed() else it })
    Scaffold(topBar = { TopAppBar(title = { Text("Ledger — ${c?.name ?: ""}") }, navigationIcon = {
        IconButton(onClick = { nav.popBackStack() }) { Icon(Icons.Default.ArrowBack, null) }
    }, actions = {
        IconButton(onClick = {
            scope.launch {
                val header = listOf("Date", "Type", "Description", "Debit", "Credit", "Balance")
                val data = txs.mapIndexed { i, t -> listOf(Dates.fmt(t.timestamp, prefs.dateFmt), t.type, t.description, t.debit.toString(), t.credit.toString(), (bals.getOrNull(i) ?: 0.0).toString()) }
                val csv = Exporters.exportCsv(ctx, "ledger-$id.csv", header, data)
                val pdf = Exporters.exportPdf(ctx, "ledger-$id.pdf", "Customer Ledger — ${c?.name}", listOf("Business: ${prefs.businessName}", "Generated: ${Dates.fmtDateTime(System.currentTimeMillis())}"), header, data)
                val uri = FileProvider.getUriForFile(ctx, ctx.packageName + ".fileprovider", pdf)
                ctx.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply { type = "application/pdf"; putExtra(Intent.EXTRA_STREAM, uri); addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) }, "Share statement"))
            }
        }) { Icon(Icons.Default.Share, null) }
    }) }) { pad ->
        LazyColumn(Modifier.padding(pad).padding(12.dp)) {
            if (txs.isEmpty()) item { EmptyState("No transactions yet.") }
            items(txs.size) { i ->
                val t = txs[i]; val rb = bals.getOrNull(i) ?: 0.0
                Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Row(Modifier.fillMaxWidth().padding(10.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column(Modifier.weight(1f)) {
                            Text(t.description.ifBlank { t.category }, fontWeight = FontWeight.SemiBold)
                            Text("${Dates.fmt(t.timestamp, prefs.dateFmt)} • ${t.paymentMethod} • ${t.type}", style = MaterialTheme.typography.bodySmall)
                        }
                        Column(horizontalAlignment = androidx.compose.ui.Alignment.End) {
                            if (t.debit > 0) Text("+${formatMoney(t.debit, prefs.currency, prefs.decimals)}", color = MaterialTheme.colorScheme.error)
                            if (t.credit > 0) Text("-${formatMoney(t.credit, prefs.currency, prefs.decimals)}", color = MaterialTheme.colorScheme.primary)
                            Text("Bal: ${formatMoney(rb, prefs.currency, prefs.decimals)}", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTxScreen(vm: AppViewModel, nav: NavController, id: Long, isDebit: Boolean) {
    val prefs by vm.prefs.collectAsState(initial = AppPrefs())
    val scope = rememberCoroutineScope()
    var amount by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var method by remember { mutableStateOf(prefs.defaultPay.ifBlank { "Cash" }) }
    var category by remember { mutableStateOf(if (isDebit) "meal_charge" else "payment") }
    var err by remember { mutableStateOf("") }
    var lastTxId by remember { mutableStateOf<Long?>(null) }
    Scaffold(topBar = { TopAppBar(title = { Text(if (isDebit) "Add Charge (Debit)" else "Add Payment (Credit)") }, navigationIcon = {
        IconButton(onClick = { nav.popBackStack() }) { Icon(Icons.Default.ArrowBack, null) }
    }) }) { pad ->
        Column(Modifier.padding(pad).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (err.isNotBlank()) Text(err, color = MaterialTheme.colorScheme.error)
            OutlinedTextField(amount, { amount = it }, label = { Text("Amount *") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            OutlinedTextField(desc, { desc = it }, label = { Text("Description *") }, modifier = Modifier.fillMaxWidth())
            if (isDebit) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("meal_charge", "extra_meal", "adjustment", "opening").forEach { cat ->
                        FilterChip(selected = category == cat, onClick = { category = cat }, label = { Text(cat) })
                    }
                }
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("payment", "advance", "discount").forEach { cat ->
                        FilterChip(selected = category == cat, onClick = { category = cat }, label = { Text(cat) })
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Cash", "Bank transfer", "Card", "Mobile wallet", "Other").forEach { m ->
                    FilterChip(selected = method == m, onClick = { method = m }, label = { Text(m) })
                }
            }
            Button(onClick = {
                val a = amount.toDoubleOrNull()
                if (a == null || a <= 0) { err = "Enter a valid amount."; return@Button }
                if (desc.isBlank()) { err = "Description is required."; return@Button }
                scope.launch {
                    try {
                        val e = CustomerTransactionEntity(customerId = id, type = if (isDebit) "DEBIT" else "CREDIT",
                            category = category, description = desc.trim(),
                            debit = if (isDebit) a else 0.0, credit = if (!isDebit) a else 0.0,
                            paymentMethod = method)
                        val txId = vm.addTx(e)
                        lastTxId = txId
                        if (!isDebit) nav.navigate(Routes.receipt(txId)) { popUpTo(Routes.CUSTOMER_DETAIL.replace("{id}", id.toString())) }
                        else nav.popBackStack()
                    } catch (ex: Exception) { err = ex.message ?: "Save failed (possible duplicate). Change description or amount." }
                }
            }, modifier = Modifier.fillMaxWidth()) { Text(if (isDebit) "Save Charge" else "Save Payment") }
            lastTxId?.let { Text("Saved. Receipt # $it") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptScreen(vm: AppViewModel, nav: NavController, txId: Long) {
    val prefs by vm.prefs.collectAsState(initial = AppPrefs())
    val cash by vm.cashbook.collectAsState()
    Scaffold(topBar = { TopAppBar(title = { Text("Payment Receipt") }, navigationIcon = {
        IconButton(onClick = { nav.popBackStack() }) { Icon(Icons.Default.ArrowBack, null) }
    }) }) { pad ->
        Column(Modifier.padding(pad).padding(16.dp)) {
            Card { Column(Modifier.padding(16.dp)) {
                Text(prefs.businessName, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text("Payment Receipt #$txId")
                Text("Date: ${Dates.fmtDateTime(System.currentTimeMillis())}")
                Text(prefs.invoiceFooter, style = MaterialTheme.typography.bodySmall)
            } }
            Spacer(Modifier.height(12.dp))
            Button(onClick = { nav.popBackStack() }) { Text("Done") }
        }
    }
}
