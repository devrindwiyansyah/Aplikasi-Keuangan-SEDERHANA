package com.devrin.dailyexpense.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devrin.dailyexpense.data.local.TransactionEntity
import com.devrin.dailyexpense.data.repository.TransactionRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TransactionViewModel(
    private val repository: TransactionRepository
) : ViewModel() {

    val allTransactions: StateFlow<List<TransactionEntity>> =
        repository.allTransactions
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    val totalIncome: StateFlow<Double> =
        repository.totalIncome
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = 0.0
            )

    val totalExpense: StateFlow<Double> =
        repository.totalExpense
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = 0.0
            )

    fun addTransaction(
        amount: Double,
        type: String,
        category: String,
        note: String,
        date: Long
    ) {
        viewModelScope.launch {
            repository.insertTransaction(
                TransactionEntity(
                    amount = amount,
                    type = type,
                    category = category,
                    note = note,
                    date = date
                )
            )
        }
    }

    fun insertTransactions(
        transactions: List<TransactionEntity>,
        onComplete: (Int) -> Unit = {}
    ) {
        viewModelScope.launch {
            repository.insertTransactions(transactions)
            onComplete(transactions.size)
        }
    }

    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }
}
