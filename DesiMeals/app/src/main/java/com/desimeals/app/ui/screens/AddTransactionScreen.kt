package com.desimeals.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.desimeals.app.data.local.TransactionType
import com.desimeals.app.di.AppContainer
import com.desimeals.app.ui.viewmodel.DesiViewModelFactory
import com.desimeals.app.ui.viewmodel.LedgerViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    factory: DesiViewModelFactory,
    container: AppContainer,
    preselectedCustomerId: Long?,
    onBack: () -> Unit,
    onSaved: () -> Unit
) {
    val vm: LedgerViewModel = viewModel(factory = factory)
    val members by container.customerRepository.observeAll()
        .collectAsState(initial = emptyList())
    var type by remember { mutableStateOf(TransactionType.CHARGE) }
    var amount by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var customerId by remember(preselectedCustomerId) { mutableStateOf(preselectedCustomerId) }
    var expanded by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val snacks = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val selectedName = members.firstOrNull { it.id == customerId }?.name ?: "Mess (no member)"

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Add entry", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snacks) }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = type == TransactionType.CHARGE, onClick = { type = TransactionType.CHARGE }, label = { Text("Bill") })
                FilterChip(selected = type == TransactionType.PAYMENT, onClick = { type = TransactionType.PAYMENT }, label = { Text("Payment") })
                FilterChip(selected = type == TransactionType.EXPENSE, onClick = { type = TransactionType.EXPENSE }, label = { Text("Expense") })
                FilterChip(selected = type == TransactionType.INCOME, onClick = { type = TransactionType.INCOME }, label = { Text("Income") })
            }

            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                OutlinedTextField(
                    value = selectedName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Member (optional for mess expenses)") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    DropdownMenuItem(text = { Text("Mess (no member)") }, onClick = { customerId = null; expanded = false })
                    members.forEach { m ->
                        DropdownMenuItem(
                            text = { Text(m.name) },
                            onClick = { customerId = m.id; expanded = false }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Amount (Rs) *") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = desc,
                onValueChange = { desc = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )
            if (error != null) {
                Text(error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = {
                    val amt = amount.toDoubleOrNull()
                    if (amt == null || amt <= 0) {
                        error = "Enter a valid amount greater than zero"
                        return@Button
                    }
                    if ((type == TransactionType.CHARGE || type == TransactionType.PAYMENT) && customerId == null) {
                        error = "Bills and payments need a member. Pick one, or use Expense/Income for mess-level entries."
                        return@Button
                    }
                    vm.addMessTransaction(type, amt, desc, customerId) { err ->
                        if (err == null) onSaved()
                        else {
                            error = err
                            scope.launch { snacks.showSnackbar(err) }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save entry (offline)")
            }
            Text(
                "Saved locally in Room — no internet needed. Bills increase a member's due; payments reduce it.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
