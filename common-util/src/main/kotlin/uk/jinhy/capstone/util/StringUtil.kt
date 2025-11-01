package uk.jinhy.capstone.util

object StringUtil {
    fun isBlankOrNull(str: String?): Boolean {
        return str.isNullOrBlank()
    }

    fun defaultIfBlank(str: String?, default: String): String {
        return if (str.isNullOrBlank()) default else str
    }

    fun truncate(str: String, maxLength: Int): String {
        return if (str.length <= maxLength) {
            str
        } else {
            str.substring(0, maxLength) + "..."
        }
    }

    fun maskEmail(email: String?): String {
        if (email.isNullOrBlank()) return ""
        val atIndex = email.indexOf("@")
        if (atIndex <= 0 || atIndex >= email.length - 1) return email

        val localPart = email.substring(0, atIndex)
        val domain = email.substring(atIndex + 1)

        val maskedLocal = if (localPart.length <= 2) {
            "*".repeat(localPart.length)
        } else {
            localPart.substring(0, 2) + "*".repeat(localPart.length - 2)
        }

        return "$maskedLocal@$domain"
    }

    fun maskPhoneNumber(phoneNumber: String?): String {
        if (phoneNumber.isNullOrBlank()) return ""
        val digitsOnly = phoneNumber.replace(Regex("[^0-9]"), "")
        return when (digitsOnly.length) {
            10 -> "${digitsOnly.substring(0, 3)}-****-${digitsOnly.substring(7)}"
            11 -> "${digitsOnly.substring(0, 3)}-****-${digitsOnly.substring(7)}"
            else -> phoneNumber
        }
    }
}
