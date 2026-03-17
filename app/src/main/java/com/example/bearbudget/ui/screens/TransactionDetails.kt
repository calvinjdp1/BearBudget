package com.example.bearbudget.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
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
    var isEditing by remember(transaction.id) { mutableStateOf(false) }

    if (isEditing) {
        EditTransactionForm(
            transaction = transaction,
            categories = categories,
            cards = cards,
            onUpdate = onUpdate,
            onDelete = onDelete,
            onCancelEdit = { isEditing = false }
        )
    } else {
        TransactionDetailsView(
            transaction = transaction,
            onEditClick = { isEditing = true },
            onDelete = onDelete
        )
    }
}

@Composable
private fun TransactionDetailsView(
    transaction: Transaction,
    onEditClick: () -> Unit,
    onDelete: () -> Unit
) {
    val descriptionLines = remember(transaction.description) {
        transaction.description
            ?.lines()
            ?.map { it.trim() }
            ?.filter { it.isNotEmpty() }
            ?: emptyList()
    }

    val displayDescription = when {
        descriptionLines.isNotEmpty() -> descriptionLines
        transaction.description.isNullOrBlank() ||
                transaction.description.equals("no description", ignoreCase = true) -> listOf("Transfer")
        else -> listOf(transaction.description!!)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .padding(bottom = 24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Transaction Details",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )

            IconButton(onClick = onEditClick) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit transaction"
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        DetailRow(label = "Date", value = transaction.date)
        DetailRow(label = "Category", value = transaction.category ?: "No Category")
        DetailRow(label = "Card", value = transaction.card ?: "N/A")
        DetailRow(label = "Amount", value = String.format("$%.2f", transaction.amount))
        DetailRow(label = "Type", value = transaction.transaction_type ?: "expense")

        Spacer(modifier = Modifier.height(20.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Items",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(8.dp))

        displayDescription.forEach { line ->
            Text(
                text = "• $line",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(vertical = 2.dp)
            )
        }

        if (!transaction.receiptImageUri.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Receipt",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            AsyncImage(
                model = transaction.receiptImageUri,
                contentDescription = "Receipt image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
        }

        if (!transaction.notes.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Notes",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = transaction.notes.orEmpty(),
                style = MaterialTheme.typography.bodyLarge
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onDelete,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error,
                contentColor = MaterialTheme.colorScheme.onError
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Delete")
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.height(12.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditTransactionForm(
    transaction: Transaction,
    categories: List<String>,
    cards: List<String>,
    onUpdate: (Transaction) -> Unit,
    onDelete: () -> Unit,
    onCancelEdit: () -> Unit
) {
    val context = LocalContext.current

    var date by remember(transaction.id) { mutableStateOf(transaction.date) }
    var description by remember(transaction.id) { mutableStateOf(transaction.description ?: "") }
    var amount by remember(transaction.id) { mutableStateOf(transaction.amount.toString()) }
    var notes by remember(transaction.id) { mutableStateOf(transaction.notes ?: "") }
    var transactionType by remember(transaction.id) {
        mutableStateOf(transaction.transaction_type ?: "expense")
    }

    var expandedCategory by remember { mutableStateOf(false) }
    var selectedCategory by remember(transaction.id) {
        mutableStateOf(transaction.category ?: "")
    }

    var expandedCard by remember { mutableStateOf(false) }
    var selectedCard by remember(transaction.id) {
        mutableStateOf(transaction.card ?: "")
    }

    var receiptImageUri by remember(transaction.id) {
        mutableStateOf(transaction.receiptImageUri)
    }

    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            receiptImageUri = pendingCameraUri?.toString()
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            receiptImageUri = uri.toString()
        }
    }

    LaunchedEffect(transaction.id) {
        date = transaction.date
        description = transaction.description ?: ""
        amount = transaction.amount.toString()
        notes = transaction.notes ?: ""
        transactionType = transaction.transaction_type ?: "expense"
        selectedCategory = transaction.category ?: ""
        selectedCard = transaction.card ?: ""
        receiptImageUri = transaction.receiptImageUri
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .padding(bottom = 24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Edit Transaction",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )

            IconButton(onClick = onCancelEdit) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back to details"
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = date,
            onValueChange = { date = it },
            label = { Text("Date (YYYY-MM-DD)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategory)
                },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )

            DropdownMenu(
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
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCard)
                },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )

            DropdownMenu(
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
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description / Items") },
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

        Text(
            text = "Receipt",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedButton(
                onClick = {
                    val uri = ReceiptCameraUtils.createImageUri(context)
                    pendingCameraUri = uri
                    cameraLauncher.launch(uri)
                },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Camera")
            }

            OutlinedButton(
                onClick = { galleryLauncher.launch("image/*") },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Image, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Gallery")
            }
        }

        if (!receiptImageUri.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(12.dp))

            AsyncImage(
                model = receiptImageUri,
                contentDescription = "Receipt preview",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = { receiptImageUri = null },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Delete, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Remove Receipt")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = {
                    val updated = transaction.copy(
                        date = date,
                        category = selectedCategory,
                        card = selectedCard,
                        description = description,
                        amount = amount.toDoubleOrNull() ?: transaction.amount,
                        notes = notes,
                        transaction_type = transactionType,
                        receiptImageUri = receiptImageUri
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