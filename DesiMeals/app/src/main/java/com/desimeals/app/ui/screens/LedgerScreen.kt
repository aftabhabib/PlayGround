package com.desimeals.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.desimeals.app.data.local.TransactionType
import com.desimeals.app.ui.components.ConfirmDeleteDialog
import com.desimeals.app.ui.components.EmptyState
import com.desimeals.app.ui.components.TransactionRow
import com.desimeals.app.ui.viewmodel.DesiViewModelFactory
import com.desimeals.app.ui.viewmodel.LedgerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LedgerScreen(factory: DesiViewModelFactory, onAdd: () -> Unit) {
    val vm: LedgerViewModel = viewModel(factory = factory)
    val txs by vm.transactions.collectAsState()
    val filter by vm.filter.collectAsState()
    var toDelete by remember { mutableStateOf<Long?>(null) }
    val snacks = remember { SnackbarHostState() }

    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text("Ledger", fontWeight = FontWeight.Bold) }) },
        snackbarHost = { SnackbarHost(snacks) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAdd) {
                Icon(Icons.Filled.Add, contentDescription = "Add entry")
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(selected = filter == null, onClick = { vm.setFilter(null) }, label = { Text("All") })
                FilterChip(selected = filter == TransactionType.CHARGE, onClick = { vm.setFilter(TransactionType.CHARGE) }, label = { Text("Bills") })
                FilterChip(selected = filter == TransactionType.PAYMENT, onClick = { vm.setFilter(TransactionType.PAYMENT) }, label = { Text("Payments") })
                FilterChip(selected = filter == TransactionType.EXPENSE, onClick = { vm.setFilter(TransactionType.EXPENSE) }, label = { Text("Expenses") })
            }
            if (txs.isEmpty()) {
                EmptyState(
                    title = "Ledger is empty",
                    message = "Record monthly bills, received payments and mess expenses. All entries are stored offline.",
                    icon = Icons.Filled.ReceiptLong,
                    actionLabel = "Add entry",
                    onAction = onAdd
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(txs, key = { it.id }) { tx ->
                        TransactionRow(tx = tx)
                    }
                }
            }
        }
    }

    toDelete?.let { id ->
        ConfirmDeleteDialog(
            title = "Delete entry?",
            message = "This ledger entry will be permanently removed from this device.",
            onConfirm = { vm.delete(id); toDelete = null },
            onDismiss = { toDelete = null }
        )
    }
}
