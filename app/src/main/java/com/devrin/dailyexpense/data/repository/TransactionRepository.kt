package com.devrin.dailyexpense.data.repository

import com.devrin.dailyexpense.data.local.TransactionDao
import com.devrin.dailyexpense.data.local.TransactionEntity
import kotlinx.coroutines.flow.Flow

class TransactionRepository(private val transactionDao: TransactionDao) {

    val allTransactions: Flow<List<TransactionEntity>> =
        transactionDao.getAllTransactions()

    val totalIncome: Flow<Double> =
        transactionDao.getTotalIncome()

    val totalExpense: Flow<Double> =
        transactionDao.getTotalExpense()

    suspend fun insertTransaction(transaction: TransactionEntity) {
        transactionDao.insertTransaction(transaction)
    }

    suspend fun insertTransactions(transactions: List<TransactionEntity>) {
        transactionDao.insertTransactions(transactions)
    }

    suspend fun deleteTransaction(transaction: TransactionEntity) {
        transactionDao.deleteTransaction(transaction)
    }
}
