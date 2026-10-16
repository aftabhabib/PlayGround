package com.desimeals.app.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object Dates {
    fun fmt(ts: Long, pattern: String = "dd MMM yyyy"): String {
        return try {
            SimpleDateFormat(pattern, Locale.getDefault()).format(Date(ts))
        } catch (_: Exception) { ts.toString() }
    }
    fun fmtDateTime(ts: Long): String = fmt(ts, "dd MMM yyyy, hh:mm a")

    fun startOfDay(ts: Long = System.currentTimeMillis()): Long {
        val c = Calendar.getInstance(); c.timeInMillis = ts
        c.set(Calendar.HOUR_OF_DAY, 0); c.set(Calendar.MINUTE, 0); c.set(Calendar.SECOND, 0); c.set(Calendar.MILLISECOND, 0)
        return c.timeInMillis
    }
    fun endOfDay(ts: Long = System.currentTimeMillis()): Long = startOfDay(ts) + 24 * 60 * 60 * 1000L - 1
    fun startOfWeek(ts: Long = System.currentTimeMillis(), firstDay: Int = Calendar.MONDAY): Long {
        val c = Calendar.getInstance(); c.timeInMillis = ts
        c.firstDayOfWeek = firstDay
        c.set(Calendar.HOUR_OF_DAY, 0); c.set(Calendar.MINUTE, 0); c.set(Calendar.SECOND, 0); c.set(Calendar.MILLISECOND, 0)
        c.set(Calendar.DAY_OF_WEEK, firstDay)
        return c.timeInMillis
    }
    fun startOfMonth(ts: Long = System.currentTimeMillis()): Long {
        val c = Calendar.getInstance(); c.timeInMillis = ts
        c.set(Calendar.DAY_OF_MONTH, 1); c.set(Calendar.HOUR_OF_DAY, 0); c.set(Calendar.MINUTE, 0); c.set(Calendar.SECOND, 0); c.set(Calendar.MILLISECOND, 0)
        return c.timeInMillis
    }
    fun endOfMonth(ts: Long = System.currentTimeMillis()): Long {
        val c = Calendar.getInstance(); c.timeInMillis = startOfMonth(ts)
        c.add(Calendar.MONTH, 1); c.add(Calendar.MILLISECOND, -1)
        return c.timeInMillis
    }
    fun startOfYear(ts: Long = System.currentTimeMillis()): Long {
        val c = Calendar.getInstance(); c.timeInMillis = ts
        c.set(Calendar.MONTH, 0); c.set(Calendar.DAY_OF_MONTH, 1)
        c.set(Calendar.HOUR_OF_DAY, 0); c.set(Calendar.MINUTE, 0); c.set(Calendar.SECOND, 0); c.set(Calendar.MILLISECOND, 0)
        return c.timeInMillis
    }
    fun addMonths(ts: Long, n: Int): Long {
        val c = Calendar.getInstance(); c.timeInMillis = ts; c.add(Calendar.MONTH, n); return c.timeInMillis
    }
}
