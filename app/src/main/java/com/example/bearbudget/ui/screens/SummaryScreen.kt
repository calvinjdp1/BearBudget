package com.example.bearbudget.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bearbudget.network.SummaryItem

@Composable
fun SummaryScreen(viewModel: SummaryViewModel = viewModel()) {
    val summary by viewModel.summary.collectAsState()
    var expandedCategory by remember { mutableStateOf<String?>(null) }

    // Compute totals directly from summary
    val totalBudgeted = summary.sumOf { it.budget }
    val totalRemaining = summary.sumOf { it.remaining }
    val totalRemainingColor = if (totalRemaining < 0) {
        MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Top totals
        Text(
            text = "Summary",
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Total Budgeted: $${String.format("%.2f", totalBudgeted)}",
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = "Total Remaining: $${String.format("%.2f", totalRemaining)}",
            style = MaterialTheme.typography.titleMedium,
            color = totalRemainingColor
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Header row for categories
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Categories",
                fontSize = 22.sp,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Remaining Balance",
                fontSize = 22.sp,
                style = MaterialTheme.typography.titleMedium
            )
        }

        // Category list
        LazyColumn {
            items(summary) { item ->
                CategorySummaryRow(
                    item = item,
                    isExpanded = expandedCategory == item.category,
                    onClick = {
                        expandedCategory =
                            if (expandedCategory == item.category) null else item.category
                    }
                )
            }
        }
    }
}

@Composable
fun CategorySummaryRow(item: SummaryItem, isExpanded: Boolean, onClick: () -> Unit) {
    val remainingColor = if (item.remaining < 0) {
        MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .clickable { onClick() }
    ) {
        // Always visible row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                item.category,
                fontSize = 20.sp,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                "$${String.format("%.2f", item.remaining)}",
                fontSize = 20.sp,
                style = MaterialTheme.typography.bodyLarge,
                color = remainingColor
            )
        }

        // Expanded details
        if (isExpanded) {
            Spacer(modifier = Modifier.height(12.dp))
            Column(modifier = Modifier.padding(start = 12.dp, end = 8.dp)) {
                Text("Used: $${String.format("%.2f", item.amount_used)}", fontSize = 18.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Rollover: $${String.format("%.2f", item.rollover)}", fontSize = 18.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Budget: $${String.format("%.2f", item.budget)}", fontSize = 18.sp)
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        HorizontalDivider(modifier = Modifier.padding(top = 12.dp))
    }
}
