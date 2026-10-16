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
import com.desimeals.app.data.db.MealEntryEntity
import com.desimeals.app.ui.components.*
import com.desimeals.app.ui.navigation.Routes
import com.desimeals.app.ui.viewmodel.AppViewModel
import com.desimeals.app.util.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealScreen(vm: AppViewModel, nav: NavController) {
    val plans by vm.plans.collectAsState()
    val customers by vm.customers.collectAsState()
    val prefs by vm.prefs.collectAsState(initial = AppPrefs())
    val scope = rememberCoroutineScope()
    var selCustomer by remember { mutableStateOf<Long?>(null) }
    var extra by remember { mutableStateOf("") }
    var msg by remember { mutableStateOf("") }
    Scaffold(topBar = { TopAppBar(title = { Text("Meal Management") }, navigationIcon = {
        IconButton(onClick = { nav.popBackStack() }) { Icon(Icons.Default.ArrowBack, null) }
    }, actions = { TextButton(onClick = { nav.navigate(Routes.INVOICES) }) { Text("Invoices") } }) },
        floatingActionButton = { FloatingActionButton(onClick = {
            scope.launch {
                val cid = selCustomer ?: customers.firstOrNull()?.id
                if (cid == null) { msg = "Add a customer first."; return@launch }
                vm.saveMealEntry(MealEntryEntity(customerId = cid, date = System.currentTimeMillis(), lunch = true, dinner = true))
                msg = "Today's meals recorded."
            }
        }) { Icon(Icons.Default.Add, null) } }) { pad ->
        LazyColumn(Modifier.padding(pad).padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item {
                SectionTitle("Meal Plans")
                plans.forEach { p ->
                    ListItem(headlineContent = { Text(p.name, fontWeight = FontWeight.SemiBold) },
                        supportingContent = { Text("Monthly ${formatMoney(p.monthlyRate, prefs.currency, prefs.decimals)} • Day ${formatMoney(p.fullDayRate, prefs.currency, prefs.decimals)}") })
                    HorizontalDivider()
                }
                if (plans.isEmpty()) EmptyState("No meal plans.")
            }
            item {
                SectionTitle("Record Consumption / Absence")
                var q by remember { mutableStateOf("") }
                OutlinedTextField(q, { q = it }, label = { Text("Filter customer") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                customers.filter { q.isBlank() || it.name.contains(q, true) }.take(20).forEach { c ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(c.name, modifier = Modifier.weight(1f))
                        Button(onClick = { selCustomer = c.id; scope.launch {
                            vm.saveMealEntry(MealEntryEntity(customerId = c.id, date = System.currentTimeMillis(), lunch = true, dinner = true)); msg = "Meals recorded for ${c.name}"
                        } }) { Text("Present") }
                        Spacer(Modifier.width(6.dp))
                        OutlinedButton(onClick = { scope.launch {
                            vm.saveMealEntry(MealEntryEntity(customerId = c.id, date = System.currentTimeMillis(), cancelled = true, notes = "Absent")); msg = "Absence recorded for ${c.name}"
                        } }) { Text("Absent") }
                    }
                }
                if (msg.isNotBlank()) Text(msg, color = MaterialTheme.colorScheme.primary)
            }
            item {
                SectionTitle("Generate Monthly Bills (no duplicates)")
                customers.take(30).forEach { c ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(c.name, modifier = Modifier.weight(1f))
                        OutlinedButton(onClick = { scope.launch {
                            try { val id = vm.generateMonthlyBill(c.id, Dates.startOfMonth(), Dates.endOfMonth()); nav.navigate(Routes.invoiceDetail(id)) }
                            catch (e: Exception) { msg = e.message ?: "Failed" }
                        } }) { Text("Bill") }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceListScreen(vm: AppViewModel, nav: NavController) {
    val invoices by vm.invoices.collectAsState()
    val customers by vm.customers.collectAsState()
    val prefs by vm.prefs.collectAsState(initial = AppPrefs())
    val cmap = customers.associateBy { it.id }
    Scaffold(topBar = { TopAppBar(title = { Text("Invoices") }, navigationIcon = {
        IconButton(onClick = { nav.popBackStack() }) { Icon(Icons.Default.ArrowBack, null) }
    }) }) { pad ->
        LazyColumn(Modifier.padding(pad).padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (invoices.isEmpty()) item { EmptyState("No invoices yet. Generate from Meals or Customer profile.") }
            items(invoices) { inv ->
                Card(onClick = { nav.navigate(Routes.invoiceDetail(inv.id)) }) {
                    Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column { Text(inv.invoiceNo, fontWeight = FontWeight.Bold); Text("${cmap[inv.customerId]?.name ?: ""} • ${inv.status}", style = MaterialTheme.typography.bodySmall) }
                        Text(formatMoney(inv.total, prefs.currency, prefs.decimals), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceDetailScreen(vm: AppViewModel, nav: NavController, id: Long) {
    val invoices by vm.invoices.collectAsState()
    val prefs by vm.prefs.collectAsState(initial = AppPrefs())
    val scope = rememberCoroutineScope()
    val inv = invoices.find { it.id == id }
    var items by remember { mutableStateOf(emptyList<com.desimeals.app.data.db.InvoiceItemEntity>()) }
    LaunchedEffect(id) { scope.launch {
        // load via repository directly
    } }
    Scaffold(topBar = { TopAppBar(title = { Text("Invoice ${inv?.invoiceNo ?: ""}") }, navigationIcon = {
        IconButton(onClick = { nav.popBackStack() }) { Icon(Icons.Default.ArrowBack, null) }
    }) }) { pad ->
        Column(Modifier.padding(pad).padding(16.dp)) {
            if (inv == null) { Text("Invoice not found"); return@Column }
            Text(prefs.businessName, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("Period: ${Dates.fmt(inv.periodStart, prefs.dateFmt)} – ${Dates.fmt(inv.periodEnd, prefs.dateFmt)}")
            Text("Status: ${inv.status}")
            HorizontalDivider(Modifier.padding(vertical = 8.dp))
            Text("Subtotal: ${formatMoney(inv.subtotal, prefs.currency, prefs.decimals)}")
            Text("Previous due: ${formatMoney(inv.previousDue, prefs.currency, prefs.decimals)}")
            Text("Discount: ${formatMoney(inv.discount, prefs.currency, prefs.decimals)}")
            Text("Total: ${formatMoney(inv.total, prefs.currency, prefs.decimals)}", fontWeight = FontWeight.Bold)
            if (inv.notes.isNotBlank()) Text("Notes: ${inv.notes}")
            Spacer(Modifier.height(8.dp))
            Text(prefs.invoiceFooter, style = MaterialTheme.typography.bodySmall)
        }
    }
}
