package com.desimeals.app.util

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.settingsStore by preferencesDataStore(name = "desi_settings")

object PrefsKeys {
    val BUSINESS_NAME = stringPreferencesKey("business_name")
    val OWNER_NAME = stringPreferencesKey("owner_name")
    val CONTACT = stringPreferencesKey("contact")
    val EMAIL = stringPreferencesKey("email")
    val ADDRESS = stringPreferencesKey("address")
    val TAX_NO = stringPreferencesKey("tax_no")
    val INVOICE_FOOTER = stringPreferencesKey("invoice_footer")
    val BILLING_CYCLE = stringPreferencesKey("billing_cycle")
    val CURRENCY = stringPreferencesKey("currency")
    val THEME = stringPreferencesKey("theme") // light, dark, system
    val ACCENT = stringPreferencesKey("accent")
    val DATE_FMT = stringPreferencesKey("date_fmt")
    val TIME_24 = booleanPreferencesKey("time_24")
    val DECIMALS = intPreferencesKey("decimals")
    val DEFAULT_PAY = stringPreferencesKey("default_pay")
    val PIN = stringPreferencesKey("pin")
    val NOTIF_DUE = booleanPreferencesKey("notif_due")
    val NOTIF_OVERDUE = booleanPreferencesKey("notif_overdue")
    val NOTIF_SUMMARY = booleanPreferencesKey("notif_summary")
    val NOTIF_BACKUP = booleanPreferencesKey("notif_backup")
    val LAST_BACKUP = stringPreferencesKey("last_backup")
}

data class AppPrefs(
    val businessName: String = "Desi Meals",
    val ownerName: String = "",
    val contact: String = "",
    val email: String = "",
    val address: String = "",
    val taxNo: String = "",
    val invoiceFooter: String = "Thank you for your business!",
    val billingCycle: String = "Monthly",
    val currency: String = "PKR",
    val theme: String = "system",
    val accent: String = "Green",
    val dateFmt: String = "dd MMM yyyy",
    val time24: Boolean = false,
    val decimals: Int = 2,
    val defaultPay: String = "Cash",
    val pin: String = "",
    val notifDue: Boolean = true,
    val notifOverdue: Boolean = true,
    val notifSummary: Boolean = false,
    val notifBackup: Boolean = true,
    val lastBackup: String = "Never"
)

fun prefsFlow(ctx: Context): Flow<AppPrefs> =
    ctx.settingsStore.data.map { p ->
        AppPrefs(
            businessName = p[PrefsKeys.BUSINESS_NAME] ?: "Desi Meals",
            ownerName = p[PrefsKeys.OWNER_NAME] ?: "",
            contact = p[PrefsKeys.CONTACT] ?: "",
            email = p[PrefsKeys.EMAIL] ?: "",
            address = p[PrefsKeys.ADDRESS] ?: "",
            taxNo = p[PrefsKeys.TAX_NO] ?: "",
            invoiceFooter = p[PrefsKeys.INVOICE_FOOTER] ?: "Thank you for your business!",
            billingCycle = p[PrefsKeys.BILLING_CYCLE] ?: "Monthly",
            currency = p[PrefsKeys.CURRENCY] ?: "PKR",
            theme = p[PrefsKeys.THEME] ?: "system",
            accent = p[PrefsKeys.ACCENT] ?: "Green",
            dateFmt = p[PrefsKeys.DATE_FMT] ?: "dd MMM yyyy",
            time24 = p[PrefsKeys.TIME_24] ?: false,
            decimals = p[PrefsKeys.DECIMALS] ?: 2,
            defaultPay = p[PrefsKeys.DEFAULT_PAY] ?: "Cash",
            pin = p[PrefsKeys.PIN] ?: "",
            notifDue = p[PrefsKeys.NOTIF_DUE] ?: true,
            notifOverdue = p[PrefsKeys.NOTIF_OVERDUE] ?: true,
            notifSummary = p[PrefsKeys.NOTIF_SUMMARY] ?: false,
            notifBackup = p[PrefsKeys.NOTIF_BACKUP] ?: true,
            lastBackup = p[PrefsKeys.LAST_BACKUP] ?: "Never"
        )
    }

suspend fun setPref(ctx: Context, mutate: suspend (androidx.datastore.preferences.core.MutablePreferences) -> Unit) {
    ctx.settingsStore.edit { mutate(it) }
}
