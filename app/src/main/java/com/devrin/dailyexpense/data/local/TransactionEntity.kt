package com.devrin.dailyexpense.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: Double,
    val type: String,       // "INCOME" or "EXPENSE"
    val category: String,
    val note: String = "",
    val date: Long           // timestamp in milliseconds
)
