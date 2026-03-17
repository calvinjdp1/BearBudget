package com.example.bearbudget.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bearbudget.network.Transaction
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

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

    // ✅ Ensure newest-first + same-day newest-first using id as tie-breaker
    val sorted = remember(transactions) {
        transactions.sortedWith(
            compareByDescending<Transaction> { it.date }
                .thenByDescending { it.id ?: 0 }
        )
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddSheet = true }) { Text("+") }
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
                var lastHeader: String? = null

                for (tx in sorted) {
                    val headerLabel = dateHeaderLabel(tx.date)

                    if (headerLabel != lastHeader) {
                        lastHeader = headerLabel
                        item {
                            DateGroupHeader(text = headerLabel)
                        }
                    }

                    item {
                        TransactionRow(
                            transaction = tx,
                            onClick = {
                                selectedTransaction = tx
                                scope.launch { sheetState.show() }
                            }
                        )
                        HorizontalDivider()
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

@Composable
private fun TransactionRow(
    transaction: Transaction,
    onClick: () -> Unit
) {
    val isTransfer =
        transaction.description.isNullOrBlank() ||
                transaction.description.equals("no description", true)

    val desc = if (isTransfer) "Transfer" else (transaction.description ?: "No description")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {

            // Date (small)
            Text(
                text = formatDisplayDate(transaction.date),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(2.dp))

            // Description (bigger)
            Text(
                text = desc,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.height(2.dp))

            // Category (small, same as card)
            Text(
                text = "Category: ${transaction.category ?: "No Category"}",
                style = MaterialTheme.typography.bodySmall
            )

            // Card (small)
            Text(
                text = "Card: ${transaction.card ?: "N/A"}",
                style = MaterialTheme.typography.bodySmall
            )
        }

        Text(
            text = String.format("$%.2f", transaction.amount),
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
private fun DateGroupHeader(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HorizontalDivider(modifier = Modifier.weight(1f))

        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 14.dp),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )

        HorizontalDivider(modifier = Modifier.weight(1f))
    }
}


private fun dateHeaderLabel(dateStr: String): String {
    val txDate = parseIsoDateOrNull(dateStr) ?: return dateStr
    val today = LocalDate.now()

    return when (txDate) {
        today -> "Today"
        today.minusDays(1) -> "Yesterday"
        else -> txDate.format(DateTimeFormatter.ofPattern("MMM d, yyyy"))
    }
}

private fun parseIsoDateOrNull(dateStr: String): LocalDate? {
    return try {
        LocalDate.parse(dateStr) // expects YYYY-MM-DD
    } catch (_: DateTimeParseException) {
        null
    }
}

private fun formatDisplayDate(dateStr: String): String {
    val date = parseIsoDateOrNull(dateStr) ?: return dateStr
    return date.format(DateTimeFormatter.ofPattern("MMM d, yyyy"))
}
