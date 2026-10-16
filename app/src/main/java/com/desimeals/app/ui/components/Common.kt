package com.desimeals.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.desimeals.app.util.formatMoney

@Composable
fun SummaryCard(title: String, value: Double, currency: String, decimals: Int, modifier: Modifier = Modifier, tone: String = "neutral") {
    val container = when (tone) {
        "green" -> MaterialTheme.colorScheme.primaryContainer
        "red" -> MaterialTheme.colorScheme.errorContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    Card(modifier = modifier, shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = container)) {
        Column(Modifier.padding(14.dp)) {
            Text(title, style = MaterialTheme.typography.labelMedium)
            Spacer(Modifier.height(4.dp))
            Text(formatMoney(value, currency, decimals), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, maxLines = 1)
        }
    }
}

@Composable
fun EmptyState(text: String, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
        Text(text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun ConfirmDialog(title: String, message: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = { TextButton(onClick = onConfirm) { Text("Confirm") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun BarChart(values: List<Pair<String, Float>>, modifier: Modifier = Modifier, barColor: Color = Color(0xFF1B8C4B)) {
    val max = (values.maxOfOrNull { it.second } ?: 1f).coerceAtLeast(1f)
    Canvas(modifier = modifier.height(160.dp).fillMaxWidth()) {
        if (values.isEmpty()) return@Canvas
        val bw = size.width / (values.size * 2f)
        values.forEachIndexed { i, (_, v) ->
            val h = (v / max) * (size.height - 30)
            val x = bw * (i * 2 + 0.5f)
            drawRect(barColor, topLeft = androidx.compose.ui.geometry.Offset(x, size.height - h - 20), size = androidx.compose.ui.geometry.Size(bw, h))
        }
    }
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        values.take(6).forEach { (l, _) -> Text(l, style = MaterialTheme.typography.labelSmall) }
    }
}

@Composable
fun SectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(vertical = 8.dp))
}
