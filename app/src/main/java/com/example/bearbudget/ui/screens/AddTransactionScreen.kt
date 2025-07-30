package com.example.bearbudget.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bearbudget.network.Transaction
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    viewModel: TransactionsViewModel = viewModel(),
    onTransactionAdded: () -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("") }
    var selectedCard by remember { mutableStateOf("") }
    var expandedCategory by remember { mutableStateOf(false) }
    var expandedCard by remember { mutableStateOf(false) }
    var showConfirmation by remember { mutableStateOf(false) }

    val categories by viewModel.categories.collectAsState()
    val cards by viewModel.cards.collectAsState()

    if (showConfirmation) {
        Text(
            text = "Transaction Added Successfully!",
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        )
        LaunchedEffect(Unit) {
            kotlinx.coroutines.delay(3000)
            showConfirmation = false
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Add Transaction", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = amount,
            onValueChange = { input -> if (input.all { it.isDigit() || it == '.' }) amount = input },
            label = { Text("Amount") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Category dropdown
        ExposedDropdownMenuBox(expanded = expandedCategory, onExpandedChange = { expandedCategory = !expandedCategory }) {
            OutlinedTextField(
                value = selectedCategory,
                onValueChange = {},
                readOnly = true,
                label = { Text("Category") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategory) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            DropdownMenu(expanded = expandedCategory, onDismissRequest = { expandedCategory = false }) {
                categories.forEach { category ->
                    DropdownMenuItem(
                        text = { Text(category) },
                        onClick = {
                            selectedCategory = category
                            expandedCategory = false
                        }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        // Card dropdown (only accounts now, no Add Card option)
        ExposedDropdownMenuBox(expanded = expandedCard, onExpandedChange = { expandedCard = !expandedCard }) {
            OutlinedTextField(
                value = selectedCard,
                onValueChange = {},
                readOnly = true,
                label = { Text("Account") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCard) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            DropdownMenu(expanded = expandedCard, onDismissRequest = { expandedCard = false }) {
                cards.forEach { card ->
                    DropdownMenuItem(
                        text = { Text(card) },
                        onClick = {
                            selectedCard = card
                            expandedCard = false
                        }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val transaction = Transaction(
                    id = null,
                    date = LocalDate.now().format(DateTimeFormatter.ISO_DATE),
                    amount = amount.toDoubleOrNull() ?: 0.0,
                    description = description,
                    card = selectedCard,
                    category = selectedCategory,
                    notes = "",
                    transaction_type = "expense",
                    related_account = null
                )
                viewModel.addTransaction(transaction) {
                    amount = ""
                    description = ""
                    selectedCategory = ""
                    selectedCard = ""
                    showConfirmation = true
                    onTransactionAdded() // popBackStack() from navigation
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add Transaction")
        }
    }
}
