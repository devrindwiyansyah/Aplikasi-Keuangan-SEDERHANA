package com.devrin.dailyexpense.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.devrin.dailyexpense.ui.components.BackupRestoreCard
import com.devrin.dailyexpense.ui.components.BalanceCard
import com.devrin.dailyexpense.ui.components.TransactionItem
import com.devrin.dailyexpense.ui.theme.DarkText
import com.devrin.dailyexpense.ui.theme.GrayText
import com.devrin.dailyexpense.ui.theme.LightBlueBackground
import com.devrin.dailyexpense.util.formatDate
import com.devrin.dailyexpense.viewmodel.TransactionViewModel

@Composable
fun HomeScreen(
    viewModel: TransactionViewModel,
    modifier: Modifier = Modifier,
    onShowSnackbar: (String) -> Unit = {}
) {
    val transactions by viewModel.allTransactions.collectAsStateWithLifecycle()
    val totalIncome by viewModel.totalIncome.collectAsStateWithLifecycle()
    val totalExpense by viewModel.totalExpense.collectAsStateWithLifecycle()
    val totalBalance = totalIncome - totalExpense

    // Group transactions by formatted date string
    val groupedTransactions = transactions.groupBy { transaction ->
        formatDate(transaction.date)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(LightBlueBackground, Color.White),
                    startY = 0f,
                    endY = 800f
                )
            )
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // ── Greeting Section ──
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Euy! \uD83D\uDC4B",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = DarkText
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Kelola Keuangan bikinan aa devrin kasep",
                    style = MaterialTheme.typography.bodyLarge,
                    color = GrayText
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            // ── Balance Card ──
            item {
                BalanceCard(
                    totalBalance = totalBalance,
                    totalIncome = totalIncome,
                    totalExpense = totalExpense
                )
                Spacer(modifier = Modifier.height(20.dp))
            }

            // ── Backup & Restore Card ──
            item {
                BackupRestoreCard(
                    transactions = transactions,
                    onImportTransactions = { importedTransactions, onFinished ->
                        viewModel.insertTransactions(importedTransactions, onFinished)
                    },
                    onShowMessage = onShowSnackbar
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            // ── Transaction History Header ──
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Riwayat Transaksi Terkini",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = DarkText
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // ── Empty State or Transaction List ──
            if (transactions.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                                contentDescription = null,
                                modifier = Modifier.size(72.dp),
                                tint = GrayText.copy(alpha = 0.4f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Belum ada transaksi",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium,
                                color = GrayText
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Tap + untuk menambahkan transaksi baru",
                                style = MaterialTheme.typography.bodyMedium,
                                color = GrayText.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            } else {
                groupedTransactions.forEach { (dateString, transactionsForDate) ->
                    // Date header
                    item(key = "header_$dateString") {
                        Text(
                            text = dateString,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = GrayText,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    // Transactions for that date
                    items(
                        items = transactionsForDate,
                        key = { it.id }
                    ) { transaction ->
                        TransactionItem(
                            transaction = transaction,
                            onDelete = { viewModel.deleteTransaction(transaction) }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            // Bottom spacer for FAB & NavigationBar clearance
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}
