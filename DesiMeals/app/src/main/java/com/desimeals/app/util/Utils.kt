package com.desimeals.app.util

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object CurrencyFormatter {
    private val nf: NumberFormat = NumberFormat.getCurrencyInstance(Locale("en", "PK")).apply {
        maximumFractionDigits = 0
    }

    fun format(amount: Double): String = try {
        "Rs " + NumberFormat.getNumberInstance(Locale.US).apply {
            maximumFractionDigits = 0
        }.format(amount)
    } catch (_: Exception) {
        "Rs $amount"
    }
}

object DateUtils {
    private val dayFmt = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    private val shortFmt = SimpleDateFormat("dd MMM", Locale.getDefault())

    fun format(dateMillis: Long): String = dayFmt.format(Date(dateMillis))
    fun formatShort(dateMillis: Long): String = shortFmt.format(Date(dateMillis))

    fun startOfDay(millis: Long = System.currentTimeMillis()): Long {
        val cal = Calendar.getInstance().apply {
            timeInMillis = millis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }

    fun monthRange(millis: Long): Pair<Long, Long> {
        val cal = Calendar.getInstance().apply { timeInMillis = millis }
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val from = cal.timeInMillis
        cal.add(Calendar.MONTH, 1)
        val to = cal.timeInMillis - 1
        return from to to
    }
}

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}
