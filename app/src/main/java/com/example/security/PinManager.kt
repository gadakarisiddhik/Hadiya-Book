package com.example.security

import android.content.Context
import android.content.SharedPreferences
import java.security.MessageDigest

class PinManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("hadiyabook_security_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_PIN_HASH = "pin_hash"
        private const val KEY_PIN_ENABLED = "pin_enabled"
        private const val SALT = "HadiyaBook_ValQStudio_SecureSalt_2026"
    }

    fun isPinEnabled(): Boolean {
        return prefs.getBoolean(KEY_PIN_ENABLED, false) && isPinSet()
    }

    fun isPinSet(): Boolean {
        return !prefs.getString(KEY_PIN_HASH, null).isNullOrBlank()
    }

    fun setPin(pin: String) {
        val hash = hashPin(pin)
        prefs.edit()
            .putString(KEY_PIN_HASH, hash)
            .putBoolean(KEY_PIN_ENABLED, true)
            .apply()
    }

    fun verifyPin(input: String): Boolean {
        val storedHash = prefs.getString(KEY_PIN_HASH, null) ?: return false
        val inputHash = hashPin(input)
        return storedHash == inputHash
    }

    fun disablePin() {
        prefs.edit()
            .putBoolean(KEY_PIN_ENABLED, false)
            .apply()
    }

    fun removePin() {
        prefs.edit()
            .remove(KEY_PIN_HASH)
            .putBoolean(KEY_PIN_ENABLED, false)
            .apply()
    }

    private fun hashPin(pin: String): String {
        val inputWithSalt = pin + SALT
        val bytes = MessageDigest.getInstance("SHA-256").digest(inputWithSalt.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
