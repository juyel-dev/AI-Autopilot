package com.example.util

import android.util.Patterns

object ValidationUtils {
    fun isValidUrl(url: String): Boolean {
        if (url.isBlank()) return false
        if (!url.startsWith("https://") && !url.startsWith("http://")) return false
        return Patterns.WEB_URL.matcher(url).matches()
    }

    fun isValidSupabaseKey(key: String): Boolean {
        // Typically a JWT token format like eyJ...
        if (key.isBlank()) return false
        if (key.length < 30) return false
        if (key.contains("****")) return true // masked value
        return key.startsWith("ey") && key.split(".").size == 3
    }

    fun isValidFacebookToken(token: String): Boolean {
        if (token.isBlank()) return false
        if (token.contains("****")) return true
        if (token.length < 20) return false
        return token.startsWith("EAA")
    }

    fun isValidOpenAiKey(key: String): Boolean {
        if (key.isBlank()) return false
        if (key.contains("****")) return true
        if (key.length < 20) return false
        return key.startsWith("sk-") || key.length > 30 // Gemini / OpenAI
    }
}
