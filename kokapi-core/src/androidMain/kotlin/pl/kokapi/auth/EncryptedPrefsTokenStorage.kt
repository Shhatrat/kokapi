package pl.kokapi.auth

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class EncryptedPrefsTokenStorage(context: Context) : TokenStorage {
    private val prefs by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context, "kokapi_tokens", masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    override fun save(key: String, value: String) = prefs.edit().putString(key, value).apply()
    override fun get(key: String): String? = prefs.getString(key, null)
    override fun clear(key: String) = prefs.edit().remove(key).apply()
    override fun clearAll() = prefs.edit().clear().apply()
}
