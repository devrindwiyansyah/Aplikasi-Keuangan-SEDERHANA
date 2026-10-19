package com.devrin.dailyexpense.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.devrin.dailyexpense.data.local.TransactionEntity
import java.io.File
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Utility for exporting transactions to RFC 4180 compliant CSV format,
 * fully compatible with Microsoft Excel and Google Sheets.
 */
object CsvExporter {

    // Prepend UTF-8 BOM so Microsoft Excel automatically recognizes UTF-8 encoding
    private const val UTF8_BOM = "\uFEFF"
    private const val CSV_HEADER = "ID,Tanggal,Waktu,Tipe,Kategori,Nominal,Catatan"

    /**
     * Converts a list of transactions into a CSV formatted string.
     */
    fun generateCsv(transactions: List<TransactionEntity>): String {
        val sb = StringBuilder()
        sb.append(UTF8_BOM)
        sb.append(CSV_HEADER).append("\r\n")

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val timeFormat = SimpleDateFormat("HH:mm", Locale.US)

        for (tx in transactions) {
            val id = tx.id
            val dateStr = dateFormat.format(Date(tx.date))
            val timeStr = timeFormat.format(Date(tx.date))
            val typeStr = if (tx.type.equals("INCOME", ignoreCase = true)) "Pemasukan" else "Pengeluaran"
            val category = escapeCsv(tx.category)
            val nominal = if (tx.amount % 1.0 == 0.0) tx.amount.toLong().toString() else tx.amount.toString()
            val note = escapeCsv(tx.note)

            sb.append("$id,$dateStr,$timeStr,$typeStr,$category,$nominal,$note\r\n")
        }

        return sb.toString()
    }

    /**
     * Writes CSV content directly to a user-selected Uri via Storage Access Framework.
     */
    fun exportToUri(context: Context, uri: Uri, transactions: List<TransactionEntity>): Result<Int> {
        return runCatching {
            val csvContent = generateCsv(transactions)
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                OutputStreamWriter(outputStream, StandardCharsets.UTF_8).use { writer ->
                    writer.write(csvContent)
                    writer.flush()
                }
            } ?: throw IllegalStateException("Tidak dapat membuka file untuk ditulis")
            transactions.size
        }
    }

    /**
     * Writes CSV to cache directory and shares it via Intent.ACTION_SEND using FileProvider.
     */
    fun shareCsv(context: Context, transactions: List<TransactionEntity>): Result<Unit> {
        return runCatching {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val fileName = "transaksi_keuangan_$timeStamp.csv"
            val cacheFile = File(context.cacheDir, fileName)
            val csvContent = generateCsv(transactions)

            cacheFile.writeText(csvContent, StandardCharsets.UTF_8)

            val contentUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                cacheFile
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                putExtra(Intent.EXTRA_SUBJECT, "Backup Transaksi Keuangan - $fileName")
                putExtra(Intent.EXTRA_TEXT, "File cadangan transaksi keuangan (${transactions.size} transaksi) format CSV/Excel.")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "Bagikan Data Transaksi")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        }
    }

    /**
     * Escapes a CSV field according to RFC 4180 rules.
     */
    private fun escapeCsv(value: String): String {
        return if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
            "\"" + value.replace("\"", "\"\"") + "\""
        } else {
            value
        }
    }
}
