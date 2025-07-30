package com.example.bearbudget.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.bearbudget.network.Transaction
import java.time.LocalDate

@Composable
fun AccountDetailsScreen(
    accountName: String,
    viewModel: AccountsViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    var selectedMonth by remember { mutableStateOf(getCurrentMonth()) }
    val transactions by viewModel.transactions.collectAsState()
    var selectedTransaction by remember { mutableStateOf<Transaction?>(null) }

    LaunchedEffect(selectedMonth, accountName) {
        viewModel.fetchTransactions(selectedMonth, accountName)
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(accountName, style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = selectedMonth,
            onValueChange = { selectedMonth = it },
            label = { Text("Month (YYYY-MM)") }
        )
        Spacer(Modifier.height(16.dp))

        Text("Previous Balance: $0.00")
        Text("New Balance: $0.00")
        Spacer(Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
            Button(onClick = { /* Transfer */ }) { Text("Transfer") }
            Button(onClick = { /* Withdraw */ }) { Text("Withdraw") }
            Button(onClick = { /* Deposit */ }) { Text("Deposit") }
            Button(onClick = { /* Update Balance */ }) { Text("Update Balance") }
        }

        Spacer(Modifier.height(24.dp))
        Text("Transactions for $selectedMonth", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))

        if (transactions.isEmpty()) {
            Text("No transactions for this month.")
        } else {
            LazyColumn {
                items(transactions) { tx ->
                    Text("${tx.date} - ${tx.description} - $${tx.amount}")
                }
            }
        }
    }

    selectedTransaction?.let { tx ->
        AlertDialog(
            onDismissRequest = { selectedTransaction = null },
            title = { Text("Transaction Details") },
            text = {
                Column {
                    Text("Date: ${tx.date}")
                    Text("Description: ${tx.description}")
                    Text("Category: ${tx.category}")
                    Text("Account/Card: ${tx.card}")
                    Text("Notes: ${tx.notes}")
                    Text("Amount: $${tx.amount}")
                }
            },
            confirmButton = {
                Button(onClick = { selectedTransaction = null }) { Text("Close") }
            }
        )
    }
}

fun getCurrentMonth(): String {
    val now = LocalDate.now()
    return "${now.year}-${now.monthValue.toString().padStart(2, '0')}"
}
