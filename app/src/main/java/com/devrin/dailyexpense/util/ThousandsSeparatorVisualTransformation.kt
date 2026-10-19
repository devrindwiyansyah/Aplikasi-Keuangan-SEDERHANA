package com.devrin.dailyexpense.util

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

/**
 * VisualTransformation that formats digit strings with Indonesian thousands separator (dots).
 * Example: when user types "2000000" -> visually displays "2.000.000".
 * The underlying raw string state remains clean digits ("2000000").
 */
class ThousandsSeparatorVisualTransformation : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {
        val originalText = text.text
        if (originalText.isEmpty()) {
            return TransformedText(text, OffsetMapping.Identity)
        }

        // Build transformed string with dots every 3 digits from right
        val formattedBuilder = StringBuilder()
        val originalToTransformed = IntArray(originalText.length + 1)
        originalToTransformed[0] = 0

        val totalDigits = originalText.length
        for (i in 0 until totalDigits) {
            formattedBuilder.append(originalText[i])
            val digitsRemaining = totalDigits - 1 - i
            if (digitsRemaining > 0 && digitsRemaining % 3 == 0) {
                formattedBuilder.append('.')
            }
            originalToTransformed[i + 1] = formattedBuilder.length
        }

        val formattedText = formattedBuilder.toString()
        val transformedToOriginal = IntArray(formattedText.length + 1)
        transformedToOriginal[0] = 0

        var originalIndex = 0
        for (j in 0 until formattedText.length) {
            if (formattedText[j] != '.') {
                originalIndex++
            }
            transformedToOriginal[j + 1] = originalIndex
        }

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                val clamped = offset.coerceIn(0, originalText.length)
                return originalToTransformed[clamped]
            }

            override fun transformedToOriginal(offset: Int): Int {
                val clamped = offset.coerceIn(0, formattedText.length)
                return transformedToOriginal[clamped]
            }
        }

        return TransformedText(AnnotatedString(formattedText), offsetMapping)
    }
}
