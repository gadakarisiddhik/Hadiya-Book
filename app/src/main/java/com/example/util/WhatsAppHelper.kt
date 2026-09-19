package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

object WhatsAppHelper {

    fun formatIndianPhoneNumber(phoneInput: String): String {
        val digitsOnly = phoneInput.replace(Regex("[^0-9]"), "")
        return when {
            digitsOnly.length == 10 -> "91$digitsOnly"
            digitsOnly.length == 11 && digitsOnly.startsWith("0") -> "91${digitsOnly.substring(1)}"
            digitsOnly.length == 12 && digitsOnly.startsWith("91") -> digitsOnly
            else -> digitsOnly
        }
    }

    fun isValidIndianPhoneNumber(phoneInput: String): Boolean {
        val digitsOnly = phoneInput.replace(Regex("[^0-9]"), "")
        return when {
            digitsOnly.length == 10 && digitsOnly.first() in '6'..'9' -> true
            digitsOnly.length == 11 && digitsOnly.startsWith("0") && digitsOnly[1] in '6'..'9' -> true
            digitsOnly.length == 12 && digitsOnly.startsWith("91") && digitsOnly[2] in '6'..'9' -> true
            else -> false
        }
    }

    fun buildHadiyaMessage(name: String, amount: Long, dateIso: String): String {
        val formattedDate = DateUtils.formatForWhatsApp(dateIso)
        return """
Assalamualaikum $name,

Aapne ₹$amount Hadiya diya hai.

Date: $formattedDate

JazakAllahu Khairan.
""".trimIndent()
    }

    /**
     * Attempts to open WhatsApp with pre-filled message and target phone number.
     * Returns true if successfully launched, false if WhatsApp is not installed.
     */
    fun openWhatsApp(
        context: Context,
        phoneInput: String,
        message: String,
        onError: (String) -> Unit = {}
    ): Boolean {
        val cleanedPhone = formatIndianPhoneNumber(phoneInput)
        val encodedMessage = Uri.encode(message)
        val url = "https://api.whatsapp.com/send?phone=$cleanedPhone&text=$encodedMessage"

        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(url)
                setPackage("com.whatsapp")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            return true
        } catch (_: Exception) {
            // WhatsApp direct package not found, try generic view (e.g. WhatsApp Business or browser/fallback)
            try {
                val fallbackIntent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse(url)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(fallbackIntent)
                return true
            } catch (_: Exception) {
                val errorMsg = "WhatsApp is not installed on this device."
                Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                onError(errorMsg)
                return false
            }
        }
    }
}
