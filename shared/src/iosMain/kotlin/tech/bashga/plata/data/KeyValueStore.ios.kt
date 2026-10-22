package tech.bashga.plata.data

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.Foundation.NSUserDefaults

private class UserDefaultsStore(private val defaults: NSUserDefaults) : KeyValueStore {
    override fun get(key: String): String? = defaults.stringForKey(key)

    override fun put(key: String, value: String) {
        defaults.setObject(value, forKey = key)
    }

    override fun remove(key: String) {
        defaults.removeObjectForKey(key)
    }
}

@Composable
actual fun rememberKeyValueStore(): KeyValueStore = remember { UserDefaultsStore(NSUserDefaults.standardUserDefaults) }
