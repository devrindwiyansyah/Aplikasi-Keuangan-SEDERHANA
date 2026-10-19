package com.devrin.dailyexpense.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.devrin.dailyexpense.ui.components.BalanceCard
import com.devrin.dailyexpense.ui.components.TransactionItem
import com.devrin.dailyexpense.ui.theme.BluePrimary
import com.devrin.dailyexpense.ui.theme.DarkText
import com.devrin.dailyexpense.ui.theme.GrayText
import com.devrin.dailyexpense.ui.theme.LightBlueBackground
import com.devrin.dailyexpense.util.formatDate
import com.devrin.dailyexpense.util.formatMonthYear
import com.devrin.dailyexpense.viewmodel.TransactionViewModel

@Composable
fun MonthlyHistoryScreen(
    viewModel: TransactionViewModel,
    modifier: Modifier = Modifier
) {
    val transactions by viewModel.allTransactions.collectAsStateWithLifecycle()

    // Determine available months from transactions + current month
    val currentMonthFormatted = remember { formatMonthYear(System.currentTimeMillis()) }
    val availableMonths = remember(transactions) {
        val monthsFromData = transactions.map { formatMonthYear(it.date) }.distinct()
        if (monthsFromData.contains(currentMonthFormatted)) {
            monthsFromData
        } else {
            listOf(currentMonthFormatted) + monthsFromData
        }
    }

    var selectedMonthIndex by remember(availableMonths) { mutableIntStateOf(0) }
    var dropdownExpanded by remember { mutableStateOf(false) }

    val safeIndex = selectedMonthIndex.coerceIn(0, availableMonths.size - 1)
    val selectedMonth = availableMonths[safeIndex]

    // Filter transactions for selected month
    val monthlyTransactions = remember(transactions, selectedMonth) {
        transactions.filter { formatMonthYear(it.date) == selectedMonth }
    }

    val monthlyIncome = remember(monthlyTransactions) {
        monthlyTransactions.filter { it.type == "INCOME" }.sumOf { it.amount }
    }
    val monthlyExpense = remember(monthlyTransactions) {
        monthlyTransactions.filter { it.type == "EXPENSE" }.sumOf { it.amount }
    }
    val monthlyBalance = monthlyIncome - monthlyExpense

    val groupedTransactions = remember(monthlyTransactions) {
        monthlyTransactions.groupBy { formatDate(it.date) }
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
            // ── Title Section ──
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Riwayat Bulanan",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = DarkText
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Laporan transaksi per periode bulan",
                    style = MaterialTheme.typography.bodyLarge,
                    color = GrayText
                )
                Spacer(modifier = Modifier.height(20.dp))
            }

            // ── Month Selector Card ──
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Previous Month Button (index + 1 in descending order)
                        IconButton(
                            onClick = {
                                if (safeIndex < availableMonths.size - 1) {
                                    selectedMonthIndex = safeIndex + 1
                                }
                            },
                            enabled = safeIndex < availableMonths.size - 1
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChevronLeft,
                                contentDescription = "Bulan Sebelumnya",
                                tint = if (safeIndex < availableMonths.size - 1) BluePrimary else GrayText.copy(alpha = 0.3f)
                            )
                        }

                        // Center Month Dropdown Button
                        Box {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { dropdownExpanded = true }
                                    .padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = null,
                                    tint = BluePrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = selectedMonth,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = DarkText
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Pilih Bulan",
                                    tint = DarkText
                                )
                            }

                            DropdownMenu(
                                expanded = dropdownExpanded,
                                onDismissRequest = { dropdownExpanded = false }
                            ) {
                                availableMonths.forEachIndexed { index, monthName ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = monthName,
                                                fontWeight = if (index == safeIndex) FontWeight.Bold else FontWeight.Normal,
                                                color = if (index == safeIndex) BluePrimary else DarkText
                                            )
                                        },
                                        onClick = {
                                            selectedMonthIndex = index
                                            dropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // Next Month Button (index - 1 in descending order)
                        IconButton(
                            onClick = {
                                if (safeIndex > 0) {
                                    selectedMonthIndex = safeIndex - 1
                                }
                            },
                            enabled = safeIndex > 0
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = "Bulan Berikutnya",
                                tint = if (safeIndex > 0) BluePrimary else GrayText.copy(alpha = 0.3f)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // ── Monthly Summary Card ──
            item {
                BalanceCard(
                    totalBalance = monthlyBalance,
                    totalIncome = monthlyIncome,
                    totalExpense = monthlyExpense
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            // ── Section Header ──
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Daftar Transaksi ($selectedMonth)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = DarkText
                    )
                    Text(
                        text = "${monthlyTransactions.size} transaksi",
                        style = MaterialTheme.typography.bodySmall,
                        color = GrayText
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // ── Empty State or Grouped Transactions ──
            if (monthlyTransactions.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.ReceiptLong,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = GrayText.copy(alpha = 0.35f)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Tidak ada transaksi di bulan $selectedMonth",
                                style = MaterialTheme.typography.bodyMedium,
                                color = GrayText
                            )
                        }
                    }
                }
            } else {
                groupedTransactions.forEach { (dateString, transactionsForDate) ->
                    item(key = "monthly_header_$dateString") {
                        Text(
                            text = dateString,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = GrayText,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

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

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}
