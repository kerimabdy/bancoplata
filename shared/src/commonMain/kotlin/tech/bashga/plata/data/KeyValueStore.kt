package tech.bashga.plata.data

import androidx.compose.runtime.Composable

/** String values under string keys; the app keeps one key, the JSON snapshot. */
interface KeyValueStore {
    fun get(key: String): String?

    fun put(key: String, value: String)

    fun remove(key: String)
}

/** SharedPreferences on Android, NSUserDefaults on iOS. */
@Composable
expect fun rememberKeyValueStore(): KeyValueStore

class InMemoryKeyValueStore(initial: Map<String, String> = emptyMap()) : KeyValueStore {
    private val values = initial.toMutableMap()

    override fun get(key: String): String? = values[key]

    override fun put(key: String, value: String) {
        values[key] = value
    }

    override fun remove(key: String) {
        values.remove(key)
    }
}
