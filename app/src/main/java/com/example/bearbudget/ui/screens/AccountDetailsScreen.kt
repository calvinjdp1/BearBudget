package com.example.bearbudget.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.bearbudget.network.Transaction
import kotlinx.coroutines.launch
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountDetailsScreen(
    accountName: String,
    accountType: String,
    startingBalance: Double = 0.0,
    navController: NavController,
    viewModel: AccountsViewModel = viewModel()
) {
    var selectedMonth by remember { mutableStateOf(getCurrentMonth()) }
    val transactions by viewModel.transactions.collectAsState()
    val accounts by viewModel.accounts.collectAsState()
    val account = accounts.find { it.name == accountName }
    val balance = account?.balance ?: startingBalance

    var selectedTransaction by remember { mutableStateOf<Transaction?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showActionDialog by remember { mutableStateOf(false) }
    var actionType by remember { mutableStateOf("") }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    LaunchedEffect(selectedMonth, accountName) {
        viewModel.fetchTransactions(selectedMonth, accountName)
        viewModel.fetchAccounts()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(accountName) },
                actions = {
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Account")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text(
                text = "Balance: $${String.format("%.2f", balance)}",
                style = MaterialTheme.typography.titleMedium,
                color = if (balance < 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                if (accountType in listOf("Credit Card", "Loan", "Debt")) {
                    Button(onClick = { actionType = "payment"; showActionDialog = true }) {
                        Text("Make Payment")
                    }
                } else {
                    Button(onClick = { actionType = "deposit"; showActionDialog = true }) {
                        Text("Deposit")
                    }
                    Button(onClick = { actionType = "withdraw"; showActionDialog = true }) {
                        Text("Withdraw")
                    }
                    Button(onClick = { actionType = "transfer"; showActionDialog = true }) {
                        Text("Transfer")
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                OutlinedTextField(
                    value = selectedMonth,
                    onValueChange = { selectedMonth = it },
                    label = { Text("Month (YYYY-MM)") },
                    modifier = Modifier.width(180.dp)
                )
            }

            Spacer(Modifier.height(24.dp))
            Text("Transactions for $selectedMonth", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            if (transactions.isEmpty()) {
                Text("No transactions for this month.")
            } else {
                LazyColumn {
                    items(transactions) { tx ->
                        val isTransfer = tx.description.isNullOrBlank() || tx.description.equals("no description", true)
                        val desc = if (isTransfer) "Transfer" else tx.description
                        val category = if (isTransfer) {
                            if (tx.card == accountName) "Withdrawal" else "Deposit"
                        } else tx.category ?: if (tx.amount >= 0) "Deposit" else "Withdrawal"

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedTransaction = tx.copy(description = desc, category = category)
                                    scope.launch { sheetState.show() }
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(desc ?: "Transfer", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                                Text("Category: $category", style = MaterialTheme.typography.bodySmall)
                            }
                            val amountColor = if (tx.amount < 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                            Text("$${String.format("%.2f", tx.amount)}", style = MaterialTheme.typography.bodyLarge, color = amountColor)
                        }
                        Divider()
                    }

                    // --- Mirror entry if no transfer transaction is present for this account ---
                    val hasTransfer = transactions.any {
                        it.description.equals("no description", true) || it.description.equals("Transfer", true)
                    }
                    if (!hasTransfer) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Transfer", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                                    Text("Category: Deposit", style = MaterialTheme.typography.bodySmall)
                                }
                                Text("$0.00", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
                            }
                            Divider()
                        }
                    }
                }
            }
        }
    }

    if (selectedTransaction != null) {
        ModalBottomSheet(
            sheetState = sheetState,
            onDismissRequest = { selectedTransaction = null }
        ) {
            TransactionDetailContent(
                transaction = selectedTransaction!!,
                accountName = accountName
            ) {
                selectedTransaction = null
                scope.launch { sheetState.hide() }
            }
        }
    }

    if (showDeleteDialog) {
        ConfirmDeleteDialog(
            accountName = accountName,
            onConfirm = {
                val accountToDelete = viewModel.accounts.value.find { it.name == accountName }
                if (accountToDelete?.type == "Debt") {
                    viewModel.deleteDebt(accountName)
                } else {
                    viewModel.deleteBank(accountName)
                }
                showDeleteDialog = false
                viewModel.fetchAccounts()
                navController.popBackStack()
            },
            onDismiss = { showDeleteDialog = false }
        )
    }

    if (showActionDialog) {
        val destinationAccounts = viewModel.accounts.value
            .filter { it.name != accountName }
            .map { it.name }

        MoneyActionDialog(
            action = actionType,
            allAccounts = destinationAccounts,
            onDismiss = { showActionDialog = false },
            onConfirm = { amount, destination ->
                when (actionType) {
                    "payment" -> viewModel.transferFunds(destination ?: "", accountName, amount)
                    "transfer" -> viewModel.transferFunds(accountName, destination ?: "", amount)
                    else -> viewModel.adjustAccountFunds(accountName, actionType, amount)
                }
                showActionDialog = false
                viewModel.fetchAccounts()
                navController.popBackStack() // <-- Go back to main accounts page
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoneyActionDialog(
    action: String,
    allAccounts: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (Double, String?) -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var selectedAccount by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = {
                val amt = amount.toDoubleOrNull() ?: 0.0
                onConfirm(amt, if (action == "transfer" || action == "payment") selectedAccount else null)
            }) { Text("Confirm") }
        },
        dismissButton = { Button(onClick = onDismiss) { Text("Cancel") } },
        title = { Text(action.replaceFirstChar { it.uppercase() }) },
        text = {
            Column {
                if (action == "transfer" || action == "payment") {
                    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                        OutlinedTextField(
                            value = selectedAccount,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(if (action == "transfer") "Destination Account" else "Bank Source") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            allAccounts.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = { selectedAccount = option; expanded = false }
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                }
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        }
    )
}

fun getCurrentMonth(): String {
    val now = LocalDate.now()
    return "${now.year}-${now.monthValue.toString().padStart(2, '0')}"
}

@Composable
fun TransactionDetailContent(transaction: Transaction, accountName: String, onClose: () -> Unit) {
    val isTransfer = transaction.description.isNullOrBlank() || transaction.description.equals("no description", true)
    val desc = if (isTransfer) "Transfer" else transaction.description
    val category = if (isTransfer) {
        if (transaction.card == accountName) "Withdrawal" else "Deposit"
    } else transaction.category ?: if (transaction.amount >= 0) "Deposit" else "Withdrawal"

    Column(modifier = Modifier.padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(desc ?: "Transfer", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(
                text = "$${String.format("%.2f", transaction.amount)}",
                style = MaterialTheme.typography.titleLarge,
                color = if (transaction.amount < 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )
        }
        Spacer(Modifier.height(16.dp))
        Text("Date: ${transaction.date}", style = MaterialTheme.typography.bodyLarge)
        Text("Category: $category", style = MaterialTheme.typography.bodyLarge)
        Text("Account/Card: ${transaction.card ?: "N/A"}", style = MaterialTheme.typography.bodyLarge)
        if (!transaction.notes.isNullOrBlank()) {
            Text("Notes: ${transaction.notes}", style = MaterialTheme.typography.bodyLarge)
        }
        Spacer(Modifier.height(16.dp))
        Button(onClick = onClose, modifier = Modifier.fillMaxWidth()) {
            Text("Close")
        }
    }
}

@Composable
fun ConfirmDeleteDialog(accountName: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = onConfirm, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
                Text("Delete")
            }
        },
        dismissButton = { Button(onClick = onDismiss) { Text("Cancel") } },
        title = { Text("Delete Account") },
        text = { Text("Are you sure you want to delete \"$accountName\"? This action cannot be undone.") }
    )
}
