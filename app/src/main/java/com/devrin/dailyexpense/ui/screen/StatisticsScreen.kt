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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.devrin.dailyexpense.ui.components.DonutChart
import com.devrin.dailyexpense.ui.components.PieChartSlice
import com.devrin.dailyexpense.ui.components.getCategoryIcon
import com.devrin.dailyexpense.ui.theme.DarkText
import com.devrin.dailyexpense.ui.theme.ExpenseRed
import com.devrin.dailyexpense.ui.theme.GrayText
import com.devrin.dailyexpense.ui.theme.IncomeGreen
import com.devrin.dailyexpense.ui.theme.LightBlueBackground
import com.devrin.dailyexpense.ui.theme.getCategoryColor
import com.devrin.dailyexpense.util.formatPercent
import com.devrin.dailyexpense.util.formatRupiah
import com.devrin.dailyexpense.viewmodel.TransactionViewModel

@Composable
fun StatisticsScreen(
    viewModel: TransactionViewModel,
    modifier: Modifier = Modifier
) {
    val transactions by viewModel.allTransactions.collectAsStateWithLifecycle()
    var selectedType by remember { mutableStateOf("EXPENSE") }

    // Filter by type: EXPENSE or INCOME
    val filteredTransactions = remember(transactions, selectedType) {
        transactions.filter { it.type == selectedType }
    }

    // Group and aggregate by category, sorted by total amount descending
    val categoryTotals = remember(filteredTransactions) {
        filteredTransactions.groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount } }
            .toList()
            .sortedByDescending { it.second }
    }

    val totalAmount = remember(categoryTotals) {
        categoryTotals.sumOf { it.second }
    }

    val chartSlices = remember(categoryTotals, totalAmount) {
        categoryTotals.map { (category, amount) ->
            PieChartSlice(
                label = category,
                value = amount,
                color = getCategoryColor(category)
            )
        }
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
                    text = "Statistik \uD83D\uDCCA",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = DarkText
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Distribusi pengeluaran dan pemasukan",
                    style = MaterialTheme.typography.bodyLarge,
                    color = GrayText
                )
                Spacer(modifier = Modifier.height(20.dp))
            }

            // ── Toggle Switch (Pengeluaran / Pemasukan) ──
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp)
                    ) {
                        // Pengeluaran Tab
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (selectedType == "EXPENSE") ExpenseRed
                                    else Color.Transparent
                                )
                                .clickable { selectedType = "EXPENSE" }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Pengeluaran",
                                fontWeight = FontWeight.SemiBold,
                                color = if (selectedType == "EXPENSE") Color.White else GrayText
                            )
                        }

                        // Pemasukan Tab
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (selectedType == "INCOME") IncomeGreen
                                    else Color.Transparent
                                )
                                .clickable { selectedType = "INCOME" }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Pemasukan",
                                fontWeight = FontWeight.SemiBold,
                                color = if (selectedType == "INCOME") Color.White else GrayText
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // ── Donut Chart Card ──
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        DonutChart(
                            slices = chartSlices,
                            centerTitle = if (selectedType == "EXPENSE") "Total Keluar" else "Total Masuk",
                            centerValue = formatRupiah(totalAmount)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // ── Breakdown Header ──
            item {
                Text(
                    text = "Rincian Kategori",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = DarkText
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // ── Empty State or Category Breakdown ──
            if (categoryTotals.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.PieChart,
                                contentDescription = null,
                                modifier = Modifier.size(56.dp),
                                tint = GrayText.copy(alpha = 0.35f)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Belum ada transaksi ${if (selectedType == "EXPENSE") "pengeluaran" else "pemasukan"}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = GrayText
                            )
                        }
                    }
                }
            } else {
                items(
                    items = categoryTotals,
                    key = { it.first }
                ) { (category, amount) ->
                    val color = getCategoryColor(category)
                    val icon = getCategoryIcon(category)
                    val percentage = if (totalAmount > 0.0) (amount / totalAmount) * 100.0 else 0.0

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Category color dot
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .background(color, CircleShape)
                                )

                                Spacer(modifier = Modifier.width(10.dp))

                                // Category icon in soft colored container
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(color.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = category,
                                        tint = color,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                // Category Name & Percentage
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = category,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium,
                                        color = DarkText
                                    )
                                    Text(
                                        text = formatPercent(percentage),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = GrayText
                                    )
                                }

                                // Amount
                                Text(
                                    text = formatRupiah(amount),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = DarkText
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Linear progress indicator for proportion
                            LinearProgressIndicator(
                                progress = { (percentage / 100.0).toFloat() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = color,
                                trackColor = color.copy(alpha = 0.12f)
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}
