package com.devrin.dailyexpense.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Format amount as Indonesian Rupiah (e.g. "Rp 1.500.000").
 */
fun formatRupiah(amount: Double): String {
    val symbols = DecimalFormatSymbols(Locale("id", "ID"))
    val formatter = DecimalFormat("#,###", symbols)
    return "Rp ${formatter.format(amount.toLong())}"
}

/**
 * Format timestamp to full Indonesian date (e.g. "17 Oktober 2026").
 */
fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
    return sdf.format(Date(timestamp))
}

/**
 * Format timestamp to short Indonesian date (e.g. "17 Okt 2026").
 */
fun formatDateShort(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
    return sdf.format(Date(timestamp))
}

/**
 * Format timestamp to Month & Year in Indonesian (e.g. "Oktober 2026").
 */
fun formatMonthYear(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMMM yyyy", Locale("id", "ID"))
    return sdf.format(Date(timestamp))
}

/**
 * Format timestamp to Month & Year key for sorting (e.g. "2026-10").
 */
fun formatMonthYearKey(timestamp: Long): String {
    val sdf = SimpleDateFormat("yyyy-MM", Locale.US)
    return sdf.format(Date(timestamp))
}

/**
 * Format percentage value (e.g. 45.2 -> "45.2%").
 */
fun formatPercent(percent: Double): String {
    return String.format(Locale("id", "ID"), "%.1f%%", percent)
}
