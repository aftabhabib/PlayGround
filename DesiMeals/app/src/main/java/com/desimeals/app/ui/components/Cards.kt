package com.desimeals.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.desimeals.app.data.local.TransactionEntity
import com.desimeals.app.data.local.TransactionType
import com.desimeals.app.util.CurrencyFormatter
import com.desimeals.app.util.DateUtils

@Composable
fun StatCard(
    title: String,
    value: String,
    subtitle: String = "",
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onPrimaryContainer)
            Spacer(Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
            if (subtitle.isNotBlank()) {
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
        }
    }
}

@Composable
fun TransactionRow(
    tx: TransactionEntity,
    customerName: String? = null,
    modifier: Modifier = Modifier,
    onDelete: (() -> Unit)? = null
) {
    val isCredit = tx.type == TransactionType.PAYMENT || tx.type == TransactionType.INCOME
    Card(modifier = modifier.fillMaxWidth()) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                if (isCredit) Icons.Filled.ArrowDownward else Icons.Filled.ArrowUpward,
                contentDescription = tx.type.name,
                tint = if (isCredit) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    tx.description.ifBlank { tx.type.name },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(tx.type.name, style = MaterialTheme.typography.labelSmall)
                    if (customerName != null) Text("• $customerName", style = MaterialTheme.typography.labelSmall)
                    Text("• ${DateUtils.format(tx.date)}", style = MaterialTheme.typography.labelSmall)
                }
            }
            Text(
                (if (isCredit) "+" else "-") + CurrencyFormatter.format(tx.amount),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = if (isCredit) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
fun BalanceCard(billed: Double, paid: Double, modifier: Modifier = Modifier) {
    val due = billed - paid
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Billed", style = MaterialTheme.typography.labelMedium)
                Text(CurrencyFormatter.format(billed), fontWeight = FontWeight.Bold)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Paid", style = MaterialTheme.typography.labelMedium)
                Text(CurrencyFormatter.format(paid), fontWeight = FontWeight.Bold)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Due", style = MaterialTheme.typography.labelMedium)
                Text(
                    CurrencyFormatter.format(due),
                    fontWeight = FontWeight.Bold,
                    color = if (due > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
