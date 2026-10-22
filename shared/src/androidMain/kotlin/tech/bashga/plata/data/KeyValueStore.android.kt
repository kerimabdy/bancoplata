package tech.bashga.plata.data

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

private class SharedPreferencesStore(private val preferences: SharedPreferences) : KeyValueStore {
    override fun get(key: String): String? = preferences.getString(key, null)

    override fun put(key: String, value: String) {
        preferences.edit().putString(key, value).apply()
    }

    override fun remove(key: String) {
        preferences.edit().remove(key).apply()
    }
}

@Composable
actual fun rememberKeyValueStore(): KeyValueStore {
    val context = LocalContext.current.applicationContext
    return remember(context) { SharedPreferencesStore(context.getSharedPreferences("plata", Context.MODE_PRIVATE)) }
}
