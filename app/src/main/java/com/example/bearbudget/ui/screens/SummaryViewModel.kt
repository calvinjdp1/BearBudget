package com.example.bearbudget.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bearbudget.network.ApiClient
import com.example.bearbudget.network.SummaryItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SummaryViewModel : ViewModel() {
    private val api = ApiClient.apiService

    private val _summary = MutableStateFlow<List<SummaryItem>>(emptyList())
    val summary: StateFlow<List<SummaryItem>> = _summary

    init {
        fetchSummary()
    }

    fun fetchSummary() {
        viewModelScope.launch {
            try {
                _summary.value = api.getSummary()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun totalBudgeted(): Double = summary.value.sumOf { it.budget }
    fun totalRemaining(): Double = summary.value.sumOf { it.remaining }
}
