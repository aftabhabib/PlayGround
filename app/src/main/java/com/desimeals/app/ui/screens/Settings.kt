package com.desimeals.app.ui.screens

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
import androidx.navigation.NavController
import com.desimeals.app.ui.components.*
import com.desimeals.app.ui.navigation.Routes
import com.desimeals.app.ui.viewmodel.AppViewModel
import com.desimeals.app.util.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(vm: AppViewModel, nav: NavController) {
    val prefs by vm.prefs.collectAsState(initial = AppPrefs())
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    var name by remember(prefs) { mutableStateOf(prefs.businessName) }
    var owner by remember(prefs) { mutableStateOf(prefs.ownerName) }
    var contact by remember(prefs) { mutableStateOf(prefs.contact) }
    var email by remember(prefs) { mutableStateOf(prefs.email) }
    var address by remember(prefs) { mutableStateOf(prefs.address) }
    var footer by remember(prefs) { mutableStateOf(prefs.invoiceFooter) }
    Scaffold(topBar = { TopAppBar(title = { Text("Settings") }) }) { pad ->
        LazyColumn(Modifier.padding(pad).padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item {
                SectionTitle("Business")
                OutlinedTextField(name, { name = it }, label = { Text("Business name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(owner, { owner = it }, label = { Text("Owner name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(contact, { contact = it }, label = { Text("Contact") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(email, { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(address, { address = it }, label = { Text("Address") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(footer, { footer = it }, label = { Text("Invoice footer") }, modifier = Modifier.fillMaxWidth())
                Button(onClick = { scope.launch {
                    setPref(ctx) { it[PrefsKeys.BUSINESS_NAME] = name; it[PrefsKeys.OWNER_NAME] = owner; it[PrefsKeys.CONTACT] = contact; it[PrefsKeys.EMAIL] = email; it[PrefsKeys.ADDRESS] = address; it[PrefsKeys.INVOICE_FOOTER] = footer }
                } }) { Text("Save Business Settings") }
            }
            item {
                SectionTitle("Preferences")
                Card(onClick = { nav.navigate(Routes.CURRENCY) }) { Row(Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween) { Text("Currency: ${prefs.currency}"); Icon(Icons.Default.ChevronRight, null) } }
                Spacer(Modifier.height(6.dp))
                Card(onClick = { nav.navigate(Routes.THEME) }) { Row(Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween) { Text("Appearance: ${prefs.theme}"); Icon(Icons.Default.ChevronRight, null) } }
                Spacer(Modifier.height(6.dp))
                Card(onClick = { nav.navigate(Routes.BACKUP) }) { Row(Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween) { Text("Backup & Restore (last: ${prefs.lastBackup})"); Icon(Icons.Default.ChevronRight, null) } }
                Spacer(Modifier.height(6.dp))
                Card(onClick = { nav.navigate(Routes.ABOUT) }) { Row(Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween) { Text("About Desi Meals"); Icon(Icons.Default.ChevronRight, null) } }
            }
            item {
                SectionTitle("Notifications")
                @Composable fun ToggleRow(label: String, value: Boolean, set: suspend (Boolean) -> Unit) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(label); Switch(value, { v -> scope.launch { set(v) } })
                    }
                }
                ToggleRow("Payment due reminders", prefs.notifDue) { v -> setPref(ctx) { p -> p[PrefsKeys.NOTIF_DUE] = v } }
                ToggleRow("Overdue alerts", prefs.notifOverdue) { v -> setPref(ctx) { p -> p[PrefsKeys.NOTIF_OVERDUE] = v } }
                ToggleRow("Daily summary", prefs.notifSummary) { v -> setPref(ctx) { p -> p[PrefsKeys.NOTIF_SUMMARY] = v } }
                ToggleRow("Backup reminders", prefs.notifBackup) { v -> setPref(ctx) { p -> p[PrefsKeys.NOTIF_BACKUP] = v } }
            }
            item {
                SectionTitle("Security (optional PIN)")
                var pin by remember(prefs) { mutableStateOf(prefs.pin) }
                OutlinedTextField(pin, { pin = it }, label = { Text("App PIN (empty = off)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                Button(onClick = { scope.launch { setPref(ctx) { it[PrefsKeys.PIN] = pin } } }) { Text("Save PIN") }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencyScreen(vm: AppViewModel, nav: NavController) {
    val prefs by vm.prefs.collectAsState(initial = AppPrefs())
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    var q by remember { mutableStateOf("") }
    Scaffold(topBar = { TopAppBar(title = { Text("Select Currency") }, navigationIcon = {
        IconButton(onClick = { nav.popBackStack() }) { Icon(Icons.Default.ArrowBack, null) }
    }) }) { pad ->
        LazyColumn(Modifier.padding(pad).padding(12.dp)) {
            item { OutlinedTextField(q, { q = it }, Modifier.fillMaxWidth(), label = { Text("Search currencies") }, singleLine = true) }
            item { Text("Display-only change: existing records keep original amounts. No silent conversion.", style = MaterialTheme.typography.labelSmall) }
            items(SUPPORTED_CURRENCIES.filter { q.isBlank() || it.name.contains(q, true) || it.code.contains(q, true) }) { c ->
                Card(onClick = { scope.launch { setPref(ctx) { it[PrefsKeys.CURRENCY] = c.code }; nav.popBackStack() } }) {
                    Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column { Text("${c.name}", fontWeight = FontWeight.SemiBold); Text(c.code, style = MaterialTheme.typography.bodySmall) }
                        Text(c.symbol + "  " + formatMoney(1234.56, c.code, prefs.decimals))
                    }
                }
                Spacer(Modifier.height(6.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeScreen(vm: AppViewModel, nav: NavController) {
    val prefs by vm.prefs.collectAsState(initial = AppPrefs())
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    Scaffold(topBar = { TopAppBar(title = { Text("Appearance") }, navigationIcon = {
        IconButton(onClick = { nav.popBackStack() }) { Icon(Icons.Default.ArrowBack, null) }
    }) }) { pad ->
        Column(Modifier.padding(pad).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("light" to "Light", "dark" to "Dark", "system" to "System default").forEach { (v, l) ->
                Card(onClick = { scope.launch { setPref(ctx) { it[PrefsKeys.THEME] = v } } }) {
                    Row(Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(l); if (prefs.theme == v) Icon(Icons.Default.Check, null)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupScreen(vm: AppViewModel, nav: NavController) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val prefs by vm.prefs.collectAsState(initial = AppPrefs())
    var msg by remember { mutableStateOf("") }
    var files by remember { mutableStateOf(listOf<java.io.File>()) }
    fun reload() { files = BackupHelper.listBackups(ctx) }
    LaunchedEffect(Unit) { reload() }
    var confirmRestore by remember { mutableStateOf<java.io.File?>(null) }
    confirmRestore?.let { f -> ConfirmDialog("Restore backup", "Replace current data with ${f.name}? This cannot be undone.", {
        try { BackupHelper.restore(ctx, f); msg = "Restored. Restart app to reload." } catch (e: Exception) { msg = e.message ?: "Restore failed" }
        confirmRestore = null; reload()
    }, { confirmRestore = null }) }
    Scaffold(topBar = { TopAppBar(title = { Text("Backup & Restore") }, navigationIcon = {
        IconButton(onClick = { nav.popBackStack() }) { Icon(Icons.Default.ArrowBack, null) }
    }) }) { pad ->
        LazyColumn(Modifier.padding(pad).padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                Text("Last backup: ${prefs.lastBackup}")
                if (msg.isNotBlank()) Text(msg, color = MaterialTheme.colorScheme.primary)
                Button(onClick = { scope.launch {
                    try { val f = BackupHelper.backup(ctx); BackupHelper.markBackup(ctx, Dates.fmtDateTime(System.currentTimeMillis()) + " • " + f.name); msg = "Backup saved: ${f.name}"; reload(); vm.refresh() }
                    catch (e: Exception) { msg = e.message ?: "Backup failed" }
                } }) { Text("Create Manual Backup") }
                SectionTitle("Saved backups (validated before restore)")
            }
            items(files) { f ->
                Card { Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(Modifier.weight(1f)) { Text(f.name, fontWeight = FontWeight.SemiBold); Text(if (BackupHelper.validate(f)) "Valid SQLite backup • ${f.length() / 1024} KB" else "Invalid file", style = MaterialTheme.typography.bodySmall) }
                    TextButton(onClick = { confirmRestore = f }) { Text("Restore") }
                } }
            }
            if (files.isEmpty()) item { EmptyState("No backups yet.") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(nav: NavController) {
    Scaffold(topBar = { TopAppBar(title = { Text("About Desi Meals") }, navigationIcon = {
        IconButton(onClick = { nav.popBackStack() }) { Icon(Icons.Default.ArrowBack, null) }
    }) }) { pad ->
        Column(Modifier.padding(pad).padding(16.dp)) {
            Text("Desi Meals", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text("Version 1.0.0")
            Spacer(Modifier.height(8.dp))
            Text("Desi Meals is a simple and reliable mess management and accounting application designed to help food businesses manage customers, meal charges, payments, expenses, and financial reports in one convenient place.")
            Spacer(Modifier.height(8.dp))
            Text("© 2026 Desi Meals. All rights reserved.", style = MaterialTheme.typography.bodySmall)
            TextButton(onClick = { nav.navigate(Routes.PRIVACY) }) { Text("Privacy Policy & Terms") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyScreen(nav: NavController) {
    Scaffold(topBar = { TopAppBar(title = { Text("Privacy & Terms") }, navigationIcon = {
        IconButton(onClick = { nav.popBackStack() }) { Icon(Icons.Default.ArrowBack, null) }
    }) }) { pad ->
        Column(Modifier.padding(pad).padding(16.dp)) {
            Text("Privacy Policy", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("All business data is stored locally on your device. Optional backups you create stay under your control. No data is uploaded without your action.")
            Spacer(Modifier.height(8.dp))
            Text("Terms", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("This app provides bookkeeping assistance and does not constitute professional financial advice. Verify reports with your records.")
        }
    }
}
