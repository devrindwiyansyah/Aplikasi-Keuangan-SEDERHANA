package com.devrin.dailyexpense.util

import android.content.Context
import android.net.Uri
import com.devrin.dailyexpense.data.local.TransactionEntity
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Result wrapper for CSV import operations.
 */
sealed class ImportResult {
    data class Success(val transactions: List<TransactionEntity>) : ImportResult()
    data class Error(val message: String) : ImportResult()
}

/**
 * Utility for parsing and importing CSV files into Room database entities.
 * Supports RFC 4180 format, automatic UTF-8 BOM removal, and flexible delimiters (, or ;).
 */
object CsvImporter {

    /**
     * Reads and parses a CSV file from the given SAF content Uri.
     */
    fun importFromUri(context: Context, uri: Uri): ImportResult {
        return try {
            val rawContent = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream, StandardCharsets.UTF_8)).use { reader ->
                    reader.readText()
                }
            } ?: return ImportResult.Error("Tidak dapat membuka file yang dipilih.")

            if (rawContent.isBlank()) {
                return ImportResult.Error("File CSV yang dipilih kosong.")
            }

            // Remove UTF-8 BOM if present
            val cleanContent = if (rawContent.startsWith("\uFEFF")) {
                rawContent.substring(1)
            } else {
                rawContent
            }

            val rows = parseCsv(cleanContent)
            if (rows.isEmpty()) {
                return ImportResult.Error("Format file tidak valid atau data tidak ditemukan.")
            }

            // Detect and drop header row if present
            val firstRow = rows.first()
            val hasHeader = firstRow.any { col ->
                col.equals("ID", ignoreCase = true) ||
                col.equals("Tanggal", ignoreCase = true) ||
                col.equals("Date", ignoreCase = true) ||
                col.equals("Nominal", ignoreCase = true) ||
                col.equals("Tipe", ignoreCase = true) ||
                col.equals("Amount", ignoreCase = true)
            }

            val dataRows = if (hasHeader) rows.drop(1) else rows
            if (dataRows.isEmpty()) {
                return ImportResult.Error("Tidak ada baris data transaksi dalam file CSV.")
            }

            val transactions = mutableListOf<TransactionEntity>()

            val dateTimeFormats = listOf(
                SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US),
                SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US),
                SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.US),
                SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US),
                SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.US)
            )

            val dateOnlyFormats = listOf(
                SimpleDateFormat("yyyy-MM-dd", Locale.US),
                SimpleDateFormat("dd-MM-yyyy", Locale.US),
                SimpleDateFormat("dd/MM/yyyy", Locale.US),
                SimpleDateFormat("yyyy/MM/dd", Locale.US)
            )

            for (row in dataRows) {
                if (row.size < 5) continue

                try {
                    val parsed = when {
                        row.size >= 7 -> {
                            // Standard format: ID, Tanggal, Waktu, Tipe, Kategori, Nominal, Catatan
                            ParsedRow(
                                dateStr = row[1],
                                timeStr = row[2],
                                typeStr = row[3],
                                categoryStr = row[4],
                                nominalStr = row[5],
                                noteStr = row[6]
                            )
                        }
                        row.size == 6 -> {
                            // Either: ID, Tanggal, Waktu, Tipe, Kategori, Nominal
                            // OR: Tanggal, Waktu, Tipe, Kategori, Nominal, Catatan (no ID column)
                            if (row[0].toLongOrNull() != null && (row[1].contains("-") || row[1].contains("/"))) {
                                ParsedRow(row[1], row[2], row[3], row[4], row[5], "")
                            } else {
                                ParsedRow(row[0], row[1], row[2], row[3], row[4], row[5])
                            }
                        }
                        else -> {
                            // 5 columns: Tanggal, Waktu, Tipe, Kategori, Nominal
                            ParsedRow(row[0], row[1], row[2], row[3], row[4], "")
                        }
                    }

                    // Parse Type
                    val type = if (
                        parsed.typeStr.contains("masuk", ignoreCase = true) ||
                        parsed.typeStr.equals("INCOME", ignoreCase = true)
                    ) {
                        "INCOME"
                    } else {
                        "EXPENSE"
                    }

                    // Parse Nominal safely (strip "Rp", currency symbols, spaces)
                    val cleanNominal = parsed.nominalStr
                        .replace("Rp", "", ignoreCase = true)
                        .replace("IDR", "", ignoreCase = true)
                        .replace(" ", "")
                        .trim()

                    val amount = cleanNominal.toDoubleOrNull()
                        ?: cleanNominal.replace(",", ".").toDoubleOrNull()
                        ?: continue

                    if (amount <= 0) continue

                    // Parse Date & Time
                    val combinedDateTime = "${parsed.dateStr.trim()} ${parsed.timeStr.trim()}".trim()
                    var timestamp: Long? = null

                    for (df in dateTimeFormats) {
                        try {
                            val parsedDate = df.parse(combinedDateTime)
                            if (parsedDate != null) {
                                timestamp = parsedDate.time
                                break
                            }
                        } catch (_: Exception) {}
                    }

                    if (timestamp == null) {
                        for (df in dateOnlyFormats) {
                            try {
                                val parsedDate = df.parse(parsed.dateStr.trim())
                                if (parsedDate != null) {
                                    timestamp = parsedDate.time
                                    break
                                }
                            } catch (_: Exception) {}
                        }
                    }

                    val finalDate = timestamp ?: System.currentTimeMillis()
                    val category = parsed.categoryStr.trim().ifBlank {
                        if (type == "INCOME") "Lainnya" else "Lain-lain"
                    }

                    // Use id = 0L so Room auto-generates sequential IDs without colliding with existing rows
                    transactions.add(
                        TransactionEntity(
                            id = 0L,
                            amount = amount,
                            type = type,
                            category = category,
                            note = parsed.noteStr.trim(),
                            date = finalDate
                        )
                    )
                } catch (_: Exception) {
                    // Gracefully skip corrupted rows
                }
            }

            if (transactions.isEmpty()) {
                ImportResult.Error("Tidak ada baris transaksi yang berhasil diimpor. Pastikan format kolom sesuai.")
            } else {
                ImportResult.Success(transactions)
            }
        } catch (e: Exception) {
            ImportResult.Error("Gagal mengimpor file: ${e.localizedMessage ?: "Kesalahan tidak dikenal"}")
        }
    }

    /**
     * Parses RFC 4180 CSV content into list of string lists.
     * Automatically senses delimiter (, or ;) from the header line.
     */
    private fun parseCsv(content: String): List<List<String>> {
        val firstLine = content.lineSequence().firstOrNull { it.isNotBlank() } ?: ""
        val delimiter = if (firstLine.count { it == ';' } > firstLine.count { it == ',' }) ';' else ','

        val rows = mutableListOf<List<String>>()
        val currentRow = mutableListOf<String>()
        val currentField = StringBuilder()
        var inQuotes = false
        var i = 0
        val length = content.length

        while (i < length) {
            val c = content[i]
            if (inQuotes) {
                if (c == '"') {
                    if (i + 1 < length && content[i + 1] == '"') {
                        currentField.append('"')
                        i++ // Skip escaped quote
                    } else {
                        inQuotes = false
                    }
                } else {
                    currentField.append(c)
                }
            } else {
                when (c) {
                    '"' -> inQuotes = true
                    delimiter -> {
                        currentRow.add(currentField.toString().trim())
                        currentField.clear()
                    }
                    '\r' -> {
                        if (i + 1 < length && content[i + 1] == '\n') {
                            i++
                        }
                        currentRow.add(currentField.toString().trim())
                        currentField.clear()
                        if (currentRow.any { it.isNotBlank() }) {
                            rows.add(ArrayList(currentRow))
                        }
                        currentRow.clear()
                    }
                    '\n' -> {
                        currentRow.add(currentField.toString().trim())
                        currentField.clear()
                        if (currentRow.any { it.isNotBlank() }) {
                            rows.add(ArrayList(currentRow))
                        }
                        currentRow.clear()
                    }
                    else -> currentField.append(c)
                }
            }
            i++
        }

        if (currentField.isNotEmpty() || currentRow.isNotEmpty()) {
            currentRow.add(currentField.toString().trim())
            if (currentRow.any { it.isNotBlank() }) {
                rows.add(ArrayList(currentRow))
            }
        }

        return rows
    }

    private data class ParsedRow(
        val dateStr: String,
        val timeStr: String,
        val typeStr: String,
        val categoryStr: String,
        val nominalStr: String,
        val noteStr: String
    )
}
