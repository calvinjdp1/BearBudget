package com.example.bearbudget.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.bearbudget.network.AccountItem
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountsScreen(navController: NavController, viewModel: AccountsViewModel = viewModel()) {
    val accounts by viewModel.accounts.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.fetchAccounts() }

    // --- Calculate totals ---
    val totalSavings = accounts.filter { it.type != "Credit Card" && it.type != "Loan" && it.type != "Debt" }
        .sumOf { it.balance }
    val totalDebt = accounts.filter { it.type == "Credit Card" || it.type == "Loan" || it.type == "Debt" }
        .sumOf { abs(it.balance) }
    val netWorth = totalSavings - totalDebt

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Account")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // --- Gray Box Summary ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2E2E2E)), // Dark gray box
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    // Net Worth big and color based on value
                    Text(
                        text = "Net Worth: $${String.format("%.2f", netWorth)}",
                        style = MaterialTheme.typography.headlineMedium,
                        color = if (netWorth < 0) MaterialTheme.colorScheme.error else Color(0xFF4CAF50)
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Total Savings: $${String.format("%.2f", totalSavings)}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White
                        )
                        Text(
                            text = "Total Debt: ${if (totalDebt == 0.0) "$0.00" else "-$${String.format("%.2f", totalDebt)}"}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("Accounts", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))

            if (accounts.isEmpty()) {
                Text("No accounts available.")
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(accounts) { account ->
                        AccountItemRow(account = account) {
                            navController.navigate("account_details/${account.name}?type=${account.type}")
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        AddAccountDialog(
            onDismiss = { showDialog = false },
            onAdd = { name, type, balance ->
                viewModel.addAccount(name, type, balance)
                showDialog = false
            }
        )
    }
}

@Composable
fun AccountItemRow(account: AccountItem, onClick: () -> Unit) {
    val balance = if (account.type == "Credit Card" || account.type == "Loan" || account.type == "Debt") {
        -abs(account.balance)
    } else {
        account.balance
    }

    val balanceColor = if (balance < 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(account.name, style = MaterialTheme.typography.titleMedium)
            Text(account.type, style = MaterialTheme.typography.bodySmall)
        }
        Text(
            "$${String.format("%.2f", balance)}",
            style = MaterialTheme.typography.titleMedium,
            color = balanceColor
        )
    }
    Divider()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAccountDialog(onDismiss: () -> Unit, onAdd: (String, String, Double) -> Unit) {
    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("Debit") }
    var balance by remember { mutableStateOf("") }
    val accountTypes = listOf("Debit", "Credit Card", "Loan", "Savings")
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = {
                val bal = balance.toDoubleOrNull() ?: 0.0
                onAdd(name, type, bal)
            }) { Text("Save") }
        },
        dismissButton = { Button(onClick = onDismiss) { Text("Cancel") } },
        title = { Text("Add Account") },
        text = {
            Column {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Account Name") })
                Spacer(Modifier.height(12.dp))
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                    OutlinedTextField(
                        value = type,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Account Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        accountTypes.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = { type = option; expanded = false }
                            )
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = balance,
                    onValueChange = { balance = it },
                    label = { Text("Balance") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        }
    )
}
