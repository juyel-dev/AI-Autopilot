package com.example.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class SecureStorage(context: Context) {

    private val sharedPreferences: SharedPreferences

    init {
        var prefs: SharedPreferences? = null
        try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            prefs = EncryptedSharedPreferences.create(
                context,
                "aether_secrets",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
            Log.d("SecureStorage", "Successfully initialized EncryptedSharedPreferences.")
        } catch (t: Throwable) {
            Log.e("SecureStorage", "Failed to initialize EncryptedSharedPreferences, falling back to standard SharedPreferences", t)
            try {
                prefs = context.getSharedPreferences("aether_secrets_fallback", Context.MODE_PRIVATE)
            } catch (e: Exception) {
                Log.e("SecureStorage", "Failed to initialize fallback SharedPreferences as well", e)
            }
        }
        sharedPreferences = prefs ?: context.getSharedPreferences("aether_secrets_fallback", Context.MODE_PRIVATE)
    }

    fun saveString(key: String, value: String?) {
        sharedPreferences.edit().putString(key, value).apply()
    }

    fun getString(key: String, defaultValue: String = ""): String {
        return sharedPreferences.getString(key, defaultValue) ?: defaultValue
    }

    fun clear() {
        sharedPreferences.edit().clear().apply()
    }
}
