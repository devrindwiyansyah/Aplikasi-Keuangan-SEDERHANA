package com.devrin.dailyexpense

import android.app.Application
import com.devrin.dailyexpense.data.local.AppDatabase
import com.devrin.dailyexpense.data.repository.TransactionRepository

class DailyExpenseApplication : Application() {

    val database: AppDatabase by lazy {
        AppDatabase.getDatabase(this)
    }

    val repository: TransactionRepository by lazy {
        TransactionRepository(database.transactionDao())
    }
}
