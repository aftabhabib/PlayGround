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
import com.desimeals.app.ui.components.*
import com.desimeals.app.ui.navigation.Routes
import com.desimeals.app.ui.viewmodel.AppViewModel
import com.desimeals.app.util.*
import kotlinx.coroutines.launch

val REPORT_KINDS = listOf(
    "profit" to "Profit & Loss",
    "income" to "Income Report",
    "expense" to "Expense Report",
    "receivables" to "Receivables (Due)",
    "payables" to "Payables (Suppliers)",
    "cashflow" to "Cash Flow / Cashbook",
    "meals" to "Meal Revenue",
    "overdue" to "Overdue Payments",
    "customers" to "Customer List"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(vm: AppViewModel, nav: NavController) {
    Scaffold(topBar = { TopAppBar(title = { Text("Reports") }) }) { pad ->
        LazyColumn(Modifier.padding(pad).padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            item { Text("Date-filtered financial reports with PDF / Excel / CSV export.", style = MaterialTheme.typography.bodyMedium) }
            items(REPORT_KINDS) { (kind, label) ->
                Card(onClick = { nav.navigate(Routes.reportView(kind)) }) {
                    Row(Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(label, fontWeight = FontWeight.SemiBold)
                        Icon(Icons.Default.ChevronRight, null)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportViewScreen(vm: AppViewModel, nav: NavController, kind: String) {
    val prefs by vm.prefs.collectAsState(initial = AppPrefs())
    val scope = rememberCoroutineScope()
    val ctx = LocalContext.current
    var preset by remember { mutableStateOf("This month") }
    var from by remember { mutableStateOf(Dates.startOfMonth()) }
    var to by remember { mutableStateOf(Dates.endOfMonth()) }
    var lines by remember { mutableStateOf<List<List<String>>>(emptyList()) }
    var header by remember { mutableStateOf<List<String>>(emptyList()) }
    var summary by remember { mutableStateOf("") }
    var msg by remember { mutableStateOf("") }

    fun applyPreset(p: String) {
        preset = p
        val now = System.currentTimeMillis()
        val cal = java.util.Calendar.getInstance()
        when (p) {
            "Today" -> { from = Dates.startOfDay(now); to = Dates.endOfDay(now) }
            "Yesterday" -> { from = Dates.startOfDay(now) - 86400000; to = Dates.endOfDay(now) - 86400000 }
            "This week" -> { from = Dates.startOfWeek(now); to = Dates.endOfDay(now) }
            "Last week" -> { from = Dates.startOfWeek(now) - 7 * 86400000; to = Dates.startOfWeek(now) - 1 }
            "This month" -> { from = Dates.startOfMonth(now); to = Dates.endOfMonth(now) }
            "Last month" -> { from = Dates.addMonths(Dates.startOfMonth(now), -1); to = Dates.startOfMonth(now) - 1 }
            "This year" -> { from = Dates.startOfYear(now); to = Dates.endOfDay(now) }
            "Last year" -> { cal.timeInMillis = Dates.startOfYear(now); cal.add(java.util.Calendar.YEAR, -1); from = cal.timeInMillis; cal.add(java.util.Calendar.YEAR, 1); to = cal.timeInMillis - 1 }
        }
    }

    suspend fun load() {
        try {
            val repo = com.desimeals.app.data.Repository.get(ctx)
            when (kind) {
                "profit", "income", "meals" -> {
                    val txs = repo.txs.between(from, to)
                    val debits = txs.filter { it.type == "DEBIT" && it.category != "opening" }.sumOf { it.debit }
                    val collects = txs.filter { it.type == "CREDIT" }.sumOf { it.credit }
                    val exps = repo.expenses.sumPaid(from, to)
                    val cash = repo.cash.between(from, to)
                    val otherIn = cash.filter { it.kind == "IN" && (it.category == "cash_sale" || it.category == "other_income") }.sumOf { it.amount }
                    val income = debits + otherIn
                    val profit = Accounting.profit(income, exps)
                    header = listOf("Metric", "Amount")
                    lines = listOf(
                        listOf("Meal charges (recognized income)", formatMoney(debits, prefs.currency, prefs.decimals)),
                        listOf("Other income (cash sales etc.)", formatMoney(otherIn, prefs.currency, prefs.decimals)),
                        listOf("Total recognized income", formatMoney(income, prefs.currency, prefs.decimals)),
                        listOf("Collections from customers (reduce receivables, not new income)", formatMoney(collects, prefs.currency, prefs.decimals)),
                        listOf("Business expenses", formatMoney(exps, prefs.currency, prefs.decimals)),
                        listOf(if (profit >= 0) "PROFIT" else "LOSS", formatMoney(profit, prefs.currency, prefs.decimals))
                    )
                    summary = "Income ${formatMoney(income, prefs.currency, prefs.decimals)} − Expenses ${formatMoney(exps, prefs.currency, prefs.decimals)} = ${formatMoney(profit, prefs.currency, prefs.decimals)}"
                }
                "expense" -> {
                    val exps = repo.expenses.between(from, to)
                    header = listOf("Date", "Category", "Title", "Amount", "Paid?")
                    lines = exps.map { listOf(Dates.fmt(it.date, prefs.dateFmt), it.category, it.title, it.amount.toString(), if (it.isPaid) "Paid" else "Unpaid") }
                    val byCat = exps.groupBy { it.category }.mapValues { it.value.sumOf { e -> e.amount } }.toList().sortedByDescending { it.second }
                    summary = "Total ${formatMoney(exps.sumOf { it.amount }, prefs.currency, prefs.decimals)} • Top: " + (byCat.take(3).joinToString(", ") { "${it.first}=${it.second}" })
                }
                "receivables", "overdue", "customers" -> {
                    val all = repo.customers.getAllOnce()
                    val bals = repo.allCustomerBalances()
                    val filtered = all.filter { c ->
                        val b = bals[c.id] ?: 0.0
                        when (kind) { "overdue" -> b > 0.005; "receivables" -> b > 0.005; else -> true }
                    }
                    header = listOf("Customer", "Mobile", "Status", "Balance")
                    lines = filtered.map { c -> listOf(c.name, c.mobile, c.status, (bals[c.id] ?: 0.0).toString()) }
                    summary = "Outstanding ${formatMoney(filtered.sumOf { bals[it.id] ?: 0.0 }, prefs.currency, prefs.decimals)} across ${filtered.size} customers"
                }
                "payables" -> {
                    val sups = repo.suppliers.allOnce()
                    header = listOf("Supplier", "Billed", "Paid", "Outstanding")
                    val rows = mutableListOf<List<String>>()
                    var tot = 0.0
                    sups.forEach { s ->
                        val billed = repo.suppliers.billsOnce(s.id).sumOf { it.amount }
                        val paid = repo.suppliers.paymentsOnce(s.id).sumOf { it.amount }
                        val o = Accounting.supplierOutstanding(billed, s.openingBalance, paid)
                        tot += o
                        rows.add(listOf(s.name, billed.toString(), paid.toString(), o.toString()))
                    }
                    lines = rows
                    summary = "Total payables ${formatMoney(tot, prefs.currency, prefs.decimals)}"
                }
                "cashflow" -> {
                    val cash = repo.cash.between(from, to)
                    header = listOf("Date", "Title", "Kind", "Category", "Amount")
                    lines = cash.map { listOf(Dates.fmt(it.date, prefs.dateFmt), it.title, it.kind, it.category, it.amount.toString()) }
                    val inn = cash.filter { it.kind == "IN" }.sumOf { it.amount }
                    val out = cash.filter { it.kind == "OUT" }.sumOf { it.amount }
                    summary = "In ${formatMoney(inn, prefs.currency, prefs.decimals)} • Out ${formatMoney(out, prefs.currency, prefs.decimals)} • Net ${formatMoney(inn - out, prefs.currency, prefs.decimals)} (transfers excluded from income/expense)"
                }
                else -> { header = listOf("Info"); lines = listOf(listOf("No data")) }
            }
        } catch (e: Exception) { msg = e.message ?: "Failed to load" }
    }

    LaunchedEffect(from, to, kind) { load() }

    Scaffold(topBar = { TopAppBar(title = { Text(REPORT_KINDS.find { it.first == kind }?.second ?: "Report") }, navigationIcon = {
        IconButton(onClick = { nav.popBackStack() }) { Icon(Icons.Default.ArrowBack, null) }
    }) }) { pad ->
        LazyColumn(Modifier.padding(pad).padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("Today", "This week", "This month", "Last month", "This year").forEach { p ->
                        FilterChip(preset == p, { applyPreset(p); scope.launch { load() } }, { Text(p.take(8)) })
                    }
                }
                Text("${Dates.fmt(from, prefs.dateFmt)} → ${Dates.fmt(to, prefs.dateFmt)} • ${prefs.businessName} • ${Dates.fmtDateTime(System.currentTimeMillis())}", style = MaterialTheme.typography.labelSmall)
                if (summary.isNotBlank()) Card { Text(summary, Modifier.padding(10.dp), fontWeight = FontWeight.SemiBold) }
                if (msg.isNotBlank()) Text(msg, color = MaterialTheme.colorScheme.error)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { scope.launch {
                        val f = Exporters.exportPdf(ctx, "report-$kind.pdf", REPORT_KINDS.find { it.first == kind }?.second ?: kind,
                            listOf("Business: ${prefs.businessName}", "Range: ${Dates.fmt(from)} to ${Dates.fmt(to)}", "Generated: ${Dates.fmtDateTime(System.currentTimeMillis())}"), header, lines, listOf(summary))
                        val uri = FileProvider.getUriForFile(ctx, ctx.packageName + ".fileprovider", f)
                        ctx.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply { type = "application/pdf"; putExtra(Intent.EXTRA_STREAM, uri); addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) }, "Share PDF"))
                    } }) { Text("PDF / Print") }
                    OutlinedButton(onClick = { scope.launch {
                        val f = Exporters.exportXlsx(ctx, "report-$kind.xlsx", kind, header, lines)
                        val uri = FileProvider.getUriForFile(ctx, ctx.packageName + ".fileprovider", f)
                        ctx.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply { type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"; putExtra(Intent.EXTRA_STREAM, uri); addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) }, "Share Excel"))
                    } }) { Text("Excel") }
                    OutlinedButton(onClick = { scope.launch {
                        val f = Exporters.exportCsv(ctx, "report-$kind.csv", header, lines)
                        val uri = FileProvider.getUriForFile(ctx, ctx.packageName + ".fileprovider", f)
                        ctx.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply { type = "text/csv"; putExtra(Intent.EXTRA_STREAM, uri); addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) }, "Share CSV"))
                    } }) { Text("CSV") }
                }
            }
            items(lines.take(300)) { r ->
                ListItem(headlineContent = { Text(r.firstOrNull() ?: "") }, supportingContent = { Text(r.drop(1).joinToString(" • ")) })
                HorizontalDivider()
            }
            if (lines.isEmpty()) item { EmptyState("No records in this period.") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(vm: AppViewModel, nav: NavController) {
    val customers by vm.customers.collectAsState()
    val expenses by vm.expenses.collectAsState()
    val cash by vm.cashbook.collectAsState()
    val prefs by vm.prefs.collectAsState(initial = AppPrefs())
    var q by remember { mutableStateOf("") }
    Scaffold(topBar = { TopAppBar(title = { Text("Search & Filter") }, navigationIcon = {
        IconButton(onClick = { nav.popBackStack() }) { Icon(Icons.Default.ArrowBack, null) }
    }) }) { pad ->
        LazyColumn(Modifier.padding(pad).padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            item { OutlinedTextField(q, { q = it }, Modifier.fillMaxWidth(), label = { Text("Search customers, transactions, expenses…") }, singleLine = true) }
            if (q.isNotBlank()) {
                item { SectionTitle("Customers") }
                items(customers.filter { it.name.contains(q, true) || it.mobile.contains(q) }.take(10)) { c ->
                    Card(onClick = { nav.navigate(Routes.customerDetail(c.id)) }) { Text(c.name + " • " + c.mobile, Modifier.padding(10.dp)) }
                }
                item { SectionTitle("Expenses") }
                items(expenses.filter { it.title.contains(q, true) || it.vendor.contains(q, true) }.take(10)) { e ->
                    Card(onClick = { nav.navigate(Routes.expenseDetail(e.id)) }) { Text(e.title + " • " + formatMoney(e.amount, prefs.currency, prefs.decimals), Modifier.padding(10.dp)) }
                }
                item { SectionTitle("Transactions") }
                items(cash.filter { it.title.contains(q, true) }.take(10)) { t ->
                    ListItem(headlineContent = { Text(t.title) }, supportingContent = { Text(t.category) })
                }
            } else item { EmptyState("Type to search across modules.") }
        }
    }
}
