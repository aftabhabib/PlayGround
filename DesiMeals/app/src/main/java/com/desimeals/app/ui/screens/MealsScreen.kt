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
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.desimeals.app.ui.components.EmptyState
import com.desimeals.app.ui.viewmodel.DesiViewModelFactory
import com.desimeals.app.ui.viewmodel.MealsViewModel
import com.desimeals.app.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealsScreen(factory: DesiViewModelFactory) {
    val vm: MealsViewModel = viewModel(factory = factory)
    val day by vm.day.collectAsState()
    val members by vm.members.collectAsState()
    val entries by vm.entries.collectAsState()
    val byCustomer = rememberMap(entries)

    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text("Daily Meals", fontWeight = FontWeight.Bold) }) }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            Row(
                Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = { vm.shiftDay(-1) }) {
                    Icon(Icons.Filled.ChevronLeft, contentDescription = "Previous day")
                }
                Text(DateUtils.format(day), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                IconButton(onClick = { vm.shiftDay(1) }) {
                    Icon(Icons.Filled.ChevronRight, contentDescription = "Next day")
                }
            }
            if (members.isEmpty()) {
                EmptyState(
                    title = "No active members",
                    message = "Add members first, then mark their daily breakfast, lunch and dinner here."
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(members, key = { it.id }) { m ->
                        val e = byCustomer[m.id]
                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(12.dp)) {
                                Text(m.name, fontWeight = FontWeight.SemiBold)
                                Text(
                                    if (e == null) "No meals marked"
                                    else "${e.mealCount} meal(s) marked",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    FilterChip(
                                        selected = e?.breakfast == true,
                                        onClick = { vm.toggle(m.id, "breakfast") },
                                        label = { Text("Breakfast") }
                                    )
                                    FilterChip(
                                        selected = e?.lunch == true,
                                        onClick = { vm.toggle(m.id, "lunch") },
                                        label = { Text("Lunch") }
                                    )
                                    FilterChip(
                                        selected = e?.dinner == true,
                                        onClick = { vm.toggle(m.id, "dinner") },
                                        label = { Text("Dinner") }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun rememberMap(entries: List<com.desimeals.app.data.local.MealEntryEntity>): Map<Long, com.desimeals.app.data.local.MealEntryEntity> {
    return entries.associateBy { it.customerId }
}
