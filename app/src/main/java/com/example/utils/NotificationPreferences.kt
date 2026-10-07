package com.example.utils

import android.content.Context
import android.content.SharedPreferences

/**
 * Manages local persistent storage of notification preferences for
 * both Shopkeeper and Customer panels.
 */
object NotificationPreferences {

    private const val PREFS_NAME = "digital_khata_notification_prefs"

    // Shopkeeper Keys
    const val KEY_SHOP_NEW_CUSTOMER = "shop_notif_new_customer"
    const val KEY_SHOP_NEW_KHATA_ENTRY = "shop_notif_new_khata_entry"
    const val KEY_SHOP_PAYMENT_REQUEST = "shop_notif_payment_request"
    const val KEY_SHOP_PAYMENT_ACCEPTED = "shop_notif_payment_accepted"
    const val KEY_SHOP_PAYMENT_REJECTED = "shop_notif_payment_rejected"
    const val KEY_SHOP_PREV_BALANCE = "shop_notif_prev_balance"
    const val KEY_SHOP_MONTHLY_REMINDER = "shop_notif_monthly_reminder"

    // Customer Keys
    const val KEY_CUST_NEW_KHATA_ENTRY = "cust_notif_new_khata_entry"
    const val KEY_CUST_PAYMENT_UPDATES = "cust_notif_payment_updates"
    const val KEY_CUST_PAYMENT_ACCEPTED = "cust_notif_payment_accepted"
    const val KEY_CUST_PAYMENT_REJECTED = "cust_notif_payment_rejected"
    const val KEY_CUST_PREV_BALANCE = "cust_notif_prev_balance"
    const val KEY_CUST_MONTHLY_REMINDER = "cust_notif_monthly_reminder"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun isEnabled(context: Context, key: String, default: Boolean = true): Boolean {
        return getPrefs(context).getBoolean(key, default)
    }

    fun setEnabled(context: Context, key: String, value: Boolean) {
        getPrefs(context).edit().putBoolean(key, value).apply()
    }
}
