package com.desimeals.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.desimeals.app.data.local.TransactionType
import com.desimeals.app.ui.components.BalanceCard
import com.desimeals.app.ui.components.EmptyState
import com.desimeals.app.ui.components.SectionHeader
import com.desimeals.app.ui.components.TransactionRow
import com.desimeals.app.ui.viewmodel.CustomerDetailViewModel
import com.desimeals.app.util.CurrencyFormatter
import com.desimeals.app.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerDetailScreen(
    viewModel: CustomerDetailViewModel,
    onBack: () -> Unit,
    onAddTransaction: () -> Unit
) {
    val customer by viewModel.customer.collectAsState()
    val balance by viewModel.balance.collectAsState()
    val txs by viewModel.transactions.collectAsState()
    val meals by viewModel.meals.collectAsState()
    val error by viewModel.error.collectAsState()
    val snacks = remember { SnackbarHostState() }

    LaunchedEffect(error) {
        if (error != null) {
            snacks.showSnackbar(error!!)
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(customer?.name ?: "Member", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snacks) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddTransaction,
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Bill / Payment") }
            )
        }
    ) { padding ->
        val c = customer
        if (c == null) {
            Column(Modifier.fillMaxSize().padding(padding).padding(24.dp)) {
                EmptyState(
                    title = "Member not found",
                    message = "This record may have been deleted. Go back and pick another member."
                )
            }
            return@Scaffold
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (c.phone.isNotBlank() || c.address.isNotBlank()) {
                        Text(
                            listOf(c.phone, c.address).filter { it.isNotBlank() }.joinToString(" • "),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                    Text(
                        "Rate: ${CurrencyFormatter.format(c.monthlyRate)}/month",
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    BalanceCard(
                        billed = balance.billed,
                        paid = balance.paid,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                viewModel.addTransaction(
                                    TransactionType.CHARGE, c.monthlyRate,
                                    "Monthly mess bill — ${DateUtils.formatShort(System.currentTimeMillis())}"
                                ) {}
                            },
                            modifier = Modifier.weight(1f)
                        ) { Text("Add monthly bill") }
                        Button(onClick = onAddTransaction, modifier = Modifier.weight(1f)) {
                            Text("Record payment")
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    SectionHeader(title = "Ledger (${txs.size})")
                }
            }
            if (txs.isEmpty()) {
                item {
                    EmptyState(
                        title = "No ledger entries",
                        message = "Add a monthly charge or record a payment. Balance is calculated offline automatically.",
                        icon = Icons.Filled.ReceiptLong
                    )
                }
            } else {
                items(txs, key = { it.id }) { tx ->
                    TransactionRow(tx = tx, modifier = Modifier.padding(horizontal = 16.dp))
                }
            }
            item {
                SectionHeader(title = "Meal history (${meals.sumOf { it.mealCount }} meals)")
                if (meals.isEmpty()) {
                    EmptyState(
                        title = "No meals marked",
                        message = "Mark breakfast, lunch and dinner from the Meals tab."
                    )
                }
            }
            items(meals.take(30), key = { it.id }) { m ->
                Column(Modifier.padding(horizontal = 16.dp)) {
                    Text(
                        "${DateUtils.format(m.date)} — " + listOf(
                            if (m.breakfast) "Breakfast" else null,
                            if (m.lunch) "Lunch" else null,
                            if (m.dinner) "Dinner" else null
                        ).filterNotNull().joinToString(", "),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}
