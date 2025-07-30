package com.example.bearbudget.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.graphics.Color
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

    val categories by viewModel.categories.collectAsState()
    var selectedCategory by remember { mutableStateOf("") }
    var expandedCategory by remember { mutableStateOf(false) }

    val cards by viewModel.cards.collectAsState()
    var selectedCard by remember { mutableStateOf("") }
    var expandedCard by remember { mutableStateOf(false) }
    var newCardName by remember { mutableStateOf("") }

    var showConfirmation by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(16.dp)) {

        if (showConfirmation) {
            Text(
                text = "Transaction Added Successfully!",
                color = Color(0xFF4CAF50),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )
        }

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
            onValueChange = { input ->
                if (input.all { it.isDigit() || it == '.' }) {
                    amount = input
                }
            },
            label = { Text("Amount") },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        ExposedDropdownMenuBox(
            expanded = expandedCategory,
            onExpandedChange = { expandedCategory = !expandedCategory }
        ) {
            OutlinedTextField(
                value = selectedCategory,
                onValueChange = {},
                readOnly = true,
                label = { Text("Category") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategory) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expandedCategory,
                onDismissRequest = { expandedCategory = false }
            ) {
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

        ExposedDropdownMenuBox(
            expanded = expandedCard,
            onExpandedChange = { expandedCard = !expandedCard }
        ) {
            OutlinedTextField(
                value = selectedCard,
                onValueChange = {},
                readOnly = true,
                label = { Text("Card") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCard) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expandedCard,
                onDismissRequest = { expandedCard = false }
            ) {
                cards.forEach { card ->
                    DropdownMenuItem(
                        text = { Text(card) },
                        onClick = {
                            selectedCard = card
                            expandedCard = false
                        }
                    )
                }
                DropdownMenuItem(
                    text = { Text("Add New Card") },
                    onClick = {
                        selectedCard = "Add New Card"
                        expandedCard = false
                    }
                )
            }
        }

        if (selectedCard == "Add New Card") {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = newCardName,
                onValueChange = { newCardName = it },
                label = { Text("New Card Name") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val finalCard = if (selectedCard == "Add New Card") newCardName else selectedCard
                val transaction = Transaction(
                    id = null,
                    date = LocalDate.now().format(DateTimeFormatter.ISO_DATE),
                    amount = amount.toDoubleOrNull() ?: 0.0,
                    description = description,
                    card = finalCard,
                    category = selectedCategory
                )

                viewModel.addTransaction(transaction) {
                    amount = ""
                    description = ""
                    selectedCategory = ""
                    newCardName = ""
                    selectedCard = finalCard
                    showConfirmation = true
                    onTransactionAdded()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add Transaction")
        }
    }
}
