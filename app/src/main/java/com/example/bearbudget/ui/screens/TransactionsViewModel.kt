package com.example.bearbudget.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bearbudget.network.ApiClient
import com.example.bearbudget.network.Transaction
import com.example.bearbudget.network.Card
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

    init {
        fetchCards()
        fetchCategories()
        fetchTransactions()
    }

    fun fetchCards() {
        viewModelScope.launch {
            try {
                val response: List<Card> = api.getCards()
                _cards.value = response.map { card -> card.name }
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
                fetchCards()
                fetchTransactions()
                onSuccess()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
