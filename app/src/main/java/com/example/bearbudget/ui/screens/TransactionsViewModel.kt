package com.example.bearbudget.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bearbudget.network.ApiClient
import com.example.bearbudget.network.Transaction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TransactionsViewModel : ViewModel() {

    private val api = ApiClient.apiService

    // Dropdown options (only accounts)
    private val _cards = MutableStateFlow<List<String>>(emptyList())
    val cards: StateFlow<List<String>> = _cards

    private val _categories = MutableStateFlow<List<String>>(emptyList())
    val categories: StateFlow<List<String>> = _categories

    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions: StateFlow<List<Transaction>> = _transactions

    init {
        fetchCardsFromAccounts()
        fetchCategories()
        fetchTransactions()
    }

    /**
     * Fetch accounts and use them as card options
     */
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

    fun deleteTransaction(id: Int, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                api.deleteTransaction(id)
                fetchTransactions()
                onSuccess()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun updateTransaction(id: Int, transaction: Transaction, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                api.updateTransaction(id, transaction)
                fetchTransactions()
                onSuccess()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun addTransaction(transaction: Transaction, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                api.addTransaction(transaction)
                fetchCardsFromAccounts()
                fetchTransactions()
                onSuccess()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Optional: Delete legacy cards if server supports it
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