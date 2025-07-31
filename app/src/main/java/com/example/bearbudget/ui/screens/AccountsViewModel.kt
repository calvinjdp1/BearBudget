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
import com.example.bearbudget.network.TransferRequest
import java.time.LocalDate

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

                // Banks always positive
                banks.forEach {
                    val balance = (it["balance"] as Number).toDouble()
                    all.add(AccountItem(it["name"] as String, balance, "Bank"))
                }

                // Debts always negative (normalized)
                debts.forEach {
                    val balance = (it["balance"] as Number).toDouble()
                    all.add(AccountItem(it["name"] as String, -kotlin.math.abs(balance), "Debt"))
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
                val filtered = data.filter { it.date.startsWith(month) && it.card == accountName }
                    .map { tx ->
                        val isTransfer = tx.description.isNullOrBlank() || tx.description.equals("no description", true)
                        val desc = if (isTransfer) "Transfer" else tx.description
                        val category = if (isTransfer) {
                            if (tx.card == accountName) "Withdrawal" else "Deposit"
                        } else tx.category ?: if (tx.amount >= 0) "Deposit" else "Withdrawal"
                        tx.copy(description = desc, category = category)
                    }.toMutableList()

                // --- Mirror transfers for destination accounts (if viewing accountB) ---
                if (filtered.isEmpty() && accountName != "") {
                    val incomingTransfers = data.filter {
                        it.date.startsWith(month) &&
                                it.description.equals("no description", true) &&
                                it.card != accountName
                    }
                    incomingTransfers.forEach { transfer ->
                        filtered.add(
                            transfer.copy(
                                card = accountName,
                                description = "Transfer",
                                category = "Deposit"
                            )
                        )
                    }
                }
                _transactions.value = filtered
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    // --- Adjust account funds ---
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

    fun deleteBank(name: String) {
        viewModelScope.launch {
            try {
                api.deleteBank(name)
                fetchAccounts()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deleteDebt(name: String) {
        viewModelScope.launch {
            try {
                api.deleteDebt(name)
                fetchAccounts()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun transferFunds(from: String, to: String, amount: Double) {
        viewModelScope.launch {
            try {
                val transfer = TransferRequest(
                    date = LocalDate.now().toString(),
                    from_account = from,
                    to_account = to,
                    amount = amount
                )
                api.makeTransfer(transfer)
                fetchAccounts()
                fetchTransactions(getCurrentMonth(), to)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

