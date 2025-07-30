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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(viewModel: TransactionsViewModel = viewModel()) {
    val transactions by viewModel.transactions.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val cards by viewModel.cards.collectAsState()

    var selectedTransaction by remember { mutableStateOf<Transaction?>(null) }
    var showAddSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddSheet = true }) {
                Text("+")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text("Transactions", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn {
                items(transactions) { transaction ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedTransaction = transaction
                                scope.launch { sheetState.show() }
                            }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(transaction.date, style = MaterialTheme.typography.bodyMedium)
                            Text(transaction.category ?: "No Category", style = MaterialTheme.typography.bodyLarge)
                            if (!transaction.card.isNullOrBlank()) {
                                Text("Card: ${transaction.card}", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        Text(
                            text = String.format("$%.2f", transaction.amount),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    HorizontalDivider()
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
            TransactionDetails(
                transaction = selectedTransaction!!,
                categories = categories,
                cards = cards,
                onUpdate = { updated ->
                    viewModel.updateTransaction(updated.id!!, updated) {
                        scope.launch { sheetState.hide() }
                        selectedTransaction = null
                    }
                },
                onDelete = {
                    viewModel.deleteTransaction(selectedTransaction!!.id!!) {
                        scope.launch { sheetState.hide() }
                        selectedTransaction = null
                    }
                }
            )
        }
    }

    // Add transaction sheet
    if (showAddSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAddSheet = false }
        ) {
            AddTransactionScreen(viewModel = viewModel) {
                viewModel.fetchTransactions() // refresh after adding
                showAddSheet = false
            }
        }
    }
}
