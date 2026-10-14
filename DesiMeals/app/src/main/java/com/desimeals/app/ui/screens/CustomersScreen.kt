package com.desimeals.app.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.desimeals.app.data.local.CustomerEntity
import com.desimeals.app.ui.components.ConfirmDeleteDialog
import com.desimeals.app.ui.components.EmptyState
import com.desimeals.app.ui.viewmodel.CustomersViewModel
import com.desimeals.app.ui.viewmodel.DesiViewModelFactory
import com.desimeals.app.util.CurrencyFormatter

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CustomersScreen(factory: DesiViewModelFactory, onCustomerClick: (Long) -> Unit) {
    val vm: CustomersViewModel = viewModel(factory = factory)
    val list by vm.filtered.collectAsState()
    val query by vm.query.collectAsState()
    var showAdd by remember { mutableStateOf(false) }
    var toDelete by remember { mutableStateOf<CustomerEntity?>(null) }

    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text("Members", fontWeight = FontWeight.Bold) }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAdd = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Add member")
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            OutlinedTextField(
                value = query,
                onValueChange = vm::setQuery,
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                label = { Text("Search members") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true
            )
            if (list.isEmpty()) {
                EmptyState(
                    title = if (query.isBlank()) "No members yet" else "No matches",
                    message = if (query.isBlank()) "Add mess members to start tracking monthly bills and payments."
                    else "Try a different name, phone or address.",
                    icon = Icons.Filled.People,
                    actionLabel = if (query.isBlank()) "Add member" else null,
                    onAction = { showAdd = true }
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(list, key = { it.id }) { c ->
                        CustomerCard(
                            customer = c,
                            onClick = { onCustomerClick(c.id) },
                            onToggle = { vm.toggleActive(c) },
                            onDelete = { toDelete = c }
                        )
                    }
                }
            }
        }
    }

    if (showAdd) {
        AddCustomerDialog(
            onDismiss = { showAdd = false },
            onSave = { n, p, a, r, cb -> vm.addCustomer(n, p, a, r) { err -> if (err == null) showAdd = false; cb(err) } }
        )
    }

    toDelete?.let { c ->
        ConfirmDeleteDialog(
            title = "Remove ${c.name}?",
            message = "This will also delete their ledger and meal history stored on this device. This cannot be undone.",
            onConfirm = { vm.delete(c); toDelete = null },
            onDismiss = { toDelete = null }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CustomerCard(
    customer: CustomerEntity,
    onClick: () -> Unit,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick, onLongClick = { showMenu = true })
    ) {
        Row(
            Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(customer.name, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleSmall)
                if (customer.phone.isNotBlank()) Text(customer.phone, style = MaterialTheme.typography.bodySmall)
                Text(
                    "${CurrencyFormatter.format(customer.monthlyRate)}/mo • ${if (customer.isActive) "Active" else "Inactive"}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.width(8.dp))
            Switch(checked = customer.isActive, onCheckedChange = { onToggle() })
        }
    }
    if (showMenu) {
        AlertDialog(
            onDismissRequest = { showMenu = false },
            title = { Text(customer.name) },
            text = { Text("Long-press menu. Delete removes all local records for this member.") },
            confirmButton = {
                TextButton(onClick = { showMenu = false; onDelete() }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showMenu = false; onClick() }) { Text("Open") }
            }
        )
    }
}

@Composable
private fun AddCustomerDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, String, Double, ((String?) -> Unit)) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var rate by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add member") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name *") }, singleLine = true)
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone))
                OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Address") }, singleLine = true)
                OutlinedTextField(value = rate, onValueChange = { rate = it }, label = { Text("Monthly rate (Rs)") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                if (error != null) Text(error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val r = rate.toDoubleOrNull() ?: 0.0
                onSave(name, phone, address, r) { err -> error = err }
            }) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
