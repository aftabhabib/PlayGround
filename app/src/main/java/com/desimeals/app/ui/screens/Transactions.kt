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
import com.desimeals.app.data.db.CashbookEntity
import com.desimeals.app.ui.components.*
import com.desimeals.app.ui.navigation.Routes
import com.desimeals.app.ui.viewmodel.AppViewModel
import com.desimeals.app.util.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionHistoryScreen(vm: AppViewModel, nav: NavController) {
    val cash by vm.cashbook.collectAsState()
    val prefs by vm.prefs.collectAsState(initial = AppPrefs())
    var q by remember { mutableStateOf("") }
    var kind by remember { mutableStateOf("All") } // All, IN, OUT
    val shown = cash.filter {
        (kind == "All" || it.kind == kind) && (q.isBlank() || it.title.contains(q, true) || it.category.contains(q, true))
    }
    Scaffold(topBar = { TopAppBar(title = { Text("Transactions") }, actions = {
        IconButton(onClick = { nav.navigate(Routes.CASHBOOK) }) { Icon(Icons.Default.Book, null) }
        IconButton(onClick = { nav.navigate(Routes.ACCOUNTS) }) { Icon(Icons.Default.AccountBalance, null) }
    }) }) { pad ->
        Column(Modifier.padding(pad).padding(12.dp)) {
            OutlinedTextField(q, { q = it }, Modifier.fillMaxWidth(), label = { Text("Search transactions") }, singleLine = true)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("All", "IN", "OUT").forEach { k -> FilterChip(selected = kind == k, onClick = { kind = k }, label = { Text(k) }) }
            }
            if (shown.isEmpty()) EmptyState("No transactions match.")
            LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items(shown.take(200)) { t ->
                    ListItem(headlineContent = { Text(t.title) },
                        supportingContent = { Text("${Dates.fmt(t.date, prefs.dateFmt)} • ${t.category} • ${t.method}") },
                        trailingContent = {
                            Text((if (t.kind == "IN") "+" else "−") + formatMoney(t.amount, prefs.currency, prefs.decimals),
                                color = if (t.kind == "IN") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Bold)
                        })
                    HorizontalDivider()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CashbookScreen(vm: AppViewModel, nav: NavController) {
    val cash by vm.cashbook.collectAsState()
    val accounts by vm.accounts.collectAsState()
    val prefs by vm.prefs.collectAsState(initial = AppPrefs())
    val scope = rememberCoroutineScope()
    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var kind by remember { mutableStateOf("IN") }
    var category by remember { mutableStateOf("other_income") }
    var show by remember { mutableStateOf(false) }
    val cashAcc = accounts.firstOrNull { it.type == "CASH" } ?: accounts.firstOrNull()
    Scaffold(topBar = { TopAppBar(title = { Text("Cashbook") }, navigationIcon = {
        IconButton(onClick = { nav.popBackStack() }) { Icon(Icons.Default.ArrowBack, null) }
    }) }, floatingActionButton = { FloatingActionButton(onClick = { show = true }) { Icon(Icons.Default.Add, null) } }) { pad ->
        LazyColumn(Modifier.padding(pad).padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            item {
                val now = System.currentTimeMillis()
                val day = cash.filter { it.date in Dates.startOfDay(now)..Dates.endOfDay(now) }
                val inAmt = day.filter { it.kind == "IN" }.sumOf { it.amount }
                val outAmt = day.filter { it.kind == "OUT" }.sumOf { it.amount }
                Text("Today: In ${formatMoney(inAmt, prefs.currency, prefs.decimals)} • Out ${formatMoney(outAmt, prefs.currency, prefs.decimals)} • Net ${formatMoney(inAmt - outAmt, prefs.currency, prefs.decimals)}", fontWeight = FontWeight.SemiBold)
            }
            if (cash.isEmpty()) item { EmptyState("Cashbook is empty.") }
            items(cash.take(200)) { t ->
                ListItem(headlineContent = { Text(t.title) }, supportingContent = { Text("${Dates.fmt(t.date, prefs.dateFmt)} • ${t.category}") },
                    trailingContent = { Text(formatMoney(t.amount, prefs.currency, prefs.decimals)) })
                HorizontalDivider()
            }
        }
        if (show) AlertDialog(onDismissRequest = { show = false }, title = { Text("Add Cashbook Entry") }, text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(title, { title = it }, label = { Text("Title") })
                OutlinedTextField(amount, { amount = it }, label = { Text("Amount") })
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(kind == "IN", { kind = "IN" }, { Text("Cash In") })
                    FilterChip(kind == "OUT", { kind = "OUT" }, { Text("Cash Out") })
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("other_income", "cash_sale", "capital_in", "withdrawal", "refund", "adjustment").forEach { c ->
                        FilterChip(category == c, { category = c }, { Text(c.take(8)) })
                    }
                }
            }
        }, confirmButton = { TextButton(onClick = {
            val a = amount.toDoubleOrNull() ?: 0.0
            if (title.isNotBlank() && a > 0 && cashAcc != null) scope.launch {
                vm.saveCash(CashbookEntity(accountId = cashAcc.id, kind = kind, category = category, title = title.trim(), amount = a))
                show = false; title = ""; amount = ""
            }
        }) { Text("Save") } }, dismissButton = { TextButton(onClick = { show = false }) { Text("Cancel") } })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountsScreen(vm: AppViewModel, nav: NavController) {
    val accounts by vm.accounts.collectAsState()
    val cash by vm.cashbook.collectAsState()
    val prefs by vm.prefs.collectAsState(initial = AppPrefs())
    val scope = rememberCoroutineScope()
    var name by remember { mutableStateOf("") }
    var opening by remember { mutableStateOf("0") }
    var type by remember { mutableStateOf("CASH") }
    var fromId by remember { mutableStateOf<Long?>(null) }
    var toId by remember { mutableStateOf<Long?>(null) }
    var tAmt by remember { mutableStateOf("") }
    Scaffold(topBar = { TopAppBar(title = { Text("Cash & Bank Accounts") }, navigationIcon = {
        IconButton(onClick = { nav.popBackStack() }) { Icon(Icons.Default.ArrowBack, null) }
    }) }) { pad ->
        LazyColumn(Modifier.padding(pad).padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item {
                SectionTitle("Accounts")
                accounts.forEach { a ->
                    val bal = a.openingBalance + cash.filter { it.accountId == a.id }.sumOf { if (it.kind == "IN") it.amount else -it.amount }
                    Card { Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column { Text(a.name, fontWeight = FontWeight.Bold); Text(a.type, style = MaterialTheme.typography.bodySmall) }
                        Text(formatMoney(bal, prefs.currency, prefs.decimals), fontWeight = FontWeight.Bold)
                    } }
                    Spacer(Modifier.height(6.dp))
                }
                OutlinedTextField(name, { name = it }, label = { Text("Account name") }, modifier = Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(type == "CASH", { type = "CASH" }, { Text("Cash") })
                    FilterChip(type == "BANK", { type = "BANK" }, { Text("Bank") })
                    OutlinedTextField(opening, { opening = it }, label = { Text("Opening") }, modifier = Modifier.weight(1f))
                }
                Button(onClick = { if (name.isNotBlank()) scope.launch {
                    vm.saveAccount(com.desimeals.app.data.db.AccountEntity(name = name.trim(), type = type, openingBalance = opening.toDoubleOrNull() ?: 0.0)); name = ""
                } }) { Text("Add Account") }
            }
            item {
                SectionTitle("Transfer Between Accounts (not income/expense)")
                OutlinedTextField(tAmt, { tAmt = it }, label = { Text("Amount") }, modifier = Modifier.fillMaxWidth())
                Text("From: ${accounts.find { it.id == fromId }?.name ?: "select"} → To: ${accounts.find { it.id == toId }?.name ?: "select"}")
                accounts.forEach { a ->
                    Row { FilterChip(fromId == a.id, { fromId = a.id }, { Text("From ${a.name}") }); Spacer(Modifier.width(6.dp)); FilterChip(toId == a.id, { toId = a.id }, { Text("To ${a.name}") }) }
                }
                Button(onClick = {
                    val f = fromId; val t = toId; val amt = tAmt.toDoubleOrNull() ?: 0.0
                    if (f != null && t != null && f != t && amt > 0) scope.launch { vm.saveTransfer(f, t, amt, "Manual transfer"); tAmt = "" }
                }) { Text("Transfer") }
            }
        }
    }
}
