package com.example.bearbudget.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bearbudget.network.ApiClient
import com.example.bearbudget.network.CategoryConfig
import com.example.bearbudget.network.CategoryUpsertRequest
import com.example.bearbudget.network.SummaryItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SummaryViewModel : ViewModel() {
    private val api = ApiClient.apiService

    private val _summary = MutableStateFlow<List<SummaryItem>>(emptyList())
    val summary: StateFlow<List<SummaryItem>> = _summary

    private val _categoryDetails = MutableStateFlow<List<CategoryConfig>>(emptyList())
    val categoryDetails: StateFlow<List<CategoryConfig>> = _categoryDetails

    init {
        refreshAll()
    }

    fun refreshAll() {
        fetchSummary()
        fetchCategoryDetails()
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

    fun fetchCategoryDetails() {
        viewModelScope.launch {
            try {
                _categoryDetails.value = api.getCategoryDetails()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun addCategory(name: String, monthlyBudget: Double, rollover: Boolean, onDone: () -> Unit) {
        viewModelScope.launch {
            try {
                api.addCategory(CategoryUpsertRequest(name, monthlyBudget, rollover))
                refreshAll()
                onDone()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun updateCategory(oldName: String, newName: String, monthlyBudget: Double, rollover: Boolean, onDone: () -> Unit) {
        viewModelScope.launch {
            try {
                api.updateCategory(oldName, CategoryUpsertRequest(newName, monthlyBudget, rollover))
                refreshAll()
                onDone()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deleteCategory(name: String, onDone: () -> Unit) {
        viewModelScope.launch {
            try {
                api.deleteCategory(name)
                refreshAll()
                onDone()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
