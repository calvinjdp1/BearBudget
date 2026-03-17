package com.example.bearbudget.ui.screens

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bearbudget.network.ApiClient
import com.example.bearbudget.network.Transaction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TransactionsViewModel : ViewModel() {

    private val api = ApiClient.apiService

    private val _cards = MutableStateFlow<List<String>>(emptyList())
    val cards: StateFlow<List<String>> = _cards

    private val _categories = MutableStateFlow<List<String>>(emptyList())
    val categories: StateFlow<List<String>> = _categories

    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions: StateFlow<List<Transaction>> = _transactions

    private val _isUploading = MutableStateFlow(false)
    val isUploading: StateFlow<Boolean> = _isUploading

    init {
        fetchCardsFromAccounts()
        fetchCategories()
        fetchTransactions()
    }

    fun fetchCardsFromAccounts() {
        viewModelScope.launch {
            try {
                val accountResponse = api.getAccounts()
                val banks = accountResponse["banks"] as List<Map<String, Any>>
                val debts = accountResponse["debts"] as List<Map<String, Any>>

                val accounts = mutableListOf<String>()
                banks.forEach { accounts.add(it["name"] as String) }
                debts.forEach { accounts.add(it["name"] as String) }

                _cards.value = accounts.distinct()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun fetchCategories() {
        viewModelScope.launch {
            try {
                _categories.value = api.getCategories()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun fetchTransactions() {
        viewModelScope.launch {
            try {
                _transactions.value = api.getTransactions()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private suspend fun uploadReceiptIfNeeded(
        context: Context,
        receiptImageUri: String?
    ): String? {
        if (receiptImageUri.isNullOrBlank()) return null

        val parsed = Uri.parse(receiptImageUri)
        val scheme = parsed.scheme?.lowercase()

        if (scheme == "http" || scheme == "https") {
            return receiptImageUri
        }

        if (scheme == "content" || scheme == "file") {
            val part = ReceiptUploadUtils.buildMultipartPart(context, parsed)
            val response = api.uploadReceipt(part)
            return response.receiptImageUri
        }

        return receiptImageUri
    }

    fun deleteTransaction(id: Int, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                _isUploading.value = true
                api.deleteTransaction(id)
                fetchTransactions()
                onSuccess()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isUploading.value = false
            }
        }
    }

    fun updateTransaction(
        context: Context,
        id: Int,
        transaction: Transaction,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                _isUploading.value = true
                val uploadedUrl = uploadReceiptIfNeeded(context, transaction.receiptImageUri)
                val updated = transaction.copy(receiptImageUri = uploadedUrl)

                api.updateTransaction(id, updated)
                fetchTransactions()
                onSuccess()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isUploading.value = false
            }
        }
    }

    fun addTransaction(
        context: Context,
        transaction: Transaction,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                _isUploading.value = true
                val uploadedUrl = uploadReceiptIfNeeded(context, transaction.receiptImageUri)
                val finalTransaction = transaction.copy(receiptImageUri = uploadedUrl)

                api.addTransaction(finalTransaction)
                fetchCardsFromAccounts()
                fetchTransactions()
                onSuccess()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isUploading.value = false
            }
        }
    }

    fun deleteCard(name: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                api.deleteCard(name)
                fetchCardsFromAccounts()
                onSuccess()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}