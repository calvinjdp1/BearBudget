package com.example.bearbudget.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bearbudget.network.ApiClient
import com.example.bearbudget.network.AccountItem
import com.example.bearbudget.network.BankBody
import com.example.bearbudget.network.DebtBody
import com.example.bearbudget.network.Transaction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.example.bearbudget.network.AdjustmentRequest


// --- New request model for adjustment ---
data class AdjustmentRequest(
    val action: String,
    val amount: Double,
    val description: String? = null
)

class AccountsViewModel : ViewModel() {
    private val api = ApiClient.apiService

    private val _accounts = MutableStateFlow<List<AccountItem>>(emptyList())
    val accounts: StateFlow<List<AccountItem>> = _accounts

    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions: StateFlow<List<Transaction>> = _transactions

    fun fetchAccounts() {
        viewModelScope.launch {
            try {
                val response = api.getAccounts()
                val banks = response["banks"] as List<Map<String, Any>>
                val debts = response["debts"] as List<Map<String, Any>>
                val all = mutableListOf<AccountItem>()

                banks.forEach {
                    all.add(AccountItem(it["name"] as String, (it["balance"] as Number).toDouble(), "Bank"))
                }
                debts.forEach {
                    all.add(AccountItem(it["name"] as String, (it["balance"] as Number).toDouble(), "Debt"))
                }
                _accounts.value = all
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun addAccount(name: String, type: String, balance: Double) {
        viewModelScope.launch {
            try {
                if (type == "Credit Card" || type == "Loan") {
                    api.addDebt(DebtBody(name, -kotlin.math.abs(balance)))
                } else {
                    api.addBank(BankBody(name, balance))
                }
                fetchAccounts()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun fetchTransactions(month: String, accountName: String) {
        viewModelScope.launch {
            try {
                val data = api.getTransactions()
                _transactions.value = data.filter {
                    it.date.startsWith(month) && it.card == accountName
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // --- New function to adjust account funds ---
    fun adjustAccountFunds(accountName: String, action: String, amount: Double) {
        viewModelScope.launch {
            try {
                api.adjustAccountFunds(accountName, AdjustmentRequest(action, amount))
                // Refresh accounts and transactions after adjustment
                fetchAccounts()
                fetchTransactions(getCurrentMonth(), accountName)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

// Helper to get current month (for refresh after adjustment)

