package tech.bashga.plata.data

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.browser.localStorage

private object LocalStorageStore : KeyValueStore {
    override fun get(key: String): String? = localStorage.getItem(key)

    override fun put(key: String, value: String) = localStorage.setItem(key, value)

    override fun remove(key: String) = localStorage.removeItem(key)
}

@Composable
actual fun rememberKeyValueStore(): KeyValueStore = remember { LocalStorageStore }
