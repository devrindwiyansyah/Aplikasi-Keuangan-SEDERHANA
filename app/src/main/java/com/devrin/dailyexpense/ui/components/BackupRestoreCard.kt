package com.devrin.dailyexpense.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devrin.dailyexpense.data.local.TransactionEntity
import com.devrin.dailyexpense.ui.theme.BluePrimary
import com.devrin.dailyexpense.ui.theme.DarkText
import com.devrin.dailyexpense.ui.theme.DividerColor
import com.devrin.dailyexpense.ui.theme.GrayText
import com.devrin.dailyexpense.ui.theme.LightBlueBackground
import com.devrin.dailyexpense.ui.theme.SurfaceWhite
import com.devrin.dailyexpense.util.CsvExporter
import com.devrin.dailyexpense.util.CsvImporter
import com.devrin.dailyexpense.util.ImportResult
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun BackupRestoreCard(
    transactions: List<TransactionEntity>,
    onImportTransactions: (List<TransactionEntity>, onFinished: (Int) -> Unit) -> Unit,
    onShowMessage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // SAF Launcher for exporting CSV file
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri: Uri? ->
        if (uri != null) {
            val result = CsvExporter.exportToUri(context, uri, transactions)
            result.onSuccess { count ->
                onShowMessage("Berhasil mengekspor $count transaksi ke file CSV.")
            }.onFailure { error ->
                onShowMessage("Gagal mengekspor data: ${error.localizedMessage ?: "Terjadi kesalahan"}")
            }
        }
    }

    // SAF Launcher for importing CSV file
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            when (val importResult = CsvImporter.importFromUri(context, uri)) {
                is ImportResult.Success -> {
                    onImportTransactions(importResult.transactions) { count ->
                        onShowMessage("Berhasil mengimpor $count transaksi ke database!")
                    }
                }
                is ImportResult.Error -> {
                    onShowMessage(importResult.message)
                }
            }
        }
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(LightBlueBackground),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Backup,
                        contentDescription = "Backup & Restore",
                        tint = BluePrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Cadangkan & Pulihkan",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = DarkText
                    )
                    Text(
                        text = "Ekspor / impor data ke Excel atau CSV",
                        style = MaterialTheme.typography.bodySmall,
                        color = GrayText
                    )
                }

                // Share Button
                IconButton(
                    onClick = {
                        if (transactions.isEmpty()) {
                            onShowMessage("Belum ada data transaksi untuk dibagikan.")
                        } else {
                            CsvExporter.shareCsv(context, transactions).onFailure { error ->
                                onShowMessage("Gagal membagikan file: ${error.localizedMessage ?: "Terjadi kesalahan"}")
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Bagikan File CSV",
                        tint = BluePrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = DividerColor, thickness = 1.dp)
            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons (Export & Import)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Export Button
                Button(
                    onClick = {
                        if (transactions.isEmpty()) {
                            onShowMessage("Belum ada data transaksi untuk diekspor.")
                        } else {
                            val timeStamp = SimpleDateFormat("yyyyMMdd", Locale.US).format(Date())
                            val defaultName = "transaksi_keuangan_$timeStamp.csv"
                            exportLauncher.launch(defaultName)
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BluePrimary,
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.FileDownload,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Ekspor CSV",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                }

                // Import Button
                OutlinedButton(
                    onClick = {
                        // Open file picker allowing CSV and plain text MIME types
                        importLauncher.launch(
                            arrayOf(
                                "text/csv",
                                "text/comma-separated-values",
                                "text/plain",
                                "*/*"
                            )
                        )
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.5.dp, BluePrimary),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = BluePrimary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.FileUpload,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Impor CSV",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Info footnote
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = null,
                    tint = GrayText.copy(alpha = 0.7f),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Format standar kompatibel dengan Excel dan Google Sheets",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 11.sp,
                    color = GrayText.copy(alpha = 0.7f)
                )
            }
        }
    }
}
