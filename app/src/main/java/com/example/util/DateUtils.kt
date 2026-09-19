package com.example.util

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtils {

    private val isoFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
    private val displayFormat = SimpleDateFormat("dd MMMM yyyy", Locale.ENGLISH)
    private val shortDisplayFormat = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
    private val whatsappDateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH)
    private val monthYearFormat = SimpleDateFormat("MMMM yyyy", Locale.ENGLISH)

    fun getTodayIso(): String {
        return isoFormat.format(Date())
    }

    fun getCurrentMonthPrefix(): String {
        val cal = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH) + 1
        return String.format(Locale.ENGLISH, "%04d-%02d", year, month)
    }

    fun getCurrentYearPrefix(): String {
        val cal = Calendar.getInstance()
        return cal.get(Calendar.YEAR).toString()
    }

    fun formatToDisplay(isoDate: String): String {
        return try {
            val date = isoFormat.parse(isoDate) ?: return isoDate
            displayFormat.format(date)
        } catch (_: Exception) {
            isoDate
        }
    }

    fun formatToShortDisplay(isoDate: String): String {
        return try {
            val date = isoFormat.parse(isoDate) ?: return isoDate
            shortDisplayFormat.format(date)
        } catch (_: Exception) {
            isoDate
        }
    }

    fun formatForWhatsApp(isoDate: String): String {
        return try {
            val date = isoFormat.parse(isoDate) ?: return isoDate
            whatsappDateFormat.format(date)
        } catch (_: Exception) {
            isoDate
        }
    }

    fun formatMonthYear(year: Int, month1Based: Int): String {
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, month1Based - 1)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        return monthYearFormat.format(cal.time)
    }

    fun formatCurrency(amount: Long): String {
        return try {
            val formatter = NumberFormat.getNumberInstance(Locale("en", "IN"))
            "₹" + formatter.format(amount)
        } catch (_: Exception) {
            "₹$amount"
        }
    }

    fun getStartOfWeekIso(): String {
        val cal = Calendar.getInstance()
        cal.firstDayOfWeek = Calendar.MONDAY
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        return isoFormat.format(cal.time)
    }

    fun getEndOfWeekIso(): String {
        val cal = Calendar.getInstance()
        cal.firstDayOfWeek = Calendar.MONDAY
        cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
        return isoFormat.format(cal.time)
    }

    fun getMonthPrefix(year: Int, month1Based: Int): String {
        return String.format(Locale.ENGLISH, "%04d-%02d", year, month1Based)
    }
}
