package com.example.bearbudget.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.foundation.text.KeyboardOptions

import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.bearbudget.network.Transaction

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetails(
    transaction: Transaction,
    categories: List<String>,
    cards: List<String>,
    onUpdate: (Transaction) -> Unit,
    onDelete: () -> Unit
) {
    var date by remember { mutableStateOf(transaction.date) }
    var description by remember { mutableStateOf(transaction.description ?: "") }
    var amount by remember { mutableStateOf(transaction.amount.toString()) }
    var notes by remember { mutableStateOf(transaction.notes ?: "") }
    var transactionType by remember { mutableStateOf(transaction.transaction_type ?: "expense") }

    var expandedCategory by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf(transaction.category ?: "") }

    var expandedCard by remember { mutableStateOf(false) }
    var selectedCard by remember { mutableStateOf(transaction.card ?: "") }

    Column(modifier = Modifier.padding(16.dp)) {
        OutlinedTextField(
            value = date,
            onValueChange = { date = it },
            label = { Text("Date (YYYY-MM-DD)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        ExposedDropdownMenuBox(expanded = expandedCategory, onExpandedChange = { expandedCategory = !expandedCategory }) {
            OutlinedTextField(
                value = selectedCategory,
                onValueChange = {},
                readOnly = true,
                label = { Text("Category") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategory) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(expanded = expandedCategory, onDismissRequest = { expandedCategory = false }) {
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

        ExposedDropdownMenuBox(expanded = expandedCard, onExpandedChange = { expandedCard = !expandedCard }) {
            OutlinedTextField(
                value = selectedCard,
                onValueChange = {},
                readOnly = true,
                label = { Text("Card") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCard) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(expanded = expandedCard, onDismissRequest = { expandedCard = false }) {
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
        Spacer(modifier = Modifier.height(8.dp))

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

        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("Notes") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = transactionType,
            onValueChange = { transactionType = it },
            label = { Text("Transaction Type") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = {
                    val updated = transaction.copy(
                        date = date,
                        category = selectedCategory,
                        card = selectedCard,
                        description = description,
                        amount = amount.toDoubleOrNull() ?: transaction.amount,
                        notes = notes,
                        transaction_type = transactionType
                    )
                    onUpdate(updated)
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Update")
            }
            Button(
                onClick = onDelete,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                ),
                modifier = Modifier.weight(1f)
            ) {
                Text("Delete")
            }
        }
    }
}
