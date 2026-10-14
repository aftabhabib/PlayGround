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
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.desimeals.app.ui.components.BalanceCard
import com.desimeals.app.ui.components.EmptyState
import com.desimeals.app.ui.components.OfflineBanner
import com.desimeals.app.ui.components.SectionHeader
import com.desimeals.app.ui.components.StatCard
import com.desimeals.app.ui.components.TransactionRow
import com.desimeals.app.ui.viewmodel.DashboardViewModel
import com.desimeals.app.ui.viewmodel.DesiViewModelFactory
import com.desimeals.app.util.CurrencyFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    factory: DesiViewModelFactory,
    onNavigateToCustomers: () -> Unit,
    onNavigateToLedger: () -> Unit,
    onNavigateToMeals: () -> Unit,
    onCustomerClick: (Long) -> Unit
) {
    val vm: DashboardViewModel = viewModel(factory = factory)
    val stats by vm.stats.collectAsState()
    val recent by vm.recent.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text("Desi Meals", fontWeight = FontWeight.Bold) })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToLedger) {
                Icon(Icons.Filled.Add, contentDescription = "Add entry")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Column {
                    OfflineBanner()
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Assalam-o-Alaikum! Here's your mess at a glance.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatCard(
                            title = "Receivable",
                            value = CurrencyFormatter.format(stats.totalReceivable),
                            subtitle = "${stats.activeMembers} active members",
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = "This month net",
                            value = CurrencyFormatter.format(stats.monthNet),
                            subtitle = "Received - Expenses",
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    BalanceCard(
                        billed = stats.totalBilled,
                        paid = stats.totalReceived,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(onClick = onNavigateToCustomers, modifier = Modifier.weight(1f)) {
                            Text("Members")
                        }
                        OutlinedButton(onClick = onNavigateToMeals, modifier = Modifier.weight(1f)) {
                            Text("Meals")
                        }
                        Button(onClick = onNavigateToLedger, modifier = Modifier.weight(1f)) {
                            Text("Ledger")
                        }
                    }
                    SectionHeader(title = "Recent activity")
                }
            }
            if (recent.isEmpty()) {
                item {
                    EmptyState(
                        title = "No entries yet",
                        message = "Add your first member, monthly bill or expense to see it here. Everything is saved offline on this device.",
                        icon = Icons.Filled.ReceiptLong,
                        actionLabel = "Open ledger",
                        onAction = onNavigateToLedger
                    )
                }
            } else {
                items(recent.take(20), key = { it.id }) { tx ->
                    TransactionRow(tx = tx, modifier = Modifier.padding(horizontal = 16.dp))
                }
            }
        }
    }
}
