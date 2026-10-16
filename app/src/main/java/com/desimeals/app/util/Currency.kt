package com.desimeals.app.util

import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

data class CurrencyInfo(val code: String, val symbol: String, val name: String)

val SUPPORTED_CURRENCIES = listOf(
    CurrencyInfo("PKR", "₨", "Pakistani Rupee"),
    CurrencyInfo("INR", "₹", "Indian Rupee"),
    CurrencyInfo("USD", "$", "US Dollar"),
    CurrencyInfo("EUR", "€", "Euro"),
    CurrencyInfo("GBP", "£", "British Pound"),
    CurrencyInfo("AED", "د.إ", "UAE Dirham"),
    CurrencyInfo("SAR", "﷼", "Saudi Riyal"),
    CurrencyInfo("CAD", "CA$", "Canadian Dollar"),
    CurrencyInfo("AUD", "A$", "Australian Dollar"),
    CurrencyInfo("BDT", "৳", "Bangladeshi Taka"),
    CurrencyInfo("QAR", "QR", "Qatari Riyal"),
    CurrencyInfo("KWD", "KD", "Kuwaiti Dinar"),
    CurrencyInfo("OMR", "RO", "Omani Rial"),
    CurrencyInfo("MYR", "RM", "Malaysian Ringgit"),
    CurrencyInfo("TRY", "₺", "Turkish Lira")
)

fun formatMoney(amount: Double, currencyCode: String = "PKR", decimals: Int = 2): String {
    val info = SUPPORTED_CURRENCIES.find { it.code == currencyCode }
    return try {
        val nf = NumberFormat.getNumberInstance(Locale.US)
        nf.minimumFractionDigits = decimals
        nf.maximumFractionDigits = decimals
        "${info?.symbol ?: currencyCode + " "}${nf.format(amount)}"
    } catch (_: Exception) {
        "${info?.symbol ?: ""}$amount"
    }
}
