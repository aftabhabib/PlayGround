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
import com.desimeals.app.ui.components.*
import com.desimeals.app.ui.navigation.Routes
import com.desimeals.app.ui.viewmodel.AppViewModel
import com.desimeals.app.util.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(vm: AppViewModel, nav: NavController) {
    val dash by vm.dash.collectAsState()
    val prefs by vm.prefs.collectAsState(initial = AppPrefs())
    val cash by vm.cashbook.collectAsState()
    val balances by vm.balances.collectAsState()
    val customers by vm.customers.collectAsState()
    val scope = rememberCoroutineScope()
    var range by remember { mutableStateOf("Month") }

    fun applyRange(r: String) {
        range = r
        scope.launch {
            val now = System.currentTimeMillis()
            when (r) {
                "Today" -> vm.refresh(Dates.startOfDay(now), Dates.endOfDay(now))
                "Week" -> vm.refresh(Dates.startOfWeek(now), Dates.endOfDay(now))
                "Month" -> vm.refresh(Dates.startOfMonth(now), Dates.endOfMonth(now))
                "Year" -> vm.refresh(Dates.startOfYear(now), Dates.endOfDay(now))
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(prefs.businessName, fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { nav.navigate(Routes.SEARCH) }) { Icon(Icons.Default.Search, null) }
                    IconButton(onClick = { nav.navigate(Routes.SETTINGS) }) { Icon(Icons.Default.Settings, null) }
                })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { nav.navigate(Routes.ADD_CUSTOMER) }) { Icon(Icons.Default.Add, null) }
        }
    ) { pad ->
        LazyColumn(Modifier.padding(pad).padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Today", "Week", "Month", "Year").forEach { r ->
                        FilterChip(selected = range == r, onClick = { applyRange(r) }, label = { Text(r) })
                    }
                }
            }
            item {
                SectionTitle("Financial Summary")
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SummaryCard("Received", dash.received, prefs.currency, prefs.decimals, Modifier.weight(1f), "green")
                        SummaryCard("Paid Out", dash.paidOut, prefs.currency, prefs.decimals, Modifier.weight(1f), "red")
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SummaryCard("Outstanding", dash.outstanding, prefs.currency, prefs.decimals, Modifier.weight(1f))
                        SummaryCard("Expenses", dash.expenses, prefs.currency, prefs.decimals, Modifier.weight(1f))
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SummaryCard("Advance", dash.advance, prefs.currency, prefs.decimals, Modifier.weight(1f), "green")
                        SummaryCard("Net (M)", dash.monthIncome - dash.monthExpense, prefs.currency, prefs.decimals, Modifier.weight(1f))
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SummaryCard("Today In", dash.todayIncome, prefs.currency, prefs.decimals, Modifier.weight(1f))
                        SummaryCard("Today Out", dash.todayExpense, prefs.currency, prefs.decimals, Modifier.weight(1f))
                    }
                }
            }
            item {
                SectionTitle("Customers")
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SummaryCard("Total", dash.totalCustomers.toDouble(), prefs.currency, 0, Modifier.weight(1f))
                    SummaryCard("Active", dash.activeCustomers.toDouble(), prefs.currency, 0, Modifier.weight(1f))
                    SummaryCard("Due", dash.dueCustomers.toDouble(), prefs.currency, 0, Modifier.weight(1f))
                }
            }
            item {
                SectionTitle("Quick Actions")
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { nav.navigate(Routes.CUSTOMERS) }, modifier = Modifier.weight(1f)) { Text("Customers") }
                    Button(onClick = { nav.navigate(Routes.EXPENSES) }, modifier = Modifier.weight(1f)) { Text("Expenses") }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { nav.navigate(Routes.MEALS) }, modifier = Modifier.weight(1f)) { Text("Meals") }
                    OutlinedButton(onClick = { nav.navigate(Routes.CASHBOOK) }, modifier = Modifier.weight(1f)) { Text("Cashbook") }
                    OutlinedButton(onClick = { nav.navigate(Routes.INVOICES) }, modifier = Modifier.weight(1f)) { Text("Invoices") }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { nav.navigate(Routes.SUPPLIERS) }, modifier = Modifier.weight(1f)) { Text("Suppliers") }
                    OutlinedButton(onClick = { nav.navigate(Routes.ACCOUNTS) }, modifier = Modifier.weight(1f)) { Text("Accounts") }
                    OutlinedButton(onClick = { nav.navigate(Routes.BACKUP) }, modifier = Modifier.weight(1f)) { Text("Backup") }
                }
            }
            item {
                SectionTitle("Pending Payments")
                val due = customers.mapNotNull { c ->
                    val b = balances[c.id] ?: 0.0
                    if (b > 0.005) c to b else null
                }.sortedByDescending { it.second }.take(5)
                if (due.isEmpty()) Text("All settled. No pending payments.", style = MaterialTheme.typography.bodyMedium)
                due.forEach { (c, b) ->
                    Card(onClick = { nav.navigate(Routes.customerDetail(c.id)) }) {
                        Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(c.name, fontWeight = FontWeight.SemiBold)
                            Text(formatMoney(b, prefs.currency, prefs.decimals), color = MaterialTheme.colorScheme.error)
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                }
            }
            item {
                SectionTitle("Recent Activity")
                cash.take(6).forEach { t ->
                    ListItem(headlineContent = { Text(t.title) }, supportingContent = { Text(Dates.fmt(t.date, prefs.dateFmt)) },
                        trailingContent = { Text(formatMoney(t.amount, prefs.currency, prefs.decimals)) })
                    HorizontalDivider()
                }
                if (cash.isEmpty()) EmptyState("No transactions yet. Add customers and record charges.")
            }
        }
    }
}
