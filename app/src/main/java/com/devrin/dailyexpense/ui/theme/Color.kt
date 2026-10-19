package com.devrin.dailyexpense.ui.theme

import androidx.compose.ui.graphics.Color

// Primary colors — soft blue theme inspired by reference UI
val BluePrimary = Color(0xFF5B9BD5)
val BluePrimaryDark = Color(0xFF4A87C0)
val BluePrimaryLight = Color(0xFF8FBEE8)
val LightBlueBackground = Color(0xFFE8F4FD)
val SurfaceWhite = Color(0xFFFFFFFF)

// Text colors
val DarkText = Color(0xFF1A1A2E)
val GrayText = Color(0xFF8E8E93)
val LightGrayText = Color(0xFFAEAEB2)

// Transaction colors
val IncomeGreen = Color(0xFF34C759)
val IncomeGreenLight = Color(0xFFE8F8ED)
val ExpenseRed = Color(0xFFFF3B30)
val ExpenseRedLight = Color(0xFFFDE8E7)

// Card & surface
val CardBackground = Color(0xFFFFFFFF)
val DividerColor = Color(0xFFF2F2F7)

// Category-specific colors for charts and breakdown indicators
val CategoryFood = Color(0xFFFF7043)          // Makanan (Coral)
val CategoryTransport = Color(0xFF42A5F5)     // Transportasi (Blue)
val CategoryShopping = Color(0xFFAB47BC)      // Belanja (Purple)
val CategoryEntertainment = Color(0xFFFFA726) // Hiburan (Amber)
val CategoryBills = Color(0xFFEF5350)         // Tagihan (Red)
val CategoryHealth = Color(0xFF26A69A)        // Kesehatan (Teal)
val CategoryEducation = Color(0xFF5C6BC0)     // Pendidikan (Indigo)
val CategorySalary = Color(0xFF4CAF50)        // Gaji (Green)
val CategoryFreelance = Color(0xFF26C6DA)     // Freelance (Cyan)
val CategoryInvestment = Color(0xFF7E57C2)    // Investasi (Deep Purple)
val CategoryGift = Color(0xFFEC407A)          // Hadiah (Pink)
val CategoryOther = Color(0xFF78909C)         // Lainnya (Blue Grey)

fun getCategoryColor(category: String): Color {
    return when (category) {
        "Makanan" -> CategoryFood
        "Transportasi" -> CategoryTransport
        "Belanja" -> CategoryShopping
        "Hiburan" -> CategoryEntertainment
        "Tagihan" -> CategoryBills
        "Kesehatan" -> CategoryHealth
        "Pendidikan" -> CategoryEducation
        "Gaji" -> CategorySalary
        "Freelance" -> CategoryFreelance
        "Investasi" -> CategoryInvestment
        "Hadiah" -> CategoryGift
        else -> CategoryOther
    }
}
