package com.example.bearbudget.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bearbudget.network.CategoryConfig
import com.example.bearbudget.network.SummaryItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SummaryScreen(viewModel: SummaryViewModel = viewModel()) {
    val summary by viewModel.summary.collectAsState()
    val categoryDetails by viewModel.categoryDetails.collectAsState()

    var expandedCategory by remember { mutableStateOf<String?>(null) }

    var menuExpanded by remember { mutableStateOf(false) }
    var showEditCategories by remember { mutableStateOf(false) }

    val totalBudgeted = summary.sumOf { it.budget }
    val totalRemaining = summary.sumOf { it.remaining }
    val totalRemainingColor = if (totalRemaining < 0) MaterialTheme.colorScheme.error
    else MaterialTheme.colorScheme.onSurface

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Summary") },
                actions = {
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Edit categories") },
                                onClick = {
                                    menuExpanded = false
                                    showEditCategories = true
                                    viewModel.fetchCategoryDetails()
                                }
                            )
                        }
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
                text = "Total Budgeted: $${String.format("%.2f", totalBudgeted)}",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Total Remaining: $${String.format("%.2f", totalRemaining)}",
                style = MaterialTheme.typography.titleMedium,
                color = totalRemainingColor
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Categories",
                    fontSize = 22.sp,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Remaining Balance",
                    fontSize = 22.sp,
                    style = MaterialTheme.typography.titleMedium
                )
            }

            LazyColumn {
                items(summary) { item ->
                    CategorySummaryRow(
                        item = item,
                        isExpanded = expandedCategory == item.category,
                        onClick = {
                            expandedCategory =
                                if (expandedCategory == item.category) null else item.category
                        }
                    )
                }
            }
        }
    }

    if (showEditCategories) {
        EditCategoriesSheet(
            categories = categoryDetails,
            onDismiss = { showEditCategories = false },
            onAdd = { name, budget, rollover ->
                viewModel.addCategory(name, budget, rollover) { }
            },
            onEdit = { oldName, newName, budget, rollover ->
                viewModel.updateCategory(oldName, newName, budget, rollover) { }
            },
            onDelete = { name ->
                viewModel.deleteCategory(name) { }
            }
        )
    }
}

@Composable
fun CategorySummaryRow(item: SummaryItem, isExpanded: Boolean, onClick: () -> Unit) {
    val remainingColor = if (item.remaining < 0) MaterialTheme.colorScheme.error
    else MaterialTheme.colorScheme.onSurface

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(item.category, fontSize = 20.sp, style = MaterialTheme.typography.bodyLarge)
            Text(
                "$${String.format("%.2f", item.remaining)}",
                fontSize = 20.sp,
                style = MaterialTheme.typography.bodyLarge,
                color = remainingColor
            )
        }

        if (isExpanded) {
            Spacer(modifier = Modifier.height(12.dp))
            Column(modifier = Modifier.padding(start = 12.dp, end = 8.dp)) {
                Text("Used: $${String.format("%.2f", item.amount_used)}", fontSize = 18.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Rollover: $${String.format("%.2f", item.rollover)}", fontSize = 18.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Budget: $${String.format("%.2f", item.budget)}", fontSize = 18.sp)
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        HorizontalDivider(modifier = Modifier.padding(top = 12.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditCategoriesSheet(
    categories: List<CategoryConfig>,
    onDismiss: () -> Unit,
    onAdd: (String, Double, Boolean) -> Unit,
    onEdit: (String, String, Double, Boolean) -> Unit,
    onDelete: (String) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var editTarget by remember { mutableStateOf<CategoryConfig?>(null) }
    var deleteTarget by remember { mutableStateOf<CategoryConfig?>(null) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Edit Categories",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { showAddDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Category")
                }
            }

            Spacer(Modifier.height(8.dp))

            if (categories.isEmpty()) {
                Text("No categories found.")
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 420.dp)
                ) {
                    items(categories) { cat ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(cat.name, style = MaterialTheme.typography.bodyLarge)
                                Text(
                                    "Budget: $${String.format("%.2f", cat.monthly_budget)} • Rollover: ${if (cat.rollover) "On" else "Off"}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }

                            IconButton(onClick = { editTarget = cat }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit")
                            }
                            IconButton(onClick = { deleteTarget = cat }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete")
                            }
                        }
                        HorizontalDivider()
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Text("Close")
            }
        }
    }

    if (showAddDialog) {
        CategoryEditDialog(
            title = "Add Category",
            initialName = "",
            initialBudget = 0.0,
            initialRollover = false,
            confirmText = "Add",
            onDismiss = { showAddDialog = false },
            onConfirm = { name, budget, rollover ->
                onAdd(name, budget, rollover)
                showAddDialog = false
            }
        )
    }

    if (editTarget != null) {
        val cat = editTarget!!
        CategoryEditDialog(
            title = "Edit Category",
            initialName = cat.name,
            initialBudget = cat.monthly_budget,
            initialRollover = cat.rollover,
            confirmText = "Save",
            onDismiss = { editTarget = null },
            onConfirm = { newName, budget, rollover ->
                onEdit(cat.name, newName, budget, rollover)
                editTarget = null
            }
        )
    }

    if (deleteTarget != null) {
        val cat = deleteTarget!!
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("Delete Category") },
            text = { Text("Delete \"${cat.name}\"? Existing transactions will keep the old category text.") },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete(cat.name)
                        deleteTarget = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete") }
            },
            dismissButton = {
                Button(onClick = { deleteTarget = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun CategoryEditDialog(
    title: String,
    initialName: String,
    initialBudget: Double,
    initialRollover: Boolean,
    confirmText: String,
    onDismiss: () -> Unit,
    onConfirm: (String, Double, Boolean) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var budgetText by remember { mutableStateOf(if (initialBudget == 0.0) "" else initialBudget.toString()) }
    var rollover by remember { mutableStateOf(initialRollover) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = budgetText,
                    onValueChange = { input -> if (input.all { it.isDigit() || it == '.' }) budgetText = input },
                    label = { Text("Monthly Budget") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Rollover", modifier = Modifier.weight(1f))
                    Switch(checked = rollover, onCheckedChange = { rollover = it })
                }
            }
        },
        confirmButton = {
            val cleanedName = name.trim()
            val budget = budgetText.toDoubleOrNull() ?: 0.0
            Button(
                onClick = { onConfirm(cleanedName, budget, rollover) },
                enabled = cleanedName.isNotEmpty()
            ) { Text(confirmText) }
        },
        dismissButton = { Button(onClick = onDismiss) { Text("Cancel") } }
    )
}
