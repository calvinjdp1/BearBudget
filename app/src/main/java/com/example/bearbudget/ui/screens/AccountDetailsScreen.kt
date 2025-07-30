package com.example.bearbudget.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bearbudget.network.Transaction
import kotlinx.coroutines.launch
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountDetailsScreen(
    accountName: String,
    accountType: String = "Bank",   // Pass actual type when navigating
    viewModel: AccountsViewModel = viewModel()
) {
    var selectedMonth by remember { mutableStateOf(getCurrentMonth()) }
    val transactions by viewModel.transactions.collectAsState()
    var selectedTransaction by remember { mutableStateOf<Transaction?>(null) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    // Manage Funds dialog
    var showAdjustDialog by remember { mutableStateOf(false) }

    LaunchedEffect(selectedMonth, accountName) {
        viewModel.fetchTransactions(selectedMonth, accountName)
    }

    // Calculate balance (expenses negative, income positive)
    val balance = remember(transactions) {
        transactions.sumOf { tx ->
            when (tx.transaction_type) {
                "expense" -> -tx.amount
                "income" -> tx.amount
                else -> 0.0
            }
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showAdjustDialog = true }) {
                Text("+") // Simple label; you can replace with Icon
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(16.dp)) {
            Text(text = accountName, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Balance: $${String.format("%.2f", balance)}",
                style = MaterialTheme.typography.titleMedium,
                color = if (balance < 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = selectedMonth,
                onValueChange = { selectedMonth = it },
                label = { Text("Month (YYYY-MM)") }
            )

            Spacer(Modifier.height(24.dp))
            Text(text = "Transactions for $selectedMonth", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            if (transactions.isEmpty()) {
                Text(text = "No transactions for this month.")
            } else {
                LazyColumn {
                    items(transactions) { tx ->
                        AccountTransactionRow(transaction = tx) {
                            selectedTransaction = tx
                            scope.launch { sheetState.show() }
                        }
                    }
                }
            }
        }
    }

    // Transaction details bottom sheet
    if (selectedTransaction != null) {
        ModalBottomSheet(
            sheetState = sheetState,
            onDismissRequest = { selectedTransaction = null }
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Date: ${selectedTransaction!!.date}", style = MaterialTheme.typography.bodyLarge)
                Text("Description: ${selectedTransaction!!.description}", style = MaterialTheme.typography.bodyLarge)
                Text("Category: ${selectedTransaction!!.category}", style = MaterialTheme.typography.bodyLarge)
                Text("Account/Card: ${selectedTransaction!!.card}", style = MaterialTheme.typography.bodyLarge)
                Text("Notes: ${selectedTransaction!!.notes}", style = MaterialTheme.typography.bodyLarge)
                Text("Amount: $${selectedTransaction!!.amount}", style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = {
                        selectedTransaction = null
                        scope.launch { sheetState.hide() }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Close") }
            }
        }
    }

    // Adjust funds dialog
    if (showAdjustDialog) {
        AdjustFundsDialog(
            accountType = accountType,
            onDismiss = { showAdjustDialog = false },
            onConfirm = { action, amount ->
                viewModel.adjustAccountFunds(accountName, action, amount)
                showAdjustDialog = false
            }
        )
    }
}

@Composable
fun AccountTransactionRow(transaction: Transaction, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            val description = transaction.description?.ifBlank { "No Description" } ?: "No Description"
            Text(description, style = MaterialTheme.typography.bodyLarge)

            val category = transaction.category?.ifBlank { "No Category" } ?: "No Category"
            Text("Category: $category", style = MaterialTheme.typography.bodySmall)
        }

        val amountColor = if (transaction.amount < 0)
            MaterialTheme.colorScheme.error
        else
            MaterialTheme.colorScheme.primary

        Text(
            text = String.format("$%.2f", transaction.amount),
            style = MaterialTheme.typography.bodyLarge,
            color = amountColor
        )
    }
    HorizontalDivider()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdjustFundsDialog(
    accountType: String,
    onDismiss: () -> Unit,
    onConfirm: (String, Double) -> Unit
) {
    var action by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    val actions = when (accountType) {
        "Bank", "Savings" -> listOf("deposit", "withdraw", "update_balance")
        else -> listOf("payment", "update_balance")
    }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = {
                val amt = amount.toDoubleOrNull() ?: 0.0
                onConfirm(action, amt)
            }) { Text("Apply") }
        },
        dismissButton = { Button(onClick = onDismiss) { Text("Cancel") } },
        title = { Text("Adjust Funds") },
        text = {
            Column {
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                    OutlinedTextField(
                        value = action,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Action") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        actions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = { action = option; expanded = false }
                            )
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("Amount") })
            }
        }
    )
}

fun getCurrentMonth(): String {
    val now = LocalDate.now()
    return "${now.year}-${now.monthValue.toString().padStart(2, '0')}"
}
